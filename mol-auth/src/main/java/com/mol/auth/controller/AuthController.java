package com.mol.auth.controller;

import cn.dev33.satoken.stp.StpUtil;
import com.mol.api.dto.LoginBody;
import com.mol.auth.service.AuthService;
import com.mol.common.core.util.R;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 认证中心控制器
 * 负责系统的登录、退出及 Token 状态查询
 * 使用统一响应体 R 对象进行封装
 *
 * @author mol
 */
@Slf4j
@Tag(name = "认证中心", description = "负责用户登录、注销及权限核验")
@RestController
@RequestMapping("/") // 为了防止 URL 变成 /api/auth/auth/login
@RequiredArgsConstructor
public class AuthController {
    
    private final AuthService authService;
    
    /**
     * 用户登录接口
     * 业务成功后 code 为 0，data 中包含 Token 信息
     *
     * @param loginBody 登录参数 (username, password, userType)
     * @return 统一响应体 R<Map<String, String>>
     */
    @Operation(summary = "用户登录", description = "根据用户类型进行分表校验，成功后签发并返回 Sa-Token 令牌")
    @PostMapping("/login")
    public R<Map<String, String>> login(@Validated @RequestBody LoginBody loginBody) {
        log.info("收到登录请求: 用户名={}, 类型={}", loginBody.getUsername(), loginBody.getUserType());
        
        // 1. 调用 Service 执行校验逻辑并获取 Token 值
        String tokenValue = authService.login(loginBody);
        
        // 2. 封装 Token 信息
        Map<String, String> tokenInfo = new HashMap<>();
        tokenInfo.put("tokenName", StpUtil.getTokenName()); // 获取配置的名称，如 mol-token
        tokenInfo.put("tokenValue", tokenValue);
        
        // 3. 使用 R.ok 静态方法返回成功结果 (code = 0)
        log.info("用户 {} 登录成功", loginBody.getUsername());
        return R.ok(tokenInfo);
    }
    
    /**
     * 注销登录
     * 逻辑：清除当前 Session 会话及 Redis 中的 Token 记录
     *
     * @return 统一响应体 R<Void>
     */
    @Operation(summary = "注销登录", description = "强制使当前的 Token 失效并退出会话")
    @PostMapping("/logout")
    public R<Void> logout() {
        String loginId = (String) StpUtil.getLoginIdDefaultNull();
        StpUtil.logout();
        log.info("用户 {} 已安全退出", loginId);
        return R.ok();
    }
    
    /**
     * 查询当前登录详情
     * 调试用：查看当前 Token 关联的身份及登录状态
     *
     * @return 统一响应体 R<Map<String, Object>>
     */
    @Operation(summary = "查询登录状态", description = "返回当前 Token 对应的 LoginId 及有效性")
    @GetMapping("/info")
    public R<Map<String, Object>> getInfo() {
        Map<String, Object> statusInfo = new HashMap<>();
        statusInfo.put("isLogin", StpUtil.isLogin());
        statusInfo.put("loginId", StpUtil.getLoginIdDefaultNull());
        statusInfo.put("tokenTimeout", StpUtil.getTokenTimeout());
        
        return R.ok(statusInfo);
    }
}