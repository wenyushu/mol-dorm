package com.mol.sys.biz.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.mol.common.core.entity.SysOrdinaryUser;

/**
 * 普通用户业务接口 (学生/教职工)
 */
public interface SysOrdinaryUserService extends IService<SysOrdinaryUser> {
    
    /**
     * 新增用户 (包含密码加密)
     * @param user 用户信息
     * @return 是否成功
     */
    boolean saveUser(SysOrdinaryUser user);
    
    /**
     * 修改用户信息 (不修改密码)
     * @param user 用户信息
     * @return 是否成功
     */
    boolean updateUser(SysOrdinaryUser user);
    
    /**
     * 重置密码
     * @param userId 用户 ID
     * @param newPassword 新密码 (明文)
     */
    void resetPassword(Long userId, String newPassword);
    
    /**
     * 用户自行修改密码
     * @param userId 用户 ID
     * @param oldPassword 旧密码
     * @param newPassword 新密码
     */
    void updatePassword(Long userId, String oldPassword, String newPassword);
}