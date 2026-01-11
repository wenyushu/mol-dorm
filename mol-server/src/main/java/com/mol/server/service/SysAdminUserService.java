package com.mol.server.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.mol.common.core.entity.SysAdminUser;

/**
 * 系统管理员业务接口
 * <p>
 * 负责宿管、后勤等管理人员的账号维护。
 * </p>
 *
 * @author mol
 */
public interface SysAdminUserService extends IService<SysAdminUser> {
    
    /**
     * 新增管理员
     * @param admin 管理员信息
     * @return 是否成功
     */
    boolean saveAdmin(SysAdminUser admin);
    
    /**
     * 修改管理员信息 (不含密码)
     * @param admin 管理员信息
     * @return 是否成功
     */
    boolean updateAdmin(SysAdminUser admin);
    
    /**
     * 重置管理员密码
     * @param userId 管理员 ID
     * @param newPassword 新密码 (明文)
     */
    void resetPassword(Long userId, String newPassword);
}