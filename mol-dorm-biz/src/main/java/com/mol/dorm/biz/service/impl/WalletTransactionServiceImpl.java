package com.mol.dorm.biz.service.impl;

import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mol.common.core.exception.ServiceException;
import com.mol.dorm.biz.entity.DormRoomWallet;
import com.mol.dorm.biz.entity.WalletTransaction;
import com.mol.dorm.biz.mapper.WalletTransactionMapper;
import com.mol.dorm.biz.service.DormRoomWalletService;
import com.mol.dorm.biz.service.WalletTransactionService;
import com.mol.dorm.biz.vo.BuildingBillSummaryVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class WalletTransactionServiceImpl extends ServiceImpl<WalletTransactionMapper, WalletTransaction> implements WalletTransactionService {
    
    private final DormRoomWalletService walletService;
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public WalletTransaction executeTransaction(Long roomId, BigDecimal amount, Integer type, String bizNo, String remark) {
        // 🛡️ [防刁民逻辑 1]：零元动账拦截
        if (amount.compareTo(BigDecimal.ZERO) == 0) {
            throw new ServiceException("动账失败：变动金额不能为0");
        }
        
        // 🛡️ [防刁民逻辑 2]：获取钱包并加锁（乐观锁在 Service 处理）
        DormRoomWallet wallet = walletService.getWalletByRoomId(roomId);
        if (wallet.getStatus() == 3) {
            throw new ServiceException("账户异常：该寝室钱包已被行政封锁");
        }
        
        // 🛡️ [防刁民逻辑 3]：时空穿越校验
        // 禁止对已经执行过“未来计费”的账户进行“过期补缴” (业务规则可根据需求调整)
        if (wallet.getLastBillingTime() != null && wallet.getLastBillingTime().isAfter(LocalDateTime.now())) {
            throw new ServiceException("系统异常：检测到未来计费记录，当前计费被阻断");
        }
        
        // 计算新余额
        BigDecimal postBalance = wallet.getBalance().add(amount);
        
        // 🛡️ [防刁民逻辑 4]：余额熔断 (除非是系统强制扣费，否则不准扣成负数)
        // 允许水电费扣成负数（停电），但维修耗材等业务如果不充值不给扣
        if (postBalance.compareTo(BigDecimal.ZERO) < 0 && type >= 4) {
            throw new ServiceException("动账失败：余额不足，请先充值后再处理该业务");
        }
        
        // 1. 更新钱包余额 (利用 MyBatis-Plus 乐观锁)
        wallet.setBalance(postBalance);
        if (amount.compareTo(BigDecimal.ZERO) < 0) {
            wallet.setTotalConsume(wallet.getTotalConsume().add(amount.abs()));
        }
        
        // 自动切换状态：如果余额不足则变为 2-欠费冻结
        if (postBalance.compareTo(BigDecimal.ZERO) < 0) {
            wallet.setStatus(2);
        } else {
            wallet.setStatus(1);
        }
        
        walletService.updateById(wallet);
        
        // 2. 生成流水
        WalletTransaction trx = new WalletTransaction();
        trx.setTrxNo("TRX" + IdUtil.getSnowflakeNextIdStr());
        trx.setRoomId(roomId);
        trx.setTrxType(type);
        trx.setAmount(amount);
        trx.setPostBalance(postBalance);
        trx.setBizNo(bizNo);
        trx.setRemark(remark);
        
        this.save(trx);
        
        log.info("💳 [动账存证] 房间: {}, 类型: {}, 金额: {}, 余额: {}", roomId, type, amount, postBalance);
        return trx;
    }
    
    
    @Override
    public BuildingBillSummaryVO getBuildingMonthlyStats(Long buildingId, String month) {
        if (buildingId == null || StrUtil.isBlank(month)) {
            throw new ServiceException("查询参数不完整");
        }
        
        // 1. 获取原始数据
        Map<String, Object> rawData = baseMapper.selectBuildingBillSummary(buildingId, month);
        
        // 2. 组装 VO
        BuildingBillSummaryVO vo = new BuildingBillSummaryVO();
        vo.setMonth(month);
        
        if (rawData != null) {
            // 安全转换并填充
            if (rawData.get("totalIncome") != null) {
                vo.setTotalIncome(new BigDecimal(rawData.get("totalIncome").toString()));
            }
            if (rawData.get("totalOutcome") != null) {
                vo.setTotalOutcome(new BigDecimal(rawData.get("totalOutcome").toString()));
            }
        }
        
        return vo;
    }
}