package com.mol.common.core.util;

import cn.dev33.satoken.context.SaHolder;
import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.util.StrUtil;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * 登录鉴权助手
 * <p>
 * 解决 StpUtil.getLoginIdAsLong() 无法解析 "PREFIX:ID" 格式的问题
 * 统一封装用户ID、用户类型的获取逻辑
 * </p>
 *
 * @author mol
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class LoginHelper {
    
    /**
     * 获取当前登录用户 ID (Long)
     * <p>
     * 自动解析 "0:1001" -> 1001
     * </p>
     *
     * @return userId 或 null (未登录)
     */
    public static Long getUserId() {
        try {
            // 1. 尝试从 Session 获取 (性能最优，AuthServiceImpl 登录时已写入)
            // 需确保 AuthServiceImpl 中有: StpUtil.getSession().set("originalId", userId);
            Object sessionVal = StpUtil.getSessionByLoginId(StpUtil.getLoginIdDefaultNull(), false)
                    .get("originalId");
            if (sessionVal != null) {
                return Long.valueOf(sessionVal.toString());
            }
            
            // 2. 兜底：如果 Session 没取到，解析 Token (无状态模式下常用)
            String loginId = StpUtil.getLoginIdAsString();
            if (StrUtil.isBlank(loginId) || !loginId.contains(":")) {
                return null;
            }
            // 分割 "0:1001" 取第2部分
            String[] parts = loginId.split(":");
            return Long.parseLong(parts[1]);
        } catch (Exception e) {
            return null;
        }
    }
    
    /**
     * 获取当前登录用户类型
     *
     * @return 0-管理员, 1-普通用户, null-未知
     */
    public static Integer getUserType() {
        try {
            String loginId = StpUtil.getLoginIdAsString();
            if (StrUtil.isBlank(loginId) || !loginId.contains(":")) {
                return null;
            }
            return Integer.parseInt(loginId.split(":")[0]);
        } catch (Exception e) {
            return null;
        }
    }
    
    /**
     * 获取当前登录用户名 (RealName)
     */
    public static String getUsername() {
        try {
            return (String) StpUtil.getSession().get("name");
        } catch (Exception e) {
            return null;
        }
    }
    
    /**
     * 是否已登录
     */
    public static boolean isLogin() {
        return StpUtil.isLogin();
    }
}