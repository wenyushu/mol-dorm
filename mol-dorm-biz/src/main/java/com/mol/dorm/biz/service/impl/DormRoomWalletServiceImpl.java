package com.mol.dorm.biz.service.impl;

import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mol.common.core.exception.ServiceException;
import com.mol.dorm.biz.entity.DormRoomWallet;
import com.mol.dorm.biz.entity.RechargeLog;
import com.mol.dorm.biz.mapper.DormRoomWalletMapper;
import com.mol.dorm.biz.mapper.RechargeLogMapper;
import com.mol.dorm.biz.service.DormRoomWalletService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

/**
 * 房间统一钱包业务实现类
 * 🛡️ [金融级设计要素]：
 * 1. 动账审计：Snapshot 机制，流水表记录变更前后的 balance。
 * 2. 原子更新：WHERE id = ? 的 SQL 原生加法，解决高并发下“两人同时充值导致金额覆盖”的问题。
 * 3. 容错初始化：针对新房间提供“查询即创建”的静默初始化逻辑。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DormRoomWalletServiceImpl extends ServiceImpl<DormRoomWalletMapper, DormRoomWallet> implements DormRoomWalletService {
    
    private final DormRoomWalletMapper walletMapper;
    private final RechargeLogMapper rechargeLogMapper;
    
    /**
     * 1. 核心充值逻辑 (带审计流水)
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void recharge(Long roomId, BigDecimal amount, String orderNo, Long userId) {
        log.info(">>> [金融审计] 触发房间充值。房间ID:{}, 金额:{}", roomId, amount);
        
        // A. [参数防御]：拦截非法负数充值
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new ServiceException("非法操作：充值金额必须大于零");
        }
        
        // B. [幂等防线]：生成/校验唯一订单号，防止前端“连点”导致重复到账
        String finalOrderNo = StrUtil.isBlank(orderNo) ? "REC_" + IdUtil.getSnowflakeNextIdStr() : orderNo;
        Long exists = rechargeLogMapper.selectCount(Wrappers.<RechargeLog>lambdaQuery().eq(RechargeLog::getOrderNo, finalOrderNo));
        if (exists > 0) {
            throw new ServiceException("业务拦截：流水单号[" + finalOrderNo + "]已处理，严禁重复入账");
        }
        
        // C. [资产快照]：获取动账前的原始余额 (用于对账)
        DormRoomWallet wallet = this.getWalletByRoomId(roomId);
        if (wallet.getStatus() == 3) { // 3-停用
            throw new ServiceException("账户异常：该寝室钱包已被财务冻结停用");
        }
        BigDecimal beforeBalance = wallet.getBalance();
        
        // D. [原子更新]：通过 Mapper 执行 balance = balance + amount
        // 🛡️ 为什么不直接用 updateById？
        // 因为 updateById 是基于实体类值的覆盖，如果两个线程同时读取余额为10，各充5块，
        // 最后结果可能是15而不是20。SQL 原生加法则能保证 10+5+5=20。
        int rows = walletMapper.rechargeBalance(roomId, amount);
        if (rows == 0) {
            throw new ServiceException("动账失败：账户系统繁忙，请重试");
        }
        
        // E. [流水存证]：记录充值后的快照
        BigDecimal afterBalance = beforeBalance.add(amount);
        
        RechargeLog logEntity = new RechargeLog();
        logEntity.setOrderNo(finalOrderNo);
        logEntity.setRoomId(roomId);
        logEntity.setUserId(userId);
        logEntity.setAmount(amount);
        logEntity.setBeforeBalance(beforeBalance);
        logEntity.setAfterBalance(afterBalance);
        logEntity.setStatus(1); // 成功
        logEntity.setPayType("WALLET_RECHARGE");
        
        // 🛡️ [审计增强]：显式设置创建者，对应刚才 SQL 补齐的 create_by 字段
        // userId 是 Long 类型，createBy 是 String 类型，这里做个转换
        logEntity.setCreateBy(String.valueOf(userId));
        
        // 💡 注意：logEntity.setCreateTime(LocalDateTime.now()) 这行不需要写！
        // 只要你配置了 MyBatis-Plus 的自动填充，或者数据库字段设置了 DEFAULT CURRENT_TIMESTAMP，
        // 它会自动生成，手动写反而容易造成时间偏差。
        
        rechargeLogMapper.insert(logEntity);
        
        log.info("<<< [充值成功] ，您的房间:{} 余额已更新。新余额为:{}", roomId, afterBalance);
    }
    
    /**
     * 2. 分页查询流水明细
     */
    @Override
    public Page<RechargeLog> getRechargePage(Page<RechargeLog> page, Long roomId) {
        return rechargeLogMapper.selectPage(page, Wrappers.<RechargeLog>lambdaQuery()
                .eq(RechargeLog::getRoomId, roomId)
                .orderByDesc(RechargeLog::getCreateTime)); // 时间倒序，最新流水排第一
    }
    
    /**
     * 3. 根据房间ID查询钱包 (含静默初始化)
     */
    @Override
    public DormRoomWallet getWalletByRoomId(Long roomId) {
        DormRoomWallet wallet = this.getOne(Wrappers.<DormRoomWallet>lambdaQuery().eq(DormRoomWallet::getRoomId, roomId));
        
        // 🛡️ [自愈逻辑]：如果因为系统迁移或新房入驻导致没钱包，在此进行自动的补全，防止 Controller 报空指针
        if (wallet == null) {
            log.warn("⚠️ 自动为房间 {} 创建初始化钱包", roomId);
            wallet = new DormRoomWallet();
            wallet.setRoomId(roomId);
            wallet.setBalance(BigDecimal.ZERO);
            wallet.setStatus(1); // 1-正常
            this.save(wallet);
        }
        return wallet;
    }
    
    /**
     * 4. 账户状态强制切换
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateWalletStatus(Long roomId, Integer status) {
        boolean success = this.update(Wrappers.<DormRoomWallet>lambdaUpdate()
                .eq(DormRoomWallet::getRoomId, roomId)
                .set(DormRoomWallet::getStatus, status));
        if (!success) {
            throw new ServiceException("操作失败：目标房间钱包不存在");
        }
    }
}