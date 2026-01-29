package com.mol.server.service;

import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.mol.common.core.entity.SysOrdinaryUser;
import com.mol.common.core.exception.ServiceException;
import com.mol.common.core.util.LoginHelper;
import com.mol.server.mapper.SysOrdinaryUserMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * 寒暑假状态管理服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CampusStatusService {
    
    private final SysOrdinaryUserMapper userMapper;
    
    /**
     * 切换在校/离校状态
     * @param targetStatus 1:返校 0:离校
     */
    @Transactional(rollbackFor = Exception.class)
    public void toggleCampusStatus(Integer targetStatus) {
        // 1. 获取当前登录用户
        Long userId = LoginHelper.getUserId();
        
        // 2. 🛡️ 防刁民：检查用户是否处于"正常"状态
        // 如果用户已经被归档(status=2)或者封禁(status=1)，不允许他自己修改在校状态
        SysOrdinaryUser user = userMapper.selectById(userId);
        if (user == null || !"0".equals(user.getStatus())) {
            throw new ServiceException("账号状态异常，无法进行打卡操作");
        }
        
        // 3. 🛡️ 防刁民：参数校验
        if (!ObjectUtil.contains(new Integer[]{0, 1}, targetStatus)) {
            throw new ServiceException("非法状态码");
        }
        
        // 4. 执行更新
        userMapper.update(null, new LambdaUpdateWrapper<SysOrdinaryUser>()
                .set(SysOrdinaryUser::getCampusStatus, targetStatus)
                .set(SysOrdinaryUser::getUpdateTime, LocalDateTime.now())
                .eq(SysOrdinaryUser::getId, userId));
        
        log.info("用户[{}] 更新在校状态为: {}", userId, targetStatus == 1 ? "在校" : "离校");
    }
}