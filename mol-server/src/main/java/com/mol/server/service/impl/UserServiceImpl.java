package com.mol.server.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.digest.BCrypt;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.mol.common.core.entity.SysAdminUser;
import com.mol.common.core.entity.SysOrdinaryUser;
import com.mol.common.core.exception.ServiceException;
import com.mol.common.core.util.LoginHelper;
import com.mol.server.dto.AdminUpdateStudentBody;
import com.mol.server.dto.UpdatePasswordBody;
import com.mol.server.dto.UserProfileBody;
import com.mol.server.mapper.SysAdminUserMapper;
import com.mol.server.mapper.SysOrdinaryUserMapper;
import com.mol.server.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    
    private final SysAdminUserMapper adminMapper;
    private final SysOrdinaryUserMapper ordinaryMapper;
    
    // ==========================================
    // 场景 A: 个人修改资料
    // 🛡️ 防刁民核心：只从 DTO 取允许改的字段，其他字段一律不碰
    // ==========================================
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateProfile(UserProfileBody body) {
        Long userId = LoginHelper.getUserId();
        String userType = LoginHelper.getUserType();
        
        // --- 1. 修改管理员资料 ---
        if ("admin".equals(userType)) {
            SysAdminUser user = adminMapper.selectById(userId);
            checkPhoneUnique(body.getPhone(), userId, true); // 检查手机号
            
            // 更新非敏感信息
            if (StrUtil.isNotBlank(body.getNickname())) user.setNickname(body.getNickname());
            if (StrUtil.isNotBlank(body.getPhone())) user.setPhone(body.getPhone());
            if (StrUtil.isNotBlank(body.getAvatar())) user.setAvatar(body.getAvatar());
            if (StrUtil.isNotBlank(body.getEmail())) user.setEmail(body.getEmail());
            
            adminMapper.updateById(user);
            updateSessionCache(user.getNickname(), user.getAvatar());
        }
        // --- 2. 修改学生资料 ---
        else {
            SysOrdinaryUser user = ordinaryMapper.selectById(userId);
            checkPhoneUnique(body.getPhone(), userId, false);
            
            // 🛡️ 这里绝对不调用 setGender()，setRealName()，setUsername()
            // 确保学生无法通过此接口修改性别、姓名、学号
            if (StrUtil.isNotBlank(body.getNickname())) user.setNickname(body.getNickname());
            if (StrUtil.isNotBlank(body.getPhone())) user.setPhone(body.getPhone());
            if (StrUtil.isNotBlank(body.getAvatar())) user.setAvatar(body.getAvatar());
            
            ordinaryMapper.updateById(user);
            updateSessionCache(user.getNickname(), user.getAvatar());
        }
    }
    
    // ==========================================
    // 场景 B: 修改密码 (代码与之前一致，省略重复注释)
    // ==========================================
    @Override
    public void updatePassword(UpdatePasswordBody body) {
        Long userId = LoginHelper.getUserId();
        String userType = LoginHelper.getUserType();
        String dbPassword;
        
        if ("admin".equals(userType)) {
            dbPassword = adminMapper.selectById(userId).getPassword();
        } else {
            dbPassword = ordinaryMapper.selectById(userId).getPassword();
        }
        
        if (!BCrypt.checkpw(body.getOldPassword(), dbPassword)) {
            throw new ServiceException("旧密码错误");
        }
        if (BCrypt.checkpw(body.getNewPassword(), dbPassword)) {
            throw new ServiceException("新密码不能与旧密码相同");
        }
        
        String newHash = BCrypt.hashpw(body.getNewPassword());
        
        if ("admin".equals(userType)) {
            SysAdminUser update = new SysAdminUser();
            update.setId(userId);
            update.setPassword(newHash);
            adminMapper.updateById(update);
        } else {
            SysOrdinaryUser update = new SysOrdinaryUser();
            update.setId(userId);
            update.setPassword(newHash);
            ordinaryMapper.updateById(update);
        }
        StpUtil.logout();
    }
    
    // ==========================================
    // 场景 C: 管理员修改学生信息 (特权操作)
    // ==========================================
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateStudentByAdmin(AdminUpdateStudentBody body) {
        SysOrdinaryUser student = ordinaryMapper.selectById(body.getId());
        if (student == null) throw new ServiceException("学生不存在");
        
        SysOrdinaryUser update = new SysOrdinaryUser();
        update.setId(body.getId());
        
        // 基础学籍变更
        if (StrUtil.isNotBlank(body.getRealName())) update.setRealName(body.getRealName());
        if (ObjectUtil.isNotNull(body.getCollegeId())) update.setCollegeId(body.getCollegeId());
        if (ObjectUtil.isNotNull(body.getMajorId())) update.setMajorId(body.getMajorId());
        if (ObjectUtil.isNotNull(body.getClassId())) update.setClassId(body.getClassId());
        if (StrUtil.isNotBlank(body.getStatus())) update.setStatus(body.getStatus());
        
        // 🟢 特权：管理员可以修正性别 (例如新生录入错误)
        if (StrUtil.isNotBlank(body.getGender())) update.setGender(body.getGender());
        
        // 🛡️ 依然不更新 Username (学号)，学号是系统唯一标识，通常不允许变更
        // 如果非要变学号，建议删除重开账号
        
        ordinaryMapper.updateById(update);
    }
    
    // --- 内部辅助方法 ---
    
    // 检查手机号唯一性
    private void checkPhoneUnique(String phone, Long userId, boolean isAdmin) {
        if (StrUtil.isBlank(phone)) return;
        
        boolean exists;
        if (isAdmin) {
            exists = adminMapper.exists(new LambdaQueryWrapper<SysAdminUser>()
                    .eq(SysAdminUser::getPhone, phone)
                    .ne(SysAdminUser::getId, userId));
        } else {
            exists = ordinaryMapper.exists(new LambdaQueryWrapper<SysOrdinaryUser>()
                    .eq(SysOrdinaryUser::getPhone, phone)
                    .ne(SysOrdinaryUser::getId, userId));
        }
        if (exists) throw new ServiceException("该手机号已被绑定");
    }
    
    // 更新 Session
    private void updateSessionCache(String nickname, String avatar) {
        StpUtil.getSession().set("nickname", nickname);
        StpUtil.getSession().set("avatar", avatar);
    }
}