package com.mol.server.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.crypto.digest.BCrypt;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.mol.common.core.entity.SysAdminUser;
import com.mol.common.core.entity.SysOrdinaryUser;
import com.mol.common.core.entity.SysRole;
import com.mol.common.core.entity.SysUserRole;
import com.mol.common.core.exception.ServiceException;
import com.mol.server.dto.LoginBody;
import com.mol.server.mapper.SysAdminUserMapper;
import com.mol.server.mapper.SysOrdinaryUserMapper;
import com.mol.server.mapper.SysRoleMapper;
import com.mol.server.mapper.SysUserRoleMapper;
import com.mol.server.service.AuthService;
import com.mol.server.vo.LoginVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 认证服务实现
 * <p>
 * 负责统一登录逻辑，支持管理员和普通用户(学生/老师)
 * </p>
 *
 * @author mol
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {
    
    private final SysAdminUserMapper adminUserMapper;
    private final SysOrdinaryUserMapper ordinaryUserMapper;
    private final SysUserRoleMapper userRoleMapper;
    private final SysRoleMapper roleMapper;
    
    /**
     * 统一登录接口
     */
    @Override
    public LoginVO login(LoginBody loginBody) {
        // 1. 校验参数
        String username = loginBody.getUsername();
        String password = loginBody.getPassword();
        String userType = loginBody.getUserType();
        
        if (ObjectUtil.hasEmpty(username, password, userType)) {
            throw new ServiceException("账号、密码或用户类型不能为空");
        }
        
        Long userId;
        String realName;
        String avatar;
        String roleKey;
        
        // 2. 根据用户类型查不同的表
        if ("admin".equals(userType)) {
            // ================== 管理员登录 ==================
            SysAdminUser admin = adminUserMapper.selectOne(new LambdaQueryWrapper<SysAdminUser>()
                    .eq(SysAdminUser::getUsername, username));
            
            if (admin == null) {
                throw new ServiceException("管理员账号不存在");
            }
            if (!BCrypt.checkpw(password, admin.getPassword())) {
                throw new ServiceException("密码错误");
            }
            if ("1".equals(admin.getStatus())) {
                throw new ServiceException("账号已停用，请联系上级");
            }
            
            userId = admin.getId();
            realName = admin.getRealName();
            avatar = admin.getAvatar();
            
            // 从数据库动态查询角色 (RBAC)
            // 步骤A: 查 sys_user_role 关联表
            List<SysUserRole> userRoles = userRoleMapper.selectList(new LambdaQueryWrapper<SysUserRole>()
                    .eq(SysUserRole::getUserId, userId));
            
            if (CollUtil.isNotEmpty(userRoles)) {
                // 步骤B: 提取所有 roleId
                List<Long> roleIds = userRoles.stream()
                        .map(SysUserRole::getRoleId)
                        .collect(Collectors.toList());
                
                // 步骤C: 查 sys_role 表获取 roleKey
                List<SysRole> roles = roleMapper.selectByIds(roleIds);
                
                // 步骤D: 拼接为字符串 (如 "super_admin,dorm_manager")
                roleKey = roles.stream()
                        .map(SysRole::getRoleKey)
                        .collect(Collectors.joining(","));
            } else {
                // 如果没分配角色，给一个默认值防止报错
                roleKey = "guest";
            }
            
        } else {
            // ================== 普通用户登录 ==================
            SysOrdinaryUser user = ordinaryUserMapper.selectOne(new LambdaQueryWrapper<SysOrdinaryUser>()
                    .eq(SysOrdinaryUser::getUsername, username));
            
            if (user == null) {
                throw new ServiceException("学号/工号不存在");
            }
            if (!BCrypt.checkpw(password, user.getPassword())) {
                throw new ServiceException("密码错误");
            }
            if ("1".equals(user.getStatus())) {
                throw new ServiceException("账号已停用，请联系宿管");
            }
            
            userId = user.getId();
            realName = user.getRealName();
            avatar = null;
            // 学生/教工的角色逻辑比较简单，直接根据 userCategory 判断
            roleKey = (user.getUserCategory() != null && user.getUserCategory() == 1) ? "teacher" : "student";
        }
        
        // 3. 执行 Sa-Token 登录
        StpUtil.login(userId);
        
        // 4. 缓存关键信息到 Session
        StpUtil.getSession().set("role", roleKey);
        StpUtil.getSession().set("name", realName);
        StpUtil.getSession().set("type", userType);
        
        // 5. 返回结果
        LoginVO vo = new LoginVO();
        vo.setToken(StpUtil.getTokenValue());
        vo.setUserId(userId);
        vo.setRealName(realName);
        vo.setRole(roleKey);
        vo.setAvatar(avatar);
        
        return vo;
    }
}