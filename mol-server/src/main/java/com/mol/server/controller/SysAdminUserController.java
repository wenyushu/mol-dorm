package com.mol.server.controller;

import cn.dev33.satoken.annotation.SaCheckRole;
import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.mol.common.core.constant.RoleConstants;
import com.mol.common.core.entity.SysAdminUser;
import com.mol.common.core.exception.ServiceException;
import com.mol.common.core.util.R;
import com.mol.server.service.SysAdminUserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;


/**
 * 系统管理员管理控制器
 * <p>
 * 仅限超级管理员 (Super Admin) 操作。
 * 用于维护宿管、后勤等工作人员的账号。这些账号拥有较高的系统权限。
 * </p>
 * * @author mol
 */
@Tag(name = "用户管理-系统管理员", description = "宿管/后勤人员的账号维护")
@RestController
@RequestMapping("/admin-user")
@RequiredArgsConstructor
public class SysAdminUserController {
    
    private final SysAdminUserService adminUserService;
    
    // 查个人信息，移交给 UserProfileController，具体参考 UserServiceImpl
    
    // =================================================================================
    // 1. 查询 (Read)
    // =================================================================================
    
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
    
    // =================================================================================
    // 2. 新增 (Create)
    // =================================================================================
    
    @Operation(summary = "新增管理员 (宿管/后勤/辅导员)", description = "新增系统管理人员，默认密码通常为: 123456")
    @SaCheckRole(RoleConstants.SUPER_ADMIN) // 🔒 权限锁
    @PostMapping
    // 加上 @Validated，触发实体类里的 @NotBlank 校验
    public R<Boolean> save(@RequestBody @Validated SysAdminUser admin) {
        return R.ok(adminUserService.saveAdmin(admin));
    }
    
    // =================================================================================
    // 3. 导出 (Export) - 新增
    // =================================================================================
    
    @Operation(summary = "导出管理员名单", description = "导出 Excel，仅限超级管理员操作")
    @SaCheckRole(RoleConstants.SUPER_ADMIN) // 🔒 严格限制导出权限
    @GetMapping("/export")
    public void export(
            HttpServletResponse response,
            SysAdminUser queryParams) {
        // 调用 Service 层的全功能导出（含 ID 转 Name）
        adminUserService.exportData(response, queryParams);
    }
    
    // =================================================================================
    // 4. 修改与删除 (Update & Delete)
    // =================================================================================
    
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
        boolean result = adminUserService.removeById(id);
        
        // 删除成功后，强制踢下线，防止他持有旧 Token 搞破坏
        if (result) {
            StpUtil.logout(id);
        }
        return R.ok(result);
    }
    
    
    // =================================================================================
    // 5. 密码管理 (Password)
    // =================================================================================
    
    @Operation(summary = "重置管理员密码", description = "强制重置某管理员的密码 (如宿管忘记密码)")
    // 🔒 权限锁：只有超管能重置其他管理员的密码
    @SaCheckRole(RoleConstants.SUPER_ADMIN)
    @PostMapping("/reset-pwd")
    public R<Void> resetPwd(
            @Parameter(description = "目标管理员 ID") @RequestParam Long userId,
            @Parameter(description = "新密码") @RequestParam String newPassword) {
        
        // 1. 防刁民：禁止重置超管自己的密码 (防误操作把自己锁死，虽然 update-pwd 可以改)
        // 假设 ID=1 是初始超管
        if (userId == 1L) {
            // 获取当前登录人 ID
            long operatorId = StpUtil.getLoginIdAsLong();
            // 如果不是自己重置自己，而是想重置 ID=1 的账号 -> 拦截
            if (operatorId != 1L) {
                return R.fail("非法操作：无法重置超级管理员的密码");
            }
        }
        
        // 2. 执行业务
        adminUserService.resetPassword(userId, newPassword);
        
        // 3. 强制踢下线：确保旧 Token 失效
        StpUtil.logout(userId);
        
        return R.ok(null, "密码重置成功，该管理员已被强制下线");
    }
    
    
    // =================================================================================
    // 6. 状态管理 (Status) - 遗漏点补全
    // =================================================================================
    
    @Operation(summary = "修改管理员状态 (封号/解封)", description = "0=正常, 1=停用。仅限超管操作。")
    // 🔒 权限：只有超管能封禁其他管理员
    @SaCheckRole(RoleConstants.SUPER_ADMIN)
    @PutMapping("/change-status")
    public R<Void> changeStatus(
            @Parameter(description = "管理员ID") @RequestParam Long userId,
            @Parameter(description = "新状态 (0/1)") @RequestParam String status) {
        
        // 1. 校验参数
        if (!"0".equals(status) && !"1".equals(status)) {
            throw new ServiceException("状态值不合法");
        }
        
        // 🛡️ 防刁民 A：保护初始超管 (ID=1)
        if (userId == 1L) {
            return R.fail("非法操作：最高管理员账号禁止停用！");
        }
        
        // 🛡️ 防刁民 B：防止自己封自己
        // 获取当前登录者的 ID
        long currentOperatorId = StpUtil.getLoginIdAsLong();
        if (userId == currentOperatorId && "1".equals(status)) {
            return R.fail("操作被拦截：您不能封禁自己的账号，否则将无法登录系统。");
        }
        
        // 2. 执行更新
        SysAdminUser admin = new SysAdminUser();
        admin.setId(userId);
        admin.setStatus(status);
        
        boolean result = adminUserService.updateById(admin);
        if (!result) {
            return R.fail("管理员不存在");
        }
        
        // 3. 强制踢下线
        if ("1".equals(status)) {
            StpUtil.logout(userId);
        }
        
        return R.ok(null, "1".equals(status) ? "该管理员已被冻结并强制下线" : "该管理员已恢复正常");
    }
}