package com.mol.dorm.biz.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.mol.dorm.biz.entity.DormChangeRequest;

/**
 * 宿舍调动服务接口
 *
 * @author mol
 */
public interface DormAdjustmentService extends IService<DormChangeRequest> {
    
    /**
     * 提交调宿申请
     * @param userId 当前用户 ID
     * @param reason 申请原因
     * @param targetRoomId 目标房间ID (迁移模式)
     * @param swapStudentId 互换学生ID (互换模式)
     * @return 是否提交成功
     */
    boolean applyForAdjustment(Long userId, String reason, Long targetRoomId, Long swapStudentId);
    
    /**
     * 审批申请 (管理员)
     * @param requestId 申请单 ID
     * @param agree 是否同意
     * @param rejectReason 拒绝原因
     */
    void auditApply(Long requestId, boolean agree, String rejectReason);
}