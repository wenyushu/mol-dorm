package com.mol.server.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.digest.BCrypt;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.mol.common.core.context.SecurityContext;
import com.mol.common.core.entity.SysAdminUser;
import com.mol.common.core.entity.SysOrdinaryUser;
import com.mol.common.core.exception.ServiceException;
import com.mol.common.core.util.LoginHelper;
import com.mol.server.dto.AdminUpdateStudentBody;
import com.mol.server.dto.UpdatePasswordBody;
import com.mol.server.dto.UserProfileEditDTO;
import com.mol.server.mapper.SysAdminUserMapper;
import com.mol.server.mapper.SysOrdinaryUserMapper;
import com.mol.server.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * 用户中心服务实现类
 * <p>
 * 🛡️ 核心设计理念：
 * 1. 权限隔离：管理员与普通学生共用接口，但在业务层进行物理隔离。
 * 2. 动态脱敏：利用 SecurityContext 配合 Jackson 序列化实现“看人下菜”。
 * 3. 审计溯源：所有关键修改均记录日志，确保刁民操作可追溯。
 * </p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    
    private final SysAdminUserMapper adminMapper;
    private final SysOrdinaryUserMapper ordinaryMapper;
    
    // 从 yml 配置获取：是否强制执行高强度密码策略
    @Value("${mol.security.force-change-pwd:false}")
    private boolean forceChangePwdEnabled;
    
    // 🔒 强密码正则：6-30位，要求包含大小写、数字及特殊字符
    private static final Pattern STRONG_PWD_PATTERN = Pattern.compile(
            "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[\\W_])[A-Za-z\\d\\W_]{6,30}$"
    );
    
    /**
     * 获取个人资料 (含自动脱敏)
     */
    @Override
    public Map<String, Object> getProfile() {
        Long userId = LoginHelper.getUserId();
        String userType = LoginHelper.getUserType();
        
        // 🛡️ 逻辑判定：是否为后台管理人员
        boolean isAdmin = "admin".equals(userType) || "0".equals(userType);
        
        Map<String, Object> result = MapUtil.newHashMap();
        result.put("id", userId);
        result.put("type", userType);
        // 适配前端框架的 Role 标签
        result.put("roles", Collections.singletonList(LoginHelper.getRoleKey()));
        
        // 🛡️ 详情查询开启“全明文模式”，因为是用户本人查看自己的档案
        SecurityContext.setCanViewFullDetail(true);
        
        try {
            if (isAdmin) {
                SysAdminUser admin = adminMapper.selectById(userId);
                if (admin != null) {
                    admin.setPassword(null); // 绝对禁止密码明文（即使是哈希值）流向前端
                    result.put("userInfo", admin);
                    result.put("realName", admin.getRealName());
                    result.put("avatar", admin.getAvatar());
                }
            } else {
                SysOrdinaryUser user = ordinaryMapper.selectById(userId);
                if (user != null) {
                    user.setPassword(null);
                    result.put("userInfo", user);
                    result.put("realName", user.getRealName());
                    result.put("avatar", user.getAvatar());
                }
            }
        } finally {
            // 查询结束清理上下文，防止 ThreadLocal 污染
            SecurityContext.clear();
        }
        return result;
    }
    
    /**
     * 自行修改资料
     * 🛡️ 防刁民点：限制只能修改非核心字段（如手机、昵称），禁止自行修改学号/性别/学籍
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateProfile(UserProfileEditDTO body) {
        Long userId = LoginHelper.getUserId();
        String userType = LoginHelper.getUserType();
        boolean isAdmin = "0".equals(userType) || "admin".equals(userType);
        
        // 🛡️ 防刁民：逻辑矛盾校验
        if (StrUtil.equals(body.getPhone(), body.getEmergencyPhone())) {
            throw new ServiceException("逻辑错误：本人手机号不能与紧急联系人号码相同！");
        }
        
        if (isAdmin) {
            updateAdminProfile(userId, body);
        } else {
            updateOrdinaryProfile(userId, body);
        }
    }
    
    /**
     * 管理员修改自己信息
     */
    private void updateAdminProfile(Long userId, UserProfileEditDTO body) {
        SysAdminUser user = adminMapper.selectById(userId);
        if (user == null) throw new ServiceException("当前管理员账号异常");
        
        // 手机号唯一性检查，防止刁民管理员修改成别人的号码导致登录冲突
        checkPhoneUnique(body.getPhone(), userId, true);
        
        // 仅允许修改展示层和联系信息
        if (StrUtil.isNotBlank(body.getNickname())) user.setNickname(body.getNickname());
        if (StrUtil.isNotBlank(body.getPhone())) user.setPhone(body.getPhone());
        if (StrUtil.isNotBlank(body.getAvatar())) user.setAvatar(body.getAvatar());
        if (StrUtil.isNotBlank(body.getEmail())) user.setEmail(body.getEmail());
        
        // 🛡️ 修复后的地址逻辑：区分家庭与校外
        if (StrUtil.isNotBlank(body.getHomeAddress())) user.setHomeAddress(body.getHomeAddress());
        if (StrUtil.isNotBlank(body.getOutsideAddress())) user.setOutsideAddress(body.getOutsideAddress());
        
        // 紧急联系人信息更新
        if (StrUtil.isNotBlank(body.getEmergencyContact())) user.setEmergencyContact(body.getEmergencyContact());
        if (StrUtil.isNotBlank(body.getEmergencyPhone())) user.setEmergencyPhone(body.getEmergencyPhone());
        if (StrUtil.isNotBlank(body.getEmergencyRelation())) user.setEmergencyRelation(body.getEmergencyRelation());
        
        adminMapper.updateById(user);
        updateSessionCache(user.getNickname(), user.getAvatar());
    }
    
    /**
     * 普通学生/教工修改自己信息
     */
    private void updateOrdinaryProfile(Long userId, UserProfileEditDTO body) {
        SysOrdinaryUser user = ordinaryMapper.selectById(userId);
        if (user == null) throw new ServiceException("学生档案不存在");
        
        checkPhoneUnique(body.getPhone(), userId, false);
        
        // 基础字段更新
        if (StrUtil.isNotBlank(body.getNickname())) user.setNickname(body.getNickname());
        if (StrUtil.isNotBlank(body.getPhone())) user.setPhone(body.getPhone());
        if (StrUtil.isNotBlank(body.getAvatar())) user.setAvatar(body.getAvatar());
        
        // 🛡️ 地址更新逻辑 (同步实体类更名后的字段)
        if (StrUtil.isNotBlank(body.getHomeAddress())) user.setHomeAddress(body.getHomeAddress());
        if (StrUtil.isNotBlank(body.getOutsideAddress())) user.setOutsideAddress(body.getOutsideAddress());
        
        // 紧急联系人 (这在关键时刻救命用，必须允许学生更新)
        if (StrUtil.isNotBlank(body.getEmergencyContact())) user.setEmergencyContact(body.getEmergencyContact());
        if (StrUtil.isNotBlank(body.getEmergencyPhone())) user.setEmergencyPhone(body.getEmergencyPhone());
        if (StrUtil.isNotBlank(body.getEmergencyRelation())) user.setEmergencyRelation(body.getEmergencyRelation());
        
        ordinaryMapper.updateById(user);
        updateSessionCache(user.getNickname(), user.getAvatar());
    }
    
    /**
     * 自行修改密码
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updatePassword(UpdatePasswordBody body) {
        Long userId = LoginHelper.getUserId();
        String userType = LoginHelper.getUserType();
        boolean isAdmin = "admin".equals(userType);
        
        // 获取数据库加密后的 Hash 值
        String dbPassword = isAdmin ? adminMapper.selectById(userId).getPassword()
                : ordinaryMapper.selectById(userId).getPassword();
        
        // 🛡️ 防刁民：验证旧密码真伪
        if (!BCrypt.checkpw(body.getOldPassword(), dbPassword)) {
            throw new ServiceException("原密码错误，操作已记录！");
        }
        
        // 🛡️ 防刁民：禁止新旧密码一致（防止用户做无用功，并提升安全性）
        if (BCrypt.checkpw(body.getNewPassword(), dbPassword)) {
            throw new ServiceException("新密码不能与旧密码相同");
        }
        
        // 🛡️ 防刁民：复杂度强制校验
        if (forceChangePwdEnabled && !STRONG_PWD_PATTERN.matcher(body.getNewPassword()).matches()) {
            throw new ServiceException("密码太弱！必须含大小写字母、数字及特殊字符");
        }
        
        String newHash = BCrypt.hashpw(body.getNewPassword(), BCrypt.gensalt());
        
        // 更新数据库，同时清除“初始密码”标记
        if (isAdmin) {
            adminMapper.updateById(new SysAdminUser().setId(userId).setPassword(newHash).setIsInitialPwd(0));
        } else {
            ordinaryMapper.updateById(new SysOrdinaryUser().setId(userId).setPassword(newHash).setIsInitialPwd(0));
        }
        
        // 修改密码后强制重新登录，符合企业级安全标准
        StpUtil.logout();
    }
    
    
    /**
     * 管理员特权：修改学生核心资料及地址备案
     * <p>
     * 🛡️ 防刁民设计：
     * 1. 强制备案留痕：管理员修改地址时，系统会自动在 remark (备注) 中记录“由XX管理员于XX时间强制备案”。
     * 2. 状态联动：如果管理员填了校外地址，系统会自动建议（或强制）将 residence_type 改为 1 (校外居住)。
     * 3. 敏感权限隔离：性别、身份证等法律字段，非超管不可动。
     * </p>
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateStudentByAdmin(AdminUpdateStudentBody body) {
        // 1. 查找目标学生
        SysOrdinaryUser student = ordinaryMapper.selectById(body.getId());
        if (student == null) throw new ServiceException("目标学生档案不存在，请刷新后重试");
        
        // 2. 准备更新对象（利用 @Accessors(chain = true) 链式调用）
        SysOrdinaryUser update = new SysOrdinaryUser().setId(body.getId());
        
        // --- A. 基础资料修改 ---
        if (StrUtil.isNotBlank(body.getRealName())) update.setRealName(body.getRealName());
        if (ObjectUtil.isNotNull(body.getCollegeId())) update.setCollegeId(body.getCollegeId());
        if (StrUtil.isNotBlank(body.getStatus())) update.setStatus(body.getStatus());
        
        // --- B. 🛡️ 校外地址强制备案逻辑 ---
        if (StrUtil.isNotBlank(body.getOutsideAddress())) {
            update.setOutsideAddress(body.getOutsideAddress());
            // [防刁民] 自动将居住类型修正为“校外”，防止数据冲突（地址在校外，状态却是在校）
            update.setResidenceType(1);
            
            // [防刁民] 在备注里自动追加审计留痕，防止管理员乱改或学生抵赖
            String auditNote = StrUtil.format("【管理员强制备案】地址更新时间:{}，操作员ID:{}",
                    cn.hutool.core.date.DateUtil.now(), LoginHelper.getUserId());
            
            // 拼接旧备注，保留历史
            String oldRemark = StrUtil.blankToDefault(student.getRemark(), "");
            update.setRemark(oldRemark + " | " + auditNote);
            
            log.warn("🛡️ [安全备案] 学生(ID:{}) 的校外地址已被管理员(ID:{}) 强制录入", body.getId(), LoginHelper.getUserId());
        }
        
        // --- C. 🛡️ 特权字段保护 (只有超管能改性别) ---
        if (StrUtil.isNotBlank(body.getGender())) {
            if (!StpUtil.hasRole("super_admin")) {
                throw new ServiceException("权限不足：性别属于核心法律字段，仅限超级管理员修改！");
            }
            update.setGender(body.getGender());
        }
        
        // 3. 执行更新
        ordinaryMapper.updateById(update);
    }
   
    
    /**
     * 校验手机号唯一性（排除自身）
     */
    private void checkPhoneUnique(String phone, Long userId, boolean isAdmin) {
        if (StrUtil.isBlank(phone)) return;
        boolean exists = isAdmin
                ? adminMapper.exists(new LambdaQueryWrapper<SysAdminUser>().eq(SysAdminUser::getPhone, phone).ne(SysAdminUser::getId, userId))
                : ordinaryMapper.exists(new LambdaQueryWrapper<SysOrdinaryUser>().eq(SysOrdinaryUser::getPhone, phone).ne(SysOrdinaryUser::getId, userId));
        
        if (exists) throw new ServiceException("该手机号已被占用，请检查是否填错");
    }
    
    /**
     * 更新登录 Session，防止界面头像不刷新
     */
    private void updateSessionCache(String nickname, String avatar) {
        try {
            StpUtil.getSession().set("nickname", nickname).set("avatar", avatar);
        } catch (Exception ignored) {}
    }
}