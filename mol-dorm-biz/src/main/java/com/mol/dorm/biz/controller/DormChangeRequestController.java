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
    
    @Operation(summary = "提交普通换房申请", description = "需指定目标房间 ID")
    @SaCheckLogin
    @PostMapping("/submit")
    public R<Void> submit(@RequestBody DormChangeRequest request) {
        requestService.submitRequest(
                StpUtil.getLoginIdAsLong(),
                request.getTargetRoomId(),
                request.getReason()
        );
        return R.ok();
    }
    
    @Operation(summary = "提交退宿/休学申请", description = "无需目标房间")
    @SaCheckLogin
    @PostMapping("/submit-leave")
    public R<Void> submitLeave(@RequestBody DormChangeRequest request) {
        requestService.submitLeaveRequest(
                StpUtil.getLoginIdAsLong(),
                request.getReason()
        );
        return R.ok();
    }
    
    @Operation(summary = "提交互换申请", description = "需指定互换目标学生 ID")
    @SaCheckLogin
    @PostMapping("/submit-swap")
    public R<Void> submitSwap(@RequestBody DormChangeRequest request) {
        if (request.getSwapStudentId() == null) {
            return R.failed("必须指定互换目标学生");
        }
        requestService.submitSwapRequest(
                StpUtil.getLoginIdAsLong(),
                request.getSwapStudentId(),
                request.getReason()
        );
        return R.ok();
    }
    
    @Operation(summary = "查询申请列表", description = "普通用户查自己，管理员可查所有")
    @SaCheckLogin
    @GetMapping("/list")
    public R<Page<DormChangeRequest>> list(
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") Integer pageNum,
            @Parameter(description = "大小") @RequestParam(defaultValue = "10") Integer pageSize,
            @Parameter(description = "状态") @RequestParam(required = false) Integer status) {
        
        Page<DormChangeRequest> page = new Page<>(pageNum, pageSize);
        Long userId = null;
        
        if (!StpUtil.hasRole(RoleConstants.SUPER_ADMIN) &&
                !StpUtil.hasRole(RoleConstants.DORM_MANAGER) &&
                !StpUtil.hasRole(RoleConstants.COUNSELOR)) {
            userId = StpUtil.getLoginIdAsLong();
        }
        
        return R.ok(requestService.getRequestList(page, userId, status));
    }
    
    @Operation(summary = "审批申请 (同意/拒绝)", description = "辅导员或宿管使用")
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
        
        requestService.approveRequest(requestId, agree, remark);
        return R.ok();
    }
}