package com.mol.dorm.biz.task;

import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.RandomUtil;
import com.mol.dorm.biz.entity.DormRoomWallet;
import com.mol.dorm.biz.service.DormRoomWalletService;
import com.mol.dorm.biz.service.WalletTransactionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 🌙 月度水电费自动计费任务 (生产力版)
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class MonthlyBillingTask {
    
    private final DormRoomWalletService walletService;
    private final WalletTransactionService trxService;
    
    /**
     * 每月 1 号凌晨 1 点执行
     * 🛡️ [防刁民逻辑]：基于 lastBillingTime 校验，确保不重扣、不漏扣。
     */
    @Scheduled(cron = "0 0 1 1 * ?")
    @Transactional(rollbackFor = Exception.class)
    public void runMonthlyBilling() {
        log.info("🚀 [计费引擎] 开始执行本月水电费模拟扣缴...");
        List<DormRoomWallet> wallets = walletService.list();
        LocalDateTime now = LocalDateTime.now();
        
        for (DormRoomWallet wallet : wallets) {
            // 1. 幂等校验：如果最后一次计费是在同月，则跳过
            if (wallet.getLastBillingTime() != null &&
                    wallet.getLastBillingTime().getMonth() == now.getMonth()) {
                continue;
            }
            
            // 2. 模拟随机能耗 (电费 50-200, 水费 10-50)
            BigDecimal electricityFee = new BigDecimal(RandomUtil.randomInt(50, 200));
            BigDecimal waterFee = new BigDecimal(RandomUtil.randomInt(10, 50));
            
            // 3. 调用流水引擎扣费 (此时会触发余额熔断和状态变更)
            trxService.executeTransaction(wallet.getRoomId(), electricityFee.negate(), 2, "SYS_E_" + IdUtil.fastSimpleUUID(), now.getMonthValue() + "月模拟电费");
            trxService.executeTransaction(wallet.getRoomId(), waterFee.negate(), 3, "SYS_W_" + IdUtil.fastSimpleUUID(), now.getMonthValue() + "月模拟水费");
            
            // 4. 更新计费锚点
            wallet.setLastBillingTime(now);
            walletService.updateById(wallet);
        }
        log.info("✅ [计费引擎] 任务完成，处理房间数：{}", wallets.size());
    }
}