package com.mol.dorm.biz.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.mol.dorm.biz.entity.DormFloor;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

/**
 * 楼层 Mapper 接口
 * 🛡️ [防刁民]：实现与楼栋状态联动的安全查询
 */
@Mapper
public interface DormFloorMapper extends BaseMapper<DormFloor> {
    
    /**
     * 查询指定楼栋下所有“物理可用”的楼层
     * 🛡️ 规则：强制过滤掉所属楼栋处于“装修、停用”状态的楼层
     * 逻辑：楼层状态=20 且 所属楼栋状态=20
     */
    List<DormFloor> selectActiveFloors(@Param("buildingId") Long buildingId);
}