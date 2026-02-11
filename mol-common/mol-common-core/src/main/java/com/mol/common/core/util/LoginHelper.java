package com.mol.common.core.util;

import cn.dev33.satoken.context.SaHolder;
import cn.dev33.satoken.session.SaSession;
import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.convert.Convert;
import cn.hutool.core.util.StrUtil;
import com.mol.common.core.constant.RoleConstants; // 🟢 引入常量
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
     * 判断当前用户是否为超级管理员
     * 作用：用于业务代码中的越权判断 (如：维修工单完工、强制退宿等)
     */
    public static boolean isAdmin() {
        try {
            // 复用 getRoleKey() 方法
            String roleKey = getRoleKey();
            // 比对 "super_admin"
            return RoleConstants.SUPER_ADMIN.equals(roleKey);
        } catch (Exception e) {
            return false;
        }
    }
    
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
            
            // 2. 兜底：如果 Session 没取到 (极端情况)，解析 Token 字符串
            // 假设 LoginId 格式为 "Type:ID" (如 "0:10001")
            String loginId = StpUtil.getLoginIdAsString();
            return parseIdFromToken(loginId);
            
        } catch (Exception e) {
            // 🛡️ 静默失败，不抛出 500 异常给前端
            return null;
        }
    }
    
    /**
     * 获取当前用户类型/角色 Key (全能进化版)
     * 🛡️ 核心职能：自动识别当前登录者是学生、老师、维修人员还是超级管理员。
     *
     * @return 对应 RoleConstants 中的常量值 (如: "student", "repair_master", "super_admin")
     */
    public static String getUserType() {
        try {
            if (!isLogin()) return null;
            
            // 1. 核心路径：直接从 Session 获取登录时缓存的 roleKey
            // 这是最快且最准的，因为 roleKey 与 RoleConstants 严格对应
            String roleKey = getRoleKey();
            if (StrUtil.isNotBlank(roleKey)) {
                return roleKey;
            }
            
            // 2. 兜底路径：如果 Session 丢失，尝试从 LoginId 解析 (格式 "Type:ID")
            String loginId = StpUtil.getLoginIdAsString();
            if (loginId.contains(":")) {
                return loginId.split(":")[0];
            }
            return null;
        } catch (Exception e) {
            return null;
        }
    }
    
    /**
     * 🟢 [新增] 判断当前用户是否拥有指定角色
     * @param roleKey 传入 RoleConstants 中的常量
     */
    public static boolean hasRole(String roleKey) {
        String currentType = getUserType();
        return StrUtil.equals(roleKey, currentType);
    }
    
    /**
     * 🟢 [新增] 快捷判断：是否为学生
     */
    public static boolean isStudent() {
        return hasRole(RoleConstants.STUDENT);
    }
    
    /**
     * 🟢 [新增] 快捷判断：是否为维修人员
     */
    public static boolean isRepairMaster() {
        return hasRole(RoleConstants.REPAIR_MASTER);
    }
    
    /**
     * 🟢 [新增] 快捷判断：是否为宿管经理
     */
    public static boolean isDormManager() {
        return hasRole(RoleConstants.DORM_MANAGER);
    }
    
    
    /**
     * 判断是否已登录
     */
    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public static boolean isLogin() {
        return StpUtil.isLogin();
    }
    
    
    /**
     * 辅助：安全解析 Token 中的 ID 部分
     */
    private static Long parseIdFromToken(String loginId) {
        if (StrUtil.isBlank(loginId)) {
            return null;
        }
        // 如果包含冒号，取冒号后面部分
        if (loginId.contains(":")) {
            String[] parts = loginId.split(":");
            if (parts.length == 2 && StrUtil.isNumeric(parts[1])) {
                return Long.parseLong(parts[1]);
            }
        }
        // 如果纯数字 (兼容部分旧逻辑)，直接返回
        else if (StrUtil.isNumeric(loginId)) {
            return Long.parseLong(loginId);
        }
        return null;
    }
    
    
    /**
     * 获取客户端 IP 地址
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
                // Sa-Token 提供了封装好的方法 getHost()
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
    
    /**
     * 判断目标 ID 是否为当前登录用户本人
     * 用于：防止 A 用户通过 Postman 修改 B 用户的资料
     */
    public static boolean isSelf(Long targetUserId) {
        Long currentId = getUserId();
        return currentId != null && currentId.equals(targetUserId);
    }
    
    /**
     * 获取当前登录人负责的楼栋 ID
     * 🛡️ [防刁民逻辑]：
     * 1. 登录时，系统已将该宿管负责的 buildingId 写入 SaSession。
     * 2. 此处直接读取，避免了频繁查库，提高了驾驶舱的响应速度。
     */
    public static Long getManagedBuildingId() {
        try {
            if (!isLogin()) return null;
            
            // 直接从 Sa-Token 的 Session 中读取登录时缓存的楼栋信息
            SaSession session = StpUtil.getSession(false);
            if (session != null) {
                Object buildingId = session.get("buildingId");
                return buildingId != null ? Convert.toLong(buildingId) : null;
            }
            return null;
        } catch (Exception e) {
            return null;
        }
    }
}