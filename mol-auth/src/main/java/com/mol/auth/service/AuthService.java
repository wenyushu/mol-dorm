package com.mol.auth.service;


import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.crypto.digest.BCrypt;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import com.mol.apl.dto.LoginBody;
import com.mol.common.core.entity.SysAdminUser;
import com.mol.common.core.entity.SysOrdinaryUser;
import com.mol.common.mybatis.mapper.SysAdminUserMapper;
import com.mol.common.mybatis.mapper.SysOrdinaryUserMapper;
import org.springframework.stereotype.Service;

/**
 * 认证授权业务类
 * 负责多表账户校验、密码比对及 Token 签发
 *
 * @author mol
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    // 注入管理员表 Mapper
    private final SysAdminUserMapper adminUserMapper;
    // 注入普通用户表 (学生/教工) Mapper
    private final SysOrdinaryUserMapper ordinaryUserMapper;

    /**
     * 统一登录逻辑
     *
     * @param loginBody 登录参数对象
     * @return 登录成功后的 Token 字符串
     */
    public String login(LoginBody loginBody) {
        String username = loginBody.getUsername();
        String password = loginBody.getPassword();
        Integer userType = loginBody.getUserType();

        log.info("用户尝试登录: username={}, type={}", username, userType);

        // 声明两个核心变量，用于后续校验
        String dbPassword;
        Long userId;

        // 1. 根据用户类型进行分流查询
        if (userType == 0) {
            // --- 管理员登录逻辑 ---
            LambdaQueryWrapper<SysAdminUser> query = Wrappers.lambdaQuery(SysAdminUser.class)
                    .eq(SysAdminUser::getUsername, username);
            SysAdminUser admin = adminUserMapper.selectOne(query);

            if (admin == null) {
                throw new RuntimeException("管理员账号不存在");
            }
            if ("1".equals(admin.getDelFlag())) {
                throw new RuntimeException("该账号已被逻辑删除");
            }

            dbPassword = admin.getPassword();
            userId = admin.getId();

        } else if (userType == 1) {
            // --- 普通用户 (学生/职工) 登录逻辑 ---
            LambdaQueryWrapper<SysOrdinaryUser> query = Wrappers.lambdaQuery(SysOrdinaryUser.class)
                    .eq(SysOrdinaryUser::getUsername, username);
            SysOrdinaryUser user = ordinaryUserMapper.selectOne(query);

            if (user == null) {
                throw new RuntimeException("学生或教工账号不存在");
            }
            // 校验账户状态 (数据库中 1-活跃, 0-归档)
            if (user.getAccountStatus() != 1) {
                throw new RuntimeException("该账号已归档或不可用");
            }

            dbPassword = user.getPassword();
            userId = user.getId();

        } else {
            throw new RuntimeException("错误的用户类型");
        }

        // 2. 密码校验 (使用 BCrypt 算法)
        // 注意：Sa-Token 的 BCrypt.checkpw(明文, 密文)
        // 数据库中的密码必须是 $2a$10$... 开头的加密串
        if (!BCrypt.checkpw(password, dbPassword)) {
            log.warn("用户 {} 密码错误", username);
            throw new RuntimeException("用户名或密码错误");
        }

        // 3. 登录成功，签发 Token
        // 我们在 loginId 中揉入 userType，格式为 "类型:ID"
        // 这样 StpInterfaceImpl 里的 getRoleList 才能解析出用户类型去查角色表
        String loginId = userType + ":" + userId;
        StpUtil.login(loginId);

        log.info("用户 {} 登录成功，签发 ID: {}", username, loginId);

        // 4. 返回 Token 给前端
        return StpUtil.getTokenValue();
    }
}