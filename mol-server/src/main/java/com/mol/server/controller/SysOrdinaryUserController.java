package com.mol.server.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.annotation.SaCheckPermission;
import cn.dev33.satoken.annotation.SaCheckRole;
import cn.dev33.satoken.annotation.SaMode;
import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.util.DesensitizedUtil;
import cn.hutool.core.util.ObjectUtil;
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
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

/**
 * 普通用户管理控制器 (学生/教工)
 * <p>
 * 包含：基础增删改查、密码管理、以及特殊的【角色提权】功能。
 * 涉及数据加密与脱敏，以及严格的权限控制。
 * </p>
 *
 * @author mol
 */
@Tag(name = "用户管理-普通用户", description = "学生与教职工的信息管理及密码维护")
@RestController
@RequestMapping("/ordinary-user")
@RequiredArgsConstructor
public class SysOrdinaryUserController {
    
    // 统一只保留一个 Service 注入，删除之前重复的 sysOrdinaryUserService
    private final SysOrdinaryUserService userService;
    private final SysUserRoleMapper userRoleMapper;
    
    // 查个人信息，移交给 UserProfileController，具体参考 UserServiceImpl
    
    // =================================================================================
    // 1. 基础查询 (Read)
    // =================================================================================
    
    @Operation(summary = "获取用户详情 (权限控制)", description = "获取指定 ID 用户的详细信息。非管理员只能查询自己的信息，且部分敏感字段会脱敏。")
    @SaCheckLogin
    @GetMapping("/{id}")
    public R<SysOrdinaryUser> getInfo(@PathVariable Long id) {
        // 🛡️ 权限校验核心逻辑
        Long currentUserId = LoginHelper.getUserId();
        boolean isAdmin = StpUtil.hasRole(RoleConstants.SUPER_ADMIN) || StpUtil.hasRole(RoleConstants.DORM_MANAGER);
        
        // 🛡️ 防越权：如果不是管理员，且查询的不是自己的 ID -> 禁止访问
        if (!isAdmin && !ObjectUtil.equal(currentUserId, id)) {
            return R.fail("权限不足：您只能查看自己的个人信息");
        }
        
        // MyBatis Plus 会自动调用 EncryptTypeHandler 解密，这里拿到的已经是明文数据
        SysOrdinaryUser user = userService.getById(id);
        
        if (user == null) {
            return R.fail("用户不存在");
        }
        
        // 🔒 业务脱敏：如果是普通用户查自己，为了安全，前端展示时建议对敏感字段（身份证、手机号）进行掩码处理
        // 如果是管理员查看（例如宿管核对身份），则返回完整明文以便业务办理（视具体安全需求可调整）
        if (!isAdmin) {
            // 使用 Hutool 工具类进行脱敏
            user.setIdCard(DesensitizedUtil.idCardNum(user.getIdCard(), 4, 4));
            user.setPhone(DesensitizedUtil.mobilePhone(user.getPhone()));
            // 居住地址如果涉及具体门牌号，也可以选择性脱敏，这里暂不处理
        }
        
        // 🛡️ 安全兜底：无论谁查询，密码字段绝对不能回显
        user.setPassword(null);
        
        return R.ok(user);
    }
    
    @Operation(summary = "分页查询用户", description = "支持按姓名模糊查询，或按身份(学生/教工)筛选")
    // 🔒 权限锁：超管(1 级) + 部门管理员(2 级) + 宿管/辅导员(3 级) 均可查看
    // 普通学生无权查看用户列表
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
        
        // ⚠️ 注意：数据库中的手机号/身份证是加密存储的，无法直接使用 SQL 的 LIKE 模糊查询。
        // 因此这里只支持按 '姓名' (非加密字段) 模糊查询，或按 '学号' (非加密字段) 精确查询。
        
