package com.mol.auth.service;

import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.crypto.digest.BCrypt;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.mol.api.dto.LoginBody;
import com.mol.common.core.entity.SysAdminUser;
import com.mol.common.core.entity.SysOrdinaryUser;
import com.mol.common.mybatis.mapper.SysAdminUserMapper;
import com.mol.common.mybatis.mapper.SysOrdinaryUserMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 认证授权业务类
 * 优化点：增强了异常语义、增加了常量定义、完善了 Sa-Token 登录详情记录
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {
    
    private final SysAdminUserMapper adminUserMapper;
    private final SysOrdinaryUserMapper ordinaryUserMapper;
    
    // --- 定义用户类型常量，增强代码可读性 ---
    private static final int TYPE_ADMIN = 0;    // 管理员
    private static final int TYPE_ORDINARY = 1; // 普通用户 (学生/教师)
    
    public String login(LoginBody loginBody) {
        String username = loginBody.getUsername();
        String password = loginBody.getPassword();
        Integer userType = loginBody.getUserType();
        
        log.info("用户尝试登录 [{}], 类型: {}", username, userType);
        
        String dbPassword;
        Long userId;
        
        // 1. 分流查询：根据类型定位到不同的数据库表
        if (userType == TYPE_ADMIN) {
            // 管理员逻辑
            SysAdminUser admin = adminUserMapper.selectOne(Wrappers.<SysAdminUser>lambdaQuery()
                    .eq(SysAdminUser::getUsername, username));
            
            if (admin == null) {
                // 建议：对外统一提示“用户名或密码错误”，防止恶意探测账号是否存在
                throw new RuntimeException("该管理员账号不存在");
            }
            if ("1".equals(admin.getDelFlag())) {
                throw new RuntimeException("该管理员账号已被注销");
            }
            dbPassword = admin.getPassword();
            userId = admin.getId();
            
        } else if (userType == TYPE_ORDINARY) {
            // 学生/教工逻辑
            SysOrdinaryUser user = ordinaryUserMapper.selectOne(Wrappers.<SysOrdinaryUser>lambdaQuery()
                    .eq(SysOrdinaryUser::getUsername, username));
            
            if (user == null) {
                throw new RuntimeException("该普通用户账号不存在");
            }
            // 校验状态：1-活跃, 0-归档
            if (user.getAccountStatus() == null || user.getAccountStatus() != 1) {
                throw new RuntimeException("该账号当前状态不可用");
            }
            dbPassword = user.getPassword();
            userId = user.getId();
            
        } else {
            throw new RuntimeException("无效的用户身份类型");
        }
        
        // 2. 密码比对
        // 注意：Sa-Token 或 Hutool 提供的 BCrypt 校验必须严格匹配加密版本
        if (!BCrypt.checkpw(password, dbPassword)) {
            log.warn("用户 {} 尝试登录，但密码验证失败", username);
            throw new RuntimeException("用户名或密码输入错误");
        }
        
        // 3. 登录并签发 Token
        /*
           设计说明：loginId = "类型:ID" (例如 "0:1", "1:1001")
           这种设计的好处：
           1. 解决了主键 ID 冲突问题（如果两个表恰好都有 ID 为 1 的用户）。
           2. 在后续获取权限时，可以轻松 split(":") 拿到类型，从而决定去哪张表查权限。
         */
        String loginId = userType + ":" + userId;
        
        // 执行 Sa-Token 登录（底层会自动处理 Cookie/Header）
        StpUtil.login(loginId);
        
        log.info("用户登录成功: username={}, loginId={}", username, loginId);
        
        // 4. 获取并返回 Token 值
        return StpUtil.getTokenValue();
    }
}