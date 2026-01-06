package com.mol.common.mybatis.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.mol.common.core.entity.SysOrdinaryUser;
import org.apache.ibatis.annotations.Mapper;

/**
 * 普通用户账户 Mapper 接口
 */
@Mapper
public interface SysOrdinaryUserMapper extends BaseMapper<SysOrdinaryUser> {
}