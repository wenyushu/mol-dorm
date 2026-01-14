package com.mol.dorm.biz.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mol.common.core.exception.ServiceException;
import com.mol.dorm.biz.entity.DormBed;
import com.mol.dorm.biz.entity.DormChangeRequest;
import com.mol.dorm.biz.mapper.DormChangeRequestMapper;
import com.mol.dorm.biz.service.DormAdjustmentService;
import com.mol.dorm.biz.service.DormBedService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class DormAdjustmentServiceImpl extends ServiceImpl<DormChangeRequestMapper, DormChangeRequest> implements DormAdjustmentService {
    
    private final DormBedService bedService;
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean applyForAdjustment(Long userId, String reason, Long targetRoomId) {
        // 1. 检查重复申请 (使用 getUserId)
        Long pendingCount = this.baseMapper.selectCount(Wrappers.<DormChangeRequest>lambdaQuery()
                .eq(DormChangeRequest::getUserId, userId)
                .eq(DormChangeRequest::getStatus, 0));
        if (pendingCount > 0) {
            throw new ServiceException("您已有一条待审核的调宿申请");
        }
        
        // 2. 查找当前床位
        DormBed currentBed = bedService.getOne(Wrappers.<DormBed>lambdaQuery()
                .eq(DormBed::getOccupantId, userId));
        if (currentBed == null) {
            throw new ServiceException("未找到您的床位信息");
        }
        
        // 3. 构建申请单
        DormChangeRequest request = new DormChangeRequest();
        
        // --- 字段映射修复区 ---
        request.setUserId(userId);            // 修复 setStudentId
        request.setType(1);                   // 修复 setType (默认为1:单人调宿)
        request.setSwapStudentId(null);       // 修复 setSwapStudentId (单人模式为空)
        request.setOriginRoomId(currentBed.getRoomId()); // 修复 setCurrentRoomId
        // setApplyTime 不需要手动设，BaseEntity 会自动填充 createTime
        // --------------------
        
        request.setOriginBedId(currentBed.getId());
        request.setTargetRoomId(targetRoomId);
        request.setReason(reason);
        request.setStatus(0);
        
        return this.save(request);
    }
}