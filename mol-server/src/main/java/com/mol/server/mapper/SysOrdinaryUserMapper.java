package com.mol.server.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.mol.common.core.entity.SysOrdinaryUser; // 确保引用了正确的 Entity
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 普通用户 Mapper 接口
 * 对应表：sys_ordinary_user
 * 负责：学生、教职工的基础数据操作
 */
@Mapper
public interface SysOrdinaryUserMapper extends BaseMapper<SysOrdinaryUser> {
    // 继承 BaseMapper 后，自动拥有 CRUD 能力
    // MyBatis-Plus 已内置 CRUD，无需手写 SQL
    
    /**
     * 🟢 [新增] 查找指定前缀下的最大账号
     * 用于 Redis 缓存丢失时的兜底恢复
     * 例如: prefix='2026B05', 库里有 '2026B050001', '2026B050003' -> 返回 '2026B050003'
     */
    @Select("SELECT username FROM sys_ordinary_user WHERE username LIKE CONCAT(#{prefix}, '%') ORDER BY username DESC LIMIT 1")
    String selectMaxUsernameByPrefix(@Param("prefix") String prefix);
}