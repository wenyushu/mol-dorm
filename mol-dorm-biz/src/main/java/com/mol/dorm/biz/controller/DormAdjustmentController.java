package com.mol.dorm.biz.controller;

import cn.dev33.satoken.annotation.SaCheckRole;
import cn.dev33.satoken.annotation.SaMode;
import com.mol.common.core.constant.RoleConstants;
import com.mol.common.core.util.R;
import com.mol.dorm.biz.service.ManualAdjustmentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Tag(name = "人工调宿管理 (Admin)")
@RestController
@RequestMapping("/adjustment")
@RequiredArgsConstructor
@SaCheckRole(value = {RoleConstants.SUPER_ADMIN, RoleConstants.DORM_MANAGER}, mode = SaMode.OR)
public class DormAdjustmentController {
    
    private final ManualAdjustmentService adjustmentService;
    
    @Operation(summary = "双人互换床位", description = "强制互换，无视满员状态")
    @PostMapping("/swap")
    public R<Void> swap(@RequestParam Long studentIdA, @RequestParam Long studentIdB) {
        adjustmentService.swapBeds(studentIdA, studentIdB);
        return R.ok();
    }
    
    @Operation(summary = "强制搬迁/分配", description = "将学生移动到指定空床位，若 bedId 为空则视为强制退宿")
    @PostMapping("/move")
    public R<Void> move(@RequestParam Long studentId, @RequestParam(required = false) Long targetBedId) {
        adjustmentService.moveUserToBed(studentId, targetBedId);
        return R.ok();
    }
    
    @Operation(summary = "批量毕业生离校", description = "危险操作！清空指定年份入学学生的所有床位")
    @SaCheckRole(RoleConstants.SUPER_ADMIN)
    @PostMapping("/graduate")
    public R<Void> batchGraduate(@RequestParam Integer year) {
        adjustmentService.batchGraduate(year);
        return R.ok();
    }
}