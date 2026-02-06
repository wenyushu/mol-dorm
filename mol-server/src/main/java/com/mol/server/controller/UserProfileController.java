package com.mol.server.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.annotation.SaCheckRole;
import cn.dev33.satoken.annotation.SaMode;
import com.mol.common.core.constant.RoleConstants;
import com.mol.common.core.util.R;
import com.mol.server.dto.AdminUpdateStudentBody;
import com.mol.server.dto.UpdatePasswordBody;
import com.mol.server.dto.UserProfileEditDTO;
import com.mol.server.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 用户中心与资料管理控制器
 * <p>
 * 🛡️ 架构说明：
 * 1. 个人中心：用户自主维护（头像、密码、联系方式）。
 * 2. 档案管理：管理员特权操作（学籍修正、校外地址强制备案）。
 * </p>
 */
@Tag(name = "个人中心", description = "个人信息/资料修改/安全设置/档案管理")
@RestController
@RequestMapping("/user/profile")
@RequiredArgsConstructor
public class UserProfileController {
    
    private final UserService userService;
    
    // ==========================================
    // 1. 个人自助操作 (本人权限)
    // ==========================================
    
    @Operation(summary = "获取个人信息", description = "包含基本资料、角色权限及脱敏后的档案信息")
    @SaCheckLogin
    @GetMapping
    public R<Map<String, Object>> getProfile() {
        return R.ok(userService.getProfile());
    }
    
    @Operation(summary = "修改个人资料", description = "仅限修改头像、昵称、非敏感联系方式等")
    @SaCheckLogin
    @PutMapping
    public R<Void> updateProfile(@Validated @RequestBody UserProfileEditDTO body) {
        userService.updateProfile(body);
        return R.ok();
    }
    
    @Operation(summary = "修改登录密码", description = "旧密码验证通过后方可修改，成功后会自动强制下线")
    @SaCheckLogin
    @PutMapping("/password")
    public R<Void> updatePassword(@Validated @RequestBody UpdatePasswordBody body) {
        userService.updatePassword(body);
        return R.ok();
    }
    
    // ==========================================
    // 2. 管理员管理操作 (特权权限)
    // ==========================================
    
    @Operation(summary = "管理员修改人员信息", description = "🛡️ 特权接口：支持修改核心资料及校外地址强制备案")
    @SaCheckRole(value = {RoleConstants.SUPER_ADMIN, RoleConstants.DORM_MANAGER}, mode = SaMode.OR)
    @PutMapping("/admin/update")
    public R<Void> updateStudentByAdmin(@Validated @RequestBody AdminUpdateStudentBody body) {
        userService.updateStudentByAdmin(body);
        return R.ok();
    }
}