package com.mol.dorm.biz.config;

import cn.dev33.satoken.stp.StpInterface;
import com.mol.common.core.constant.RoleConstants;
import com.mol.common.core.entity.SysOrdinaryUser;
import com.mol.sys.biz.mapper.SysOrdinaryUserMapper;
import com.mol.sys.biz.mapper.SysUserRoleMapper;
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
 *
 * @author mol
 */
@Slf4j
@Component // 必须交给 Spring 管理
@RequiredArgsConstructor
public class StpInterfaceImpl implements StpInterface {
    
    private final SysUserRoleMapper userRoleMapper;
    private final SysOrdinaryUserMapper ordinaryUserMapper;
    
    // 用户类型常量 (0:管理员, 1:普通用户)
    // 必须与 AuthService 登录时构建 LoginId 的逻辑保持一致
    private static final int TYPE_ADMIN = 0;
    private static final int TYPE_ORDINARY = 1;
    
    /**
     * 返回一个账号所拥有的权限码集合 (Permissions)
     * <p>
     * 例如：user:add, user:delete
     * 目前系统主要基于【角色】(Role) 鉴权，权限码暂时留空。
     * 如果是超级管理员，也可以返回 "*" 表示所有权限。
     * </p>
     */
    @Override
    public List<String> getPermissionList(Object loginId, String loginType) {
        // 如果需要给超管最高权限，可以这样写：

        List<String> roleList = getRoleList(loginId, loginType);
        if (roleList.contains(RoleConstants.SUPER_ADMIN)) {
            return Collections.singletonList("*");
        }

        return new ArrayList<>();
    }
    
    /**
     * 返回一个账号所拥有的角色标识集合 (Roles)
     * <p>
     * 核心鉴权逻辑：解析 LoginId -> 判断用户类型 -> 查询对应的表或硬编码
     * loginId 格式示例: "0:1" (超管), "1:60001" (学生)
     * </p>
     */
    @Override
    public List<String> getRoleList(Object loginId, String loginType) {
        String loginIdStr = (String) loginId;
        
        // 1. 安全校验：LoginId 格式必须正确
        if (loginIdStr == null || !loginIdStr.contains(":")) {
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
            log.error("LoginId 解析失败: {}", loginIdStr);
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
                return roles;
            }
            
            // 【普通管理员】(如宿管、辅导员)
            // 查询 sys_user_role 关联表获取角色 Key (例如: "dorm_manager", "counselor")
            // 注意：selectRoleKeysByUserId 方法需要在 SysUserRoleMapper 中自定义实现
            List<String> dbRoles = userRoleMapper.selectRoleKeysByUserId(userId);
            if (dbRoles != null && !dbRoles.isEmpty()) {
                roles.addAll(dbRoles);
            }
            
        } else if (userType == TYPE_ORDINARY) {
            // ==========================================
            // 情况 B: 普通用户体系 (SysOrdinaryUser)
            // ==========================================
            
            // 普通用户通常不配置 sys_user_role 表，而是根据用户属性直接映射
            SysOrdinaryUser user = ordinaryUserMapper.selectById(userId);
            if (user != null && user.getUserCategory() != null) {
                // user_category: 0-学生, 1-职工
                if (user.getUserCategory() == 0) {
                    roles.add(RoleConstants.STUDENT);
                } else if (user.getUserCategory() == 1) {
                    roles.add(RoleConstants.STAFF);
                }
            }
        }
        
        return roles;
    }
}