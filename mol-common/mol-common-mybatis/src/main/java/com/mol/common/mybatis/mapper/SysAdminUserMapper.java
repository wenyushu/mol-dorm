package com.mol.common.mybatis.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.mol.common.core.entity.SysAdminUser;
import org.apache.ibatis.annotations.Mapper;

/**
 * 管理员账户 Mapper 接口
 */
@Mapper
public interface SysAdminUserMapper extends BaseMapper<SysAdminUser> {
}