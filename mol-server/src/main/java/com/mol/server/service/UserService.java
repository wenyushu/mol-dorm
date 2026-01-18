package com.mol.server.service;

import com.mol.server.dto.AdminUpdateStudentBody;
import com.mol.server.dto.UpdatePasswordBody;
import com.mol.server.dto.UserProfileBody;

/**
 * 用户业务接口
 * 负责处理：个人中心、学生管理等非登录类业务
 */
public interface UserService {
    
    /**
     * 场景A: 个人修改资料 (昵称、头像、手机)
     * 🚫 严禁在此处修改 学号、姓名、性别
     */
    void updateProfile(UserProfileBody body);
    
    /**
     * 场景B: 修改密码
     */
    void updatePassword(UpdatePasswordBody body);
    
    /**
     * 场景C: 管理员修改学生档案 (转专业、修正性别、封号)
     */
    void updateStudentByAdmin(AdminUpdateStudentBody body);
}