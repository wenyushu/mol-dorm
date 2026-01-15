package com.mol.server.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.annotation.SaCheckRole;
import cn.dev33.satoken.annotation.SaMode;
import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.mol.common.core.constant.RoleConstants;
import com.mol.common.core.entity.SysOrdinaryUser;
import com.mol.common.core.util.LoginHelper;
import com.mol.common.core.util.R;
import com.mol.server.service.SysOrdinaryUserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 普通用户管理控制器 (学生/教工)
 *
 * @author mol
 */
@Tag(name = "用户管理-普通用户", description = "学生与教职工的信息管理及密码维护")
@RestController
@RequestMapping("/ordinary-user")
@RequiredArgsConstructor
public class SysOrdinaryUserController {
    
    private final SysOrdinaryUserService ordinaryUserService;
    
    /**
     * 分页查询用户列表
     */
    @Operation(summary = "分页查询用户", description = "支持按姓名模糊查询，或按身份(学生/教工)筛选")
    // 🔒 权限锁：只有管理组（超管、宿管、辅导员）能查询列表
    @SaCheckRole(value = {
            RoleConstants.SUPER_ADMIN,
            RoleConstants.DORM_MANAGER,
            RoleConstants.COUNSELOR
    }, mode = SaMode.OR)
    @GetMapping("/page")
    public R<IPage<SysOrdinaryUser>> page(
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") Integer pageNum,
            @Parameter(description = "每页大小") @RequestParam(defaultValue = "10") Integer pageSize,
            @Parameter(description = "真实姓名") @RequestParam(required = false) String realName,
            @Parameter(description = "学号/工号") @RequestParam(required = false) String username,
            @Parameter(description = "身份分类: 0-学生, 1-职工") @RequestParam(required = false) Integer userCategory) {
        
        IPage<SysOrdinaryUser> result = ordinaryUserService.lambdaQuery()
                .like(realName != null, SysOrdinaryUser::getRealName, realName)
                .eq(username != null, SysOrdinaryUser::getUsername, username)
                .eq(userCategory != null, SysOrdinaryUser::getUserCategory, userCategory)
                .orderByDesc(SysOrdinaryUser::getCreateTime)
                .page(new Page<>(pageNum, pageSize));
        
        // 🛡️ 脱敏处理：密码不回显
        result.getRecords().forEach(u -> u.setPassword(null));
        
        return R.ok(result);
    }
    
    @Operation(summary = "新增用户 (仅 Admin)")
    @SaCheckRole(RoleConstants.SUPER_ADMIN)
    @PostMapping
    public R<Boolean> save(@RequestBody SysOrdinaryUser user) {
        return R.ok(ordinaryUserService.saveUser(user));
    }
    
    @Operation(summary = "修改用户信息 (仅 Admin)")
    @SaCheckRole(RoleConstants.SUPER_ADMIN)
    @PutMapping
    public R<Boolean> update(@RequestBody SysOrdinaryUser user) {
        return R.ok(ordinaryUserService.updateUser(user));
    }
    
    @Operation(summary = "删除用户 (仅 Admin)")
    @SaCheckRole(RoleConstants.SUPER_ADMIN)
    @DeleteMapping("/{id}")
    public R<Boolean> remove(@PathVariable Long id) {
        return R.ok(ordinaryUserService.removeById(id));
    }
    
    // ------------------- 密码管理接口 -------------------
    
    @Operation(summary = "管理员重置密码", description = "管理员强制重置用户密码")
    @SaCheckRole(RoleConstants.SUPER_ADMIN)
    @PostMapping("/reset-pwd")
    public R<Void> resetPwd(
            @Parameter(description = "用户 ID") @RequestParam Long userId,
            @Parameter(description = "新密码") @RequestParam String newPassword) {
        
        // 1. 执行重置逻辑
        ordinaryUserService.resetPassword(userId, newPassword);
        
        // 2. 【新增】强制注销该用户，让旧密码生成的 Token 立即失效
        // 注意：这里需要传入 userId，指定踢出某人
        StpUtil.logout(userId);
        
        return R.ok(null, "密码重置成功，该用户已被强制下线");
    }
    
    @Operation(summary = "修改个人密码", description = "用户自行修改密码")
    @SaCheckLogin
    @PostMapping("/update-pwd")
    public R<Void> updatePwd(
            // 🛡️ 防刁民设计：移除 userId 参数！
            // 不要相信前端传来的 userId，只使用 Token 解析出来的 ID
            @Parameter(description = "旧密码") @RequestParam String oldPassword,
            @Parameter(description = "新密码") @RequestParam String newPassword) {
        
        // ✅ 使用 LoginHelper 获取当前登录人真实 ID (自动处理前缀)
        Long currentUserId = LoginHelper.getUserId();
        
        // 1. 执行修改逻辑
        ordinaryUserService.updatePassword(currentUserId, oldPassword, newPassword);
        
        // 2. 【新增】强制注销当前登录状态
        StpUtil.logout();
        
        return R.ok(null, "密码修改成功，请使用新密码重新登录");
    }
}