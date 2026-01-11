package com.mol.server.service;

import com.mol.server.dto.LoginBody;
import com.mol.server.vo.LoginVO;

public interface AuthService {
    /**
     * 登录
     */
    LoginVO login(LoginBody loginBody);
}