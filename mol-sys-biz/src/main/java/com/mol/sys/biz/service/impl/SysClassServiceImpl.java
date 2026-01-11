package com.mol.sys.biz.service.impl;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mol.sys.biz.entity.SysClass;
import com.mol.sys.biz.mapper.SysClassMapper;
import com.mol.sys.biz.service.SysClassService;
import com.mol.sys.biz.vo.SysClassVO;
import org.springframework.stereotype.Service;

/**
 * 【班级】业务实现
 */
@Service
public class SysClassServiceImpl extends ServiceImpl<SysClassMapper, SysClass> implements SysClassService {
    
    /**
     * 获取班级 VO 分页列表
     */
    public IPage<SysClassVO> getClassVoPage(Page<SysClassVO> page, SysClass queryParams) {
        // 构建查询条件
        QueryWrapper<SysClass> wrapper = new QueryWrapper<>();
        if (queryParams.getGrade() != null) {
            wrapper.eq("c.grade", queryParams.getGrade());
        }
        if (StrUtil.isNotBlank(queryParams.getName())) {
            wrapper.like("c.name", queryParams.getName());
        }
        // 调用 Mapper 的自定义 SQL
        return baseMapper.selectClassVoPage(page, wrapper);
    }
}