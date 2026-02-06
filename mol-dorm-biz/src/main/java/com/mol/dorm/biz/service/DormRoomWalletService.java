package com.mol.dorm.biz.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.mol.dorm.biz.entity.DormRoomWallet;
import com.mol.dorm.biz.entity.RechargeLog;

import java.math.BigDecimal;

/**
 * 房间钱包服务接口
 */
public interface DormRoomWalletService extends IService<DormRoomWallet> {
    
    /**
     * 充值逻辑 (必须使用 BigDecimal)
     */
    void recharge(Long roomId, BigDecimal amount, String orderNo, Long userId);
    
    /**
     * 根据房间ID查询钱包
     */
    DormRoomWallet getWalletByRoomId(Long roomId);
    
    /**
     * 分页查询房间充值流水明细
     * 用于 Controller 展示财务对账单
     */
    Page<RechargeLog> getRechargePage(Page<RechargeLog> page, Long roomId);
    
    /**
     * 更新钱包状态
     */
    void updateWalletStatus(Long roomId, Integer status);
}