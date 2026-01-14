package com.mol.server.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mol.server.entity.SysClass;
import com.mol.server.mapper.SysClassMapper;
import com.mol.server.service.SysClassService;
import com.mol.server.vo.SysClassVO;
import org.springframework.stereotype.Service;

/**
 * 【班级】业务实现
 */
@Service
public class SysClassServiceImpl extends ServiceImpl<SysClassMapper, SysClass> implements SysClassService {
    
    @Override
    public IPage<SysClassVO> getClassVoPage(Page<SysClassVO> page, SysClass queryParams) {
        QueryWrapper<SysClass> wrapper = new QueryWrapper<>();
        
        // 1. 年级查询
        // 判断非空
        if (queryParams.getGrade() != null) {
            wrapper.eq("c.grade", queryParams.getGrade());
        }
        
        // 2. 班级名称查询 (className 是 String 类型，继续用 StrUtil)
        if (StrUtil.isNotBlank(queryParams.getClassName())) {
            wrapper.like("c.class_name", queryParams.getClassName());
        }
        
        // 3. 排序 (按年级倒序)
        wrapper.orderByDesc("c.grade");
        
        // 调用 Mapper 的自定义 SQL
        return baseMapper.selectClassVoPage(page, wrapper);
    }
}