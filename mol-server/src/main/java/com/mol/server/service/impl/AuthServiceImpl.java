package com.mol.server.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import cn.dev33.satoken.stp.parameter.SaLoginParameter;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.digest.BCrypt;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.mol.common.core.entity.SysAdminUser;
import com.mol.common.core.entity.SysOrdinaryUser;
import com.mol.common.core.entity.SysUserRole;
import com.mol.common.core.exception.ServiceException;
import com.mol.common.core.util.LoginHelper;
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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 认证服务实现类 (防爆破 & 自动身份识别版)
 * <p>
 * 安全升级：
 * 1. 🛡️ 引入 Redis 计数器，防御暴力破解和撞库攻击。
 * 2. 🔐 采用 "Try-Fallback" 机制自动识别管理员/普通用户。
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
    private final StringRedisTemplate redisTemplate; // 注入 Redis 模板
    
    // 默认头像
    private static final String DEFAULT_AVATAR = "https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png";
    
    // 登录 ID 前缀
    private static final String PREFIX_ADMIN = "0:";
    private static final String PREFIX_ORDINARY = "1:";
    
    // 安全常量
    private static final long TIMEOUT_REMEMBER_ME = 604800; // 7天
    private static final int MAX_DEVICE_LENGTH = 50;
    
    // 🛡️ [防爆破] Redis Key 前缀: sys:login:fail:{username}
    private static final String CACHE_LOGIN_FAIL = "sys:login:fail:";
    // 🛡️ [防爆破] 最大重试次数
    private static final int MAX_RETRY_COUNT = 5;
    // 🛡️ [防爆破] 锁定时间 (分钟)
    private static final int LOCK_TIME_MINUTES = 10;
    
    @Value("${mol.security.force-change-pwd:false}")
    private boolean forceChangePwdEnabled;
    
    @Override
    public LoginVO login(LoginBody loginBody) {
        // --- 1. 入参清洗 ---
        String username = loginBody.getUsername();
        String password = loginBody.getPassword();
        String device = loginBody.getDevice();
        
        if (ObjectUtil.hasEmpty(username, password)) {
            throw new ServiceException("非法请求：账号或密码缺失");
        }
        
        // 🛡️ [防刁民] 限制用户名长度，防止超长 Key 攻击 Redis
        if (username.length() > 50) {
            throw new ServiceException("账号长度非法");
        }
        
        // 设备标识清洗
        if (StrUtil.isBlank(device)) {
            device = "PC";
        } else if (device.length() > MAX_DEVICE_LENGTH) {
            device = device.substring(0, MAX_DEVICE_LENGTH);
        }
        
        // --- 2. 🛡️ [防爆破] 检查锁定状态 ---
        checkLoginLock(username);
        
        // --- 3. 身份自动识别 & 验密 ---
        
        // 3.1 尝试匹配管理员
        SysAdminUser admin = adminUserMapper.selectOne(Wrappers.<SysAdminUser>lambdaQuery()
                .eq(SysAdminUser::getUsername, username));
        
        if (admin != null && BCrypt.checkpw(password, admin.getPassword())) {
            // ✅ 登录成功：清除错误计数
            clearLoginFailCount(username);
            return doLoginAsAdmin(admin, device, loginBody.getRememberMe());
        }
        
        // 3.2 尝试匹配普通用户
        SysOrdinaryUser user = ordinaryUserMapper.selectOne(Wrappers.<SysOrdinaryUser>lambdaQuery()
                .eq(SysOrdinaryUser::getUsername, username));
        
        if (user != null && BCrypt.checkpw(password, user.getPassword())) {
            // ✅ 登录成功：清除错误计数
            clearLoginFailCount(username);
            return doLoginAsOrdinary(user, device, loginBody.getRememberMe());
        }
        
        // --- 4. ❌ 登录失败处理 ---
        
        // 记录错误次数，并触发锁定检测
        int retryCount = incrementLoginFailCount(username);
        
        log.warn("登录失败: username={}, ip={}, retry={}", username, LoginHelper.getClientIP(), retryCount);
        
        // 提示剩余次数，制造紧迫感，同时又不暴露账号是否存在
        throw new ServiceException("账号或密码错误");
    }
    
    /**
     * 🛡️ 检查账号是否被锁定
     */
    private void checkLoginLock(String username) {
        String key = CACHE_LOGIN_FAIL + username;
        String val = redisTemplate.opsForValue().get(key);
        
        // 1. 如果 Redis 中没有记录，直接放行
        if (StrUtil.isBlank(val)) {
            return;
        }
        
        // 2. 解析错误次数 (防御性编程：防止 Redis 数据被污染导致报错)
        int count;
        try {
            count = Integer.parseInt(val);
        } catch (NumberFormatException e) {
            // 如果解析失败，说明数据异常，清除旧数据并放行，防止用户永久无法登录
            redisTemplate.delete(key);
            return;
        }
        
        // 3. 超过最大重试次数 -> 抛出锁定异常
        if (count >= MAX_RETRY_COUNT) {
            // 获取剩余过期时间 (单位：分钟)
            Long expire = redisTemplate.getExpire(key, TimeUnit.MINUTES);
            
            // 🛡️ [逻辑优化]：消除 IDE 黄色警告，并处理负数时间
            // 策略：默认显示 LOCK_TIME_MINUTES (10分钟)。
            // 只有当 expire 大于 0 时，才显示实际剩余时间。
            // 这样既解决了 "expire != null" 的多余检查警告，也防止了显示 "-1 分钟"。
            long waitTime = LOCK_TIME_MINUTES;
            if (expire > 0) {
                waitTime = expire;
            }
            
            throw new ServiceException(StrUtil.format("账号已锁定，请 {} 分钟后再试", waitTime));
        }
    }
    
    /**
     * 🛡️ 增加错误计数
     * @return 当前错误次数
     */
    private int incrementLoginFailCount(String username) {
        String key = CACHE_LOGIN_FAIL + username;
        
        // 原子递增
        Long count = redisTemplate.opsForValue().increment(key);
        if (count != null && count == 1) {
            // 第一次失败，设置过期时间
            redisTemplate.expire(key, Duration.ofMinutes(LOCK_TIME_MINUTES));
        }
        return count != null ? count.intValue() : 0;
    }
    
    /**
     * 🛡️ 清除错误计数 (登录成功后调用)
     */
    private void clearLoginFailCount(String username) {
        String key = CACHE_LOGIN_FAIL + username;
        redisTemplate.delete(key);
    }
    
    // ================== 下方代码保持不变 ==================
    
    private LoginVO doLoginAsAdmin(SysAdminUser admin, String device, Boolean rememberMe) {
        if ("1".equals(admin.getStatus())) {
            throw new ServiceException("账号已停用，请联系系统管理员");
        }
        Long userId = admin.getId();
        String loginId = PREFIX_ADMIN + userId;
        String roleKey;
        List<SysUserRole> userRoles = userRoleMapper.selectList(Wrappers.<SysUserRole>lambdaQuery()
                .eq(SysUserRole::getUserId, userId));
        if (CollUtil.isNotEmpty(userRoles)) {
            List<Long> roleIds = userRoles.stream().map(SysUserRole::getRoleId).collect(Collectors.toList());
            List<SysRole> roles = roleMapper.selectByIds(roleIds);
            roleKey = roles.stream().map(SysRole::getRoleKey).collect(Collectors.joining(","));
        } else {
            roleKey = "guest";
        }
        return executeSaTokenLogin(loginId, userId, "admin", admin.getRealName(), admin.getNickname(),
                admin.getAvatar(), roleKey, device, rememberMe, admin.getIsInitialPwd());
    }
    
    private LoginVO doLoginAsOrdinary(SysOrdinaryUser user, String device, Boolean rememberMe) {
        if ("1".equals(user.getStatus())) {
            throw new ServiceException("账号已封禁，请联系辅导员");
        }
        Long userId = user.getId();
        String loginId = PREFIX_ORDINARY + userId;
        String userTypeStr = (user.getUserCategory() != null && user.getUserCategory() == 1) ? "staff" : "student";
        String roleKey;
        List<SysUserRole> userRoles = userRoleMapper.selectList(Wrappers.<SysUserRole>lambdaQuery()
                .eq(SysUserRole::getUserId, userId));
        if (CollUtil.isNotEmpty(userRoles)) {
            List<Long> roleIds = userRoles.stream().map(SysUserRole::getRoleId).collect(Collectors.toList());
            List<SysRole> roles = roleMapper.selectByIds(roleIds);
            roleKey = roles.stream().map(SysRole::getRoleKey).collect(Collectors.joining(","));
        } else {
            roleKey = (user.getUserCategory() != null && user.getUserCategory() == 1) ? "teacher" : "student";
        }
        return executeSaTokenLogin(loginId, userId, userTypeStr, user.getRealName(), user.getNickname(),
                user.getAvatar(), roleKey, device, rememberMe, user.getIsInitialPwd());
    }
    
    
    /**
     * 公共方法：执行 Sa-Token 最终登录并构建返回值
     */
    @SuppressWarnings("deprecation") // 🛠️ 消除 setDevice 方法的弃用警告，确保代码整洁
    private LoginVO executeSaTokenLogin(String loginId, Long originalUserId, String userType,
                                        String realName, String nickname, String avatar, String roleKey,
                                        String device, Boolean rememberMe, Integer isInitialPwd) {
        
        // 1. 构建登录参数 (SaLoginParameter)
        // 使用新版参数对象，并分别设置属性
        SaLoginParameter loginParam = new SaLoginParameter();
        
        // 设置设备标识 (用于区分 PC/Mobile 等多端登录)
        // 虽然新版标记为 Deprecated，但在复杂参数配置场景下，这仍是原子操作的必要手段
        loginParam.setDevice(device);
        
        // 设置是否为长效 Cookie (影响浏览器关闭后是否保持登录)
        loginParam.setIsLastingCookie(Boolean.TRUE.equals(rememberMe));
        
        // 2. 差异化配置 Token 有效期 (TTL)
        if (Boolean.TRUE.equals(rememberMe)) {
            // ✅ 场景 A: 用户勾选"记住我" -> 给予长效 Token (7天)
            loginParam.setTimeout(TIMEOUT_REMEMBER_ME);
        } else {
            // ✅ 场景 B: 用户未勾选 -> 给予短效 Token
            // 不设置时，默认使用 application.yml 中的 sa-token.timeout
        }
        
        // 3. 执行登录 (原子操作)
        // 生成 Token + 写入 Redis + 设置 TTL + 绑定设备，一步完成，无并发隐患
        StpUtil.login(loginId, loginParam);
        
        // 4. 写入 Session (Token 扩展信息) - 保持不变
        String finalNickname = StrUtil.isNotBlank(nickname) ? nickname : realName;
        String finalAvatar = StrUtil.isNotBlank(avatar) ? avatar : DEFAULT_AVATAR;
        
        StpUtil.getSession()
                .set("originalId", originalUserId)
                .set("name", realName)
                .set("role", roleKey)
                .set("type", userType); // 自动识别出的类型: admin / staff / student
        
        // 5. 强制改密判断 - 保持不变
        boolean needChange = false;
        if (forceChangePwdEnabled && isInitialPwd != null && isInitialPwd == 1) {
            needChange = true;
        }
        
        log.info("用户登录成功(自动识别): username={}, role={}, type={}, rememberMe={}",
                realName, roleKey, userType, rememberMe);
        
        return LoginVO.builder()
                .tokenName(StpUtil.getTokenName())
                .tokenValue(StpUtil.getTokenValue())
                .userId(originalUserId)
                .realName(realName)
                .nickname(finalNickname)
                .role(roleKey)
                .avatar(finalAvatar)
                .needChangePwd(needChange)
                .build();
    }
    
    
    @Override
    public void openSafeMode(String password) {
        Long userId = LoginHelper.getUserId();
        String userType = LoginHelper.getUserType();
        boolean isAdmin = "admin".equals(userType);
        String dbPassword;
        if (isAdmin) {
            SysAdminUser admin = adminUserMapper.selectById(userId);
            if (admin == null) throw new ServiceException("用户不存在");
            dbPassword = admin.getPassword();
        } else {
            SysOrdinaryUser user = ordinaryUserMapper.selectById(userId);
            if (user == null) throw new ServiceException("用户不存在");
            dbPassword = user.getPassword();
        }
        if (!BCrypt.checkpw(password, dbPassword)) {
            log.warn("二级认证失败，密码错误。UserID={}", userId);
            throw new ServiceException("密码错误，身份验证失败");
        }
        StpUtil.openSafe(300);
        log.info("用户开启二级认证模式: UserID={}", userId);
    }
    
    @Override
    public void logout() {
        if (StpUtil.isLogin()) {
            Object loginId = StpUtil.getLoginId();
            StpUtil.logout();
            log.info("用户注销成功: LoginId={}", loginId);
        }
    }
}