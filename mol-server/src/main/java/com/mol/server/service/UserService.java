package com.mol.server.service;

import com.mol.server.dto.AdminUpdateStudentBody;
import com.mol.server.dto.UpdatePasswordBody;
import com.mol.server.dto.UserProfileEditDTO;

import java.util.Map;

/**
 * 用户业务接口
 * 负责处理：个人中心、学生管理等非登录类业务
 */
public interface UserService {
    /**
     * 获取个人资料 (包含自动脱敏逻辑)
     */
    Map<String, Object> getProfile();
    
    /**
     * 自行修改资料 (仅限非核心字段)
     */
    void updateProfile(UserProfileEditDTO body);
    
    /**
     * 自行修改密码 (含旧密码校验与强制登出)
     */
    void updatePassword(UpdatePasswordBody body);
    
    /**
     * 管理员特权：修改学生核心学籍及校外地址强制备案
     * 🛡️ 防刁民：此接口应在 Controller 层加 @SaCheckRole
     */
    void updateStudentByAdmin(AdminUpdateStudentBody body);
}