package com.mol.server.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.mol.server.entity.SysClass;
import com.mol.server.vo.SysClassVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface SysClassMapper extends BaseMapper<SysClass> {
    
    /**
     * ✨ [新增] 单条班级视图查询
     * 用于：导出 Excel 时实时翻译“班级ID -> 全称”
     */
    @Select("""
        SELECT
            c.*,
            c.education_level AS edu_level,
            m.name AS major_name,
            col.name AS college_name
        FROM biz_class c
        LEFT JOIN sys_major m ON c.major_id = m.id
        LEFT JOIN sys_college col ON m.college_id = col.id
        WHERE c.id = #{id} AND c.del_flag = '0'
    """)
    SysClassVO selectClassVoById(@Param("id") Long id);
    
    /**
     * [分页查询] 保持不变
     */
    @Select("""
        <script>
        SELECT
            c.*,
            c.education_level AS edu_level,
            m.name AS major_name,
            col.name AS college_name
        FROM biz_class c
        LEFT JOIN sys_major m ON c.major_id = m.id
        LEFT JOIN sys_college col ON m.college_id = col.id
        WHERE c.del_flag = '0'
        <if test="ew != null and !ew.emptyOfWhere">
            AND ${ew.sqlSegment}
        </if>
        ORDER BY c.grade DESC, c.class_name ASC
        </script>
    """)
    IPage<SysClassVO> selectClassVoPage(
            Page<SysClassVO> page,
            @Param("ew") com.baomidou.mybatisplus.core.conditions.Wrapper<SysClass> wrapper);
}