package com.mol.server.config;

import cn.dev33.satoken.stp.StpInterface;
import cn.hutool.core.util.StrUtil;
import com.mol.common.core.constant.RoleConstants;
import com.mol.common.core.entity.SysOrdinaryUser;
import com.mol.server.mapper.SysOrdinaryUserMapper;
import com.mol.server.mapper.SysUserRoleMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Sa-Token 自定义权限加载接口实现类
 * <p>
 * 核心职能：
 * 在用户登录验证通过后，Sa-Token 会回调此接口，
 * 获取该用户拥有的【角色列表】和【权限列表】，用于 @SaCheckRole 等注解的鉴权。
 * </p>
 * * 对应业务场景：
 * 1. 学生提交申请 -> 需要 "student" 角色 (由此类自动赋予)
 * 2. 辅导员/宿管审批 -> 需要 "counselor"/"dorm_manager" 角色 (从数据库加载)
 * 3. 超管强制操作 -> 需要 "super_admin" 角色 (硬编码兜底 + 数据库)
 *
 * @author mol
 */
@Slf4j
@Component // 必须交给 Spring 管理，否则 Sa-Token 无法扫描到
@RequiredArgsConstructor
public class StpInterfaceImpl implements StpInterface {
    
    private final SysUserRoleMapper userRoleMapper;
    private final SysOrdinaryUserMapper ordinaryUserMapper;
    
    // 用户类型常量 (0:管理员, 1:普通用户)
    // 必须与 AuthService.login() 中构建 LoginId 的逻辑 ("0:123" 或 "1:456") 保持一致
    private static final int TYPE_ADMIN = 0;
    private static final int TYPE_ORDINARY = 1;
    
    /**
     * 返回一个账号所拥有的【权限码】集合 (Permissions)
     * <p>
     * 目前系统主要基于【角色】(Role) 鉴权，权限码暂时作为补充。
     * 策略：如果是超级管理员，直接返回 "*" (拥有所有权限)。
     * </p>
     */
    @Override
    public List<String> getPermissionList(Object loginId, String loginType) {
        List<String> permissions = new ArrayList<>();
        
        // 1. 获取该用户拥有的角色
        List<String> roleList = getRoleList(loginId, loginType);
        
        // 2. 如果包含超级管理员角色，赋予所有权限
        if (roleList.contains(RoleConstants.SUPER_ADMIN)) {
            permissions.add("*");
        }
        
        // 3. (可选) 这里可以继续查 sys_role_menu 表加载细粒度权限
        // permissions.addAll(menuMapper.selectPermsByUserId(...));
        
        return permissions;
    }
    
    /**
     * 返回一个账号所拥有的【角色标识】集合 (Roles)
     * <p>
     * 核心鉴权逻辑：解析 LoginId -> 判断用户类型 -> 查询对应的表或硬编码赋予
     * </p>
     * * @param loginId 登录ID，格式约定为 "UserType:UserId" (例如 "0:1", "1:1005")
     * @param loginType 登录体系标识 (通常是 "login")
     * @return 角色列表 (例如 ["student"], ["dorm_manager", "counselor"])
     */
    @Override
    public List<String> getRoleList(Object loginId, String loginType) {
        String loginIdStr = (String) loginId;
        
        // 1. 安全校验：LoginId 格式必须正确，防止解析异常
        if (StrUtil.isBlank(loginIdStr) || !loginIdStr.contains(":")) {
            return Collections.emptyList();
        }
        
        String[] parts = loginIdStr.split(":");
        if (parts.length != 2) {
            return Collections.emptyList();
        }
        
        int userType;
        long userId;
        try {
            userType = Integer.parseInt(parts[0]);
            userId = Long.parseLong(parts[1]);
        } catch (NumberFormatException e) {
            log.error("Sa-Token 鉴权失败，LoginId 格式错误: {}", loginIdStr);
            return Collections.emptyList();
        }
        
        List<String> roles = new ArrayList<>();
        
        // 2. 根据用户类型分流处理
        if (userType == TYPE_ADMIN) {
            // ==========================================
            // 情况 A: 管理员体系 (SysAdminUser)
            // ==========================================
            
            // 【特权兜底】如果是 ID=1 的超级管理员，直接赋予 SuperAdmin 角色
            // 优势：即使数据库 sys_user_role 表数据丢失，超管依然能登录系统进行修复
            if (userId == 1L) {
                roles.add(RoleConstants.SUPER_ADMIN);
                // 超管通常也拥有所有其他角色的能力，如果需要也可以不 return，继续往下加
                return roles;
            }
            
            // 【普通管理员】(如宿管、辅导员、后勤部长)
            // 逻辑：查询 sys_user_role 中间表 -> 关联 sys_role 表 -> 获取 role_key
            // 对应 SQL: SELECT r.role_key FROM sys_role r ... WHERE ur.user_id = ?
            List<String> dbRoles = userRoleMapper.selectRoleKeysByUserId(userId);
            if (dbRoles != null && !dbRoles.isEmpty()) {
                roles.addAll(dbRoles);
            }
            
        } else if (userType == TYPE_ORDINARY) {
            // ==========================================
            // 情况 B: 普通用户体系 (SysOrdinaryUser)
            // ==========================================
            
            // 普通用户通常不配置 sys_user_role 表，而是根据【人员类别】属性直接映射
            // 这样可以减少数据库维护成本，学生注册即自动拥有 student 角色
            SysOrdinaryUser user = ordinaryUserMapper.selectById(userId);
            if (user != null && user.getUserCategory() != null) {
                // user_category: 0-学生, 1-教职工
                if (user.getUserCategory() == 0) {
                    roles.add(RoleConstants.STUDENT); // 赋予 "student" 角色
                } else if (user.getUserCategory() == 1) {
                    roles.add(RoleConstants.STAFF);   // 赋予 "staff" 角色
                }
            }
        }
        
        // 打印日志方便调试 (生产环境可调整级别)
        // log.debug("用户 [{}] 加载角色: {}", loginIdStr, roles);
        
        return roles;
    }
}