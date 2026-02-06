package com.mol.dorm.biz.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.mol.dorm.biz.entity.UtilityBill;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Mapper
public interface UtilityBillMapper extends BaseMapper<UtilityBill> {
    
    /**
     * 🟢 [能耗审计]：获取指定楼栋内用电量 Top 排行
     * 用于发现违规电器（如热得快、电锅等）
     */
    @Select("SELECT r.room_no as roomNo, b.electric_usage as usageValue, b.month " +
            "FROM utility_bill b " +
            "INNER JOIN dorm_room r ON b.room_id = r.id " +
            "WHERE r.building_id = #{buildingId} " +
            "ORDER BY b.electric_usage DESC LIMIT 10")
    List<Map<String, Object>> selectAnomalyRank(@Param("buildingId") Long buildingId);
    
    /**
     * [计费核心] 穿透校区配置计算房间月度账单 (保留原有逻辑)
     */
    UtilityBill calculateRoomBill(@Param("roomId") Long roomId,
                                  @Param("month") String month,
                                  @Param("waterCold") BigDecimal waterCold,
                                  @Param("waterHot") BigDecimal waterHot,
                                  @Param("electricUsage") BigDecimal electricUsage);
}