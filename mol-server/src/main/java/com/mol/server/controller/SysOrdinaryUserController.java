package com.mol.server.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.annotation.SaCheckRole;
import cn.dev33.satoken.annotation.SaMode;
import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.mol.common.core.constant.RoleConstants;
import com.mol.common.core.entity.SysOrdinaryUser;
import com.mol.common.core.exception.ServiceException;
import com.mol.common.core.util.R;
import com.mol.server.service.SysOrdinaryUserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 普通用户管理控制器 (学生/教工)
 * <p>
 * 提供针对学生和教职工账户的 CRUD 及密码管理接口
 * <p>
 * 🔒 权限控制策略：
 * 1. 增删改、强制重置密码 -> 仅超级管理员 (Super Admin)
 * 2. 查询列表 -> 管理组 (超管 + 宿管 + 辅导员)，防止普通学生查询全校名单
 * 3. 修改个人密码 -> 登录用户本人
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
    // mode = SaMode.OR 表示只要具备其中任意一个角色即可通过
    @SaCheckRole(value = {
            RoleConstants.SUPER_ADMIN,
            RoleConstants.DORM_MANAGER,
            RoleConstants.COUNSELOR
    }, mode = SaMode.OR)
    @GetMapping("/page")
    public R<IPage<SysOrdinaryUser>> page(
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") Integer pageNum,
            @Parameter(description = "每页大小") @RequestParam(defaultValue = "10") Integer pageSize,
            @Parameter(description = "真实姓名 (模糊查询)") @RequestParam(required = false) String realName,
            @Parameter(description = "学号/工号 (精确查询)") @RequestParam(required = false) String username,
            @Parameter(description = "身份分类: 0-学生, 1-职工") @RequestParam(required = false) Integer userCategory) {
        
        // 构造查询条件
        IPage<SysOrdinaryUser> result = ordinaryUserService.lambdaQuery()
                .like(realName != null, SysOrdinaryUser::getRealName, realName)
                .eq(username != null, SysOrdinaryUser::getUsername, username)
                .eq(userCategory != null, SysOrdinaryUser::getUserCategory, userCategory)
                .orderByDesc(SysOrdinaryUser::getCreateTime)
                .page(new Page<>(pageNum, pageSize));
        
        // 脱敏处理：密码不回显
        result.getRecords().forEach(u -> u.setPassword(null));
        
        return R.ok(result);
    }
    
    @Operation(summary = "新增用户 (仅 Admin)", description = "如果未填密码，默认为 123456")
    // 🔒 权限锁：只有超级管理员能录入档案
    @SaCheckRole(RoleConstants.SUPER_ADMIN)
    @PostMapping
    public R<Boolean> save(@RequestBody SysOrdinaryUser user) {
        return R.ok(ordinaryUserService.saveUser(user));
    }
    
    @Operation(summary = "修改用户信息 (仅 Admin)", description = "仅修改基本信息，不包含密码")
    // 🔒 权限锁：只有超级管理员能修改档案
    @SaCheckRole(RoleConstants.SUPER_ADMIN)
    @PutMapping
    public R<Boolean> update(@RequestBody SysOrdinaryUser user) {
        return R.ok(ordinaryUserService.updateUser(user));
    }
    
    @Operation(summary = "删除用户 (仅 Admin)", description = "物理删除用户数据")
    // 🔒 权限锁：只有超级管理员能删除档案
    @SaCheckRole(RoleConstants.SUPER_ADMIN)
    @DeleteMapping("/{id}")
    public R<Boolean> remove(@PathVariable Long id) {
        return R.ok(ordinaryUserService.removeById(id));
    }
    
    // ------------------- 密码管理接口 -------------------
    
    @Operation(summary = "管理员重置密码", description = "管理员强制重置用户密码，无需旧密码")
    // 🔒 权限锁：只有超级管理员能执行 “暴力重置”
    @SaCheckRole(RoleConstants.SUPER_ADMIN)
    @PostMapping("/reset-pwd")
    public R<Void> resetPwd(
            @Parameter(description = "用户 ID", required = true) @RequestParam Long userId,
            @Parameter(description = "新密码", required = true) @RequestParam String newPassword) {
        
        ordinaryUserService.resetPassword(userId, newPassword);
        return R.ok(null, "密码重置成功");
    }
    
    @Operation(summary = "修改个人密码", description = "用户自行修改密码，需验证旧密码")
    // 🔒 权限锁：必须是已登录用户
    @SaCheckLogin
    @PostMapping("/update-pwd")
    public R<Void> updatePwd(
            @Parameter(description = "用户 ID", required = true) @RequestParam Long userId,
            @Parameter(description = "旧密码", required = true) @RequestParam String oldPassword,
            @Parameter(description = "新密码", required = true) @RequestParam String newPassword) {
        
        // 🛡️ 安全防线：防止有人带了 Token 却去改别人的密码
        long currentLoginId = StpUtil.getLoginIdAsLong();
        if (currentLoginId != userId) {
            throw new ServiceException("非法操作：您无权修改他人的密码！");
        }
        
        ordinaryUserService.updatePassword(userId, oldPassword, newPassword);
        return R.ok(null, "密码修改成功，请重新登录");
    }
}