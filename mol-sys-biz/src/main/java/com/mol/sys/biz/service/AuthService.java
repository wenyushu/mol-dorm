package com.mol.sys.biz.service;

import com.mol.sys.biz.dto.LoginBody;
import com.mol.sys.biz.vo.LoginVO;

public interface AuthService {
    /**
     * 登录
     */
    LoginVO login(LoginBody loginBody);
}