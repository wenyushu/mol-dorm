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
 * 核心职能：用户登录后，计算该用户拥有哪些【角色】和【权限】。
 * 包含“防刁民”设计：物理隔离管理员与普通用户，防止越权。
 * </p>
 *
 * @author mol
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class StpInterfaceImpl implements StpInterface {
    
    private final SysUserRoleMapper userRoleMapper;
    private final SysOrdinaryUserMapper ordinaryUserMapper;
    
    // 必须与 AuthServiceImpl 中的前缀严格一致 (0:管理员, 1:普通用户)
    private static final int TYPE_ADMIN = 0;
    private static final int TYPE_ORDINARY = 1;
    
    /**
     * 获取权限列表 (Permissions)
     */
    @Override
    public List<String> getPermissionList(Object loginId, String loginType) {
        List<String> permissions = new ArrayList<>();
        
        // 1. 先获取角色
        List<String> roleList = getRoleList(loginId, loginType);
        
        // 2. 【特权兜底】如果是超级管理员，赋予 "*" (所有权限)
        // 防刁民：只有持有 super_admin 角色的账号才能触发，普通学生无法触达
        if (roleList.contains(RoleConstants.SUPER_ADMIN)) {
            permissions.add("*");
        }
        
        return permissions;
    }
    
    /**
     * 获取角色列表 (Roles)
     * 🛡️ 核心防守区域
     */
    @Override
    public List<String> getRoleList(Object loginId, String loginType) {
        String loginIdStr = (String) loginId;
        
        // -----------------------------------------------------------
        // 🛡️ 防守层1：格式熔断
        // 如果 ID 为空或格式不对 (没有冒号)，直接返回空，防止恶意攻击导致空指针或解析报错
        // -----------------------------------------------------------
        if (StrUtil.isBlank(loginIdStr) || !loginIdStr.contains(":")) {
            return Collections.emptyList();
        }
        
        // 解析 ID 结构 "Type:Id"
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
            // 🛡️ 防守层2：异常静默
            // 如果有人恶意传字母进来，捕获异常并返回空，不给前端抛 500
            log.warn("Sa-Token 鉴权格式异常，疑似恶意请求: {}", loginIdStr);
            return Collections.emptyList();
        }
        
        List<String> roles = new ArrayList<>();
        
        // -----------------------------------------------------------
        // 🛡️ 防守层3：身份物理隔离
        // 管理员走管理员的门，学生走学生的门。学生绝对进不了管理员的逻辑。
        // -----------------------------------------------------------
        
        if (userType == TYPE_ADMIN) {
            // ================== 管理员逻辑 ==================
            
            // 1. 超管特权 (ID=1 永远是超管，防数据库被删)
            if (userId == 1L) {
                roles.add(RoleConstants.SUPER_ADMIN);
                return roles;
            }
            
            // 2. 普通管理员：查 sys_user_role 表
            List<String> dbRoles = userRoleMapper.selectRoleKeysByUserId(userId);
            if (dbRoles != null && !dbRoles.isEmpty()) {
                roles.addAll(dbRoles);
            }
            
        } else if (userType == TYPE_ORDINARY) {
            // ================== 普通用户逻辑 ==================
            
            // 🛡️ 防内鬼设计：
            // 普通用户的角色完全由代码逻辑决定，【不查】sys_user_role 表。
            // 即使数据库里有人恶意给学生插了一条 "admin" 的角色关联，这里也不会生效。
            
            SysOrdinaryUser user = ordinaryUserMapper.selectById(userId);
            if (user != null && user.getUserCategory() != null) {
                // 0-学生
                if (user.getUserCategory() == 0) {
                    roles.add(RoleConstants.STUDENT);
                }
                // 1-教职工/辅导员
                else if (user.getUserCategory() == 1) {
                    roles.add("teacher"); // 需确保常量一致
                }
            }
        }
        
        return roles;
    }
}