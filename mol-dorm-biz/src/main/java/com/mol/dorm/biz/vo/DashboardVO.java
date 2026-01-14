package com.mol.dorm.biz.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.util.List;

/**
 * 大屏数据聚合对象
 */
@Data
public class DashboardVO {
    
    @Schema(description = "基础统计卡片")
    private SummaryCard summary;
    
    @Schema(description = "入住率分析 (饼图数据)")
    private List<NameValue> occupancyPie;
    
    @Schema(description = "楼栋入住排行 (柱状图数据)")
    private AxisChart buildingBar;
    
    @Schema(description = "报修状态分布 (饼图数据)")
    private List<NameValue> repairPie;
    
    @Data
    @Builder
    public static class SummaryCard {
        private Long totalBuildings;
        private Long totalRooms;
        private Long totalBeds;
        private Long usedBeds; // 入住人数
        private String occupancyRate; // 入住率百分比
    }
    
    @Data
    @Builder
    public static class NameValue {
        private String name;
        private Long value;
    }
    
    @Data
    @Builder
    public static class AxisChart {
        private List<String> categories; // X 轴：楼栋名
        private List<Long> seriesData;   // Y 轴：人数
    }
}