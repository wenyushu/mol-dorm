package com.mol.dorm.biz.controller;

import cn.dev33.satoken.annotation.SaCheckRole;
import com.mol.common.core.constant.RoleConstants;
import com.mol.common.core.util.LoginHelper;
import com.mol.common.core.util.R;
import com.mol.dorm.biz.service.StaffDormService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Tag(name = "教职工住宿管理")
@RestController
@RequestMapping("/staff/dorm")
@RequiredArgsConstructor
public class StaffAccommodationController {
    
    private final StaffDormService staffDormService;
    
    // ================= 教职工端 =================
    
    @Operation(summary = "提交住宿申请")
    // 只有教职工能调
    @PostMapping("/apply")
    public R<Void> apply(
            @RequestParam Integer applyType, // 0-入住 1-退宿 2-换房
            @RequestParam(required = false) String targetRoomType, // 如 "一室一厅"
            @RequestParam String reason) {
        
        Long userId = LoginHelper.getUserId();
        staffDormService.submitApplication(userId, applyType, reason, targetRoomType);
        return R.ok();
    }
    
    // ================= 管理员端 =================
    
    @Operation(summary = "审批教工申请并分配")
    @SaCheckRole(RoleConstants.DORM_MANAGER) // 宿管或超管
    @PostMapping("/approve")
    public R<Void> approve(
            @RequestParam Long applicationId,
            @RequestParam(required = false) Long roomId, // 如果同意，必须选房间
            @RequestParam Boolean agree,
            @RequestParam(required = false) String rejectReason) {
        
        staffDormService.approveAndAssign(applicationId, roomId, agree, rejectReason);
        return R.ok();
    }
    
    @Operation(summary = "办理教工退宿")
    @SaCheckRole(RoleConstants.DORM_MANAGER)
    @PostMapping("/checkout")
    public R<Void> checkout(@RequestParam Long userId) {
        staffDormService.checkOut(userId);
        return R.ok();
    }
}