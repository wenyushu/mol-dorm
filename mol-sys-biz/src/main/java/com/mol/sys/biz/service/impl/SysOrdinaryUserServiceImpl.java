package com.mol.sys.biz.service.impl;

import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.digest.BCrypt;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mol.common.core.entity.SysOrdinaryUser;
import com.mol.common.core.exception.ServiceException;
import com.mol.sys.biz.mapper.SysCollegeMapper;
import com.mol.sys.biz.mapper.SysOrdinaryUserMapper;
import com.mol.sys.biz.service.SysOrdinaryUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Year;

/**
 * 普通用户 (学生/教工) 业务实现类
 * <p>
 * 特性：
 * 1. 自动生成学号/工号 (入学年份 + 层次 + 学院代码 + 流水号)
 * 2. 自动加密密码
 * 3. 关联组织架构校验
 */
@Service
@RequiredArgsConstructor // 使用 Lombok 自动注入 final 字段 (推荐)
public class SysOrdinaryUserServiceImpl extends ServiceImpl<SysOrdinaryUserMapper, SysOrdinaryUser> implements SysOrdinaryUserService {
    
    // 注入学院 Mapper，用于查询学院代码 (Code)
    private final SysCollegeMapper collegeMapper;
    
    /**
     * 新增用户 (核心逻辑：自动生成唯一账号)
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean saveUser(SysOrdinaryUser user) {
        // 1. 归属地强制校验
        if (user.getUserCategory() == 0) { // 0-学生
            if (user.getCollegeId() == null || user.getMajorId() == null || user.getClassId() == null) {
                throw new ServiceException("添加学生时，必须完整指定：学院、专业、班级！");
            }
            if (StrUtil.isBlank(user.getEduLevel())) {
                throw new ServiceException("添加学生时，必须指定培养层次 (如 B-本科)！");
            }
        } else { // 1-教工
            if (user.getCollegeId() == null) {
                throw new ServiceException("添加教职工时，必须指定所属部门(学院)！");
            }
        }
        
        // 默认当前年份
        if (user.getEntryYear() == null) {
            user.setEntryYear(Year.now().getValue());
        }
        
        // 2. 【核心】自动生成学号/工号
        // 步骤 A: 计算前缀 (如 "25B06")
        String prefix = generatePrefix(user);
        // 步骤 B: 计算流水号并拼接 (如 "25B060001")
        String finalUsername = generateUniqueUsername(prefix);
        user.setUsername(finalUsername);
        
        // 3. 密码与状态处理
        if (StrUtil.isBlank(user.getPassword())) {
            user.setPassword("123456");
        }
        user.setPassword(BCrypt.hashpw(user.getPassword(), BCrypt.gensalt()));
        
        if (user.getAccountStatus() == null) {
            user.setAccountStatus(1);
        }
        
        return this.save(user);
    }
    
    /**
     * 修改用户信息 (禁止修改学号和关键归属)
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateUser(SysOrdinaryUser user) {
        user.setPassword(null); // 密码修改走专属接口
        user.setUsername(null); // 禁止改学号
        // 暂时禁止修改学院/专业 (转专业逻辑较复杂，建议另开接口)
        user.setCollegeId(null);
        user.setMajorId(null);
        return this.updateById(user);
    }
    
    // ----------------- 辅助方法：生成学号逻辑 -----------------
    
    /**
     * 生成前缀：年份后2位 + [身份/层次] + [学院代码]
     * 例：2025年 + 本科(B) + 网安院(06) -> "25B06"
     */
    private String generatePrefix(SysOrdinaryUser user) {
        String yearCode = String.valueOf(user.getEntryYear()).substring(2);
        
        // 查学院代码 (必需)
        String collegeCode = collegeMapper.selectCodeById(user.getCollegeId());
        if (StrUtil.isBlank(collegeCode)) {
            throw new ServiceException("选中的学院数据异常，未配置学院代码 (Code)");
        }
        
        if (user.getUserCategory() == 1) {
            // 教工规则：19 + JZG + 06 -> "19JZG06"
            return yearCode + "JZG" + collegeCode;
        } else {
            // 学生规则：25 + B + 06 -> "25B06"
            return yearCode + user.getEduLevel().toUpperCase() + collegeCode;
        }
    }
    
    /**
     * 生成唯一账号：前缀 + 4位流水号
     * 查库里最大的 "25B06%"，如果是 "25B060005"，则新生成 "25B060006"
     */
    private synchronized String generateUniqueUsername(String prefix) {
        SysOrdinaryUser maxUser = this.getOne(new LambdaQueryWrapper<SysOrdinaryUser>()
                .likeRight(SysOrdinaryUser::getUsername, prefix) // LIKE 'prefix%'
                .orderByDesc(SysOrdinaryUser::getUsername)
                .last("LIMIT 1"));
        
        int sequence = 1;
        if (maxUser != null) {
            // 截取后缀数字
            String suffix = maxUser.getUsername().substring(prefix.length());
            if (StrUtil.isNumeric(suffix)) {
                sequence = Integer.parseInt(suffix) + 1;
            }
        }
        // 补齐 0 (例如 1 -> "0001")
        return prefix + String.format("%04d", sequence);
    }
    
    // ----------------- 密码管理方法 (保持原样) -----------------
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void resetPassword(Long userId, String newPassword) {
        SysOrdinaryUser user = this.getById(userId);
        if (user == null) throw new ServiceException("用户不存在");
        
        String encodePwd = BCrypt.hashpw(newPassword, BCrypt.gensalt());
        this.update(Wrappers.<SysOrdinaryUser>lambdaUpdate()
                .eq(SysOrdinaryUser::getId, userId)
                .set(SysOrdinaryUser::getPassword, encodePwd));
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updatePassword(Long userId, String oldPassword, String newPassword) {
        SysOrdinaryUser user = this.getById(userId);
        if (user == null) throw new ServiceException("用户不存在");
        
        if (!BCrypt.checkpw(oldPassword, user.getPassword())) {
            throw new ServiceException("旧密码输入错误");
        }
        if (StrUtil.length(newPassword) < 6) {
            throw new ServiceException("新密码长度不能少于6位");
        }
        
        String encodeNewPwd = BCrypt.hashpw(newPassword, BCrypt.gensalt());
        this.update(Wrappers.<SysOrdinaryUser>lambdaUpdate()
                .eq(SysOrdinaryUser::getId, userId)
                .set(SysOrdinaryUser::getPassword, encodeNewPwd));
    }
}