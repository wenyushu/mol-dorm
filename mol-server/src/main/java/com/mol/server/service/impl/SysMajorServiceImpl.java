package com.mol.server.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mol.common.core.exception.ServiceException;
import com.mol.server.entity.SysClass;
import com.mol.server.entity.SysMajor;
import com.mol.server.mapper.SysClassMapper;
import com.mol.server.mapper.SysMajorMapper;
import com.mol.server.service.SysMajorService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.Serializable;

/**
 * 【专业】业务实现
 */
@Service
@RequiredArgsConstructor
public class SysMajorServiceImpl extends ServiceImpl<SysMajorMapper, SysMajor> implements SysMajorService {
    
    private final SysClassMapper classMapper;
    
    // 🛡️ 常量定义：学制范围限制
    private static final int MIN_DURATION = 1;
    private static final int MAX_DURATION = 10; // 即使是医学本博连读通常也才8年，10年是合理的上限
    
    // =================================================================================
    // 1. 新增专业
    // =================================================================================
    @Override
    public boolean save(SysMajor major) {
        // A. 基础非空校验
        if (StrUtil.isBlank(major.getName())) throw new ServiceException("新增失败：专业名称不能为空");
        if (major.getCollegeId() == null) throw new ServiceException("新增失败：必须指定所属学院");
        if (StrUtil.isBlank(major.getLevel())) throw new ServiceException("新增失败：必须指定培养层次");
        
        // B. 🛡️ 核心：学制防刁民校验
        checkDuration(major.getDuration());
        
        // C. 唯一性校验
        checkUnique(null, major.getName());
        
        return super.save(major);
    }
    
    // =================================================================================
    // 2. 修改专业
    // =================================================================================
    @Override
    public boolean updateById(SysMajor major) {
        // A. ID校验
        if (major.getId() == null) throw new ServiceException("修改失败：ID 不能为空");
        
        // B. 唯一性校验
        if (StrUtil.isNotBlank(major.getName())) {
            checkUnique(major.getId(), major.getName());
        }
        
        // C. 🛡️ 核心：如果修改了学制，必须再次校验范围
        if (major.getDuration() != null) {
            checkDuration(major.getDuration());
        }
        
        return super.updateById(major);
    }
    
    // =================================================================================
    // 3. 删除专业
    // =================================================================================
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean removeById(Serializable id) {
        SysMajor major = this.getById(id);
        if (major == null) throw new ServiceException("删除失败：ID " + id + " 不存在");
        
        Long count = classMapper.selectCount(Wrappers.<SysClass>lambdaQuery()
                .eq(SysClass::getMajorId, id));
        
        if (count > 0) {
            throw new ServiceException("删除失败：该专业下尚有 " + count + " 个班级！请先删除或转移班级。");
        }
        
        return super.removeById(id);
    }
    
    // =================================================================================
    // 🛡️ 私有辅助方法
    // =================================================================================
    
    /**
     * 学制合法性校验 (防刁民专用)
     */
    private void checkDuration(Integer duration) {
        if (duration == null) {
            throw new ServiceException("操作失败：学制不能为空");
        }
        
        // 🛡️ 规则：必须是 1~10 年
        if (duration < MIN_DURATION || duration > MAX_DURATION) {
            throw new ServiceException(
                    String.format("学制设定异常：只能在 %d-%d 年之间。填 %d 年是打算读到退休吗？", MIN_DURATION, MAX_DURATION, duration)
            );
        }
    }
    
    private void checkUnique(Long id, String name) {
        long count = this.count(new LambdaQueryWrapper<SysMajor>()
                .eq(SysMajor::getName, name)
                .ne(id != null, SysMajor::getId, id));
        
        if (count > 0) {
            throw new ServiceException("操作失败：专业名称 [" + name + "] 已存在");
        }
    }
}