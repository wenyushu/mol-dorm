package com.mol.dorm.biz.service.impl;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mol.common.core.entity.SysOrdinaryUser;
import com.mol.common.core.exception.ServiceException;
import com.mol.dorm.biz.entity.DormBed;
import com.mol.dorm.biz.entity.DormChangeRequest;
import com.mol.dorm.biz.entity.DormRoom;
import com.mol.dorm.biz.mapper.DormBedMapper;
import com.mol.dorm.biz.mapper.DormChangeRequestMapper;
import com.mol.dorm.biz.mapper.DormRoomMapper;
import com.mol.dorm.biz.service.DormAdjustmentService;
import com.mol.dorm.biz.service.DormBedService;
import com.mol.dorm.biz.service.DormRoomService;
import com.mol.server.service.SysOrdinaryUserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 调宿/退宿 业务实现 (修复版)
 * <p>
 * 适配最新的实体类字段：userId, originRoomId, auditRemark
 * </p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DormAdjustmentServiceImpl extends ServiceImpl<DormChangeRequestMapper, DormChangeRequest> implements DormAdjustmentService {
    
    private final DormBedService bedService;
    private final DormBedMapper bedMapper;
    private final DormRoomMapper roomMapper; // 原子更新
    private final DormRoomService roomService;
    private final SysOrdinaryUserService userService; // 用于查询用户类型(学生/教工)
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean applyForAdjustment(Long userId, String reason, Long targetRoomId, Long swapUserId) {
        // 1. 检查是否有待审核的申请 (防重复提交)
        Long pendingCount = this.baseMapper.selectCount(Wrappers.<DormChangeRequest>lambdaQuery()
                .eq(DormChangeRequest::getUserId, userId) // 🟢 修复：使用 userId
                .eq(DormChangeRequest::getStatus, 0));    // 0:待审核
        
        if (pendingCount > 0) {
            throw new ServiceException("您已有一条待审核的调宿申请，请勿重复提交");
        }
        
        // 2. 查找当前床位 (确保用户确实住在这里)
        DormBed currentBed = bedService.getOne(Wrappers.<DormBed>lambdaQuery()
                .eq(DormBed::getOccupantId, userId));
        
        if (currentBed == null) {
            throw new ServiceException("未找到您的床位信息，无法申请调宿");
        }
        
        // 3. 构建申请单
        DormChangeRequest request = new DormChangeRequest();
        request.setUserId(userId);
        request.setOriginRoomId(currentBed.getRoomId()); // 🟢 修复：使用 originRoomId
        request.setOriginBedId(currentBed.getId());      // 🟢 修复：使用 originBedId
        request.setReason(reason);
        request.setStatus(0); // 0: 待审核
        
        // 4. 判断申请类型
        if (swapUserId != null) {
            // --- 场景：互换模式 ---
            request.setType(2); // 2: 互换
            request.setSwapStudentId(swapUserId); // 这里虽然叫 swapStudentId，但实际上存的是 userId
            
            // 校验目标人员是否入住
            DormBed targetBed = bedService.getOne(Wrappers.<DormBed>lambdaQuery()
                    .eq(DormBed::getOccupantId, swapUserId));
            if (targetBed == null) throw new ServiceException("互换目标对象未入住");
            
            // 互换的目标房间即对方的房间
            request.setTargetRoomId(targetBed.getRoomId());
        } else {
            // --- 场景：迁移模式 (换房) ---
            if (targetRoomId == null) throw new ServiceException("目标房间不能为空");
            request.setType(1); // 1: 单人调宿
            request.setTargetRoomId(targetRoomId);
        }
        
        return this.save(request);
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void auditApply(Long requestId, boolean agree, String rejectReason) {
        // 1. 获取申请单
        DormChangeRequest request = this.getById(requestId);
        if (request == null) throw new ServiceException("申请单不存在");
        if (request.getStatus() != 0) throw new ServiceException("该申请已被处理");
        
        // 2. 处理拒绝逻辑
        if (!agree) {
            request.setStatus(2); // 2: 驳回
            request.setAuditRemark(rejectReason); // 🟢 修复：使用 auditRemark
            this.updateById(request);
            return;
        }
        
        // 3. 处理同意逻辑
        
        // 获取申请人当前的实际床位 (Double Check)
        DormBed srcBed = bedMapper.selectById(request.getOriginBedId());
        if (srcBed == null || !request.getUserId().equals(srcBed.getOccupantId())) {
            throw new ServiceException("申请人床位状态已变更，无法执行操作");
        }
        
        // 获取申请人信息 (为了确定 occupantType)
        SysOrdinaryUser applicant = userService.getById(request.getUserId());
        if (applicant == null) throw new ServiceException("申请人账号异常");
        
        // 1:单人调宿, 2:双人互换
        if (request.getType() == 1) {
            executeMove(request, srcBed, applicant.getUserCategory());
        } else if (request.getType() == 2) {
            executeSwap(request, srcBed);
        }
        
        // 4. 更新申请单状态
        request.setStatus(1); // 1: 通过
        request.setAuditRemark("审核通过");
        this.updateById(request);
    }
    
    /**
     * 执行单人迁移逻辑
     */
    private void executeMove(DormChangeRequest request, DormBed srcBed, Integer userCategory) {
        Long targetRoomId = request.getTargetRoomId();
        Long oldRoomId = srcBed.getRoomId();
        
        // 1. 检查目标房间是否有空床 (原子检查防止超卖的简单版，配合 roomMapper 使用)
        // 注意：这里最好加锁，或者依赖数据库唯一约束。简化起见，我们查找一个空床位。
        List<DormBed> emptyBeds = bedMapper.selectList(Wrappers.<DormBed>lambdaQuery()
                .eq(DormBed::getRoomId, targetRoomId)
                .isNull(DormBed::getOccupantId)
                .orderByAsc(DormBed::getBedLabel));
        
        if (CollUtil.isEmpty(emptyBeds)) {
            throw new ServiceException("目标房间已满员，无空床位");
        }
        DormBed targetBed = emptyBeds.get(0); // 取第一个空床
        
        // 2. 执行移动 (修改床位表)
        // 2.1 释放旧床
        srcBed.setOccupantId(null);
        srcBed.setStatus(0); // 0: 空闲
        bedMapper.updateById(srcBed);
        
        // 2.2 占用新床
        targetBed.setOccupantId(request.getUserId());
        // 🟢 关键：设置正确的用户类型 (0学生/1教工)
        targetBed.setOccupantType(userCategory);
        targetBed.setStatus(1); // 1: 占用
        bedMapper.updateById(targetBed);
        
        // 3. 🟢 原子更新房间人数 (解决并发统计问题)
        // 旧房间 -1
        roomMapper.decreaseOccupancy(oldRoomId, 1);
        // 新房间 +1
        roomMapper.increaseOccupancy(targetRoomId, 1);
        
        // 4. 刷新房间满员状态 (UI展示用)
        refreshRoomStatus(oldRoomId);
        refreshRoomStatus(targetRoomId);
    }
    
    /**
     * 执行双人互换逻辑
     */
    private void executeSwap(DormChangeRequest request, DormBed srcBed) {
        Long swapUserId = request.getSwapStudentId();
        
        // 1. 获取对方床位
        DormBed targetBed = bedService.getOne(Wrappers.<DormBed>lambdaQuery()
                .eq(DormBed::getOccupantId, swapUserId));
        
        if (targetBed == null) {
            throw new ServiceException("互换目标对象已不在原床位");
        }
        
        // 2. 执行互换
        // 互换只需要交换 OccupantId，不需要动 OccupantType (假设互换双方身份一致，或者床位不绑定身份)
        // 如果严格一点，应该连 OccupantType 一起交换
        
        Integer srcType = srcBed.getOccupantType();
        Integer targetType = targetBed.getOccupantType();
        
        // A 去 B 的床
        targetBed.setOccupantId(request.getUserId());
        targetBed.setOccupantType(srcType);
        
        // B 去 A 的床
        srcBed.setOccupantId(swapUserId);
        srcBed.setOccupantType(targetType);
        
        bedMapper.updateById(srcBed);
        bedMapper.updateById(targetBed);
        
        // 互换不涉及房间总人数变化，无需调用 roomMapper
    }
    
    /**
     * 辅助：刷新房间的满员状态 (UI用，不影响核心数据准确性)
     */
    private void refreshRoomStatus(Long roomId) {
        DormRoom room = roomService.getById(roomId);
        if (room != null) {
            // 状态逻辑：人数 >= 容量 ? 满员(20) : 正常(10)
            int newStatus = (room.getCurrentNum() >= room.getCapacity()) ? 20 : 10;
            // 只有状态确实变了，且不是维修(40)状态才更新
            if (room.getStatus() != newStatus && room.getStatus() != 40) {
                DormRoom update = new DormRoom();
                update.setId(roomId);
                update.setStatus(newStatus);
                roomService.updateById(update);
            }
        }
    }
}