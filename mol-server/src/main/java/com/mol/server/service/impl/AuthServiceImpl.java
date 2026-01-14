package com.mol.server.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.crypto.digest.BCrypt;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.mol.common.core.entity.SysAdminUser;
import com.mol.common.core.entity.SysOrdinaryUser;

import com.mol.common.core.entity.SysUserRole;
import com.mol.common.core.exception.ServiceException;
import com.mol.server.dto.LoginBody;
import com.mol.server.entity.SysRole;
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
    
    // 定义ID前缀常量，防止硬编码
    private static final String PREFIX_ADMIN = "ADMIN:";
    private static final String PREFIX_STUDENT = "STU:";
    
    @Override
    public LoginVO login(LoginBody loginBody) {
        // 1. 校验参数
        String username = loginBody.getUsername();
        String password = loginBody.getPassword();
        String userType = loginBody.getUserType();
        
        if (ObjectUtil.hasEmpty(username, password, userType)) {
            throw new ServiceException("账号、密码或用户类型不能为空");
        }
        
        Long originalUserId; // 数据库原始 ID
        String loginId;      // Sa-Token 用的唯一登录标识
        String realName;
        String avatar;
        String roleKey;
        
        // 2. 根据用户类型查不同的表
        if ("admin".equals(userType)) {
            // ================== 管理员登录 ==================
            SysAdminUser admin = adminUserMapper.selectOne(new LambdaQueryWrapper<SysAdminUser>()
                    .eq(SysAdminUser::getUsername, username));
            
            if (admin == null) throw new ServiceException("管理员账号不存在");
            if (!BCrypt.checkpw(password, admin.getPassword())) throw new ServiceException("密码错误");
            if ("1".equals(admin.getStatus())) throw new ServiceException("账号已停用，请联系上级");
            
            originalUserId = admin.getId();
            // [Fix] 拼接前缀，解决ID冲突
            loginId = PREFIX_ADMIN + originalUserId;
            
            realName = admin.getRealName();
            avatar = admin.getAvatar();
            
            // 动态查询角色
            List<SysUserRole> userRoles = userRoleMapper.selectList(new LambdaQueryWrapper<SysUserRole>()
                    .eq(SysUserRole::getUserId, originalUserId));
            
            if (CollUtil.isNotEmpty(userRoles)) {
                List<Long> roleIds = userRoles.stream().map(SysUserRole::getRoleId).collect(Collectors.toList());
                List<SysRole> roles = roleMapper.selectByIds(roleIds);
                roleKey = roles.stream().map(SysRole::getRoleKey).collect(Collectors.joining(","));
            } else {
                roleKey = "guest";
            }
            
        } else {
            // ================== 普通用户登录 ==================
            SysOrdinaryUser user = ordinaryUserMapper.selectOne(new LambdaQueryWrapper<SysOrdinaryUser>()
                    .eq(SysOrdinaryUser::getUsername, username));
            
            if (user == null) throw new ServiceException("学号/工号不存在");
            if (!BCrypt.checkpw(password, user.getPassword())) throw new ServiceException("密码错误");
            if ("1".equals(user.getStatus())) throw new ServiceException("账号已停用，请联系宿管");
            
            originalUserId = user.getId();
            // [Fix] 拼接前缀
            loginId = PREFIX_STUDENT + originalUserId;
            
            realName = user.getRealName();
            avatar = null;
            roleKey = (user.getUserCategory() != null && user.getUserCategory() == 1) ? "teacher" : "student";
        }
        
        // 3. 执行 Sa-Token 登录 (使用带前缀的 ID)
        StpUtil.login(loginId);
        
        // 4. 缓存关键信息到 Session
        StpUtil.getSession().set("role", roleKey);
        StpUtil.getSession().set("name", realName);
        StpUtil.getSession().set("type", userType);
        StpUtil.getSession().set("originalId", originalUserId); // 缓存原始 ID 方便后续业务获取
        
        // 5. 返回结果
        LoginVO vo = new LoginVO();
        vo.setToken(StpUtil.getTokenValue());
        vo.setUserId(originalUserId); // 返回给前端的还是原始 ID
        vo.setRealName(realName);
        vo.setRole(roleKey);
        vo.setAvatar(avatar);
        
        return vo;
    }
}