package com.mol.sys.biz.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.mol.common.core.entity.SysOrdinaryUser;

/**
 * 普通用户业务接口 (学生/教职工)
 */
public interface SysOrdinaryUserService extends IService<SysOrdinaryUser> {
    
    /**
     * 新增用户 (包含密码加密、身份证解析)
     */
    boolean saveUser(SysOrdinaryUser user);
    
    /**
     * 修改用户信息
     * (包含密码加密检测：若 password 字段不为空则更新密码，否则保留原密码)
     */
    boolean updateUser(SysOrdinaryUser user);
    
    /**
     * 重置密码 (管理员强制重置)
     * @param userId 用户 ID
     * @param newPassword 新密码 (明文)
     */
    void resetPassword(Long userId, String newPassword);
    
    /**
     * 用户自行修改密码 (需校验旧密码)
     * @param userId 用户 ID
     * @param oldPassword 旧密码 (明文)
     * @param newPassword 新密码 (明文)
     */
    void updatePassword(Long userId, String oldPassword, String newPassword);
}