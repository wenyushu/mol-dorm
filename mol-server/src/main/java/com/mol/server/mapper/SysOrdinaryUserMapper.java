package com.mol.server.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.mol.common.core.entity.SysOrdinaryUser; // 确保引用了正确的 Entity
import org.apache.ibatis.annotations.Mapper;
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
     * 【核心兜底方法】
     * 查找指定前缀的当前最大账号
     * 例如查询 "2026B01%"，数据库里最大的是 "2026B010045"，则返回该字符串
     * * @param prefix 学号/工号前缀 (如 2026B01)
     * @return 当前最大的完整账号
     */
    @Select("SELECT username FROM sys_ordinary_user WHERE username LIKE CONCAT(#{prefix}, '%') ORDER BY username DESC LIMIT 1")
    String selectMaxUsernameByPrefix(String prefix);
}