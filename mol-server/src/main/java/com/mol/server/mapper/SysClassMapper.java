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
     * 修正说明：
     * 1. 使用 <script> 标签开启动态 SQL 模式。
     * 2. 使用 <if> 判断 wrapper 是否有查询条件 (!ew.emptyOfWhere)。
     * 3. 使用 ${ew.sqlSegment} 只获取条件部分 (例如: id = ?)，而不包含 WHERE 或 ORDER BY 关键字。
     * 4. 排序逻辑固定在最后，或者由 Page 对象控制，忽略 Wrapper 里的排序。
     * 5. 直接取班级表自己的 education_level
     */
    @Select("""
        <script>
        SELECT
            c.*,
            -- ✨ 这里改了：优先用班级表自己的 education_level
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