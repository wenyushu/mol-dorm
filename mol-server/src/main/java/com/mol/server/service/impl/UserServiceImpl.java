package com.mol.server.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.digest.BCrypt; // 🟢 确认使用 Hutool
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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    
    private final SysAdminUserMapper adminMapper;
    private final SysOrdinaryUserMapper ordinaryMapper;
    
    // 🟢 注入开关，只有开启时才校验强密码格式 (或者你可以选择一直校验)
    @Value("${mol.security.force-change-pwd:false}")
    private boolean forceChangePwdEnabled;
    
    // 🔒 强密码正则：6-30位，包含大小写字母、数字、特殊字符
    // (?=.*[a-z]) 小写
    // (?=.*[A-Z]) 大写
    // (?=.*\d) 数字
    // (?=.*[\W_]) 特殊字符 (非单词字符)
    private static final Pattern STRONG_PWD_PATTERN = Pattern.compile(
            "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[\\W_])[A-Za-z\\d\\W_]{6,30}$"
    );
    
    
    // ==========================================
    // 场景 A: 个人修改资料
    // ==========================================
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateProfile(UserProfileBody body) {
        Long userId = LoginHelper.getUserId();
        
        // 🟢 修复1: 返回类型改为 String (LoginHelper 返回的是 String)
        String userType = LoginHelper.getUserType();
        
        // 🟢 修复2: 判断逻辑兼容 "0" (Token解析值) 和 "admin" (Session存储值)
        boolean isAdmin = "0".equals(userType) || "admin".equals(userType);
        
        // --- 1. 修改管理员资料 ---
        if (isAdmin) {
            SysAdminUser user = adminMapper.selectById(userId);
            if (user == null) throw new ServiceException("用户不存在");
            
            checkPhoneUnique(body.getPhone(), userId, true);
            
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
            
            if (user == null) throw new ServiceException("用户不存在");
            
            checkPhoneUnique(body.getPhone(), userId, false);
            
            if (StrUtil.isNotBlank(body.getNickname())) user.setNickname(body.getNickname());
            if (StrUtil.isNotBlank(body.getPhone())) user.setPhone(body.getPhone());
            if (StrUtil.isNotBlank(body.getAvatar())) user.setAvatar(body.getAvatar());
            
            ordinaryMapper.updateById(user);
            updateSessionCache(user.getNickname(), user.getAvatar());
        }
    }
    
    // ==========================================
    // 场景 B: 修改密码
    // ==========================================
    @Override
    public void updatePassword(UpdatePasswordBody body) {
        Long userId = LoginHelper.getUserId();
        String userType = LoginHelper.getUserType();
        // 兼容 "0" 和 "admin"
        boolean isAdmin = "0".equals(userType) || "admin".equals(userType);
        
        String dbPassword;

        // 1. 先查出旧密码进行比对
        if (isAdmin) {
            SysAdminUser admin = adminMapper.selectById(userId);
            if (admin == null) throw new ServiceException("用户不存在");
            dbPassword = admin.getPassword();
        } else {
            SysOrdinaryUser user = ordinaryMapper.selectById(userId);
            if (user == null) throw new ServiceException("用户不存在");
            dbPassword = user.getPassword();
        }
        
        // 2. 🟢 校验：新密码不能与旧密码一致 (通过 hash 比较)
        if (!BCrypt.checkpw(body.getOldPassword(), dbPassword)) {
            throw new ServiceException("旧密码错误");
        }
        if (BCrypt.checkpw(body.getNewPassword(), dbPassword)) {
            throw new ServiceException("新密码不能与当前密码一致");
        }
        
        // 3. 🟢 校验：强密码规则 (仅在开关开启时，或者你可以去掉 if 强制一直校验)
        // 如果你希望无论开不开“强制改密”，修改密码时都必须是强密码，就去掉 if (forceChangePwdEnabled)
        if (forceChangePwdEnabled) {
            if (!STRONG_PWD_PATTERN.matcher(body.getNewPassword()).matches()) {
                throw new ServiceException("密码强度不足！需包含大小写字母、数字及特殊字符，长度6-30位");
            }
        } else {
            // 即使没开启强校验，基本的长度校验还是要有的
            if (body.getNewPassword().length() < 6) {
                throw new ServiceException("新密码长度不能少于6位");
            }
        }
        
        // 4. 加密新密码
        String newHash = BCrypt.hashpw(body.getNewPassword(), BCrypt.gensalt());

        // 5. 🟢 消除警告的核心改动：在这里直接创建对象并更新，逻辑更清晰
        if (isAdmin) {
            SysAdminUser update = new SysAdminUser();
            update.setId(userId);
            update.setPassword(newHash);
            update.setIsInitialPwd(0); // 解除初始状态
            adminMapper.updateById(update);
        } else {
            SysOrdinaryUser update = new SysOrdinaryUser();
            update.setId(userId);
            update.setPassword(newHash);
            update.setIsInitialPwd(0); // 解除初始状态
            ordinaryMapper.updateById(update);
        }
        
        // 6. 踢下线
        StpUtil.logout();
    }
    
    // ==========================================
    // 场景 C: 管理员修改学生信息
    // ==========================================
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateStudentByAdmin(AdminUpdateStudentBody body) {
        SysOrdinaryUser student = ordinaryMapper.selectById(body.getId());
        if (student == null) throw new ServiceException("学生不存在");
        
        SysOrdinaryUser update = new SysOrdinaryUser();
        update.setId(body.getId());
        
        if (StrUtil.isNotBlank(body.getRealName())) update.setRealName(body.getRealName());
        if (ObjectUtil.isNotNull(body.getCollegeId())) update.setCollegeId(body.getCollegeId());
        if (ObjectUtil.isNotNull(body.getMajorId())) update.setMajorId(body.getMajorId());
        if (ObjectUtil.isNotNull(body.getClassId())) update.setClassId(body.getClassId());
        if (StrUtil.isNotBlank(body.getStatus())) update.setStatus(body.getStatus());
        
        // 特权：修改性别
        if (StrUtil.isNotBlank(body.getGender())) {
            if (!StpUtil.hasRole("super_admin")) {
                throw new ServiceException("权限不足：性别仅限超级管理员修改，请联系运维人员");
            }
            update.setGender(body.getGender());
        }
        
        ordinaryMapper.updateById(update);
    }
    
    // --- 内部辅助方法 ---
    
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
    
    private void updateSessionCache(String nickname, String avatar) {
        StpUtil.getSession().set("nickname", nickname);
        StpUtil.getSession().set("avatar", avatar);
    }
}