package com.mol.dorm.biz.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

/**
 * 宿管/超级管理员驾驶舱看板聚合数据
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "驾驶舱看板聚合数据对象")
public class DashboardVO {
    
    @Schema(description = "顶部汇总卡片数据")
    private SummaryCard summary;
    
    @Schema(description = "入住率分布饼图数据")
    private List<NameValue> occupancyPie;
    
    @Schema(description = "报修分布饼图数据")
    private List<NameValue> repairPie;
    
    @Schema(description = "实时待办堆栈 (工作流与报修)")
    private WorkQueue workQueue;
    
    @Schema(description = "动态情报板 (公告与失物)")
    private InfoBoard infoBoard;
    
    @Data
    @Builder
    @Schema(description = "首页核心汇总卡片")
    public static class SummaryCard {
        @Schema(description = "系统总楼栋数")
        private Long totalBuildings;
        @Schema(description = "总核定床位数")
        private Long totalBeds;
        @Schema(description = "当前实住人数")
        private Long usedBeds;
        @Schema(description = "全校入住率 (如: 92.5%)")
        private String occupancyRate;
    }
    
    @Data
    @Builder
    @Schema(description = "实时待办监控模型")
    public static class WorkQueue {
        @Schema(description = "待指派或处理中的报修单数")
        private Long activeRepairs;
        
        @Schema(description = "待审批的宿舍业务流总数")
        private Long pendingWorkflow;
        
        /**
         * 🛡️ [防刁民/预警逻辑]
         */
        @Schema(description = "高压预警位：true-代表任务积压严重，建议大屏红色闪烁")
        private boolean isCritical;
    }
    
    @Data
    @Builder
    @Schema(description = "实时动态汇总板")
    public static class InfoBoard {
        @Schema(description = "最新公告列表")
        private List<NoticeItem> notices;
        @Schema(description = "最新失物招领快讯")
        private List<LostFoundItem> lostAndFound;
    }
    
    @Data
    @Builder
    @Schema(description = "公告简项模型")
    public static class NoticeItem {
        private Long id;
        @Schema(description = "公告简短标题")
        private String title;
        @Schema(description = "级别色标: #F56C6C(重要), #409EFF(普通)")
        private String levelColor;
        @Schema(description = "发布日期 (MM-dd HH:mm)")
        private String time;
    }
    
    @Data
    @Builder
    @Schema(description = "失物招领简项模型")
    public static class LostFoundItem {
        private Long id;
        @Schema(description = "拾获/丢失物品名")
        private String itemName;
        @Schema(description = "类型语义描述 (拾物/失物)")
        private String typeDesc;
        @Schema(description = "发布日期 (MM-dd)")
        private String time;
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "通用的名值对模型 (支持色标注入)")
    public static class NameValue {
        @Schema(description = "统计项名称")
        private String name;
        @Schema(description = "统计项数值")
        private Object value;
        @Schema(description = "视觉引导色 (Hex格式)")
        private String color;
    }
}