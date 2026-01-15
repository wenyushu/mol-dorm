package com.mol.dorm.biz.controller;

import cn.dev33.satoken.annotation.SaCheckRole;
import cn.dev33.satoken.annotation.SaMode;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.mol.common.core.constant.RoleConstants;
import com.mol.common.core.util.R;
import com.mol.dorm.biz.entity.DormChangeRequest;
import com.mol.dorm.biz.service.DormAdjustmentService;
import com.mol.dorm.biz.service.ManualAdjustmentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Tag(name = "调宿管理-管理员", description = "包含审批、强制调整及批量操作")
@RestController
@RequestMapping("/adjustment/manual")
@RequiredArgsConstructor
// 🔒 权限控制：仅管理员可用
@SaCheckRole(value = {RoleConstants.SUPER_ADMIN, RoleConstants.DORM_MANAGER, RoleConstants.COUNSELOR}, mode = SaMode.OR)
public class DormManualAdjustmentController {
    
    private final DormAdjustmentService adjustmentService; // 用于审批申请
    private final ManualAdjustmentService manualService;   // 用于强制操作
    
    // ==================== 查询接口 ====================
    
    @Operation(summary = "分页查询调宿申请", description = "管理员查看所有申请，可筛选状态")
    @GetMapping("/page")
    public R<IPage<DormChangeRequest>> page(
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") Integer pageNum,
            @Parameter(description = "每页大小") @RequestParam(defaultValue = "10") Integer pageSize,
            @Parameter(description = "状态: 0-待审核, 1-通过, 2-拒绝") @RequestParam(required = false) Integer status,
            @Parameter(description = "申请人姓名") @RequestParam(required = false) String studentName) {
        
        // 构造查询条件
        // 注意：这里需要关联查询学生姓名，如果 DormChangeRequest 只有 userId，
        // 建议在 Service 层做 VO 转换，或者简单点直接返回 Entity (前端根据 userId 查人名，或后端做冗余)
        // 这里演示最简版本：
        Page<DormChangeRequest> page = new Page<>(pageNum, pageSize);
        IPage<DormChangeRequest> result = adjustmentService.lambdaQuery()
                .eq(status != null, DormChangeRequest::getStatus, status)
                .orderByDesc(DormChangeRequest::getCreateTime) // 按时间倒序
                .page(page);
        
        return R.ok(result);
    }
    
    // 1. 审批接口
    @Operation(summary = "审批调宿申请")
    @PostMapping("/audit")
    public R<Void> audit(
            @RequestParam Long requestId,
            @RequestParam Boolean agree,
            @RequestParam(required = false) String rejectReason) {
        adjustmentService.auditApply(requestId, agree, rejectReason);
        return R.ok(null, agree ? "已通过" : "已拒绝");
    }
    
    // 2. 强制互换
    @Operation(summary = "强制双人互换")
    @PostMapping("/swap")
    public R<Void> swap(@RequestParam Long studentIdA, @RequestParam Long studentIdB) {
        manualService.swapBeds(studentIdA, studentIdB);
        return R.ok(null, "互换成功");
    }
    
    // 3. 强制搬迁/退宿
    @Operation(summary = "强制搬迁/退宿")
    @PostMapping("/move")
    public R<Void> move(
            @RequestParam Long studentId,
            @RequestParam(required = false) Long targetBedId) {
        manualService.moveUserToBed(studentId, targetBedId);
        return R.ok(null, targetBedId == null ? "已退宿" : "搬迁成功");
    }
    
    // 4. 批量毕业
    @Operation(summary = "批量毕业生离校")
    @SaCheckRole(RoleConstants.SUPER_ADMIN)
    @PostMapping("/graduate")
    public R<Void> graduate(@RequestParam Integer year) {
        manualService.batchGraduate(year);
        return R.ok(null, "操作完成");
    }
}