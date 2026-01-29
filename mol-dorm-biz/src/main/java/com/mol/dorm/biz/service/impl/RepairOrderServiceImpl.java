package com.mol.dorm.biz.service.impl;

import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mol.common.core.constant.RoleConstants;
import com.mol.common.core.exception.ServiceException;
import com.mol.common.core.util.LoginHelper;
import com.mol.dorm.biz.entity.RepairOrder;
import com.mol.dorm.biz.mapper.RepairOrderMapper;
import com.mol.dorm.biz.service.RepairOrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * 报修工单服务
 * <p>
 * 核心修复：
 * 1. 完工校验：只有被指派的维修工本人(或超管)才能点完工。
 * 2. 评价校验：只有申请人本人才能评价。
 * 3. 提交校验：强制绑定当前登录用户，防止代提。
 * </p>
 *
 * @author mol
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RepairOrderServiceImpl extends ServiceImpl<RepairOrderMapper, RepairOrder> implements RepairOrderService {
    
    // 状态常量: 0待处理 1维修中 2已完成 3已评价
    private static final int STATUS_PENDING = 0;
    private static final int STATUS_PROCESSING = 1;
    private static final int STATUS_FIXED = 2;
    private static final int STATUS_RATED = 3;
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void submit(Long studentId, Long roomId, String desc, String images) {
        // 1. 🛡️ 防刁民：身份一致性校验
        // 防止恶意用户通过接口抓包，修改 studentId 参数帮别人（或者恶搞别人）提交工单
        Long currentUserId = LoginHelper.getUserId();
        if (currentUserId != null && !LoginHelper.isAdmin() && !currentUserId.equals(studentId)) {
            throw new ServiceException("非法操作：只能为您自己的账号提交报修");
        }
        
        RepairOrder order = new RepairOrder();
        // 生成工单号 R + 纳秒ID (简化版，生产环境建议用 Redis 自增或雪花算法)
        order.setOrderNo("R" + IdUtil.getSnowflakeNextIdStr());
        order.setApplicantId(studentId);
        order.setRoomId(roomId);
        order.setDescription(desc);
        order.setImages(images);
        order.setStatus(STATUS_PENDING);
        
        this.save(order);
        log.info("工单提交成功: No={}, Applicant={}", order.getOrderNo(), studentId);
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void assign(Long orderId, Long repairmanId) {
        RepairOrder order = this.getById(orderId);
        if (order == null) throw new ServiceException("工单不存在");
        
        // 状态检查：只有待处理或维修中(换人)可以指派
        if (order.getStatus() != STATUS_PENDING && order.getStatus() != STATUS_PROCESSING) {
            throw new ServiceException("当前状态无法指派维修人员");
        }
        
        order.setRepairmanId(repairmanId);
        order.setStatus(STATUS_PROCESSING); // 状态流转 -> 维修中
        this.updateById(order);
        
        log.info("工单指派成功: No={}, Repairman={}", order.getOrderNo(), repairmanId);
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void complete(Long orderId, String remark) {
        RepairOrder order = this.getById(orderId);
        if (order == null) throw new ServiceException("工单不存在");
        
        // 1. 状态检查
        if (order.getStatus() != STATUS_PROCESSING) {
            throw new ServiceException("只有【维修中】的工单才能进行完工操作");
        }
        
        // 2. 🛡️ 防刁民：操作权校验
        // 只有“被指派的维修工本人”或者“管理员”可以点完工
        // 防止维修工A 恶意把 维修工B 的单子点了
        Long currentUserId = LoginHelper.getUserId();
        boolean isTheRepairman = ObjectUtil.equal(order.getRepairmanId(), currentUserId);
        
        if (!LoginHelper.isAdmin() && !isTheRepairman) {
            throw new ServiceException("无权操作：您不是该工单的指派维修员");
        }
        
        order.setStatus(STATUS_FIXED);
        order.setFinishTime(LocalDateTime.now());
        // 追加反馈信息
        if (remark != null) {
            String oldRemark = order.getRemark() == null ? "" : order.getRemark() + "; ";
            order.setRemark(oldRemark + "维修反馈: " + remark);
        }
        this.updateById(order);
        
        log.info("工单完工: No={}, Operator={}", order.getOrderNo(), currentUserId);
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void rate(Long orderId, Integer rating, String comment) {
        RepairOrder order = this.getById(orderId);
        if (order == null) throw new ServiceException("工单不存在");
        
        // 1. 状态检查
        if (order.getStatus() != STATUS_FIXED) {
            throw new ServiceException("请等待维修完成后再进行评价");
        }
        
        // 2. 🛡️ 防刁民：申请人校验
        // 只有“申请人本人”可以评价，防止被恶意刷分
        Long currentUserId = LoginHelper.getUserId();
        if (!ObjectUtil.equal(order.getApplicantId(), currentUserId)) {
            throw new ServiceException("无权评价：您不是该工单的申请人");
        }
        
        order.setRating(rating);
        order.setComment(comment);
        order.setStatus(STATUS_RATED); // 流程结束
        this.updateById(order);
        
        log.info("工单评价完成: No={}, Rating={}", order.getOrderNo(), rating);
    }
    
    @Override
    public Page<RepairOrder> getPage(Page<RepairOrder> page, RepairOrder query, Long currentUserId, String userRole) {
        LambdaQueryWrapper<RepairOrder> wrapper = Wrappers.lambdaQuery();
        
        // 1. 数据权限过滤 (Data Scope)
        if (RoleConstants.STUDENT.equals(userRole)) {
            // 学生：只能看【自己提交】的
            wrapper.eq(RepairOrder::getApplicantId, currentUserId);
        }
        else if (RoleConstants.REPAIR_MASTER.equals(userRole)) { // 假设角色Key是 repair_master
            // 维修工：看【指派给自己】的 + 【所有待分配】的(抢单模式可选)
            // 这里采用严格模式：只看自己的任务
            wrapper.eq(RepairOrder::getRepairmanId, currentUserId);
        }
        // 管理员/宿管：查看所有 (无需加限制条件)
        
        // 2. 动态查询条件
        if (query.getStatus() != null) {
            wrapper.eq(RepairOrder::getStatus, query.getStatus());
        }
        if (query.getRoomId() != null) {
            wrapper.eq(RepairOrder::getRoomId, query.getRoomId());
        }
        if (query.getOrderNo() != null) {
            wrapper.like(RepairOrder::getOrderNo, query.getOrderNo());
        }
        
        // 3. 排序：未完成的优先，时间倒序
        wrapper.orderByAsc(RepairOrder::getStatus)
                .orderByDesc(RepairOrder::getCreateTime);
        
        return this.page(page, wrapper);
    }
}