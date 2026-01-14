package com.mol.dorm.biz.service.impl;

import cn.hutool.core.util.IdUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mol.common.core.constant.RoleConstants;
import com.mol.common.core.exception.ServiceException;
import com.mol.dorm.biz.entity.RepairOrder;
import com.mol.dorm.biz.mapper.RepairOrderMapper;
import com.mol.dorm.biz.service.RepairOrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class RepairOrderServiceImpl extends ServiceImpl<RepairOrderMapper, RepairOrder> implements RepairOrderService {
    
    // 状态常量
    private static final int STATUS_PENDING = 0;
    private static final int STATUS_PROCESSING = 1;
    private static final int STATUS_FIXED = 2;
    private static final int STATUS_RATED = 3;
    private static final int STATUS_REJECT = 4;
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void submit(Long studentId, Long roomId, String desc, String images) {
        RepairOrder order = new RepairOrder();
        // 生成工单号 R + 纳秒ID (简化版)
        order.setOrderNo("R" + IdUtil.getSnowflakeNextIdStr());
        order.setApplicantId(studentId);
        order.setRoomId(roomId);
        order.setDescription(desc);
        order.setImages(images);
        order.setStatus(STATUS_PENDING);
        
        this.save(order);
    }
    
    @Override
    public void assign(Long orderId, Long repairmanId) {
        RepairOrder order = this.getById(orderId);
        if (order == null) throw new ServiceException("工单不存在");
        
        // 状态检查
        if (order.getStatus() != STATUS_PENDING && order.getStatus() != STATUS_PROCESSING) {
            throw new ServiceException("当前状态无法指派");
        }
        
        order.setRepairmanId(repairmanId);
        order.setStatus(STATUS_PROCESSING); // 变更为维修中
        this.updateById(order);
    }
    
    @Override
    public void complete(Long orderId, String remark) {
        RepairOrder order = this.getById(orderId);
        if (order == null) throw new ServiceException("工单不存在");
        
        if (order.getStatus() != STATUS_PROCESSING) {
            throw new ServiceException("只有【维修中】的工单才能完工");
        }
        
        order.setStatus(STATUS_FIXED);
        order.setFinishTime(LocalDateTime.now());
        // 维修工的反馈可以追加到 remark 字段或单独字段，这里简单追加
        if (remark != null) {
            order.setRemark("维修反馈: " + remark);
        }
        this.updateById(order);
    }
    
    @Override
    public void rate(Long orderId, Integer rating, String comment) {
        RepairOrder order = this.getById(orderId);
        if (order == null) throw new ServiceException("工单不存在");
        
        if (order.getStatus() != STATUS_FIXED) {
            throw new ServiceException("请等待维修完成后再评价");
        }
        
        order.setRating(rating);
        order.setComment(comment);
        order.setStatus(STATUS_RATED); // 流程结束
        this.updateById(order);
    }
    
    @Override
    public Page<RepairOrder> getPage(Page<RepairOrder> page, RepairOrder query, Long currentUserId, String userRole) {
        LambdaQueryWrapper<RepairOrder> wrapper = Wrappers.lambdaQuery();
        
        // 1. 数据权限过滤
        if (RoleConstants.STUDENT.equals(userRole)) {
            // 学生只能看自己提交的
            wrapper.eq(RepairOrder::getApplicantId, currentUserId);
        } else if ("worker".equals(userRole)) { // 假设维修工角色key是worker
            // 维修工看指派给自己的 或 待分配的(可选)
            wrapper.eq(RepairOrder::getRepairmanId, currentUserId)
                    .or().eq(RepairOrder::getStatus, STATUS_PENDING);
        }
        // 管理员看所有，无需额外过滤
        
        // 2. 查询条件
        if (query.getStatus() != null) {
            wrapper.eq(RepairOrder::getStatus, query.getStatus());
        }
        if (query.getRoomId() != null) {
            wrapper.eq(RepairOrder::getRoomId, query.getRoomId());
        }
        
        wrapper.orderByDesc(RepairOrder::getCreateTime);
        return this.page(page, wrapper);
    }
}