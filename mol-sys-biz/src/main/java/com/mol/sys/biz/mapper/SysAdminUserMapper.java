package com.mol.sys.biz.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import com.mol.common.core.entity.SysAdminUser;
import org.apache.ibatis.annotations.Mapper;

/**
 * 系统管理员 Mapper 接口
 * 对应表：sys_admin_user
 * 负责：宿管、辅导员、超级管理员的数据操作
 */
@Mapper
public interface SysAdminUserMapper extends BaseMapper<SysAdminUser> {
    
    // MyBatis-Plus 已经帮你写好了 selectById, selectOne(wrapper) 等方法
    // 如果需要复杂的自定义 SQL，可以在这里写 @Select 注解
}