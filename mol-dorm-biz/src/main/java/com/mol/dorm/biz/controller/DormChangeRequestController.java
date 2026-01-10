package com.mol.dorm.biz.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.annotation.SaCheckRole;
import cn.dev33.satoken.annotation.SaMode;
import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.mol.common.core.constant.RoleConstants;
import com.mol.common.core.util.R;
import com.mol.dorm.biz.entity.DormChangeRequest;
import com.mol.dorm.biz.service.DormChangeRequestService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Tag(name = "调宿申请管理")
@RestController
@RequestMapping("/change-request")
@RequiredArgsConstructor
public class DormChangeRequestController {
    
    private final DormChangeRequestService requestService;
    
    @Operation(summary = "提交调宿申请", description = "学生可提交，需登录")
    @SaCheckLogin
    @PostMapping("/submit")
    // ✅ 修复点 1：返回类型改为 R<Void> 比较合适，因为 Service 返回 void
    public R<Void> submit(@RequestBody DormChangeRequest request) {
        // ✅ 修复点 2：拆解参数调用 Service
        // 使用 StpUtil.getLoginIdAsLong() 获取当前登录用户ID，比传参更安全
        requestService.submitRequest(
                StpUtil.getLoginIdAsLong(),
                request.getTargetRoomId(),
                request.getReason()
        );
        return R.ok();
    }
    
    @Operation(summary = "查询列表", description = "所有人可查")
    @SaCheckLogin
    @GetMapping("/list")
    public R<Page<DormChangeRequest>> list(
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") Integer pageNum,
            @Parameter(description = "大小") @RequestParam(defaultValue = "10") Integer pageSize,
            @Parameter(description = "状态") @RequestParam(required = false) Integer status) {
        
        Page<DormChangeRequest> page = new Page<>(pageNum, pageSize);
        // 如果不是管理员，只能查自己的
        Long userId = null;
        if (!StpUtil.hasRole(RoleConstants.SUPER_ADMIN) &&
                !StpUtil.hasRole(RoleConstants.DORM_MANAGER) &&
                !StpUtil.hasRole(RoleConstants.COUNSELOR)) {
            userId = StpUtil.getLoginIdAsLong();
        }
        
        return R.ok(requestService.getRequestList(page, userId, status));
    }
    
    @Operation(summary = "审批申请 (同意/拒绝)", description = "仅限管理人员")
    @SaCheckRole(value = {
            RoleConstants.SUPER_ADMIN,
            RoleConstants.DORM_MANAGER,
            RoleConstants.COUNSELOR
    }, mode = SaMode.OR)
    @PostMapping("/approve")
    public R<Void> approve(
            @RequestParam Long requestId,
            @RequestParam Boolean agree,
            @RequestParam(required = false) String remark) {
        
        // ✅ 修复点 3：调用新写的 approveRequest 方法
        requestService.approveRequest(requestId, agree, remark);
        return R.ok();
    }
}