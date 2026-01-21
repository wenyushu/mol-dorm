package com.mol.server.service.impl;

import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.digest.BCrypt; // 🟢 确认使用 Hutool
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mol.common.core.entity.SysAdminUser;
import com.mol.common.core.exception.ServiceException;
import com.mol.server.mapper.SysAdminUserMapper;
import com.mol.server.service.SysAdminUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 系统管理员业务实现
 * <p>
 * 处理宿管/后勤人员的账号创建、查重及密码加密。
 * </p>
 */
@Service
@RequiredArgsConstructor
public class SysAdminUserServiceImpl extends ServiceImpl<SysAdminUserMapper, SysAdminUser> implements SysAdminUserService {
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean saveAdmin(SysAdminUser admin) {
        // 1. 必填参数校验
        if (StrUtil.isBlank(admin.getUsername())) {
            throw new ServiceException("管理员账号不能为空");
        }
        if (StrUtil.isBlank(admin.getRealName())) {
            throw new ServiceException("真实姓名不能为空");
        }
        
        // 2. 账号查重
        long count = this.count(new LambdaQueryWrapper<SysAdminUser>()
                .eq(SysAdminUser::getUsername, admin.getUsername()));
        if (count > 0) {
            throw new ServiceException("该管理员账号已存在，请更换");
        }
        
        // 3. 密码加密 (默认 123456)
        String rawPwd = StrUtil.isBlank(admin.getPassword()) ? "123456" : admin.getPassword();
        admin.setPassword(BCrypt.hashpw(rawPwd, BCrypt.gensalt()));
        
        // 🟢 核心新增：显式标记为初始密码状态 (1)
        admin.setIsInitialPwd(1);
        
        // 4. 设置默认状态
        if (StrUtil.isBlank(admin.getStatus())) {
            admin.setStatus("0");
        }
        
        return this.save(admin);
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateAdmin(SysAdminUser admin) {
        // 🛡️ 防御：禁止通过此 update 接口修改密码或账号 (应走专门的 updatePassword 接口)
        admin.setPassword(null);
        admin.setUsername(null);
        return this.updateById(admin);
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void resetPassword(Long userId, String newPassword) {
        if (StrUtil.length(newPassword) < 6) {
            throw new ServiceException("密码长度不能少于6位");
        }
        
        String encodePwd = BCrypt.hashpw(newPassword, BCrypt.gensalt());
        
        this.update(Wrappers.<SysAdminUser>lambdaUpdate()
                .eq(SysAdminUser::getId, userId)
                .set(SysAdminUser::getPassword, encodePwd));
    }
}