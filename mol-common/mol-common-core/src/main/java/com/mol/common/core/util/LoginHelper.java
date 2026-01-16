package com.mol.common.core.util;

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
     * 获取当前登录用户 ID
     * <p>
     * 🛡️ 防刁民设计：
     * 1. 优先读 Session (速度快，数据准)
     * 2. 兜底读 Token (无状态)，并进行 Try-Catch 容错，防止恶意 Token 导致 Long 解析异常
     * </p>
     *
     * @return userId (Long) 或 null (未登录/解析失败)
     */
    public static Long getUserId() {
        try {
            // 1. 尝试从 Session 获取 (登录时已写入)
            Object sessionVal = StpUtil.getSessionByLoginId(StpUtil.getLoginIdDefaultNull(), false)
                    .get("originalId");
            if (sessionVal != null) {
                return Convert.toLong(sessionVal);
            }
            
            // 2. 兜底：解析 Token 字符串 (格式 "Type:ID")
            String loginId = StpUtil.getLoginIdAsString();
            return parseIdFromToken(loginId);
        } catch (Exception e) {
            // 静默失败，不抛出 500 异常给前端，直接认为未登录
            return null;
        }
    }
    
    /**
     * 获取当前用户类型
     * @return 0-管理员, 1-普通用户, null-未知
     */
    public static Integer getUserType() {
        try {
            String loginId = StpUtil.getLoginIdAsString();
            if (StrUtil.isBlank(loginId) || !loginId.contains(":")) {
                return null;
            }
            // "0:1001" -> 0
            return Integer.parseInt(loginId.split(":")[0]);
        } catch (Exception e) {
            return null;
        }
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
    
    public static boolean isLogin() {
        return StpUtil.isLogin();
    }
}