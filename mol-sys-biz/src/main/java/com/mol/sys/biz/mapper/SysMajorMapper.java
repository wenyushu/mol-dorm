package com.mol.sys.biz.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.mol.sys.biz.entity.SysMajor;
import org.apache.ibatis.annotations.Mapper;

/**
 * 专业 Mapper 接口
 * 对应表：sys_major
 */
@Mapper
public interface SysMajorMapper extends BaseMapper<SysMajor> {
}