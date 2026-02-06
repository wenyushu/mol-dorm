package com.mol.dorm.biz.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mol.dorm.biz.entity.RechargeLog;
import com.mol.dorm.biz.mapper.RechargeLogMapper;
import com.mol.dorm.biz.service.RechargeLogService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class RechargeLogServiceImpl extends ServiceImpl<RechargeLogMapper, RechargeLog> implements RechargeLogService {
    
    @Override
    public Page<RechargeLog> getLogPage(Page<RechargeLog> page, Long roomId, String orderNo,
                                        LocalDateTime beginTime, LocalDateTime endTime) {
        LambdaQueryWrapper<RechargeLog> wrapper = Wrappers.lambdaQuery();
        
        // 1. 精确匹配条件
        wrapper.eq(roomId != null, RechargeLog::getRoomId, roomId);
        wrapper.eq(StrUtil.isNotBlank(orderNo), RechargeLog::getOrderNo, orderNo);
        
        // 2. 时间范围对账条件 (常见对账场景)
        wrapper.ge(beginTime != null, RechargeLog::getCreateTime, beginTime);
        wrapper.le(endTime != null, RechargeLog::getCreateTime, endTime);
        
        // 3. 排序逻辑：永远查看最新流水
        wrapper.orderByDesc(RechargeLog::getCreateTime);
        
        return this.page(page, wrapper);
    }
}