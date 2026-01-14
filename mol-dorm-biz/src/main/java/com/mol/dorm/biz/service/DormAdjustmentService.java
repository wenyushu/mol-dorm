package com.mol.dorm.biz.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.mol.dorm.biz.entity.DormChangeRequest;

/**
 * 调宿业务接口
 */
public interface DormAdjustmentService extends IService<DormChangeRequest> {
    
    /**
     * 提交调宿申请
     *
     * @param userId       申请学生 ID
     * @param reason       申请原因
     * @param targetRoomId 意向房间ID (可为null)
     * @return 是否提交成功
     */
    boolean applyForAdjustment(Long userId, String reason, Long targetRoomId);
}