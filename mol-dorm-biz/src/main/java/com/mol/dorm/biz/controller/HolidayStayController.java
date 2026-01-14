package com.mol.dorm.biz.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.annotation.SaCheckRole;
import cn.dev33.satoken.annotation.SaMode;
import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.mol.common.core.constant.RoleConstants;
import com.mol.common.core.util.R;
import com.mol.dorm.biz.entity.HolidayStay;
import com.mol.dorm.biz.service.HolidayStayService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Tag(name = "假期留校管理")
@RestController
@RequestMapping("/holiday")
@RequiredArgsConstructor
public class HolidayStayController {
    
    private final HolidayStayService stayService;
    
    @Operation(summary = "提交留校申请 (学生)")
    @SaCheckRole(RoleConstants.STUDENT)
    @PostMapping("/submit")
    public R<Void> submit(@RequestBody HolidayStay stay) {
        // 自动绑定当前登录学生 ID
        stay.setStudentId(StpUtil.getLoginIdAsLong());
        stayService.submit(stay);
        return R.ok();
    }
    
    @Operation(summary = "审批申请 (辅导员/宿管)")
    @SaCheckRole(value = {RoleConstants.DORM_MANAGER, RoleConstants.COUNSELOR, RoleConstants.SUPER_ADMIN}, mode = SaMode.OR)
    @PostMapping("/audit")
    public R<Void> audit(@RequestParam Long id, @RequestParam Boolean pass, @RequestParam(required = false) String msg) {
        stayService.audit(id, pass, msg);
        return R.ok();
    }
    
    @Operation(summary = "查询申请列表")
    @SaCheckLogin
    @GetMapping("/list")
    public R<Page<HolidayStay>> list(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) Integer status) {
        
        Page<HolidayStay> page = new Page<>(pageNum, pageSize);
        Long userId = null;
        
        // 如果是学生，只能查自己的
        if (StpUtil.hasRole(RoleConstants.STUDENT)) {
            userId = StpUtil.getLoginIdAsLong();
        }
        
        return R.ok(stayService.getPage(page, userId, status));
    }
}