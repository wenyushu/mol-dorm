package com.mol.server.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.annotation.SaCheckPermission;
import cn.dev33.satoken.annotation.SaCheckRole;
import cn.dev33.satoken.annotation.SaMode;
import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.util.DesensitizedUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.idev.excel.FastExcel;
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
import com.mol.server.excel.StudentImportListener;
import com.mol.server.mapper.*;
import com.mol.server.service.SysOrdinaryUserService;
import com.mol.server.vo.StudentImportVO;
import io.swagger.v3.oas.annotations.Operation;
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
 * 包含：基础增删改查、Excel 高性能导入导出、以及关键的角色提权与状态管理。
 * </p>
 */
@Tag(name = "用户管理-普通用户", description = "学生与教职工的信息管理及密码维护")
@RestController
@RequestMapping("/ordinary-user")
@RequiredArgsConstructor
public class SysOrdinaryUserController {
    
    private final SysOrdinaryUserService userService;
    private final SysUserRoleMapper userRoleMapper;
    
    // 🔥 [寻址 Mapper 注入]：用于支持 StudentImportListener 的缓存预热，消除未使用警告
    private final SysCampusMapper campusMapper;
    private final SysCollegeMapper collegeMapper;
    private final SysMajorMapper majorMapper;
    private final SysClassMapper classMapper;
    private final SysDeptMapper deptMapper;
    
    // =================================================================================
    // 1. 基础查询 (Read)
    // =================================================================================
    
    @Operation(summary = "获取用户详情 (权限控制)")
    @SaCheckLogin
    @GetMapping("/{id}")
    public R<SysOrdinaryUser> getInfo(@PathVariable Long id) {
        Long currentUserId = LoginHelper.getUserId();
        boolean isAdmin = StpUtil.hasRole(RoleConstants.SUPER_ADMIN) || StpUtil.hasRole(RoleConstants.DORM_MANAGER);
        
        if (!isAdmin && !ObjectUtil.equal(currentUserId, id)) {
            return R.fail("权限不足：您只能查看自己的个人信息");
        }
        
        SysOrdinaryUser user = userService.getById(id);
        if (user == null) return R.fail("用户不存在");
        
        if (!isAdmin) {
            user.setIdCard(DesensitizedUtil.idCardNum(user.getIdCard(), 4, 4));
            user.setPhone(DesensitizedUtil.mobilePhone(user.getPhone()));
        }
        user.setPassword(null);
        return R.ok(user);
    }
    
    @Operation(summary = "分页查询用户")
    @SaCheckRole(value = {
            RoleConstants.SUPER_ADMIN, RoleConstants.DEPT_ADMIN,
            RoleConstants.DORM_MANAGER, RoleConstants.COUNSELOR
    }, mode = SaMode.OR)
    @GetMapping("/page")
    public R<IPage<SysOrdinaryUser>> page(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            String realName, String username, Integer userCategory) {
        
        IPage<SysOrdinaryUser> result = userService.lambdaQuery()
                .like(realName != null, SysOrdinaryUser::getRealName, realName)
                .eq(username != null, SysOrdinaryUser::getUsername, username)
                .eq(userCategory != null, SysOrdinaryUser::getUserCategory, userCategory)
                .orderByDesc(SysOrdinaryUser::getCreateTime)
                .page(new Page<>(pageNum, pageSize));
        
        result.getRecords().forEach(u -> u.setPassword(null));
        return R.ok(result);
    }
    
    // =================================================================================
    // 2. 新增与导入 (Create)
    // =================================================================================
    
    @Operation(summary = "手动新增人员")
    @SaCheckRole(value = {RoleConstants.SUPER_ADMIN, RoleConstants.DEPT_ADMIN}, mode = SaMode.OR)
    @PostMapping
    public R<Boolean> save(@RequestBody @Validated SysOrdinaryUser user) {
        return R.ok(userService.saveUser(user));
    }
    
