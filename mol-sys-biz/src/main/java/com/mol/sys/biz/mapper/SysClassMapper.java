package com.mol.sys.biz.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.mol.sys.biz.entity.SysClass;
import org.apache.ibatis.annotations.Mapper;

/**
 * 班级 Mapper 接口
 * 对应表：sys_class
 */
@Mapper
public interface SysClassMapper extends BaseMapper<SysClass> {
}