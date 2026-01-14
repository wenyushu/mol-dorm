package com.mol.dorm.biz.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.mol.dorm.biz.entity.UtilityBill;

public interface UtilityService extends IService<UtilityBill> {
    
    /**
     * 录入读数并生成账单 (管理员/宿管)
     * @param bill 包含房间号、月份、四种读数
     */
    void generateBill(UtilityBill bill);
    
    /**
     * 模拟支付 (学生)
     */
    void pay(Long billId);
    
    /**
     * 分页查询账单
     */
    Page<UtilityBill> getBillPage(Page<UtilityBill> page, Long roomId, String month, Integer status);
}