    /**
     * [Excel 批量导入]
     * 🛡️ 适配 FastExcel 高性能解析，解决 StudentImportListener 未使用警告
     */
    @Operation(summary = "Excel 批量导入 (学生/教工混合导入)")
    @PostMapping("/import")
    @SaCheckPermission("sys:user:import")
    public R<String> importData(@RequestParam("file") MultipartFile file) throws IOException {
        if (file.isEmpty()) {
            return R.fail("导入失败：文件不能为空");
        }
        
        // 🚀 [实例化监听器]：传入所有 Mapper，触发缓存预热逻辑
        FastExcel.read(file.getInputStream(), StudentImportVO.class,
                        new StudentImportListener(userService, userRoleMapper, campusMapper,
                                collegeMapper, majorMapper, classMapper, deptMapper))
                .sheet()
                .doRead();
        
        return R.ok("批量导入任务已启动，系统正在对组织架构进行审计入库");
    }
    
    // =================================================================================
    // 3. 删、改 (Write)
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
        boolean result = userService.removeById(id);
        if (result) StpUtil.logout(id);
        return R.ok(result);
    }
    
    // =================================================================================
    // 4. 导出 (Export)
    // =================================================================================
    
    @Operation(summary = "Excel 导出用户")
    @SaCheckRole(value = {RoleConstants.SUPER_ADMIN, RoleConstants.DEPT_ADMIN}, mode = SaMode.OR)
    @GetMapping("/export")
    public void export(HttpServletResponse response, SysOrdinaryUser queryParams) {
        userService.exportData(response, queryParams);
    }
    
    // =================================================================================
    // 5. 权限提权 (Grant)
    // =================================================================================
    
    @Operation(summary = "给用户授权(提权)")
    @SaCheckRole(value = {RoleConstants.SUPER_ADMIN, RoleConstants.DEPT_ADMIN}, mode = SaMode.OR)
    @PostMapping("/auth/grant")
    public R<Void> grantRoleToUser(@RequestBody UserRoleGrantDTO grantDto) {
        if (grantDto.getUserId() == null || grantDto.getRoleIds() == null) {
            throw new ServiceException("参数不完整");
        }
        // 🛡️ 防内鬼：禁止通过 API 赋予超管权限
        if (grantDto.getRoleIds().contains(RoleConstants.SUPER_ADMIN_ID)) {
            throw new ServiceException("非法操作：禁止通过接口赋予超级管理员权限！");
        }
        
        userRoleMapper.delete(new LambdaQueryWrapper<SysUserRole>().eq(SysUserRole::getUserId, grantDto.getUserId()));
        
        if (!grantDto.getRoleIds().isEmpty()) {
            for (Long roleId : grantDto.getRoleIds()) {
                SysUserRole ur = new SysUserRole();
                ur.setUserId(grantDto.getUserId());
                ur.setRoleId(roleId);
                userRoleMapper.insert(ur);
            }
        }
        StpUtil.logout(grantDto.getUserId());
        return R.ok(null, "授权成功，用户需重新登录生效");
    }
    
    // =================================================================================
    // 6. 密码管理
    // =================================================================================
    
    @Operation(summary = "管理员重置密码")
    @SaCheckRole(RoleConstants.SUPER_ADMIN)
    @PostMapping("/reset-pwd")
    public R<Void> resetPwd(@RequestParam Long userId, @RequestParam String newPassword) {
        if (newPassword.length() < 6) throw new ServiceException("密码设置太简单");
        userService.resetPassword(userId, newPassword);
        StpUtil.logout(userId);
        return R.ok(null, "密码重置成功");
    }
    
    // =================================================================================
    // 7. 状态管理 (封号/解封)
    // =================================================================================
    
    @Operation(summary = "修改用户状态")
    @SaCheckRole(value = {RoleConstants.SUPER_ADMIN, RoleConstants.DEPT_ADMIN}, mode = SaMode.OR)
    @PutMapping("/change-status")
    public R<Void> changeStatus(@RequestParam Long userId, @RequestParam String status) {
        if (!"0".equals(status) && !"1".equals(status)) throw new ServiceException("状态值不合法");
        
        SysOrdinaryUser user = new SysOrdinaryUser();
        user.setId(userId);
        user.setStatus(status);
        
        if (!userService.updateById(user)) return R.fail("操作失败");
        if ("1".equals(status)) StpUtil.logout(userId);
        
        return R.ok(null, "1".equals(status) ? "已封禁" : "已解封");
    }
}