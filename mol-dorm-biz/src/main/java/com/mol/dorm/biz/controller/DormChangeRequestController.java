package com.mol.dorm.biz.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.mol.common.core.util.R;
import com.mol.dorm.biz.entity.DormChangeRequest;
import com.mol.dorm.biz.service.DormChangeRequestService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 宿舍调换申请控制器
 * <p>
 * 提供申请提交、辅导员审批、宿管审批、列表查询等接口
 * </p>
 *
 * @author mol
 */
@RestController
@RequestMapping("/dorm/change")
@RequiredArgsConstructor
public class DormChangeRequestController {
    
    private final DormChangeRequestService changeService;
    
    /**
     * 1. 提交调宿申请 (学生端)
     */
    @PostMapping("/submit")
    public R<Void> submitRequest(@RequestBody @Valid SubmitDTO dto) {
        // 获取当前登录用户ID
        // 实际开发中建议用: StpUtil.getLoginIdAsLong() 或 UserContext.getUserId()
        // 这里暂时假设前端传过来或者从 Header 获取，这里模拟一个从上下文获取
        Long currentUserId = getCurrentUserId();
        
        changeService.submitRequest(currentUserId, dto.getTargetRoomId(), dto.getReason());
        
        return R.ok();
    }
    
    /**
     * 2. 辅导员审批 (辅导员端)
     * 对应状态流转: 0 -> 1 (通过) 或 3 (驳回)
     */
    @PostMapping("/audit/counselor")
    public R<Void> auditByCounselor(@RequestBody @Valid AuditDTO dto) {
        changeService.auditByCounselor(dto.getId(), dto.getPass(), dto.getMsg());
        return R.ok();
    }
    
    /**
     * 3. 宿管经理审批 (宿管端)
     * 对应状态流转: 1 -> 2 (通过并换房) 或 3 (驳回)
     */
    @PostMapping("/audit/manager")
    public R<Void> auditByManager(@RequestBody @Valid AuditDTO dto) {
        changeService.auditByManager(dto.getId(), dto.getPass(), dto.getMsg());
        return R.ok();
    }
    
    /**
     * 4. 获取我的申请记录 (学生端)
     */
    @GetMapping("/my")
    public R<Page<DormChangeRequest>> getMyRequests(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        
        Long currentUserId = getCurrentUserId();
        Page<DormChangeRequest> page = new Page<>(pageNum, pageSize);
        
        // 查询当前用户的记录
        return R.ok(changeService.getRequestList(page, currentUserId, null));
    }
    
    /**
     * 5. 获取所有申请列表 (管理端)
     * 可以按状态筛选 (例如辅导员只看 status=0 的，宿管只看 status=1 的)
     */
    @GetMapping("/list")
    public R<Page<DormChangeRequest>> getAllRequests(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) Integer status) {
        
        Page<DormChangeRequest> page = new Page<>(pageNum, pageSize);
        
        // userId 传 null 表示查询所有人
        return R.ok(changeService.getRequestList(page, null, status));
    }
    
    // --- 模拟获取当前用户的方法 ---
    private Long getCurrentUserId() {
        // TODO: 请替换为你项目中真实的获取登录用户逻辑
        // 例如: return StpUtil.getLoginIdAsLong();
        // 临时硬编码方便调试:
        return 60001L;
    }
    
    // ================== 内部 DTO 类 ==================
    
    @Data
    public static class SubmitDTO {
        @NotNull(message = "目标宿舍 ID 不能为空")
        private Long targetRoomId;
        
        private String reason;
    }
    
    @Data
    public static class AuditDTO {
        @NotNull(message = "申请单 ID 不能为空")
        private Long id;
        
        @NotNull(message = "审核结果不能为空")
        private Boolean pass; // true: 通过, false: 驳回
        
        private String msg;   // 审核意见
    }
}