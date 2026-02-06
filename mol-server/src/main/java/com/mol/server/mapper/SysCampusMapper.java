package com.mol.server.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.mol.server.entity.SysCampus;
import com.mol.server.vo.SysCampusTreeVO; // 🟢 确保引入的是解耦后的 VO
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 校区信息 Mapper 接口
 * 🛡️ [底层核心]：负责校区基础数据及跨模块资源树的聚合查询
 */
@Mapper
public interface SysCampusMapper extends BaseMapper<SysCampus> {
    
    /**
     * 【下钻核心】一次性获取所有校区及其下属楼栋
     * 🛡️ [解耦设计]：返回 SysCampusTreeVO，内部嵌套楼栋简要信息，
     * 避免了直接引用 mol-dorm-biz 模块的实体类，彻底解决循环依赖。
     * * @param status 过滤状态 (若传 null 则查询全部)
     */
    List<SysCampusTreeVO> selectCampusBuildingTree(@Param("status") Integer status);
}