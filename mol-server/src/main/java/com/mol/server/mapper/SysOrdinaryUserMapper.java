package com.mol.server.mapper;


import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.mol.common.core.entity.SysOrdinaryUser;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

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
     * 🤖 [灭世清算核心]：筛选达到最长修读年限的学生
     * 逻辑算法：(入学年 + 专业学制 + 累计休学年 + 累计留级年) <= 当前审计年份
     * @param currentYear 当前审计年份 (如 2026)
     */
    List<SysOrdinaryUser> selectOverdueStudents(@Param("currentYear") Integer currentYear);
    
    /**
     * [兜底] 查找指定前缀下的最大账号
     * 🛡️ 设计：这里用 @Select 注解，XML 里用复杂 SQL，动静结合，非常合理。
     */
    @Select("SELECT username FROM sys_ordinary_user WHERE username LIKE CONCAT(#{prefix}, '%') ORDER BY username DESC LIMIT 1")
    String selectMaxUsernameByPrefix(@Param("prefix") String prefix);
}