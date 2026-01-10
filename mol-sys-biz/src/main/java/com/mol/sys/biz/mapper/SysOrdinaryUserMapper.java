package com.mol.sys.biz.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.mol.common.core.entity.SysOrdinaryUser; // 确保引用了正确的 Entity
import org.apache.ibatis.annotations.Mapper;

/**
 * 普通用户 Mapper 接口
 * 对应表：sys_ordinary_user
 * 负责：学生、教职工的基础数据操作
 */
@Mapper
public interface SysOrdinaryUserMapper extends BaseMapper<SysOrdinaryUser> {
    // 继承 BaseMapper 后，自动拥有 CRUD 能力
    // MyBatis-Plus 已内置 CRUD，无需手写 SQL
}