package com.mol.common.mybatis.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.mol.common.core.entity.SysUserRole;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 用户角色关系 Mapper
 */
@Mapper
@Repository // 加上这个可以消除 IDEA 内部的红色报错提示
public interface SysUserRoleMapper extends BaseMapper<SysUserRole> {
    
    /**
     * 联表查询：根据用户ID和用户类型，获取该用户拥有的所有角色 Key
     * SQL 逻辑：
     * 1. 连接 sys_role 表 (r) 和 sys_user_role 表 (ur)
     * 2. 匹配角色 ID
     * 3. 过滤用户 ID 和用户类型
     */
    @Select("SELECT r.role_key " +
            "FROM sys_role r " +
            "INNER JOIN sys_user_role ur ON r.id = ur.role_id " +
            "WHERE ur.user_id = #{userId} AND ur.user_type = #{userType}")
    List<String> getRoleKeys(@Param("userId") Long userId, @Param("userType") Integer userType);
}