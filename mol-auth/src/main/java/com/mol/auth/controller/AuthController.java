package com.mol.auth.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.stp.StpUtil;
import com.mol.common.core.entity.SysAdminUser;
import com.mol.common.core.entity.SysOrdinaryUser;
import com.mol.common.core.util.R;
import com.mol.sys.biz.dto.LoginBody;
import com.mol.sys.biz.service.AuthService;
import com.mol.sys.biz.service.SysAdminUserService;
import com.mol.sys.biz.service.SysOrdinaryUserService;
import com.mol.sys.biz.vo.LoginVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 认证中心控制器
 * <p>
 * 负责系统的 登录、登出、获取当前用户信息
 * 支持管理员与普通用户的统一入口
 * </p>
 *
 * @author mol
 */
@Tag(name = "认证中心", description = "登录/登出/个人信息")
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {
    
    private final AuthService authService;
    private final SysAdminUserService adminUserService;
    private final SysOrdinaryUserService ordinaryUserService;
    
    /**
     * 登录接口
     */
    @Operation(summary = "用户登录", description = "支持管理员(admin)和学生(student)登录")
    @PostMapping("/login")
    public R<LoginVO> login(@RequestBody LoginBody loginBody) {
        // 调用 AuthService 的双表登录逻辑
        LoginVO loginVO = authService.login(loginBody);
        return R.ok(loginVO);
    }
    
    /**
     * 退出登录
     */
    @Operation(summary = "退出登录")
    @SaCheckLogin // 只有登录状态下才能注销
    @PostMapping("/logout")
    public R<String> logout() {
        StpUtil.logout();
        return R.ok("退出成功");
    }
    
    /**
     * 获取当前用户信息
     * <p>
     * 智能路由：根据登录时的身份类型，自动去查 sys_admin_user 或 sys_ordinary_user
     * </p>
     */
    @Operation(summary = "获取个人信息", description = "获取当前登录用户的详细档案")
    @SaCheckLogin
    @GetMapping("/info")
    public R<Object> getUserInfo() {
        // 1. 获取当前登录ID
        Long loginId = StpUtil.getLoginIdAsLong();
        
        // 2. 从 Session 中获取用户类型 (我们在 AuthServiceImpl 中存入的)
        // type: "admin" 或 "student"
        String userType = (String) StpUtil.getSession().get("type");
        
        if ("admin".equals(userType)) {
            // 如果是管理员，查管理员表
            SysAdminUser admin = adminUserService.getById(loginId);
            if (admin != null) {
                admin.setPassword(null); // 擦除密码，防止泄露
                return R.ok(admin);
            }
        } else {
            // 否则是普通用户，查学生表
            SysOrdinaryUser user = ordinaryUserService.getById(loginId);
            if (user != null) {
                user.setPassword(null); // 擦除密码
                return R.ok(user);
            }
        }
        
        return R.fail("获取用户信息失败，用户不存在");
    }
}