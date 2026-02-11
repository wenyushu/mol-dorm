package com.mol.dorm.biz.service.impl;

import cn.hutool.core.util.IdUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mol.common.core.constant.DormConstants;
import com.mol.common.core.constant.RoleConstants;
import com.mol.common.core.exception.ServiceException;
import com.mol.common.core.util.LoginHelper;
import com.mol.dorm.biz.entity.RepairOrder;
import com.mol.dorm.biz.mapper.RepairOrderMapper;
import com.mol.dorm.biz.service.DormFixedAssetService;
import com.mol.dorm.biz.service.DormRoomService;
import com.mol.dorm.biz.service.RepairOrderService;
import com.mol.dorm.biz.service.WalletTransactionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.aop.framework.AopContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 报修工单全生命周期管理实现类
 * 🛡️ [防刁民逻辑矩阵]：
 * 1. 物理熔断：挂起(待大修)时强制将资源状态置为 LC_REPAIRING(50)，锁死分房引擎入口。
 * 2. 财务锁死：人为损坏赔偿与结案动作绑定事务，若学生钱包余额不足扣除赔偿款，则不允许结案，防止损失流失。
 * 3. 评价防伪：利用 Session 强校验评价人身份，严禁非申请人越权刷分。
 * 4. 原子调度：利用数据库行级锁实现师傅抢单/指派，彻底杜绝一人多接或重复接单。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RepairOrderServiceImpl extends ServiceImpl<RepairOrderMapper, RepairOrder> implements RepairOrderService {
    
    private final DormFixedAssetService assetService;
    private final DormRoomService roomService;
    private final WalletTransactionService trxService;
    private final RepairOrderMapper repairOrderMapper;
    
    /**
     * 1. 提交报修申请
     * [逻辑]：自动锁定资产 -> 触发房间降级评估 -> 触发自动派单引擎
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void submitRepair(RepairOrder order) {
        // [防重复报修]：下钻至原子级 assetCode 校验，防止对同一故障多次刷单
        Long existingId = repairOrderMapper.checkAssetUnderRepair(order.getAssetCode());
        if (existingId != null) throw new ServiceException("刁民拦截：该设备已经在维修队列中，请勿重复操作！");
        
        order.setOrderNo("REP" + IdUtil.getSnowflakeNextIdStr());
        order.setStatus(0); // 初始：待处理
        order.setCreateTime(LocalDateTime.now());
        this.save(order);
        
        // 🛡️ 状态传递：关联资产设为 50-维修中
        assetService.updateAssetStatusByCode(order.getRoomId(), order.getAssetCode(), DormConstants.LC_REPAIRING);
        // 🛡️ 房间体检：重算安全性。若故障严重，房间将自动变为“不可分配”
        roomService.evaluateRoomSafety(order.getRoomId());
        
        // 🤖 自动调度入口
        this.autoAllocate(order.getId());
    }
    
    /**
     * 2. 自动派单引擎 (增强版)
     * 🛡️ [防断档设计]：
     * 1. 负载优先：指派给当前任务最少的师傅。
     * 2. 老大保底：若全校师傅都忙或不在岗，自动指派给“维修老大”(RoleConstants.REPAIR_MASTER_ID)。
     */
    @Override
    @Transactional(rollbackFor = Exception.class) // 🟢 外层加事务，确保内部调用安全
    public void autoAllocate(Long orderId) {
        // A. 寻找最闲师傅
        List<Long> availableMasters = repairOrderMapper.selectRepairmanByTaskCount();
        Long targetMasterId;
        
        if (!availableMasters.isEmpty()) {
            targetMasterId = availableMasters.get(0);
            log.info("🤖 数字化运维：工单 {} 自动均衡指派至师傅 {}", orderId, targetMasterId);
        } else {
            // B. 🛠️ [保底机制]：如果没有师傅在线，默认指派给维修老大
            targetMasterId = RoleConstants.REPAIR_MASTER_ID;
            log.warn("🚨 预警：当前无可用师傅，工单 {} 已自动转交给维修负责人(ID:{})", orderId, targetMasterId);
        }
        
        // C. 🛡️ [解决自调用]：通过代理对象调用，确保 startRepair 的事务和幂等逻辑生效
        ((RepairOrderService) AopContext.currentProxy()).startRepair(orderId, targetMasterId);
    }
    
    /**
     * 3. 师傅接单 (开始维修)
     * [防刁民点]：利用 DB 级 update 影响行数实现原子性，防止多个师傅在同一毫秒点击导致业务重叠。
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void startRepair(Long orderId, Long repairmanId) {
        int rows = repairOrderMapper.takeOrder(orderId, repairmanId);
        if (rows == 0)
            throw new ServiceException("抢占失败：该工单可能已被其他师傅领取或已撤回");
    }
    
    /**
     * 4. 完工结项 (核心自愈与财务链路)
     * @param isHumanDamage 判定为人为损坏时，系统执行“强制赔偿”逻辑
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void finishRepair(Long orderId, String comment, Integer rating, BigDecimal materialCost, Boolean isHumanDamage) {
        RepairOrder order = this.getById(orderId);
        if (order == null || order.getStatus() >= 2) throw new ServiceException("异常拦截：工单状态已变更，禁止重复结案");

        // A. [核心财务闭环]
        // 🛡️ 只有明确判定为“人为损坏”且“有材料费”时，才强制触发钱包扣赔事务
        if (Boolean.TRUE.equals(isHumanDamage) && materialCost.compareTo(BigDecimal.ZERO) > 0) {
            // 💡 [逻辑对账]：负值表示从钱包扣除，trxService 失败会抛异常回滚事务，确保不交钱不让修完
            trxService.executeTransaction(order.getRoomId(), materialCost.negate(), 4, order.getOrderNo(), "维修人为损坏追偿");
        }
        
        // B. 状态归档
        order.setStatus(2); // 状态：2-已修复(待评价)
        order.setFinishTime(LocalDateTime.now());
        order.setComment(comment);
        this.updateById(order);
        
        // C. 🚀 [自愈逻辑]：资产状态恢复正常(20)，并触发房间安全体检，若已无隐患则房间重回“可分配”池
        assetService.updateAssetStatusByCode(order.getRoomId(), order.getAssetCode(), DormConstants.LC_NORMAL);
        roomService.evaluateRoomSafety(order.getRoomId());
        
        log.info("✅ 闭环成功：工单 {} 完工，资源已重入池。", order.getOrderNo());
    }
    
    /**
     * 5. 挂起工单 (转待大修/长周期处理)
     * [场景]：现场判定不可住人，需长时间封锁宿舍。
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void suspendRepair(Long orderId, String reason) {
        RepairOrder order = this.getById(orderId);
        order.setStatus(5); // 状态：5-挂起(待大修)
        order.setRemark("【挂起说明】" + reason);
        this.updateById(order);
        
        // 🛡️ [物理锁定]：强制将房间/床位所在资源切为 50(维修中) 状态，使管理员无法在系统中进行入住分房操作
        roomService.updateRoomStatus(order.getRoomId(), DormConstants.LC_REPAIRING);
        log.warn("🚨 物理熔断：房间 {} 因大修任务已强制下架，请尽快腾挪学生。", order.getRoomId());
    }
    
    /**
     * 6. 驳回申请 (误报或恶意报修处理)
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void rejectRepair(Long orderId, String reason) {
        RepairOrder order = this.getById(orderId);
        order.setStatus(4); // 状态：4-已驳回
        this.updateById(order);
        
        // 驳回视为从未坏过，释放资产并重新评估房态
        assetService.updateAssetStatusByCode(order.getRoomId(), order.getAssetCode(), DormConstants.LC_NORMAL);
        roomService.evaluateRoomSafety(order.getRoomId());
    }
    
    /**
     * 7. 用户评价入口
     * [防刁民逻辑]：1. 非申请人禁止评价；2. 未完工单据禁止评价；3. 已评价单据禁止篡改。
     */
    @Override
    public void rateOrder(Long orderId, Integer rating, String comment, Long userId) {
        RepairOrder order = this.getById(orderId);
        if (order == null || !order.getApplicantId().equals(userId)) {
            throw new ServiceException("越权拦截：您无权评价此报修工单！");
        }
        if (order.getStatus() != 2) throw new ServiceException("状态拦截：当前报修尚未结束，无法评价");
        
        order.setRating(rating);
        order.setComment(comment);
        order.setStatus(3); // 状态：3-已评价
        this.updateById(order);
    }
    
    /**
     * 8. 多维度角色分页查询
     * [数据权限隔离]：自动识别登录身份，实现“数据不外泄”。
     */
    @Override
    public IPage<RepairOrder> selectOrderPage(Page<RepairOrder> page, RepairOrder query, Long userId) {
        LambdaQueryWrapper<RepairOrder> lqw = new LambdaQueryWrapper<>(query);
        
        if (LoginHelper.hasRole(RoleConstants.REPAIR_MASTER)) {
            lqw.eq(RepairOrder::getRepairmanId, userId); // 师傅：视角锁定名下工单
        } else if (LoginHelper.isStudent()) {
            lqw.eq(RepairOrder::getApplicantId, userId); // 学生：视角锁定本人报修
        }
        // 管理员：默认 Query 穿透全表
        
        return this.page(page, lqw.orderByDesc(RepairOrder::getCreateTime));
    }
}