package com.mol.server.service;

import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.mol.common.core.constant.RoleConstants;
import com.mol.common.core.entity.SysAdminUser;
import com.mol.common.core.entity.SysOrdinaryUser;
import com.mol.common.core.exception.ServiceException;
import com.mol.common.core.util.LoginHelper;
import com.mol.server.mapper.SysAdminUserMapper;
import com.mol.server.mapper.SysOrdinaryUserMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 寒暑假状态管理服务 (支持 普通用户 和 管理员)
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CampusStatusService {
    
    private final SysOrdinaryUserMapper ordinaryUserMapper;
    // 🟢 1. 引入管理员 Mapper
    private final SysAdminUserMapper adminUserMapper;
    
    /**
     * 切换在校/离校状态
     * @param targetStatus 1:返校 0:离校
     */
    @Transactional(rollbackFor = Exception.class)
    public void toggleCampusStatus(Integer targetStatus) {
        // 1. 获取当前登录用户 ID
        Long userId = LoginHelper.getUserId();
        
        // 2. 参数校验
        if (!ObjectUtil.contains(new Integer[]{0, 1}, targetStatus)) {
            throw new ServiceException("非法状态码");
        }
        
        // 3. 🛡️ 核心：判断用户身份 (根据角色决定查哪张表)
        if (isOrdinaryUser()) {
            // ---> 处理普通用户 (学生/教工/职工)
            updateOrdinaryStatus(userId, targetStatus);
        } else {
            // ---> 处理管理员 (超管/宿管/辅导员等)
            updateAdminStatus(userId, targetStatus);
        }
    }
    
    // =================================================================================
    // 🕵️‍♂️ 私有方法
    // =================================================================================
    
    /**
     * 判断当前登录用户是否为普通用户 (学生/老师/职工)
     */
    private boolean isOrdinaryUser() {
        // 获取当前用户的所有角色
        List<String> roles = StpUtil.getRoleList();
        
        // 只要包含以下任意一个角色，就是普通用户表的人
        return roles.contains(RoleConstants.STUDENT)
                || roles.contains(RoleConstants.COLLEGE_TEACHER)
                || roles.contains(RoleConstants.STAFF);
    }
    
    /**
     * 更新普通用户表 (sys_ordinary_user)
     */
    private void updateOrdinaryStatus(Long userId, Integer status) {
        SysOrdinaryUser user = ordinaryUserMapper.selectById(userId);
        
        // 状态检查：账号被封禁(1)或已归档(2)不能操作
        if (user == null || !"0".equals(user.getStatus())) {
            throw new ServiceException("您的账号状态异常，无法进行打卡操作");
        }
        
        ordinaryUserMapper.update(null, new LambdaUpdateWrapper<SysOrdinaryUser>()
                .set(SysOrdinaryUser::getCampusStatus, status)
                .set(SysOrdinaryUser::getUpdateTime, LocalDateTime.now())
                .eq(SysOrdinaryUser::getId, userId));
        
        log.info("普通用户[{}] 在校状态更新为: {}", user.getRealName(), status);
    }
    
    /**
     * 更新管理员表 (sys_admin_user)
     */
    private void updateAdminStatus(Long userId, Integer status) {
        SysAdminUser admin = adminUserMapper.selectById(userId);
        
        if (admin == null || !"0".equals(admin.getStatus())) {
            throw new ServiceException("管理员账号状态异常，无法操作");
        }
        
        adminUserMapper.update(null, new LambdaUpdateWrapper<SysAdminUser>()
                .set(SysAdminUser::getCampusStatus, status)
                .set(SysAdminUser::getUpdateTime, LocalDateTime.now())
                .eq(SysAdminUser::getId, userId));
        
        log.info("管理员[{}] 在校状态更新为: {}", admin.getRealName(), status);
    }
}