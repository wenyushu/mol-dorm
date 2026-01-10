package com.mol.sys.biz.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.mol.sys.biz.entity.SysCollege;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

/**
 * 学院 Mapper 接口
 * 对应表：sys_college
 */
@Mapper
public interface SysCollegeMapper extends BaseMapper<SysCollege> {
    
    /**
     * 根据 ID 查询学院代码 (用于生成学号前缀)
     * 例如：ID=1 -> 返回 "06"
     */
    @Select("SELECT code FROM sys_college WHERE id = #{id}")
    String selectCodeById(Long id);
}