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
 * 认证服务实现类
 * <p>
 * 核心职责：
 * 1. 校验账号密码
 * 2. 构建符合 Sa-Token 规范的 LoginId (前缀:ID)
 * 3. 写入 Session 供 LoginHelper 使用
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
    
    // =========================================================================
    // 【关键修改】前缀必须与 StpInterfaceImpl 中的 TYPE 常量保持一致 (数字字符串)
    // 之前是 "ADMIN:"/"STU:"，现在改为 "0:"/"1:"，否则 StpInterfaceImpl 解析会报错
    // =========================================================================
    private static final String PREFIX_ADMIN = "0:";    // 0 代表管理员
    private static final String PREFIX_ORDINARY = "1:"; // 1 代表普通用户 (学生/教工)
    
    @Override
    public LoginVO login(LoginBody loginBody) {
        // 1. 基础参数校验
        String username = loginBody.getUsername();
        String password = loginBody.getPassword();
        String userType = loginBody.getUserType();
        
        if (ObjectUtil.hasEmpty(username, password, userType)) {
            throw new ServiceException("账号、密码或用户类型不能为空");
        }
        
        Long originalUserId; // 数据库真实 ID (例如 1001)
        String loginId;      // Sa-Token 登录 ID (例如 "0:1001")
        String realName;
        String avatar;
        String roleKey;      // 返回给前端展示用的角色标识
        
        // 2. 根据用户类型查不同的表
        if ("admin".equals(userType)) {
            // ================== 管理员登录逻辑 ==================
            SysAdminUser admin = adminUserMapper.selectOne(new LambdaQueryWrapper<SysAdminUser>()
                    .eq(SysAdminUser::getUsername, username));
            
            if (admin == null) throw new ServiceException("管理员账号不存在");
            if (!BCrypt.checkpw(password, admin.getPassword())) throw new ServiceException("密码错误");
            if ("1".equals(admin.getStatus())) throw new ServiceException("账号已停用，请联系上级");
            
            originalUserId = admin.getId();
            // 构建带前缀的 ID (0:1001)
            loginId = PREFIX_ADMIN + originalUserId;
            
            realName = admin.getRealName();
            avatar = admin.getAvatar();
            
            // 查询角色用于前端展示 (非鉴权用，鉴权走 StpInterface)
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
            // ================== 普通用户登录逻辑 ==================
            SysOrdinaryUser user = ordinaryUserMapper.selectOne(new LambdaQueryWrapper<SysOrdinaryUser>()
                    .eq(SysOrdinaryUser::getUsername, username));
            
            if (user == null) throw new ServiceException("学号/工号不存在");
            if (!BCrypt.checkpw(password, user.getPassword())) throw new ServiceException("密码错误");
            if ("1".equals(user.getStatus())) throw new ServiceException("账号已停用，请联系宿管");
            
            originalUserId = user.getId();
            // 构建带前缀的 ID (1:2005)
            loginId = PREFIX_ORDINARY + originalUserId;
            
            realName = user.getRealName();
            avatar = null; // 普通用户暂无头像
            
            // 简单判断角色给前端展示
            roleKey = (user.getUserCategory() != null && user.getUserCategory() == 1) ? "teacher" : "student";
        }
        
        // 3. 执行 Sa-Token 登录
        // 这一步会生成 Token，并与 loginId ("0:1001") 绑定
        StpUtil.login(loginId);
        
        // 4. 【关键】缓存关键信息到 Session
        // LoginHelper.getUserId() 强依赖这里的 "originalId"
        StpUtil.getSession().set("originalId", originalUserId);
        StpUtil.getSession().set("name", realName);
        StpUtil.getSession().set("role", roleKey);
        StpUtil.getSession().set("type", userType);
        
        // 5. 组装返回结果
        LoginVO vo = new LoginVO();
        vo.setToken(StpUtil.getTokenValue());
        vo.setUserId(originalUserId); // 返回给前端的是原始ID，前端不感知前缀
        vo.setRealName(realName);
        vo.setRole(roleKey);
        vo.setAvatar(avatar);
        
        return vo;
    }
}