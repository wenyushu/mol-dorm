package com.mol.dorm.biz.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.mol.dorm.biz.entity.RechargeLog;

import java.time.LocalDateTime;

public interface RechargeLogService extends IService<RechargeLog> {
    
    /**
     * 分页查询充值流水记录 (对账专用)
     */
    Page<RechargeLog> getLogPage(Page<RechargeLog> page, Long roomId, String orderNo,
                                 LocalDateTime beginTime, LocalDateTime endTime);
}