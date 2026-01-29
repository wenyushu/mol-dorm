package com.mol.server.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.mol.server.entity.SysUserArchive;
import org.apache.ibatis.annotations.Mapper;

/**
 * 用户归档 Mapper 接口
 * <p>
 * 核心职责：
 * 1. 提供对 sys_user_archive 表的基础 CRUD 能力。
 * 2. 配合 SysUserArchiveService 实现数据的冷备份与查询。
 * </p>
 *
 * @author mol
 */
@Mapper
public interface SysUserArchiveMapper extends BaseMapper<SysUserArchive> {
    // 目前业务逻辑主要依赖 MP 内置的 insert 和 selectList，
    // 暂时无需编写自定义 XML SQL。
}