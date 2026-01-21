package com.mol.server.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.mol.common.core.entity.SysUserRole;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface SysUserRoleMapper extends BaseMapper<SysUserRole> {
    
    /**
     * 根据用户 ID 查询拥有的角色 Key 列表
     * 关联查询 sys_role 表
     *
     * @param userId 用户 ID
     * @return 角色Key列表 (e.g. ["admin", "student"])
     */
    List<String> selectRoleKeysByUserId(@Param("userId") Long userId);
}