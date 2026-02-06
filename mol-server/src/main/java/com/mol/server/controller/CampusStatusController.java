package com.mol.server.controller;

import cn.dev33.satoken.annotation.SaCheckRole;
import cn.dev33.satoken.annotation.SaMode;
import com.mol.common.core.constant.RoleConstants;
import com.mol.common.core.util.R;
import com.mol.server.service.CampusStatusService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Tag(name = "假期/离校状态管理")
@RestController
@RequestMapping("/system/status")
@RequiredArgsConstructor
public class CampusStatusController {
    
    private final CampusStatusService statusService;
    
    @Operation(summary = "切换在校/离校状态", description = "学生/教工用于寒暑假离校打卡")
    @SaCheckRole(value = {
            RoleConstants.STUDENT,
            RoleConstants.COLLEGE_TEACHER,
            RoleConstants.STAFF,
            RoleConstants.SUPER_ADMIN
    }, mode = SaMode.OR)
    @PostMapping("/toggle")
    public R<Void> toggleStatus(
            @Parameter(description = "目标状态: 1在校, 0离校", required = true) @RequestParam Integer status
    ) {
        statusService.toggleCampusStatus(status);
        
        String msg = (status == 1) ? "欢迎返校！状态已更新。" : "离校登记成功，祝您假期愉快！";

        return R.ok(null, msg);
    }
}