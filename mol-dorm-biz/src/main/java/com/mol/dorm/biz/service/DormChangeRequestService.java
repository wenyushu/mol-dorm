package com.mol.dorm.biz.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.mol.dorm.biz.entity.DormChangeRequest;

public interface DormChangeRequestService extends IService<DormChangeRequest> {
    
    /**
     * 提交调宿申请
     * @param userId 当前登录用户 ID
     * @param targetRoomId 目标房间 ID
     * @param reason 申请理由
     */
    void submitRequest(Long userId, Long targetRoomId, String reason);
    
    /**
     * 辅导员审批
     * 只负责流转状态，不涉及实际换房
     */
    void auditByCounselor(Long requestId, Boolean pass, String msg);
    
    /**
     * 宿管经理审批
     * 负责最终确认，如果通过，需要执行换床位逻辑
     */
    void auditByManager(Long requestId, Boolean pass, String msg);
    
    /**
     * 分页查询
     * @param page 分页对象
     * @param userId 用户ID (null查询所有)
     * @param status 状态 (null查询所有)
     * @return 分页结果
     */
    Page<DormChangeRequest> getRequestList(Page<DormChangeRequest> page, Long userId, Integer status);
}