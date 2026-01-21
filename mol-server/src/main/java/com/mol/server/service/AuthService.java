package com.mol.server.service;

import com.mol.server.dto.LoginBody;
import com.mol.server.vo.LoginVO;

/**
 * 认证服务接口
 */
public interface AuthService {
    
    /**
     * 登录逻辑
     * @param loginBody 登录参数
     * @return 包含 Token 和用户信息的 VO 对象
     */
    LoginVO login(LoginBody loginBody);
    
    /**
     * 注销逻辑
     */
    void logout();
    
    /**
     * 开启二级认证 (Safe Mode)
     * @param password 用户密码
     */
    void openSafeMode(String password);
}