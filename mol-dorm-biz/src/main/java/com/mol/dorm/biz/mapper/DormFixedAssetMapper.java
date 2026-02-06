package com.mol.dorm.biz.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.mol.dorm.biz.entity.DormFixedAsset;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;
import java.util.Map;

/**
 * 资产管理 Mapper - 严苛审计版
 * 🛡️ [防刁民设计]：将 SQL 逻辑收拢至 XML，提升复杂审计查询的可维护性。
 */
@Mapper
public interface DormFixedAssetMapper extends BaseMapper<DormFixedAsset> {
    
    /**
     * [核心熔断审计]：统计房间内各状态资产数量
     */
    List<Map<String, Object>> countStatusByRoom(@Param("roomId") Long roomId);
    
    /**
     * [条码热审计]：资产编号唯一性预检
     */
    Long checkCodeUnique(@Param("assetCode") String assetCode);
    
    /**
     * [原子状态联动]：报修/完工自动切换状态
     */
    int updateAssetStatus(@Param("roomId") Long roomId,
                          @Param("assetCode") String assetCode,
                          @Param("status") Integer status);
    
    /**
     * 查询房间内的异常资产 (损坏/维修)
     */
    List<DormFixedAsset> selectAbnormalAssetsByRoom(@Param("roomId") Long roomId);
    
    /**
     * 按楼栋查询维修中的资产
     */
    List<DormFixedAsset> selectRepairingAssetsByBuilding(@Param("buildingId") Long buildingId);
}