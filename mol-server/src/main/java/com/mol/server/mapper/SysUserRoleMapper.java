package com.mol.server.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.mol.common.core.entity.SysUserRole;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 用户与角色关联 Mapper
 * 数据访问层
 * 对应表：sys_user_role
 */
@Mapper
public interface SysUserRoleMapper extends BaseMapper<SysUserRole> {
    
    /**
     * 根据用户ID查询拥有的角色 Key (权限标识)
     * 例如：返回 ["dorm_manager", "counselor"]
     * * @param userId 管理员ID
     * @return 角色 Key 列表
     */
    @Select("SELECT r.role_key " +
            "FROM sys_user_role ur " +
            "LEFT JOIN sys_role r ON ur.role_id = r.id " +
            "WHERE ur.user_id = #{userId} AND r.status = '0'")
    List<String> selectRoleKeysByUserId(Long userId);
}