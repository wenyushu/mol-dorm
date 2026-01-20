package com.mol.dorm.biz.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * 宿舍分配健康度监控/校验报告
 * <p>
 * 用于实时展示校区分配进度及检测数据异常（影子/幽灵/超卖数据）。
 * 1. 幽灵床位：床位表显示有人(ID)，但学生表里查无此人（可能是退学没清床位，或脏数据）。
 * 2. 孤儿用户：系统显示该学生"已住校"，但找不到他的床位记录。
 * 3. 超卖房间：房间住的人数 > 额定床位数（物理上不可能，属于严重BUG）。
 * 4. 数据不同步：Room表的 current_num 字段与 Bed 表实际占用数不一致。
 * </p>
 */
@Data
@Schema(description = "分配监控与健康报告")
public class AllocationStatsVO {
    
    @Schema(description = "校区名称")
    private String campusName;
    
    @Schema(description = "总学生数 (该校区在籍，包含休学/走读)")
    private Long totalStudents;
    
    // ================= 正常数据统计 =================
    
    @Schema(description = "已分配人数 (状态正常+住校+有床位)")
    private Long allocatedCount;
    
    @Schema(description = "待分配人数 (状态正常+住校+无床位)")
    private Long unallocatedCount;
    
    @Schema(description = "无需分配人数 (走读/校外住宿)")
    private Long offCampusCount;
    
    @Schema(description = "特殊状态人数 (休学/停用/保留学籍)")
    private Long suspendedCount;
    
    @Schema(description = "分配进度 (百分比)")
    private String progressRate;
    
    // ================= 异常检测 (红色警报) =================
    
    @Schema(description = "数据异常总数")
    private Integer errorCount;
    
    @Schema(description = "幽灵床位 (有占位 ID 但查无此人/人已退宿)")
    private Integer ghostBedCount;
    
    @Schema(description = "孤儿用户 (系统标记已住但查无床位)")
    private Integer orphanUserCount;
    
    @Schema(description = "超卖房间 (实住人数 > 额定容量)")
    private Integer oversoldRoomCount;
    
    @Schema(description = "数据不同步 (房间计数器 != 实际床位占用数)")
    private Integer syncErrorCount;
    
    @Schema(description = "异常详情列表 (仅展示前20条)")
    private List<String> errorDetails = new ArrayList<>();
}