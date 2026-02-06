package com.mol.dorm.biz.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.mol.dorm.biz.entity.DormBuilding;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * 楼栋 Mapper 接口
 * 🛡️ [防刁民审计版]
 */
@Mapper
public interface DormBuildingMapper extends BaseMapper<DormBuilding> {
    
    /**
     * 📊 校区资源看板：统计各用途楼栋及其子资源的健康度
     */
    List<Map<String, Object>> selectBuildingStats(@Param("campusId") Long campusId);
    
    /**
     * 🕵️ [校区级] 深度核账：一次性获取某校区所有房间的 [Bed表实住人数]
     */
    List<Map<String, Object>> selectRealOccupancyByCampus(@Param("campusId") Long campusId);
    
    /**
     * 🕵️ [楼栋级] 深度核账：获取指定楼栋各房间的实住人数
     * 🟢 对应 ServiceImpl 里的 syncRoomOccupancy(Long buildingId)
     */
    List<Map<String, Object>> selectRealOccupancyByBuilding(@Param("buildingId") Long buildingId);

}