package com.mol.server.controller;

import cn.dev33.satoken.annotation.SaCheckRole;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.mol.common.core.constant.RoleConstants;
import com.mol.common.core.entity.SysAdminUser;
import com.mol.common.core.util.R;
import com.mol.server.service.SysAdminUserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 系统管理员管理控制器
 * <p>
 * 仅限超级管理员 (Super Admin) 操作。
 * 用于维护宿管、后勤等工作人员的账号。这些账号拥有较高的系统权限。
 * </p>
 */
@Tag(name = "用户管理-系统管理员", description = "宿管/后勤人员的账号维护")
@RestController
@RequestMapping("/admin-user")
@RequiredArgsConstructor
public class SysAdminUserController {
    
    private final SysAdminUserService adminUserService;
    
    
    @Operation(summary = "分页查询管理员", description = "仅超管可查")
    @SaCheckRole(RoleConstants.SUPER_ADMIN) // 🔒 权限锁：最高级别
    @GetMapping("/page")
    public R<IPage<SysAdminUser>> page(
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") Integer pageNum,
            @Parameter(description = "每页大小") @RequestParam(defaultValue = "10") Integer pageSize,
            @Parameter(description = "姓名/账号模糊搜索") @RequestParam(required = false) String keyword) {
        
        IPage<SysAdminUser> result = adminUserService.lambdaQuery()
                .and(keyword != null, w -> w.like(SysAdminUser::getUsername, keyword)
                        .or().like(SysAdminUser::getRealName, keyword))
                .orderByDesc(SysAdminUser::getCreateTime)
                .page(new Page<>(pageNum, pageSize));
        
        // 🛡️ 安全处理：密码脱敏，绝对不能回显
        result.getRecords().forEach(u -> u.setPassword(null));
        return R.ok(result);
    }
    
    
    @Operation(summary = "新增管理员 (宿管/后勤)", description = "新增系统管理人员，默认密码通常为123456")
    @SaCheckRole(RoleConstants.SUPER_ADMIN) // 🔒 权限锁：只有超管能建人
    @PostMapping
    public R<Boolean> save(@RequestBody SysAdminUser admin) {
        return R.ok(adminUserService.saveAdmin(admin));
    }
    
    
    @Operation(summary = "修改管理员信息")
    @SaCheckRole(RoleConstants.SUPER_ADMIN) // 🔒 权限锁
    @PutMapping
    public R<Boolean> update(@RequestBody SysAdminUser admin) {
        return R.ok(adminUserService.updateAdmin(admin));
    }
    
    
    @Operation(summary = "删除管理员")
    @SaCheckRole(RoleConstants.SUPER_ADMIN) // 🔒 权限锁
    @DeleteMapping("/{id}")
    public R<Boolean> remove(@PathVariable Long id) {
        // 🛡️ 防刁民：防止自杀
        // 假设 ID 为 1 的是初始超级管理员，严禁删除，防止系统无人可管
        if (id == 1L) {
            return R.fail("无法删除超级管理员账号");
        }
        return R.ok(adminUserService.removeById(id));
    }
    
    
    @Operation(summary = "重置管理员密码", description = "强制重置某管理员的密码")
    @SaCheckRole(RoleConstants.SUPER_ADMIN) // 🔒 权限锁
    @PostMapping("/reset-pwd")
    public R<Void> resetPwd(
            @Parameter(description = "管理员 ID") @RequestParam Long userId,
            @Parameter(description = "新密码") @RequestParam String newPassword) {
        
        adminUserService.resetPassword(userId, newPassword);
        return R.ok();
    }
}