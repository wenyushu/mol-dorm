package com.mol.server.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.mol.server.entity.SysMajor;
import org.apache.ibatis.annotations.Mapper;

/**
 * 专业 Mapper 接口
 * 对应表：sys_major
 */
@Mapper
public interface SysMajorMapper extends BaseMapper<SysMajor> {
}