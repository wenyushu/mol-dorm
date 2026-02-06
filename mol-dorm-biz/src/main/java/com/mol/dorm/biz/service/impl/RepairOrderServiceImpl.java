package com.mol.dorm.biz.service.impl;

import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mol.common.core.constant.DormConstants;
import com.mol.common.core.exception.ServiceException;
import com.mol.dorm.biz.entity.RepairOrder;
import com.mol.dorm.biz.mapper.RepairOrderMapper;
import com.mol.dorm.biz.service.DormFixedAssetService;
import com.mol.dorm.biz.service.DormRoomService;
import com.mol.dorm.biz.service.RepairOrderService;
import com.mol.dorm.biz.service.WalletTransactionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 报修工单业务核心实现 - 财务资产全联动版
 * 🛡️ [防刁民设计]：
 * 1. 状态锁：资产报修即锁定，完工或驳回才释放，防止带障运行。
 * 2. 财务闭环：判定为人为损坏时，结案动作与扣费流水绑定在一个事务中，欠费则无法结案。
 * 3. 房间体检：修复动作自动触发 evaluateRoomSafety，实现资源“健康上线”。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RepairOrderServiceImpl extends ServiceImpl<RepairOrderMapper, RepairOrder> implements RepairOrderService {
    
    private final DormFixedAssetService assetService;
    private final DormRoomService roomService;
    private final RepairOrderMapper repairOrderMapper;
    private final WalletTransactionService trxService;
    
    /**
     * 1. 提交报修
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void submitRepair(RepairOrder order) {
        if (order.getRoomId() == null || StrUtil.isBlank(order.getAssetCode())) {
            throw new ServiceException("报修失败：房间信息或资产条码缺失");
        }
        
        // [防刁民]：拦截重复报修，防止师傅跑空单
        Long existingOrderId = repairOrderMapper.checkAssetUnderRepair(order.getAssetCode());
        if (existingOrderId != null) {
            throw new ServiceException("该设备已处于报修流程中，请勿重复刷单！");
        }
        
        order.setOrderNo("REP" + IdUtil.getSnowflakeNextIdStr());
        order.setStatus(0); // 0-待处理
        order.setCreateTime(LocalDateTime.now());
        
        if (!this.save(order)) {
            throw new ServiceException("系统繁忙，提交失败");
        }
        
        // 资产锁定为 50 (维修中)
        assetService.updateAssetStatusByCode(order.getRoomId(), order.getAssetCode(), DormConstants.LC_REPAIRING);
        // 房间安全等级重估
        roomService.evaluateRoomSafety(order.getRoomId());
        
        log.info("🛠️ 报修启动：工单 {} 已锁定资产 {}", order.getOrderNo(), order.getAssetCode());
    }
    
    /**
     * 2. 开始维修 (接单)
     * 🛡️ [防刁民]：利用数据库行锁 takeOrder 确保高并发抢单的唯一性。
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void startRepair(Long orderId, Long repairmanId) {
        int rows = repairOrderMapper.takeOrder(orderId, repairmanId);
        if (rows == 0) {
            throw new ServiceException("接单失败：工单已被抢占或状态已变更");
        }
        log.info("🔧 师傅接单：师傅 ID {} 承接了工单 {}", repairmanId, orderId);
    }
    
    /**
     * 3. 完工结项 (核心财务联动)
     * @param isHumanDamage 是否为人为损坏 (决定是否从学生钱包扣钱)
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void finishRepair(Long orderId, String comment, Integer rating, BigDecimal materialCost, Boolean isHumanDamage) {
        RepairOrder order = this.getById(orderId);
        if (order == null) throw new ServiceException("异常：工单不存在");
        
        // A. 状态审计：结案幂等性校验
        if (order.getStatus() != 1 && order.getStatus() != 5) {
            throw new ServiceException("操作拦截：当前工单状态不支持完工操作");
        }
        
        // B. [财务重头戏]：人为损坏赔偿判定
        if (Boolean.TRUE.equals(isHumanDamage) && materialCost != null && materialCost.compareTo(BigDecimal.ZERO) > 0) {
            log.warn("💳 发现人为损坏：准备对房间 {} 执行维修扣费 {} 元", order.getRoomId(), materialCost);
            
            // 💡 [闭环关键]：如果 WalletTransactionService 余额不足抛出异常，整个事务回滚，工单状态不会变
            trxService.executeTransaction(
                    order.getRoomId(),
                    materialCost.negate(), // 取负值执行扣费
                    4,                     // 业务类型：4-维修耗材支出 (赔偿)
                    order.getOrderNo(),
                    "报修单人为损坏追偿：" + order.getAssetCode()
            );
        }
        
        // C. 更新工单档案
        order.setStatus(2); // 2-已修复(待评价)
        order.setFinishTime(LocalDateTime.now());
        order.setComment(comment);
        order.setRating(rating == null ? 5 : rating);
        order.setMaterialCost(materialCost);
        this.updateById(order);
        
        // D. 释放资产资源 & 房间体检恢复
        if (StrUtil.isNotBlank(order.getAssetCode())) {
            assetService.updateAssetStatusByCode(order.getRoomId(), order.getAssetCode(), DormConstants.LC_NORMAL);
            roomService.evaluateRoomSafety(order.getRoomId());
        }
        
        log.info("✅ 结案成功：工单 {} 完工。人为损坏: {}, 耗材费: {}", order.getOrderNo(), isHumanDamage, materialCost);
    }
    
    /**
     * 4. 挂起工单 (待大修)
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void suspendRepair(Long orderId, String reason) {
        RepairOrder order = this.getById(orderId);
        if (order == null || order.getStatus() >= 2) {
            throw new ServiceException("挂起失败：单据状态已在结案流程中");
        }
        
        order.setStatus(5); // 5-待大修(挂起)
        order.setRemark("【挂起说明】" + reason);
        this.updateById(order);
        
        log.warn("⚠️ 工单挂起：单据 {} 转入长周期维修", order.getOrderNo());
    }
    
    /**
     * 5. 驳回工单 (释放资源)
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void rejectRepair(Long orderId, String reason) {
        RepairOrder order = this.getById(orderId);
        if (order == null || order.getStatus() >= 2) {
            throw new ServiceException("驳回拦截：工单已完工");
        }
        
        order.setStatus(4); // 4-已驳回
        order.setRemark("【驳回原因】" + reason);
        this.updateById(order);
        
        // 释放资产状态回到正常，避免因误报修导致资产永远被锁定
        if (StrUtil.isNotBlank(order.getAssetCode())) {
            assetService.updateAssetStatusByCode(order.getRoomId(), order.getAssetCode(), DormConstants.LC_NORMAL);
            roomService.evaluateRoomSafety(order.getRoomId());
        }
        
        log.info("🚫 驳回成功：工单 {} 状态已撤销", order.getOrderNo());
    }
}