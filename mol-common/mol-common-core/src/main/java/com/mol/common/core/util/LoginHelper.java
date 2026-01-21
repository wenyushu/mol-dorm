package com.mol.common.core.util;

import cn.dev33.satoken.context.SaHolder;
import cn.dev33.satoken.session.SaSession;
import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.convert.Convert;
import cn.hutool.core.util.StrUtil;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * 登录鉴权助手 (防刁民加强版)
 * <p>
 * 核心职能：安全地从 Token/Session 中解析用户 ID 和类型。
 * </p>
 *
 * @author mol
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class LoginHelper {
    
    /**
     * 获取当前登录用户 ID (真实 ID，非 Sa-Token 的 LoginId)
     * <p>
     * 🛡️ 防刁民设计：
     * 1. 优先读 Session (速度快，数据准)
     * 2. 兜底读 Token (无状态)，并进行 Try-Catch 容错
     * </p>
     *
     * @return userId (Long) 或 null (未登录/解析失败)
     */
    public static Long getUserId() {
        try {
            // 0. 先判断是否登录，未登录直接返回 null (防止后续操作抛异常)
            if (!isLogin()) {
                return null;
            }
            
            // 1. 尝试从 Session 获取 (登录时已写入 "originalId")
            // 注意：使用 false 参数，防止 Session 不存在时自动创建，浪费资源
            SaSession session = StpUtil.getSession(false);
            if (session != null) {
                Object originalId = session.get("originalId");
                if (originalId != null) {
                    return Convert.toLong(originalId);
                }
            }
            
            // 2. 兜底：如果 Session 没取到 (极端情况)，解析 Token 字符串 (格式 "Type:ID")
            String loginId = StpUtil.getLoginIdAsString();
            return parseIdFromToken(loginId);
            
        } catch (Exception e) {
            // 🛡️ 静默失败，不抛出 500 异常给前端
            return null;
        }
    }
    
    /**
     * 获取当前用户类型
     * @return "admin"(0) 或 "student"(1) 对应的字符串，或者原始数字字符串
     */
    public static String getUserType() {
        try {
            if (!isLogin()) {
                return null;
            }
            
            // 1. 优先从 Session 拿 (AuthServiceImpl 里存的是 String 类型的 "admin" 或 "student")
            SaSession session = StpUtil.getSession(false);
            if (session != null) {
                String type = session.getString("type");
                if (StrUtil.isNotBlank(type)) {
                    return type;
                }
            }
            
            // 2. 兜底：解析 Token 前缀 ("0:1001" -> "0")
            String loginId = StpUtil.getLoginIdAsString();
            if (StrUtil.isBlank(loginId) || !loginId.contains(":")) {
                return null;
            }
            return loginId.split(":")[0];
        } catch (Exception e) {
            return null;
        }
    }
    
    
    /**
     * 判断是否已登录
     */
    // 添加 @SuppressWarnings 注解，告诉 IDE “我知道我在做什么，别吵”。
    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public static boolean isLogin() {
        return StpUtil.isLogin();
    }
    
    
    /**
     * 辅助：安全解析 Token 中的 ID 部分
     */
    private static Long parseIdFromToken(String loginId) {
        if (StrUtil.isBlank(loginId) || !loginId.contains(":")) {
            return null;
        }
        String[] parts = loginId.split(":");
        // 确保 ID 部分是纯数字
        if (parts.length == 2 && StrUtil.isNumeric(parts[1])) {
            return Long.parseLong(parts[1]);
        }
        return null;
    }
    
    
    /**
     * 🟢 [新增] 获取客户端 IP 地址
     * 优先从 Sa-Token 上下文中获取，兼容 Web 和非 Web 环境
     */
    public static String getClientIP() {
        try {
            // 1. 尝试从 Sa-Token 请求上下文中获取
            String ip = SaHolder.getRequest().getHeader("X-Forwarded-For");
            if (StrUtil.isBlank(ip) || "unknown".equalsIgnoreCase(ip)) {
                ip = SaHolder.getRequest().getHeader("X-Real-IP");
            }
            if (StrUtil.isBlank(ip) || "unknown".equalsIgnoreCase(ip)) {
                // Sa-Token 提供了封装好的方法 getHost() ，用于获取直连 IP (String 类型)
                ip = SaHolder.getRequest().getHost();
            }
            // 处理多级代理的情况，取第一个非 unknown 的 IP
            if (StrUtil.isNotBlank(ip) && ip.contains(",")) {
                return ip.split(",")[0].trim();
            }
            return ip;
        } catch (Exception e) {
            // 如果不在 Web 上下文中（比如定时任务），返回默认值
            return "127.0.0.1";
        }
    }
    
    
    /**
     * 获取当前用户的角色权限字符
     * 例如："super_admin" 或 "student" 或 "dorm_manager"
     */
    public static String getRoleKey() {
        try {
            if (!isLogin()) {
                return null;
            }
            // 从 Session 中取出登录时存入的 "role" 字段
            SaSession session = StpUtil.getSession(false);
            if (session != null) {
                return session.getString("role");
            }
            return null;
        } catch (Exception e) {
            return null;
        }
    }
}