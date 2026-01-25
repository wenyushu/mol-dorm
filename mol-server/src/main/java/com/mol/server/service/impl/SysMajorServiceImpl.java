package com.mol.server.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mol.common.core.exception.ServiceException;
import com.mol.server.entity.SysClass;
import com.mol.server.entity.SysMajor;
import com.mol.server.mapper.SysClassMapper; // 👈 引入班级 Mapper
import com.mol.server.mapper.SysMajorMapper;
import com.mol.server.service.SysMajorService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.Serializable;

/**
 * 【专业】业务实现
 */
@Service
@RequiredArgsConstructor // 自动注入 final 字段
public class SysMajorServiceImpl extends ServiceImpl<SysMajorMapper, SysMajor> implements SysMajorService {
    
    // 注入班级 Mapper
    private final SysClassMapper classMapper;
    
    /**
     * 删除班级
     * 🛡️ 防刁民：如果该专业下还有班级，禁止删除，防止数据孤儿。
     */
    @Override
    public boolean removeById(Serializable id) {
        // 1. 检查该专业下是否有班级
        Long count = classMapper.selectCount(new LambdaQueryWrapper<SysClass>()
                .eq(SysClass::getMajorId, id));
        
        if (count > 0) {
            throw new ServiceException("删除失败：该专业下尚有 " + count + " 个班级！请先删除或转移班级。");
        }
        
        // 2. 安全删除
        return super.removeById(id);
    }
}