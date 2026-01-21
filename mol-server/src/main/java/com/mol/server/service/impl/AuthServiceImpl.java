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
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 认证服务实现类 (安全增强版)
 * <p>
 * 核心职责：
 * 1. 🛡️ 防御性校验 (Anti-Malicious): 清洗入参，防止非法字符和枚举攻击。
 * 2. 🔐 凭证校验: 使用 BCrypt 强哈希比对密码。
 * 3. 🎟️ 会话管理: 使用 Sa-Token 核心 API 建立会话、踢人下线、续期。
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
    
    // 默认头像 (当用户未上传时使用，防止前端裂图)
    private static final String DEFAULT_AVATAR = "https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png";
    
    // 登录 ID 前缀 (必须与 StpInterfaceImpl 的权限加载逻辑对齐)
    // 0:管理员, 1:普通用户
    private static final String PREFIX_ADMIN = "0:";
    private static final String PREFIX_ORDINARY = "1:";
    
    // "记住我" 的有效期 (7天 = 604800秒)
    private static final long TIMEOUT_REMEMBER_ME = 604800;
    
    // 🛡️ [防刁民] 设备标识最大长度限制 (防止恶意构造超长字符串攻击)
    private static final int MAX_DEVICE_LENGTH = 50;
    
    // 注入配置文件中的强制改密开关
    @Value("${mol.security.force-change-pwd:false}")
    private boolean forceChangePwdEnabled;
    
    /**
     * 统一登录接口
     *
     * @param loginBody 登录参数包 (账号/密码/类型/设备/记住我)
     * @return 登录成功后的视图对象 (含 Token 和 用户信息)
     */
    @Override
    public LoginVO login(LoginBody loginBody) {
        // --- 1. 入参清洗与防刁民校验 ---
        String username = loginBody.getUsername();
        String password = loginBody.getPassword();
        String userType = loginBody.getUserType();
        
        // 🛡️ 必填项校验
        if (ObjectUtil.hasEmpty(username, password, userType)) {
            throw new ServiceException("非法请求：账号、密码或用户类型缺失");
        }
        
        // 🛡️ 登录类型白名单校验 (防止恶意传参 "root", "hacker" 等绕过逻辑)
        // 🛡️ [防刁民] 白名单升级：允许 admin, student, staff
        if (!"admin".equals(userType) && !"student".equals(userType) && !"staff".equals(userType)) {
            log.warn("检测到非法登录类型攻击: IP={}, UserType={}", LoginHelper.getClientIP(), userType);
            throw new ServiceException("不支持的登录类型");
        }
        
        // 🛡️ 设备标识长度清洗
        String device = loginBody.getDevice();
        if (StrUtil.isBlank(device)) {
            device = "PC"; // 默认设备
        } else if (device.length() > MAX_DEVICE_LENGTH) {
            device = device.substring(0, MAX_DEVICE_LENGTH); // 截断超长字符
        }
        
        boolean isRemember = Boolean.TRUE.equals(loginBody.getRememberMe());
        
        // 变量准备
        Long originalUserId;
        String loginId;     // Sa-Token 用的唯一 ID (前缀 + ID)
        String realName;
        String nickname;
        String avatar;
        String roleKey;     // 权限标识符
        
        // --- 2. 分支逻辑：管理员 vs 普通用户 ---
        
        // ================== A. 管理员登录 (admin) ==================
        if ("admin".equals(userType)) {
            // 查询数据库
            SysAdminUser admin = adminUserMapper.selectOne(new LambdaQueryWrapper<SysAdminUser>()
                    .eq(SysAdminUser::getUsername, username));
            
            // 🛡️ [防枚举攻击] 账号不存在 或 密码错误，统一报"账号或密码错误"
            // 防止黑客根据报错信息差异推断出哪些账号是真实存在的
            if (admin == null || !BCrypt.checkpw(password, admin.getPassword())) {
                log.info("管理员登录失败 (密码错误或账号不存在): {}", username);
                throw new ServiceException("账号或密码错误");
            }
            
            // 状态检查
            if ("1".equals(admin.getStatus())) {
                throw new ServiceException("账号已停用，请联系系统管理员");
            }
            
            // 数据装载
            originalUserId = admin.getId();
            loginId = PREFIX_ADMIN + originalUserId;
            realName = admin.getRealName();
            nickname = StrUtil.isNotBlank(admin.getNickname()) ? admin.getNickname() : admin.getRealName();
            avatar = StrUtil.isNotBlank(admin.getAvatar()) ? admin.getAvatar() : DEFAULT_AVATAR;
            
            // 🔒 权限查询: 管理员必须查 sys_user_role 表
            List<SysUserRole> userRoles = userRoleMapper.selectList(new LambdaQueryWrapper<SysUserRole>()
                    .eq(SysUserRole::getUserId, originalUserId));
            
            if (CollUtil.isNotEmpty(userRoles)) {
                List<Long> roleIds = userRoles.stream().map(SysUserRole::getRoleId).collect(Collectors.toList());
                List<SysRole> roles = roleMapper.selectByIds(roleIds);
                roleKey = roles.stream().map(SysRole::getRoleKey).collect(Collectors.joining(","));
            } else {
                roleKey = "guest"; // 无角色兜底
            }
        }
        // ================== B. 普通用户登录 (student/staff) ==================
        else {
            // // 无论是 student 还是 staff，都查 sys_ordinary_user 表
            SysOrdinaryUser user = ordinaryUserMapper.selectOne(new LambdaQueryWrapper<SysOrdinaryUser>()
                    .eq(SysOrdinaryUser::getUsername, username));
            
            // 🛡️ [防枚举攻击] 同上
            if (user == null || !BCrypt.checkpw(password, user.getPassword())) {
                log.info("普通用户登录失败: {}", username);
                throw new ServiceException("账号或密码错误");
            }
            
            // 🛡️ [身份一致性校验] 防止学生传 staff 参，或者老师传 student 参
            // user_category: 0=学生, 1=教工
            if ("student".equals(userType) && user.getUserCategory() != 0) {
                log.warn("身份不匹配拦截: 账号{}尝试以student身份登录，但实际为教工", username);
                throw new ServiceException("身份类型不匹配");
            }
            if ("staff".equals(userType) && user.getUserCategory() != 1) {
                log.warn("身份不匹配拦截: 账号{}尝试以staff身份登录，但实际为学生", username);
                throw new ServiceException("身份类型不匹配");
            }
            
            if ("1".equals(user.getStatus())) {
                throw new ServiceException("账号已封禁，请联系辅导员");
            }
            
            originalUserId = user.getId();
            loginId = PREFIX_ORDINARY + originalUserId;
            realName = user.getRealName();
            nickname = StrUtil.isNotBlank(user.getNickname()) ? user.getNickname() : user.getRealName();
            avatar = StrUtil.isNotBlank(user.getAvatar()) ? user.getAvatar() : DEFAULT_AVATAR;
            
            // 角色判断 (0:学生, 1:教工) -> 映射为权限字符
            roleKey = (user.getUserCategory() != null && user.getUserCategory() == 1) ? "teacher" : "student";
        }
        
        // --- 3. 执行 Sa-Token 登录 (核心) ---
        
        // 💡 技巧：为了避开 Sa-Token 版本升级导致的 SaLoginConfig/Parameter 类不兼容问题，
        // 我们直接调用最原始、最稳定的 "StpUtil.login(id, device)" 方法。
        
        // 3.1 登录 (绑定 ID 和 设备类型) -> 这会生成 Token
        StpUtil.login(loginId, device);
        
        // 3.2 处理 "记住我" (长效 Token)
        // 如果用户勾选了记住我，我们手动将 Token 有效期延长到 7 天
        if (isRemember) {
            StpUtil.renewTimeout(TIMEOUT_REMEMBER_ME);
        }
        
        // 3.3 写入 Session (Token 扩展信息)
        // 这些信息保存在服务端 Redis 中，不暴露给前端，用于后续鉴权拦截器
        StpUtil.getSession()
                .set("originalId", originalUserId) // 原始数据库 ID
                .set("name", realName)             // 真实姓名
                .set("role", roleKey)              // 角色 Key
                .set("type", userType);            // 登录类型, 这里 type 就会存入 "staff" 或 "student"
        
        
        // --- 4. 强制改密检查 (安全策略) ---
        boolean needChange = false;
        if (forceChangePwdEnabled) {
            // 如果配置文件开启了强制改密，且数据库字段 isInitialPwd 为 1
            if ("admin".equals(userType)) {
                SysAdminUser adminUser = adminUserMapper.selectById(originalUserId);
                needChange = (adminUser.getIsInitialPwd() != null && adminUser.getIsInitialPwd() == 1);
            } else {
                SysOrdinaryUser ordinaryUser = ordinaryUserMapper.selectById(originalUserId);
                needChange = (ordinaryUser.getIsInitialPwd() != null && ordinaryUser.getIsInitialPwd() == 1);
            }
        }
        
        log.info("用户登录成功: username={}, role={}, ip={}", username, roleKey, LoginHelper.getClientIP());
        
        // --- 5. 构建返回值 ---
        return LoginVO.builder()
                .tokenName(StpUtil.getTokenName())   // 例如 "Authorization"
                .tokenValue(StpUtil.getTokenValue()) // Token 字符串
                .userId(originalUserId)
                .realName(realName)
                .nickname(nickname)
                .role(roleKey)
                .avatar(avatar)
                .needChangePwd(needChange) // 告诉前端是否弹窗提示改密
                .build();
    }
    
    /**
     * 开启二级认证 (Safe Mode)
     * <p>
     * 场景：用户进行敏感操作（如删除数据、修改密码）前，需要再次输入密码验证身份。
     * 验证通过后，系统开启 300秒 的安全窗口期。
     * </p>
     *
     * @param password 当前用户的登录密码 (明文)
     */
    @Override
    public void openSafeMode(String password) {
        Long userId = LoginHelper.getUserId();
        String userType = LoginHelper.getUserType();
        
        // 兼容 admin 和 staff/student 通道
        boolean isAdmin = "admin".equals(userType); // 只有 admin 类型查管理员表
        
        String dbPassword;
        
        // 1. 获取数据库中的哈希密码
        if (isAdmin) {
            SysAdminUser admin = adminUserMapper.selectById(userId);
            if (admin == null) throw new ServiceException("用户不存在");
            dbPassword = admin.getPassword();
        } else {
            SysOrdinaryUser user = ordinaryUserMapper.selectById(userId);
            if (user == null) throw new ServiceException("用户不存在");
            dbPassword = user.getPassword();
        }
        
        // 2. 校验密码 (BCrypt)
        if (!BCrypt.checkpw(password, dbPassword)) {
            // 记录日志，可能是非法尝试
            log.warn("二级认证失败，密码错误。UserID={}", userId);
            throw new ServiceException("密码错误，身份验证失败");
        }
        
        // 3. 开启安全模式 (有效期 300秒)
        // 在此期间，带有 @SaCheckSafe 注解的接口将允许通过
        StpUtil.openSafe(300);
        log.info("用户开启二级认证模式: UserID={}", userId);
    }
    
    /**
     * 注销登录
     */
    @Override
    public void logout() {
        if (StpUtil.isLogin()) {
            Object loginId = StpUtil.getLoginId();
            StpUtil.logout();
            log.info("用户注销成功: LoginId={}", loginId);
        }
    }
}