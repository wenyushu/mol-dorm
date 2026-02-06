package com.mol.server.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.annotation.SaCheckSafe;
import cn.dev33.satoken.stp.StpUtil;
import com.mol.common.core.util.R;
import com.mol.server.dto.LoginBody;
import com.mol.server.service.AuthService;
import com.mol.server.vo.LoginVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Tag(name = "认证中心", description = "登录/注销/验证码")
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {
    
    private final AuthService authService;
    
    @Operation(summary = "账号登录(支持记住我)")
    @PostMapping("/login")
    public R<LoginVO> login(@Validated @RequestBody LoginBody loginBody) {
        // 直接获取 Service 返回的完整 VO，无需再手动封装
        // Service 层已包含 BCrypt 校验、状态检查、Session 写入
        LoginVO vo = authService.login(loginBody);
        return R.ok(vo);
    }
    
    
    @Operation(summary = "退出登录")
    @SaCheckLogin // 🟢 确保只有登录状态下才能调用退出，避免无意义的报错
    @PostMapping("/logout")
    public R<String> logout() {
        authService.logout();
        return R.ok("注销成功");
    }
    
    @Operation(summary = "Token 状态检查")
    @GetMapping("/check")
    public R<String> checkToken() {
        if (StpUtil.isLogin()) {
            return R.ok("Token 有效 | 账号: " + StpUtil.getLoginId());
        }
        return R.fail(401, "Token 无效");
    }
    
    // ================== 二级认证相关 ==================
    
    @Operation(summary = "解锁二级认证 (输入密码)")
    @PostMapping("/open-safe")
    // 将 R<Void> 改为 R<String>，因为返回了一段提示文字
    public R<String> openSafe(@RequestBody Map<String, String> body) {
        // 前端传json: { "password": "..." }
        String pwd = body.get("password");
        authService.openSafeMode(pwd);
        
        // R.ok("xxx") 会把字符串放入 data 字段，所以泛型必须是 String
        return R.ok("身份验证通过，5分钟内可进行敏感操作");
    }
    
    @Operation(summary = "【测试】敏感操作接口 (需二级认证)")
    @SaCheckSafe // 🟢 关键注解：没有 openSafe 无法访问此接口
    @PostMapping("/sensitive-action")
    public R<String> sensitiveAction() {
        return R.ok("您已通过二级认证，成功删库跑路！(误)");
    }
    
    
    @Operation(summary = "检查是否处于二级认证有效期内")
    @GetMapping("/is-safe")
    public R<Boolean> isSafe() {
        return R.ok(StpUtil.isSafe());
    }
    
    
    @Operation(summary = "【测试】后端接收的 token")
    @GetMapping("/debug/header")
    public String debugHeader(HttpServletRequest request) {
        String token = request.getHeader("mol-token");
        System.out.println("后端接收到的 Token: " + token);
        return "Received: " + token;
    }
}