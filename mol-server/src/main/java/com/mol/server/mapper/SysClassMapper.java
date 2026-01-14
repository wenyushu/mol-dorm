package com.mol.server.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.mol.server.entity.SysClass;
import com.mol.server.vo.SysClassVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 班级 Mapper 接口
 * 对应表：sys_class
 */
@Mapper
public interface SysClassMapper extends BaseMapper<SysClass> {
    /**
     * 自定义分页查询：关联查询 学院表 和 专业表
     * <p>
     * 这里的 SQL 逻辑是：
     * 1. 主查 sys_class (c)
     * 2. 关联 sys_major (m) 拿到专业名和层次
     * 3. 关联 sys_college (col) 拿到学院名
     * </p>
     */
    @Select("""
        SELECT
            c.*,
            m.name AS major_name,
            m.level AS edu_level,
            col.name AS college_name
        FROM sys_class c
        LEFT JOIN sys_major m ON c.major_id = m.id
        LEFT JOIN sys_college col ON m.college_id = col.id
        WHERE c.del_flag = '0'
        AND (${ew.customSqlSegment})
        ORDER BY c.grade DESC, c.name ASC
    """)
    
    // 自定义分页查询
    IPage<SysClassVO> selectClassVoPage(
            Page<SysClassVO> page,
            @Param("ew") com.baomidou.mybatisplus.core.conditions.Wrapper<SysClass> wrapper);
}