package com.mol.dorm.biz.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.mol.dorm.biz.entity.DormChangeRequest;

/**
 * 调宿/换宿服务接口
 */
public interface DormAdjustmentService extends IService<DormChangeRequest> {
    
    /**
     * 提交调宿申请
     *
     * @param userId        申请人 ID (统一 ID)
     * @param reason        申请原因
     * @param targetRoomId  目标房间 ID (换房必填)
     * @param swapStudentId 互换目标 ID (互换必填)
     * @return 是否提交成功
     */
    boolean applyForAdjustment(Long userId, String reason, Long targetRoomId, Long swapStudentId);
    
    /**
     * 审批申请 (管理员操作)
     *
     * @param requestId    申请单 ID
     * @param agree        是否同意
     * @param rejectReason 驳回原因 (如果拒绝)
     */
    void auditApply(Long requestId, boolean agree, String rejectReason);
}