        IPage<SysOrdinaryUser> result = userService.lambdaQuery()
                .like(realName != null, SysOrdinaryUser::getRealName, realName)
                .eq(username != null, SysOrdinaryUser::getUsername, username)
                .eq(userCategory != null, SysOrdinaryUser::getUserCategory, userCategory)
                // 🛡️ 防刁民：不返回已逻辑删除的数据 (MyBatis-Plus 默认会过滤，这里显式说明)
                .orderByDesc(SysOrdinaryUser::getCreateTime)
                .page(new Page<>(pageNum, pageSize));
        
        // 🛡️ 脱敏处理：批量清除列表中的密码字段
        result.getRecords().forEach(u -> u.setPassword(null));
        
        return R.ok(result);
    }
    
    // =================================================================================
    // 2. 新增用户（Create，手动插入、批量导入）
    // =================================================================================
    // ==================== 1. 原有的：手动新增接口 ====================
    // 适用于：录入单个插班生、转校生
    @Operation(summary = "手动新增人员")
    @SaCheckRole(value = {RoleConstants.SUPER_ADMIN, RoleConstants.DEPT_ADMIN}, mode = SaMode.OR)
    @PostMapping
    // 加上 @Validated，触发实体类里的 @NotBlank 校验
    public R<Boolean> save(@RequestBody @Validated SysOrdinaryUser user) {
        // 具体的正则校验逻辑（如学号格式、手机号格式）已下沉到 Service 实现类
        return R.ok(userService.saveUser(user));
    }
    
    // ==================== 2. 新增的：Excel 批量导入接口 ====================
    // 适用于：新生入学、教工入职批量处理
    // URL: POST /api/server/ordinary-user/import
    @Operation(summary = "Excel 批量导入")
    @PostMapping("/import")
    @SaCheckPermission("sys:user:import") // 建议分配一个专门的导入权限
    public R<String> importData(@RequestParam("file") MultipartFile file) throws IOException {
        // 检查文件是否为空
        if (file.isEmpty()) {
            return R.fail("导入失败：请先上传 Excel 文件");
        }

        // 调用 Service
        userService.importStudent(file.getInputStream());
        return R.ok("导入成功！数据已在后台处理完成");
    }
    
    // =================================================================================
    // 3. 删、改 (Write) - 仅限 1 级和 2 级管理员
    // =================================================================================
    
    @Operation(summary = "修改用户信息")
    @SaCheckRole(value = {RoleConstants.SUPER_ADMIN, RoleConstants.DEPT_ADMIN}, mode = SaMode.OR)
    @PutMapping
    public R<Boolean> update(@RequestBody SysOrdinaryUser user) {
        return R.ok(userService.updateUser(user));
    }
    
    
    @Operation(summary = "删除用户")
    @SaCheckRole(value = {RoleConstants.SUPER_ADMIN, RoleConstants.DEPT_ADMIN}, mode = SaMode.OR)
    @DeleteMapping("/{id}")
    public R<Boolean> remove(@PathVariable Long id) {
        // 🛡️ 防刁民：删除用户后，必须强制踢该用户下线，防止他持有旧 Token 继续操作
        boolean result = userService.removeById(id);
        if (result) {
            StpUtil.logout(id);
        }
        return R.ok(result);
    }


    // =================================================================================
    // 4. 导出 (Export)
    // =================================================================================
    
    @Operation(summary = "Excel 导出用户")
    @SaCheckRole(value = {RoleConstants.SUPER_ADMIN, RoleConstants.DEPT_ADMIN}, mode = SaMode.OR)
    @GetMapping("/export")
    public void export(
            HttpServletResponse response,
            // 使用对象接收参数，方便直接传给 Service
            SysOrdinaryUser queryParams) {
        
        // 核心修改：直接调用 Service 的高级导出方法
        // Service 层里包含了：查询数据 -> 查字典(ID 转 Name) -> 写入Excel 的全套逻辑
        userService.exportData(response, queryParams);
    }
    
    
    // =================================================================================
    // 5. 核心功能：角色提权 (Grant)
    // =================================================================================
    
    @Operation(summary = "给用户授权(提权)", description = "例如：给研究生分配辅导员角色。仅限超管或部门管理员操作。")
    @SaCheckRole(value = {RoleConstants.SUPER_ADMIN, RoleConstants.DEPT_ADMIN}, mode = SaMode.OR)
    @PostMapping("/auth/grant")
    public R<Void> grantRoleToUser(@RequestBody UserRoleGrantDTO grantDto) {
        // 1. 参数校验
        if (grantDto.getUserId() == null || grantDto.getRoleIds() == null) {
            throw new ServiceException("参数不完整");
        }
        
        // 🛡️ 防越权：严禁任何人通过此接口赋予 "super_admin" 权限
        // 只有数据库初始化时才能指定超管，后续 API 禁止操作，防止内鬼通过接口私自提权
        // 假设 super_admin 的角色 ID 是 1 (需结合数据库实际 ID)
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
        
        // 4. 立即生效：踢用户下线，强制他重新登录以获取最新的角色权限
        StpUtil.logout(grantDto.getUserId());
        
        return R.ok(null, "授权成功，用户需重新登录生效");
    }
    
    
    // =================================================================================
    // 6. 密码管理 (Password) - 高危操作
    // =================================================================================
    
    // ❌ 已删除 updatePwd (自己改密码) -> 移交 UserProfileController
    
    @Operation(summary = "管理员重置密码", description = "强制重置用户的密码，无视旧密码。")
    // 1：权限收紧，仅限超级管理员 (RoleConstants.SUPER_ADMIN)
    // 之前允许 DEPT_ADMIN，现在已移除，防止部门的管理员滥用职权
    @SaCheckRole(RoleConstants.SUPER_ADMIN)
    @PostMapping("/reset-pwd")
    public R<Void> resetPwd(
            @Parameter(description = "用户 ID") @RequestParam Long userId,
            @Parameter(description = "新密码") @RequestParam String newPassword) {
        
        // 🛡️ 防刁民：1. 密码复杂度校验 (简单版)
        if (newPassword.length() < 6) {
            throw new ServiceException("密码设置太简单，请至少设置 6 位");
        }
        
        // 2. 执行重置业务 (加密入库)
        userService.resetPassword(userId, newPassword);
        
        // 3. 强制踢下线，确保旧密码失效
        StpUtil.logout(userId);
        
        return R.ok(null, "密码重置成功，目标用户已被强制下线");
    }
    
    
    // =================================================================================
    // 7. 状态管理 (Status) - 遗漏点补全
    // =================================================================================
    
    @Operation(summary = "修改用户状态 (封号/解封)", description = "0=正常, 1=停用。停用后用户将被强制下线。")
    // 🔒 权限：仅限超管或部门管理员
    @SaCheckRole(value = {RoleConstants.SUPER_ADMIN, RoleConstants.DEPT_ADMIN}, mode = SaMode.OR)
    @PutMapping("/change-status")
    public R<Void> changeStatus(
            @Parameter(description = "用户 ID") @RequestParam Long userId,
            @Parameter(description = "新状态 (0/1)") @RequestParam String status) {
        
        // 1. 校验参数
        if (!"0".equals(status) && !"1".equals(status)) {
            throw new ServiceException("状态值不合法，仅允许 0(正常) 或 1(停用)");
        }
        
        // 2. 构造更新对象 (只更新 status 字段)
        SysOrdinaryUser user = new SysOrdinaryUser();
        user.setId(userId);
        user.setStatus(status);
        
        boolean result = userService.updateById(user);
        if (!result) {
            return R.fail("操作失败，用户可能不存在");
        }
        
        // 3. 关键：如果是“停用”，必须强制踢下线！
        // 否则用户手里拿着旧 Token 还能继续操作，封号就没意义了
        if ("1".equals(status)) {
            StpUtil.logout(userId);
        }
        
        return R.ok(null, "1".equals(status) ? "该用户已被封禁并强制下线" : "该用户已解封");
    }
}