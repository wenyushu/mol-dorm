package com.mol.server.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mol.common.core.entity.SysOrdinaryUser;
import com.mol.common.core.exception.ServiceException;
import com.mol.server.entity.SysClass;
import com.mol.server.mapper.SysClassMapper;
import com.mol.server.mapper.SysOrdinaryUserMapper;
import com.mol.server.service.SysClassService;
import com.mol.server.vo.SysClassVO;
import lombok.RequiredArgsConstructor; // 👈 记得导入这个
import org.springframework.stereotype.Service;

import java.io.Serializable;

/**
 * 【班级】业务实现
 */
@Service
@RequiredArgsConstructor // 修复核心：自动生成构造函数，注入 final 字段
public class SysClassServiceImpl extends ServiceImpl<SysClassMapper, SysClass> implements SysClassService {
    
    // 注入 UserMapper
    private final SysOrdinaryUserMapper userMapper;
    
    /**
     * 分页查询班级列表 (VO模式)
     */
    @Override
    public IPage<SysClassVO> getClassVoPage(Page<SysClassVO> page, SysClass queryParams) {
        QueryWrapper<SysClass> wrapper = new QueryWrapper<>();
        
        // 1. 年级查询
        if (queryParams.getGrade() != null) {
            wrapper.eq("c.grade", queryParams.getGrade());
        }
        
        // 2. 班级名称查询
        if (StrUtil.isNotBlank(queryParams.getClassName())) {
            wrapper.like("c.class_name", queryParams.getClassName());
        }
        
        // 3. 排序 (按年级倒序)
        wrapper.orderByDesc("c.grade");
        
        // 调用 Mapper 的自定义 SQL
        return baseMapper.selectClassVoPage(page, wrapper);
    }
    
    /**
     * 删除班级
     * 🛡️ 防刁民：如果班里还有学生，禁止删除，防止数据变孤儿。
     */
    @Override
    public boolean removeById(Serializable id) {
        // 1. 检查该班级下是否有学生
        // 使用 LambdaQueryWrapper 避免手写字段名出错
        Long count = userMapper.selectCount(new LambdaQueryWrapper<SysOrdinaryUser>()
                .eq(SysOrdinaryUser::getClassId, id));
        
        if (count > 0) {
            throw new ServiceException("删除失败：该班级下尚有 " + count + " 名学生！请先将学生转移或删除。");
        }
        
        // 2. 确认没人后，才调用父类的删除逻辑
        return super.removeById(id);
    }
}