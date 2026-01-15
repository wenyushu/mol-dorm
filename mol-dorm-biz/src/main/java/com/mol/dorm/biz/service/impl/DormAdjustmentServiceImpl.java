package com.mol.dorm.biz.service.impl;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mol.common.core.exception.ServiceException;
import com.mol.dorm.biz.entity.DormBed;
import com.mol.dorm.biz.entity.DormChangeRequest;
import com.mol.dorm.biz.entity.DormRoom;
import com.mol.dorm.biz.mapper.DormBedMapper;
import com.mol.dorm.biz.mapper.DormChangeRequestMapper; // ✅ 必须导入这个
import com.mol.dorm.biz.mapper.DormRoomMapper;
import com.mol.dorm.biz.service.DormAdjustmentService;
import com.mol.dorm.biz.service.DormBedService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class DormAdjustmentServiceImpl extends ServiceImpl<DormChangeRequestMapper, DormChangeRequest> implements DormAdjustmentService {
    
    // 由于继承了 ServiceImpl，this.baseMapper 会自动注入 DormChangeRequestMapper
    // 所以这里不需要再声明 private final DormChangeRequestMapper requestMapper;
    
    private final DormBedService bedService;
    private final DormBedMapper bedMapper;
    private final DormRoomMapper roomMapper;
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean applyForAdjustment(Long userId, String reason, Long targetRoomId, Long swapStudentId) {
        // 1. 检查是否有待审核的申请
        // 修复后，这里的 this.baseMapper 就有了 selectCount 方法
        Long pendingCount = this.baseMapper.selectCount(Wrappers.<DormChangeRequest>lambdaQuery()
                .eq(DormChangeRequest::getUserId, userId)
                .eq(DormChangeRequest::getStatus, 0)); // 0:待审核
        if (pendingCount > 0) {
            throw new ServiceException("您已有一条待审核的调宿申请，请勿重复提交");
        }
        
        // 2. 查找当前床位
        DormBed currentBed = bedService.getOne(Wrappers.<DormBed>lambdaQuery()
                .eq(DormBed::getOccupantId, userId));
        if (currentBed == null) {
            throw new ServiceException("未找到您的床位信息，无法申请调宿");
        }
        
        // 3. 构建申请单
        DormChangeRequest request = new DormChangeRequest();
        request.setUserId(userId);
        request.setOriginRoomId(currentBed.getRoomId());
        request.setOriginBedId(currentBed.getId());
        request.setReason(reason);
        request.setStatus(0); // 0: 待审核
        
        // 4. 判断申请类型
        if (swapStudentId != null) {
            // --- 互换模式 ---
            request.setType(2); // 2: 互换
            request.setSwapStudentId(swapStudentId);
            
            // 校验目标学生是否入住
            DormBed targetBed = bedService.getOne(Wrappers.<DormBed>lambdaQuery()
                    .eq(DormBed::getOccupantId, swapStudentId));
            if (targetBed == null) throw new ServiceException("目标互换学生未入住");
            
            // 互换的目标房间即对方的房间
            request.setTargetRoomId(targetBed.getRoomId());
        } else {
            // --- 迁移模式 ---
            if (targetRoomId == null) throw new ServiceException("目标房间不能为空");
            request.setType(1); // 1: 迁移
            request.setTargetRoomId(targetRoomId);
        }
        
        return this.save(request);
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void auditApply(Long requestId, boolean agree, String rejectReason) {
        DormChangeRequest request = this.getById(requestId);
        if (request == null) throw new ServiceException("申请单不存在");
        if (request.getStatus() != 0) throw new ServiceException("该申请已被处理");
        
        if (!agree) {
            // --- 拒绝 ---
            request.setStatus(2); // 2: 已拒绝
            // 如果 Entity 有 remark 字段，可保存拒绝原因: request.setRemark(rejectReason);
            this.updateById(request);
            return;
        }
        
        // --- 同意 (执行调宿逻辑) ---
        
        // 1. 获取申请人的当前床位 (Double Check)
        DormBed srcBed = bedMapper.selectById(request.getOriginBedId());
        if (srcBed == null || !request.getUserId().equals(srcBed.getOccupantId())) {
            throw new ServiceException("申请人床位状态已变更，无法执行操作");
        }
        
        if (request.getType() == 1) {
            // ================== 场景A：单人迁移 ==================
            Long targetRoomId = request.getTargetRoomId();
            
            // 1.1 检查目标房间是否有空床
            List<DormBed> emptyBeds = bedMapper.selectList(Wrappers.<DormBed>lambdaQuery()
                    .eq(DormBed::getRoomId, targetRoomId)
                    .isNull(DormBed::getOccupantId));
            
            if (CollUtil.isEmpty(emptyBeds)) {
                throw new ServiceException("目标房间已满员，无空床位");
            }
            DormBed targetBed = emptyBeds.get(0); // 取第一个空床
            
            // 1.2 执行移动
            srcBed.setOccupantId(null); // 旧床置空
            targetBed.setOccupantId(request.getUserId()); // 新床入住
            
            bedMapper.updateById(srcBed);
            bedMapper.updateById(targetBed);
            
            // 1.3 更新房间人数
            updateRoomCount(srcBed.getRoomId());
            updateRoomCount(targetRoomId);
            
        } else if (request.getType() == 2) {
            // ================== 场景B：双人互换 ==================
            Long swapUserId = request.getSwapStudentId();
            
            // 2.1 获取对方床位
            DormBed targetBed = bedService.getOne(Wrappers.<DormBed>lambdaQuery()
                    .eq(DormBed::getOccupantId, swapUserId));
            
            if (targetBed == null) {
                throw new ServiceException("互换目标学生已不在原床位");
            }
            
            // 2.2 执行互换
            srcBed.setOccupantId(swapUserId);
            targetBed.setOccupantId(request.getUserId());
            
            bedMapper.updateById(srcBed);
            bedMapper.updateById(targetBed);
        }
        
        // 3. 更新申请单状态
        request.setStatus(1); // 1: 已通过
        this.updateById(request);
    }
    
    /**
     * 辅助方法：更新房间当前人数
     */
    private void updateRoomCount(Long roomId) {
        Long count = bedMapper.selectCount(Wrappers.<DormBed>lambdaQuery()
                .eq(DormBed::getRoomId, roomId)
                .isNotNull(DormBed::getOccupantId));
        
        DormRoom room = new DormRoom();
        room.setId(roomId);
        room.setCurrentNum(count.intValue());
        roomMapper.updateById(room);
    }
}