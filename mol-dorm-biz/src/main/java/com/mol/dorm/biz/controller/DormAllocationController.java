package com.mol.dorm.biz.controller;

import cn.dev33.satoken.annotation.SaCheckRole;
import com.mol.common.core.constant.RoleConstants;

import com.mol.common.core.entity.SysOrdinaryUser;
import com.mol.common.core.util.R;
import com.mol.dorm.biz.service.impl.DormAllocationService;
import com.mol.server.service.SysOrdinaryUserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "智能分配管理")
@RestController
@RequestMapping("/allocation")
@RequiredArgsConstructor
public class DormAllocationController {
    
    private final DormAllocationService allocationService;
    private final SysOrdinaryUserService userService;
    
    @Operation(summary = "执行一键智能分配 (仅超管)", description = "高危操作！根据画像算法批量分配床位。")
    @SaCheckRole(RoleConstants.SUPER_ADMIN) // 🔒 仅限超级管理员！宿管都没权限点这个按钮
    @PostMapping("/execute")
    public R<String> executeAllocation(@RequestBody(required = false) List<Long> studentIds) {
        
        // 1. 如果前端没传 ID，则默认查找所有 “未分配床位” 的学生（模拟一键全员分配）
        if (studentIds == null || studentIds.isEmpty()) {
            // 这里为了演示，仍然获取前100个。实际生产中应该查 `select id from sys_ordinary_user where ...`
            studentIds = userService.list().stream().map(SysOrdinaryUser::getId).toList();
        }
        
        if (studentIds.isEmpty()) {
            return R.failed("没有可分配的学生");
        }
        
        // 2. 调用核心算法
        allocationService.executeAllocation(studentIds);
        
        return R.ok("智能分配任务已完成，请查看床位状态。");
    }
}