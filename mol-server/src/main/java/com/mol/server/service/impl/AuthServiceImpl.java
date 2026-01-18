package com.mol.server.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
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
    // 🟢 定义一个默认头像 (这里用了一个开源的免费头像，你也可以换成你项目里的静态资源)
    private static final String DEFAULT_AVATAR = "https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png";
    
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

        // 🛡️ 防刁民：如果前端没传 userType，尝试自动推断 (根据 username 是否纯数字)
        // 优先信赖前端传值
        String userType = loginBody.getUserType();
        if (ObjectUtil.hasEmpty(username, password)) {
            throw new ServiceException("账号或密码不能为空");
        }
        
        Long originalUserId; // 数据库真实 ID (例如 1001)
        String loginId;      // Sa-Token 登录 ID (例如 "0:1001")
        String realName;     // 真实姓名
        String nickname;     // 昵称
        String avatar;       // 头像 url
        String roleKey;      // 返回给前端展示用的角色标识
        
        // 2. 根据用户类型查不同的表
        // ================== A. 管理员登录 ==================
        if ("admin".equals(userType)) {
            SysAdminUser admin = adminUserMapper.selectOne(new LambdaQueryWrapper<SysAdminUser>()
                    .eq(SysAdminUser::getUsername, username));
            
            if (admin == null) throw new ServiceException("账号或密码错误"); // 模糊报错
            if (!BCrypt.checkpw(password, admin.getPassword())) throw new ServiceException("账号或密码错误");
            if ("1".equals(admin.getStatus())) throw new ServiceException("账号已停用");
            
            originalUserId = admin.getId();
            loginId = PREFIX_ADMIN + originalUserId;
            realName = admin.getRealName();
            avatar = admin.getAvatar();
            
            // 1. 处理昵称：如果有昵称用昵称，没有就用真实姓名
            nickname = StrUtil.isNotBlank(admin.getNickname()) ? admin.getNickname() : admin.getRealName();
            
            // 2. 处理头像：如果有头像用头像，没有就用默认图
            avatar = StrUtil.isNotBlank(admin.getAvatar()) ? admin.getAvatar() : DEFAULT_AVATAR;
            
            // 查询角色用于前端展示
            List<SysUserRole> userRoles = userRoleMapper.selectList(new LambdaQueryWrapper<SysUserRole>()
                    .eq(SysUserRole::getUserId, originalUserId));
            if (CollUtil.isNotEmpty(userRoles)) {
                List<Long> roleIds = userRoles.stream().map(SysUserRole::getRoleId).collect(Collectors.toList());
                List<SysRole> roles = roleMapper.selectByIds(roleIds);
                roleKey = roles.stream().map(SysRole::getRoleKey).collect(Collectors.joining(","));
            } else {
                roleKey = "guest";
            }
        }
        // ================== B. 普通用户登录 (学生/教工) ==================
        else {
            SysOrdinaryUser user = ordinaryUserMapper.selectOne(new LambdaQueryWrapper<SysOrdinaryUser>()
                    .eq(SysOrdinaryUser::getUsername, username));
            
            if (user == null) throw new ServiceException("账号 or 密码错误");
            if (!BCrypt.checkpw(password, user.getPassword())) throw new ServiceException("账号 or 密码错误");
            if ("1".equals(user.getStatus())) throw new ServiceException("账号已封禁，请联系宿管");
            
            originalUserId = user.getId();
            loginId = PREFIX_ORDINARY + originalUserId;
            realName = user.getRealName();
            avatar = user.getAvatar();
            
            // 🟢 处理昵称
            nickname = StrUtil.isNotBlank(user.getNickname()) ? user.getNickname() : user.getRealName();
            
            // 🟢 处理头像
            avatar = StrUtil.isNotBlank(user.getAvatar()) ? user.getAvatar() : DEFAULT_AVATAR;
            
            // 🛡️ 防刁民，先简单判断角色
            roleKey = (user.getUserCategory() != null && user.getUserCategory() == 1) ? "teacher" : "student";
        }
        
        // 3. 执行 Sa-Token 登录
        StpUtil.login(loginId);

        // 4. 写入 Session (LoginHelper 强依赖)
        StpUtil.getSession().set("originalId", originalUserId);
        StpUtil.getSession().set("name", realName);
        StpUtil.getSession().set("role", roleKey);

        // 5. 组装 VO (Token + 用户信息)
        return LoginVO.builder()
                .tokenName(StpUtil.getTokenName())
                .tokenValue(StpUtil.getTokenValue())
                .userId(originalUserId)
                .realName(realName) // 真实姓名
                .nickname(nickname) // 昵称
                .role(roleKey)
                .avatar(avatar)
                .build();
    }
    
    // 注销当前登录状态
    @Override
    public void logout() {
        StpUtil.logout();
    }
}