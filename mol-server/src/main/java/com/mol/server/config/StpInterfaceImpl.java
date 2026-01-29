package com.mol.server.config;

import cn.dev33.satoken.stp.StpInterface;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.mol.common.core.constant.RoleConstants;
import com.mol.common.core.entity.SysAdminUser;
import com.mol.common.core.entity.SysOrdinaryUser;
import com.mol.common.core.entity.SysUserRole;
import com.mol.server.entity.SysRole;
import com.mol.server.mapper.SysAdminUserMapper;
import com.mol.server.mapper.SysOrdinaryUserMapper;
import com.mol.server.mapper.SysRoleMapper;
import com.mol.server.mapper.SysUserRoleMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Sa-Token 权限加载实现类 (最终防刁民 & 混合身份适配版)
 * <p>
 * 核心职责：
 * 1. 解析 LoginId (格式 "type:id")。
 * 2. 实时查库加载“基础身份” + “扩展身份”。
 * 3. 🛡️ 严格的账号状态检查：防止已封禁用户利用旧 Token 越权。
 * </p>
 *
 * @author mol
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class StpInterfaceImpl implements StpInterface {
    
    private final SysUserRoleMapper userRoleMapper;
    private final SysRoleMapper roleMapper;
    private final SysOrdinaryUserMapper ordinaryUserMapper;
    private final SysAdminUserMapper adminUserMapper; // 必须注入管理员Mapper以检查状态
    
    // 必须与 AuthServiceImpl 中的 PREFIX 常量逻辑严格对应
    // 0:管理员, 1:普通用户
    private static final int TYPE_ADMIN = 0;
    private static final int TYPE_ORDINARY = 1;
    
    /**
     * 返回一个账号所拥有的权限码集合
     * <p>
     * 策略：如果是超级管理员，直接赋予 "*" (皇权特许)。
     * 其他角色目前暂未启用细粒度权限码 (menu权限)，返回空即可。
     * </p>
     */
    @Override
    public List<String> getPermissionList(Object loginId, String loginType) {
        List<String> roleList = getRoleList(loginId, loginType);
        // 👑 皇权特许：超级管理员拥有所有权限
        if (roleList.contains(RoleConstants.SUPER_ADMIN)) {
            return Collections.singletonList("*");
        }
        return Collections.emptyList();
    }
    
    /**
     * 返回一个账号所拥有的角色标识集合 (RoleKey)
     * <p>
     * 逻辑：基础身份(User表) + 扩展身份(Role表)
     * </p>
     */
    @Override
    public List<String> getRoleList(Object loginId, String loginType) {
        // 1. 🛡️ [防刁民] 基础参数清洗
        // 防止传入 null 或者不带冒号的非法 ID 导致数组越界
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
            // 🛡️ [防刁民] 如果 ID 格式被恶意篡改为非数字，直接静默返回空，不给黑客任何报错提示
            return Collections.emptyList();
        }
        
        // 使用 Set 去重 (防止代码添加了 "student"，数据库扩展里又配了 "student")
        Set<String> roles = new HashSet<>();
        
        // ===========================================================
        // 场景 A: 系统管理员 (后台人员 - 0)
        // ===========================================================
        if (userType == TYPE_ADMIN) {
            // A1. 查库获取管理员信息
            SysAdminUser admin = adminUserMapper.selectById(userId);
            
            // 🛡️ [防刁民] 即使 Token 有效，如果管理员被物理删除或禁用，强制无权
            if (admin == null || "1".equals(admin.getStatus())) {
                log.warn("已封禁管理员尝试访问: {}", userId);
                return Collections.emptyList();
            }
            
            // A2. 👑 超管硬编码 (ID = 1 的用户永远是超管)
            if (userId == 1L) {
                roles.add(RoleConstants.SUPER_ADMIN);
            }
            
            // A3. 普通管理员：加载 sys_user_role 表中的角色
            roles.addAll(getDbRoles(userId));
        }
        
        // ===========================================================
        // 场景 B: 普通用户 (学生/教工 - 1)
        // ===========================================================
        else if (userType == TYPE_ORDINARY) {
            // B1. 查库获取用户信息
            SysOrdinaryUser user = ordinaryUserMapper.selectById(userId);
            
            // 🛡️ [防刁民] 账号状态实时检查
            if (user == null || "1".equals(user.getStatus())) {
                log.warn("已封禁用户尝试访问: {}", userId);
                return Collections.emptyList();
            }
            
            // B2. 【基础身份】(Intrinsic Role) - 不可剥夺
            // 根据 user_category (0:学生, 1:教工) 自动赋予基础角色
            // 这一步解决了 A 问题：无需在 AuthServiceImpl 里硬编码，而是在鉴权时动态判定
            if (user.getUserCategory() != null) {
                if (user.getUserCategory() == 0) {
                    roles.add(RoleConstants.STUDENT);
                } else if (user.getUserCategory() == 1) {
                    roles.add(RoleConstants.COLLEGE_TEACHER);
                }
            }
            
            // B3. 【扩展身份】(Extrinsic Roles) - 叠加 Buff
            // 允许给学生叠加 "counselor"(辅导员助理) 或 "dorm_manager"(层长) 等角色
            roles.addAll(getDbRoles(userId));
        }
        
        // 返回 List
        return new ArrayList<>(roles);
    }
    
    /**
     * 辅助方法：从数据库加载扩展角色
     * 使用 MP 标准查询，避免 XML SQL 依赖
     */
    private List<String> getDbRoles(Long userId) {
        // 1. 查关联表 sys_user_role
        List<SysUserRole> userRoles = userRoleMapper.selectList(Wrappers.<SysUserRole>lambdaQuery()
                .eq(SysUserRole::getUserId, userId));
        
        if (CollUtil.isEmpty(userRoles)) {
            return Collections.emptyList();
        }
        
        // 2. 提取 RoleId 列表
        List<Long> roleIds = userRoles.stream()
                .map(SysUserRole::getRoleId)
                .collect(Collectors.toList());
        
        // 3. 查角色表 sys_role 获取 role_key (例如 "dorm_manager")
        // status = 0 表示启用
        List<SysRole> sysRoles = roleMapper.selectList(Wrappers.<SysRole>lambdaQuery()
                .in(SysRole::getId, roleIds)
                .eq(SysRole::getStatus, "0")); // 🛡️ 只能加载已启用的角色
        
        return sysRoles.stream()
                .map(SysRole::getRoleKey)
                .filter(StrUtil::isNotBlank)
                .collect(Collectors.toList());
    }
}