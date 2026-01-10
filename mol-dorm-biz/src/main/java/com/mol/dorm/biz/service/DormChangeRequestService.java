package com.mol.dorm.biz.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.mol.dorm.biz.entity.DormChangeRequest;

public interface DormChangeRequestService extends IService<DormChangeRequest> {
    
    /**
     * 提交调宿申请
     */
    void submitRequest(Long userId, Long targetRoomId, String reason);
    
    /**
     * 通用审批入口 (新增)
     * <p>自动根据当前申请单的状态，判断是执行辅导员审批还是宿管审批</p>
     * * @param requestId 申请单ID
     * @param pass      是否通过
     * @param msg       审批意见
     */
    void approveRequest(Long requestId, Boolean pass, String msg);
    
    /**
     * 辅导员审批 (内部或特定调用)
     */
    void auditByCounselor(Long requestId, Boolean pass, String msg);
    
    /**
     * 宿管经理审批 (内部或特定调用)
     */
    void auditByManager(Long requestId, Boolean pass, String msg);
    
    /**
     * 分页查询
     */
    Page<DormChangeRequest> getRequestList(Page<DormChangeRequest> page, Long userId, Integer status);
}