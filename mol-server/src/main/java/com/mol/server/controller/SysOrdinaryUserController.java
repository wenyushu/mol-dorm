package com.mol.server.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.annotation.SaCheckRole;
import cn.dev33.satoken.annotation.SaMode;
import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.mol.common.core.constant.RoleConstants;
import com.mol.common.core.entity.SysOrdinaryUser;
import com.mol.common.core.entity.SysUserRole;
import com.mol.common.core.exception.ServiceException;
import com.mol.common.core.util.LoginHelper;
import com.mol.common.core.util.R;
import com.mol.server.dto.UserRoleGrantDTO;
import com.mol.server.mapper.SysUserRoleMapper;
import com.mol.server.service.SysOrdinaryUserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 普通用户管理控制器 (学生/教工)
 * <p>
 * 包含：基础增删改查、密码管理、以及特殊的【角色提权】功能。
 * </p>
 *
 * @author mol
 */
@Tag(name = "用户管理-普通用户", description = "学生与教职工的信息管理及密码维护")
@RestController
@RequestMapping("/ordinary-user")
@RequiredArgsConstructor
public class SysOrdinaryUserController {
    
    private final SysOrdinaryUserService ordinaryUserService;
    private final SysUserRoleMapper userRoleMapper; // 用于提权操作
    
    // =================================================================================
    // 1. 基础查询 (Read)
    // =================================================================================
    
    @Operation(summary = "分页查询用户", description = "支持按姓名模糊查询，或按身份(学生/教工)筛选")
    // 🔒 权限锁：超管(1级) + 部门管理员(2级) + 宿管/辅导员(3级) 均可查看
    @SaCheckRole(value = {
            RoleConstants.SUPER_ADMIN,
            RoleConstants.DEPT_ADMIN,
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
                // 🛡️ 防刁民：不返回已逻辑删除的数据 (MyBatis-Plus 默认会过滤，这里显式说明)
                .orderByDesc(SysOrdinaryUser::getCreateTime)
                .page(new Page<>(pageNum, pageSize));
        
        // 🛡️ 脱敏处理：密码绝对不能回显给前端
        result.getRecords().forEach(u -> u.setPassword(null));
        
        return R.ok(result);
    }
    
    // =================================================================================
    // 2. 增删改 (Write) - 仅限 1级和2级管理员
    // =================================================================================
    
    @Operation(summary = "新增用户")
    @SaCheckRole(value = {RoleConstants.SUPER_ADMIN, RoleConstants.DEPT_ADMIN}, mode = SaMode.OR)
    @PostMapping
    public R<Boolean> save(@RequestBody SysOrdinaryUser user) {
        // 具体的正则校验逻辑已下沉到 Service 实现类
        return R.ok(ordinaryUserService.saveUser(user));
    }
    
    @Operation(summary = "修改用户信息")
    @SaCheckRole(value = {RoleConstants.SUPER_ADMIN, RoleConstants.DEPT_ADMIN}, mode = SaMode.OR)
    @PutMapping
    public R<Boolean> update(@RequestBody SysOrdinaryUser user) {
        return R.ok(ordinaryUserService.updateUser(user));
    }
    
    @Operation(summary = "删除用户")
    @SaCheckRole(value = {RoleConstants.SUPER_ADMIN, RoleConstants.DEPT_ADMIN}, mode = SaMode.OR)
    @DeleteMapping("/{id}")
    public R<Boolean> remove(@PathVariable Long id) {
        // 🛡️ 防刁民：删除后，必须踢该用户下线，防止他持有旧 Token 继续访问
        boolean result = ordinaryUserService.removeById(id);
        if (result) {
            StpUtil.logout(id);
        }
        return R.ok(result);
    }
    
    // =================================================================================
    // 3. 核心功能：角色提权 (Grant)
    // =================================================================================
    
    @Operation(summary = "给用户授权(提权)", description = "例如：给研究生分配辅导员角色")
    @SaCheckRole(value = {RoleConstants.SUPER_ADMIN, RoleConstants.DEPT_ADMIN}, mode = SaMode.OR)
    @PostMapping("/auth/grant")
    public R<Void> grantRoleToUser(@RequestBody UserRoleGrantDTO grantDto) {
        // 1. 参数校验
        if (grantDto.getUserId() == null || grantDto.getRoleIds() == null) {
            throw new ServiceException("参数不完整");
        }
        
        // 🛡️ 防越权：严禁任何人通过此接口赋予 "super_admin" 权限
        // 只有数据库初始化时才能指定超管，后续 API 禁止操作，防止内鬼提权
        // 假设 super_admin 的 id 是 1 (需结合数据库实际 ID，或者查表判断)
        if (grantDto.getRoleIds().contains(1L)) {
            throw new ServiceException("非法操作：禁止通过接口赋予超级管理员权限！");
        }
        
        // 2. 清空旧兼职
        userRoleMapper.delete(new LambdaQueryWrapper<SysUserRole>()
                .eq(SysUserRole::getUserId, grantDto.getUserId()));
        
        // 3. 赋予新兼职
        if (!grantDto.getRoleIds().isEmpty()) {
            for (Long roleId : grantDto.getRoleIds()) {
                SysUserRole ur = new SysUserRole();
                ur.setUserId(grantDto.getUserId());
                ur.setRoleId(roleId);
                userRoleMapper.insert(ur);
            }
        }
        
        // 4. 立即生效：踢用户下线，让他重新登录以获取新角色
        StpUtil.logout(grantDto.getUserId());
        
        return R.ok(null, "授权成功，用户需重新登录生效");
    }
    
    // =================================================================================
    // 4. 密码管理 (Password)
    // =================================================================================
    
    @Operation(summary = "管理员重置密码")
    @SaCheckRole(value = {RoleConstants.SUPER_ADMIN, RoleConstants.DEPT_ADMIN}, mode = SaMode.OR)
    @PostMapping("/reset-pwd")
    public R<Void> resetPwd(
            @Parameter(description = "用户 ID") @RequestParam Long userId,
            @Parameter(description = "新密码") @RequestParam String newPassword) {
        
        // 🛡️ 防刁民：密码复杂度校验 (简单版)
        if (newPassword.length() < 6) {
            throw new ServiceException("密码设置太简单，请至少设置6位");
        }
        
        ordinaryUserService.resetPassword(userId, newPassword);
        
        // 强制踢下线
        StpUtil.logout(userId);
        
        return R.ok(null, "密码重置成功");
    }
    
    @Operation(summary = "修改个人密码", description = "用户自行修改密码")
    @SaCheckLogin // 只要登录就能改
    @PostMapping("/update-pwd")
    public R<Void> updatePwd(
            @Parameter(description = "旧密码") @RequestParam String oldPassword,
            @Parameter(description = "新密码") @RequestParam String newPassword) {
        
        // 🛡️ 防刁民核心：移除 Controller 参数中的 userId
        // 永远不要相信前端传来的 "userId"，必须从 Token 中解析
        Long currentUserId = LoginHelper.getUserId();
        
        if (currentUserId == null) {
            throw new ServiceException("登录状态已失效");
        }
        
        ordinaryUserService.updatePassword(currentUserId, oldPassword, newPassword);
        
        // 修改成功后，强制注销，要求重新登录
        StpUtil.logout();
        
        return R.ok(null, "修改成功，请重新登录");
    }
}