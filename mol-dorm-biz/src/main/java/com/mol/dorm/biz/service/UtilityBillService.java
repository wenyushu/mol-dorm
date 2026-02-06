package com.mol.dorm.biz.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.mol.dorm.biz.entity.UtilityBill;

import java.util.List;

/**
 * 水电费账单 Service 接口
 * 负责：账单生成、支付、查询
 */
public interface UtilityBillService extends IService<UtilityBill> {
    
    /**
     * 检查选中的账单中是否存在已支付的记录
     * @param ids 账单ID集合
     * @return true-包含已支付, false-全部未支付
     */
    boolean checkHasPaid(List<Long> ids);
    
    /**
     * 生成账单 (核心业务: 动态计费 + 防刁民风控)
     * @param bill 包含读数和月份的账单对象
     */
    void generateBill(UtilityBill bill);
    
    /**
     * 支付账单 (核心业务: 余额扣减)
     * @param billId 账单 ID
     */
    void pay(Long billId);
    
    /**
     * 自动清算（系统尝试抵扣）
     */
    void autoPayBill(Long billId);
    
    /**
     * 余额健康度审计与预警
     */
    void checkWalletSufficiency(Long roomId);
    
    /**
     * 分页查询账单
     * @param page 分页参数
     * @param roomId 房间ID
     * @param month 月份
     * @param status 支付状态
     * @return 分页结果
     */
    Page<UtilityBill> getBillPage(Page<UtilityBill> page, Long roomId, String month, Integer status);
}