package com.mol.sys.biz.service.impl;

import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.digest.BCrypt;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mol.common.core.entity.SysAdminUser;
import com.mol.common.core.exception.ServiceException;
import com.mol.sys.biz.mapper.SysAdminUserMapper;
import com.mol.sys.biz.service.SysAdminUserService;
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
        
        // 2. 账号查重 (确保 username 唯一)
        long count = this.count(new LambdaQueryWrapper<SysAdminUser>()
                .eq(SysAdminUser::getUsername, admin.getUsername()));
        if (count > 0) {
            throw new ServiceException("该管理员账号已存在，请更换");
        }
        
        // 3. 密码加密 (默认 123456)
        if (StrUtil.isBlank(admin.getPassword())) {
            admin.setPassword("123456");
        }
        // 使用 BCrypt 加密存储，与 AuthService 登录逻辑对应
        admin.setPassword(BCrypt.hashpw(admin.getPassword(), BCrypt.gensalt()));
        
        // 4. 设置默认状态 (0-正常)
        if (StrUtil.isBlank(admin.getStatus())) {
            admin.setStatus("0");
        }
        
        return this.save(admin);
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateAdmin(SysAdminUser admin) {
        // 禁止通过此接口修改密码或账号
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
        
        // 加密新密码
        String encodePwd = BCrypt.hashpw(newPassword, BCrypt.gensalt());
        
        // 更新数据库
        this.update(Wrappers.<SysAdminUser>lambdaUpdate()
                .eq(SysAdminUser::getId, userId)
                .set(SysAdminUser::getPassword, encodePwd));
    }
}