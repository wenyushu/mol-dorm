package com.mol.sys.biz.service;


import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.digest.BCrypt;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.mol.common.core.entity.SysAdminUser;
import com.mol.common.core.entity.SysOrdinaryUser;
import com.mol.common.core.exception.ServiceException;
import com.mol.sys.biz.mapper.SysAdminUserMapper;
import com.mol.sys.biz.mapper.SysOrdinaryUserMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 认证授权服务
 * <p>
 * 负责处理登录、注销、获取当前用户信息等核心安全逻辑。
 * 兼容两套用户体系：管理员(Admin) 和 普通用户(Ordinary)。
 * </p>
 *
 * @author mol
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {
    
    private final SysAdminUserMapper adminMapper;
    private final SysOrdinaryUserMapper ordinaryMapper;
    
    // 用户类型常量 (0:管理员, 1:普通用户)
    // 必须与 StpInterfaceImpl 中的定义保持一致
    private static final int TYPE_ADMIN = 0;
    private static final int TYPE_ORDINARY = 1;
    
    /**
     * 登录方法
     *
     * @param username 用户名 (管理员账号 或 学号/工号)
     * @param password 密码 (明文)
     * @param userType 用户类型 (0-管理员, 1-普通用户)
     * @return 生成的 Token 令牌
     */
    public String login(String username, String password, Integer userType) {
        // 1. 参数基础校验
        if (StrUtil.hasBlank(username, password)) {
            throw new ServiceException("账号或密码不能为空");
        }
        if (userType == null) {
            throw new ServiceException("必须指定用户身份类型");
        }
        
        // 2. 根据类型分流处理
        Long loginId;
        if (userType == TYPE_ADMIN) {
            loginId = loginAdmin(username, password);
        } else if (userType == TYPE_ORDINARY) {
            loginId = loginOrdinary(username, password);
        } else {
            throw new ServiceException("不支持的用户类型");
        }
        
        // 3. 构建 Sa-Token 复合 LoginId
        // 格式: "类型:ID" (例如 "0:1" 表示超管, "1:10005" 表示某个学生)
        // 这一步非常关键，StpInterfaceImpl 会解析这个格式来加载权限
        String compositeId = userType + ":" + loginId;
        
        // 4. 执行登录
        StpUtil.login(compositeId);
        
        // 5. 返回 Token 值
        return StpUtil.getTokenValue();
    }
    
    /**
     * 注销登录
     */
    public void logout() {
        StpUtil.logout();
    }
    
    /**
     * 获取当前登录用户的详细信息 (用于前端初始化)
     * 包括：基础信息、角色列表、权限列表
     */
    public Map<String, Object> getLoginUserInfo() {
        Map<String, Object> result = new HashMap<>();
        
        // 1. 获取 LoginId (格式 "类型:ID")
        String loginIdStr = StpUtil.getLoginIdAsString();
        String[] parts = loginIdStr.split(":");
        int userType = Integer.parseInt(parts[0]);
        Long userId = Long.parseLong(parts[1]);
        
        Object userInfo = null;
        
        // 2. 查询数据库获取最新信息
        if (userType == TYPE_ADMIN) {
            SysAdminUser admin = adminMapper.selectById(userId);
            // 脱敏密码
            admin.setPassword(null);
            userInfo = admin;
        } else {
            SysOrdinaryUser user = ordinaryMapper.selectById(userId);
            user.setPassword(null);
            userInfo = user;
        }
        
        // 3. 获取权限集合 (Sa-Token 自动调用 StpInterfaceImpl)
        List<String> roleList = StpUtil.getRoleList();
        List<String> permissionList = StpUtil.getPermissionList();
        
        result.put("user", userInfo);
        result.put("roles", roleList);
        result.put("permissions", permissionList);
        result.put("userType", userType);
        
        return result;
    }
    
    // ================== 内部私有方法 ==================
    
    /**
     * 管理员登录逻辑
     */
    private Long loginAdmin(String username, String password) {
        // 1. 查询数据库
        SysAdminUser admin = adminMapper.selectOne(new LambdaQueryWrapper<SysAdminUser>()
                .eq(SysAdminUser::getUsername, username));
        
        // 2. 校验账号是否存在
        if (admin == null) {
            throw new ServiceException("账号不存在");
        }
        
        // 3. 校验密码 (使用 BCrypt)
        if (!BCrypt.checkpw(password, admin.getPassword())) {
            throw new ServiceException("密码错误");
        }
        
        // 4. 校验状态 (假设有 status 字段，0正常 1停用)
        if ("1".equals(admin.getStatus())) {
            throw new ServiceException("该账号已被停用，请联系管理员");
        }
        
        return admin.getId();
    }
    
    /**
     * 普通用户(学生/教工)登录逻辑
     */
    private Long loginOrdinary(String username, String password) {
        // 1. 查询数据库
        SysOrdinaryUser user = ordinaryMapper.selectOne(new LambdaQueryWrapper<SysOrdinaryUser>()
                .eq(SysOrdinaryUser::getUsername, username));
        
        // 2. 校验账号
        if (user == null) {
            throw new ServiceException("账号不存在");
        }
        
        // 3. 校验密码
        if (!BCrypt.checkpw(password, user.getPassword())) {
            throw new ServiceException("密码错误");
        }
        
        // 4. 校验状态 (accountStatus: 1-活跃, 0-禁用)
        // 注意：SysOrdinaryUser 里使用的是 Integer 类型的 1/0
        if (user.getAccountStatus() != null && user.getAccountStatus() == 0) {
            throw new ServiceException("账号已被封禁，请联系辅导员或宿管");
        }
        
        return user.getId();
    }
}