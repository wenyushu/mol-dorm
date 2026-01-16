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
 * Sa-Token 权限加载实现类 (兼职辅导员适配版)
 * <p>
 * 核心逻辑：
 * 1. 管理员：物理隔离，仅查 sys_user_role
 * 2. 普通用户：
 * - 基础身份：根据 user_category 自动赋予 (学生/教工)
 * - 兼职身份：允许通过 sys_user_role 表给学生叠加 "college_teacher" 等管理角色
 * </p>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class StpInterfaceImpl implements StpInterface {
    
    private final SysUserRoleMapper userRoleMapper;
    private final SysOrdinaryUserMapper ordinaryUserMapper;
    
    private static final int TYPE_ADMIN = 0;
    private static final int TYPE_ORDINARY = 1;
    
    @Override
    public List<String> getPermissionList(Object loginId, String loginType) {
        List<String> permissions = new ArrayList<>();
        List<String> roleList = getRoleList(loginId, loginType);
        if (roleList.contains(RoleConstants.SUPER_ADMIN)) {
            permissions.add("*");
        }
        return permissions;
    }
    
    @Override
    public List<String> getRoleList(Object loginId, String loginType) {
        String loginIdStr = (String) loginId;
        if (StrUtil.isBlank(loginIdStr) || !loginIdStr.contains(":")) return Collections.emptyList();
        
        String[] parts = loginIdStr.split(":");
        if (parts.length != 2) return Collections.emptyList();
        
        int userType;
        long userId;
        try {
            userType = Integer.parseInt(parts[0]);
            userId = Long.parseLong(parts[1]);
        } catch (NumberFormatException e) {
            return Collections.emptyList();
        }
        
        List<String> roles = new ArrayList<>();
        
        // -----------------------------------------------------------
        // 场景 A: 系统管理员 (后台人员)
        // -----------------------------------------------------------
        if (userType == TYPE_ADMIN) {
            if (userId == 1L) {
                roles.add(RoleConstants.SUPER_ADMIN);
                return roles;
            }
            // 纯管理人员，角色完全来自数据库配置
            List<String> dbRoles = userRoleMapper.selectRoleKeysByUserId(userId);
            if (dbRoles != null) roles.addAll(dbRoles);
        }
        
        // -----------------------------------------------------------
        // 场景 B: 普通用户 (学生/兼职辅导员/教工)
        // -----------------------------------------------------------
        else if (userType == TYPE_ORDINARY) {
            SysOrdinaryUser user = ordinaryUserMapper.selectById(userId);
            
            // 1. 账号状态检查 (防离职/毕业后未注销)
            if (user == null || "1".equals(user.getStatus())) {
                return Collections.emptyList();
            }
            
            // 2. 【基础身份】(Base Role)
            // 这是写死在代码里的，由账号属性决定，不可剥夺
            if (user.getUserCategory() == 0) {
                roles.add(RoleConstants.STUDENT); // 基础：学生
            } else if (user.getUserCategory() == 1) {
                roles.add(RoleConstants.COLLEGE_TEACHER); // 基础：教工
            }
            
            // 3. 【兼职/叠加身份】(Extra Role)
            // 允许管理员在 sys_user_role 表中给这个学生 ID 绑定额外的角色
            // 例如：给李小牧 (Student) 绑定 "college_teacher" 或 "counselor" 角色
            List<String> extraRoles = userRoleMapper.selectRoleKeysByUserId(userId);
            if (extraRoles != null && !extraRoles.isEmpty()) {
                // 这里不需要过滤，直接叠加。
                // 结果示例：["student", "college_teacher"]
                roles.addAll(extraRoles);
            }
        }
        
        return roles;
    }
}