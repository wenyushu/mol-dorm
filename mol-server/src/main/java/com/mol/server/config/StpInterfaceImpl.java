package com.mol.server.config;

import cn.dev33.satoken.stp.StpInterface;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.mol.common.core.constant.RoleConstants;
import com.mol.common.core.entity.SysOrdinaryUser;
import com.mol.server.mapper.SysOrdinaryUserMapper;
import com.mol.server.mapper.SysUserRoleMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * Sa-Token 权限加载实现类 (最终防刁民 & 混合身份适配版)
 * <p>
 * 核心职责：
 * 1. 根据 LoginId (格式 "type:id") 解析用户身份。
 * 2. 加载“基础身份”(学生/教工) 和 “兼职身份”(数据库配置)。
 * 3. 实现严格的账号状态检查，防止已封禁用户通过旧 Token 越权。
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
    
    // 必须与 AuthServiceImpl 中的 PREFIX 常量逻辑严格对应
    // 0:管理员, 1:普通用户
    private static final int TYPE_ADMIN = 0;
    private static final int TYPE_ORDINARY = 1;
    
    /**
     * 返回一个账号所拥有的权限码集合
     * <p>
     * 目前系统设计主要基于角色 (RBAC)，权限码暂时简化处理。
     * 如果是超级管理员，直接赋予 "*" (所有权限)。
     * </p>
     */
    @Override
    public List<String> getPermissionList(Object loginId, String loginType) {
        List<String> roleList = getRoleList(loginId, loginType);
        // 👑皇权特许：超级管理员拥有所有权限
        if (roleList.contains(RoleConstants.SUPER_ADMIN)) {
            return Collections.singletonList("*");
        }
        // 后续如果扩展 sys_menu 表，可在此处查库
        return Collections.emptyList();
    }
    
    /**
     * 返回一个账号所拥有的角色标识集合 (RoleKey)
     */
    @Override
    public List<String> getRoleList(Object loginId, String loginType) {
        // 1. 🛡️ 防刁民：基础参数清洗
        String loginIdStr = (String) loginId;
        if (StrUtil.isBlank(loginIdStr) || !loginIdStr.contains(":")) {
            return Collections.emptyList();
        }
        
        String[] parts = loginIdStr.split(":");
        if (parts.length != 2) return Collections.emptyList();
        
        int userType;
        long userId;
        try {
            userType = Integer.parseInt(parts[0]);
            userId = Long.parseLong(parts[1]);
        } catch (NumberFormatException e) {
            // 🛡️ 防刁民：如果 ID 格式被篡改为非数字，直接返回空权限，不报错泄露信息
            return Collections.emptyList();
        }
        
        // 使用 Set 去重 (防止代码添加了 "student"，数据库里又配了 "student")
        Set<String> roles = new HashSet<>();
        
        // ===========================================================
        // 场景 A: 系统管理员 (后台人员 - 0)
        // ===========================================================
        if (userType == TYPE_ADMIN) {
            // 👑 超管硬编码 (ID = 1)
            if (userId == 1L) {
                roles.add(RoleConstants.SUPER_ADMIN);
                return new ArrayList<>(roles);
            }
            // 普通管理员：查 sys_user_role 表
            // 注意：这里调用的是我们在 Mapper 中新加的 selectRoleKeysByUserId 方法
            List<String> dbRoles = userRoleMapper.selectRoleKeysByUserId(userId);
            if (CollUtil.isNotEmpty(dbRoles)) {
                roles.addAll(dbRoles);
            }
        }
        
        // ===========================================================
        // 场景 B: 普通用户 (学生/教工 - 1)
        // ===========================================================
        else if (userType == TYPE_ORDINARY) {
            // 1. 查用户信息
            SysOrdinaryUser user = ordinaryUserMapper.selectById(userId);
            
            // 2. 🛡️ 防刁民：账号状态实时检查
            // 即使 Token 有效，如果数据库中 status='1'(封禁)，也强制视为无权限
            if (user == null || "1".equals(user.getStatus())) {
                return Collections.emptyList();
            }
            
            // 3. 【基础身份】(不可剥夺)
            // 根据 user_category (0 学生, 1 教工) 自动赋予基础角色
            if (user.getUserCategory() != null) {
                if (user.getUserCategory() == 0) {
                    roles.add(RoleConstants.STUDENT);
                } else if (user.getUserCategory() == 1) {
                    roles.add(RoleConstants.COLLEGE_TEACHER);
                }
            }
            
            // 4. 【兼职/叠加身份】(Extra Roles)
            // 允许给学生叠加 "counselor"(辅导员) 或 "dorm_manager"(宿管) 等角色
            List<String> extraRoles = userRoleMapper.selectRoleKeysByUserId(userId);
            if (CollUtil.isNotEmpty(extraRoles)) {
                roles.addAll(extraRoles);
            }
        }
        
        return new ArrayList<>(roles);
    }
}