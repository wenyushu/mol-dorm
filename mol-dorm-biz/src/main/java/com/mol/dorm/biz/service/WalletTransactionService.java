package com.mol.dorm.biz.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.mol.dorm.biz.entity.WalletTransaction;
import com.mol.dorm.biz.vo.BuildingBillSummaryVO;

import java.math.BigDecimal;

/**
 * 钱包交易流水服务
 * 🛡️ [防刁民策略]：
 * 1. 唯一流水号：利用分布式 ID 确保每一笔交易都是唯一的。
 * 2. 动账快照：强制记录变动后余额，防止财务对账困难。
 */
public interface WalletTransactionService extends IService<WalletTransaction> {
    
    /**
     * 执行动账入账 (内部核心调用)
     * @param roomId 房间ID
     * @param amount 变动金额 (支出为负)
     * @param type 业务类型
     * @param bizNo 关联业务号
     * @param remark 备注
     * @return 最终流水记录
     */
    WalletTransaction executeTransaction(Long roomId, BigDecimal amount, Integer type, String bizNo, String remark);
    
    /**
     * 获取楼栋月度财务汇总
     * 🛡️ [强类型重构]：舍弃模糊的 Map，使用 VO 确保财务数据的精确性与编译安全。
     */
    BuildingBillSummaryVO getBuildingMonthlyStats(Long buildingId, String month);
}