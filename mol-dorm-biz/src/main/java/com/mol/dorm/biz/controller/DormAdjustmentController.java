package com.mol.dorm.biz.controller;

import cn.dev33.satoken.annotation.SaCheckRole;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.mol.common.core.constant.RoleConstants;
import com.mol.common.core.util.LoginHelper;
import com.mol.common.core.util.R;
import com.mol.dorm.biz.entity.DormChangeRequest;
import com.mol.dorm.biz.service.DormAdjustmentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 调宿申请控制器 (学生端)
 */
@Tag(name = "调宿管理-学生", description = "学生提交调宿或互换申请")
@RestController
@RequestMapping("/adjustment")
@RequiredArgsConstructor
public class DormAdjustmentController {
    
    private final DormAdjustmentService adjustmentService;
    
    @Operation(summary = "提交调宿申请")
    @SaCheckRole(RoleConstants.STUDENT) // 🔒 仅学生可用
    @PostMapping("/apply")
    public R<Boolean> apply(@RequestBody DormChangeRequest request) {
        Long currentUserId = LoginHelper.getUserId();
        
        // 自动注入当前用户ID，防止代填
        return R.ok(adjustmentService.applyForAdjustment(
                currentUserId,
                request.getReason(),
                request.getTargetRoomId(),
                request.getSwapStudentId()
        ));
    }
    
    // 补充 @GetMapping("/history") 查看我的申请记录
    @Operation(summary = "查看我的申请记录")
    @SaCheckRole(RoleConstants.STUDENT)
    @GetMapping("/history")
    public R<IPage<DormChangeRequest>> history(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        
        Long currentUserId = LoginHelper.getUserId();
        
        // 强制只查自己的数据
        Page<DormChangeRequest> page = new Page<>(pageNum, pageSize);
        IPage<DormChangeRequest> result = adjustmentService.lambdaQuery()
                .eq(DormChangeRequest::getUserId, currentUserId)
                .orderByDesc(DormChangeRequest::getCreateTime)
                .page(page);
        
        return R.ok(result);
    }
}