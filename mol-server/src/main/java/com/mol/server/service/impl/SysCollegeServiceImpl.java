package com.mol.server.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mol.common.core.exception.ServiceException;
import com.mol.server.entity.SysCollege;
import com.mol.server.entity.SysMajor;
import com.mol.server.mapper.SysCollegeMapper;
import com.mol.server.mapper.SysMajorMapper; // 👈 引入专业 Mapper
import com.mol.server.service.SysCollegeService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.Serializable;

/**
 * 【学院】业务实现
 */
@Service
@RequiredArgsConstructor // 自动注入 final 字段
public class SysCollegeServiceImpl extends ServiceImpl<SysCollegeMapper, SysCollege> implements SysCollegeService {
    
    // 注入专业 Mapper，用于检查依赖
    private final SysMajorMapper majorMapper;
    
    /**
     * 删除学院
     * 🛡️ 防刁民：如果学院下还有专业，禁止删除，防止数据孤儿。
     */
    @Override
    public boolean removeById(Serializable id) {
        // 1. 检查该学院下是否有专业
        Long count = majorMapper.selectCount(new LambdaQueryWrapper<SysMajor>()
                .eq(SysMajor::getCollegeId, id));
        
        if (count > 0) {
            throw new ServiceException("删除失败：该学院下尚有 " + count + " 个专业！请先删除或转移专业。");
        }
        
        // 2. 安全删除
        return super.removeById(id);
    }
}