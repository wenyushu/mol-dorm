package com.mol.server.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.mol.server.entity.SysCampus;
import org.apache.ibatis.annotations.Mapper;

/**
 * 校区数据访问层 (Mapper)
 * 继承 BaseMapper 即可获得常用的 CRUD 功能，无需编写 SQL
 *
 * @author mol
 */
@Mapper // 标识为 MyBatis 的 Mapper 接口
public interface SysCampusMapper extends BaseMapper<SysCampus> {
    
    // 继承 BaseMapper 后，自动拥有了 SQL 读写能力
    // 如果以后有复杂的自定义 SQL，可以写在这里，并在 XML 中实现
}