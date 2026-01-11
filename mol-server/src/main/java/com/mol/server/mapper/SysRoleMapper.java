package com.mol.server.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.mol.common.core.entity.SysRole;
import org.apache.ibatis.annotations.Mapper;

/**
 * 角色表 数据访问层
 *
 * @author mol
 */
@Mapper
public interface SysRoleMapper extends BaseMapper<SysRole> {
    // MyBatis-Plus 已内置基础 CRUD，无需手写 SQL
}