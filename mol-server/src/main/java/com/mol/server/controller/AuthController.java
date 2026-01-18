package com.mol.server.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.stp.StpUtil;
import com.mol.common.core.util.R;
import com.mol.server.dto.LoginBody;
import com.mol.server.service.AuthService;
import com.mol.server.vo.LoginVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Tag(name = "认证中心", description = "登录/注销/验证码")
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {
    
    private final AuthService authService;
    
    @Operation(summary = "账号登录")
    @PostMapping("/login")
    public R<LoginVO> login(@Validated @RequestBody LoginBody loginBody) {
        // ✅ 修正：直接获取 Service 返回的完整 VO，无需再手动封装
        LoginVO vo = authService.login(loginBody);
        return R.ok(vo);
    }
    
    @Operation(summary = "退出登录")
    @SaCheckLogin
    @PostMapping("/logout")
    public R<String> logout() {
        authService.logout();
        return R.ok("注销成功");
    }
    
    @Operation(summary = "Token 有效性检查")
    @GetMapping("/check")
    public R<String> checkToken() {
        return StpUtil.isLogin() ? R.ok("Token 有效") : R.fail("Token 无效或已过期");
    }
}