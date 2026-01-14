package com.mol.dorm.biz.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.mol.dorm.biz.entity.UtilityBill;

public interface UtilityBillService extends IService<UtilityBill> {
    
    /**
     * 生成/计算账单 (录入用量后调用)
     */
    void calculateAndSave(UtilityBill bill);
    
    /**
     * 模拟支付
     * @param billId 账单 ID
     * @param success 是否模拟成功 (用于演示失败场景)
     */
    void payBill(Long billId, boolean success);
}