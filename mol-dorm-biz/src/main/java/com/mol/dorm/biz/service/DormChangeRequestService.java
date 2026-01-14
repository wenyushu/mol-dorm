package com.mol.dorm.biz.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.mol.dorm.biz.entity.DormChangeRequest;

public interface DormChangeRequestService extends IService<DormChangeRequest> {
    
    /**
     * 提交普通换房申请 (Type=0)
     */
    void submitRequest(Long userId, Long targetRoomId, String reason);
    
    /**
     * 提交退宿/休学申请 (Type=1)
     */
    void submitLeaveRequest(Long userId, String reason);
    
    /**
     * 提交互换申请 (Type=2)
     */
    void submitSwapRequest(Long userId, Long targetStudentId, String reason);
    
    /**
     * 通用审批入口 (自动路由)
     */
    void approveRequest(Long requestId, Boolean pass, String msg);
    
    /**
     * 辅导员审批 (内部调用)
     */
    void auditByCounselor(Long requestId, Boolean pass, String msg);
    
    /**
     * 宿管经理审批 (内部调用 - 终审并执行操作)
     */
    void auditByManager(Long requestId, Boolean pass, String msg);
    
    /**
     * 分页查询申请历史
     */
    Page<DormChangeRequest> getRequestList(Page<DormChangeRequest> page, Long userId, Integer status);
}