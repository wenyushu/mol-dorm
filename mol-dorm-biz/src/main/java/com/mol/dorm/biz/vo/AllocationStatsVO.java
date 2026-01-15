package com.mol.dorm.biz.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * 宿舍分配健康度监控/校验报告
 * <p>
 * 用于实时展示校区分配进度及检测数据异常（影子/幽灵/超卖数据）。
 * </p>
 */
@Data
@Schema(description = "分配监控与健康报告")
public class AllocationStatsVO {
    
    @Schema(description = "校区名称")
    private String campusName;
    
    @Schema(description = "总学生数 (该校区在籍)")
    private Long totalStudents;
    
    // ================= 正常数据统计 =================
    
    @Schema(description = "已分配人数 (住校且有床)")
    private Long allocatedCount;
    
    @Schema(description = "待分配人数 (住校但无床)")
    private Long unallocatedCount;
    
    @Schema(description = "无需分配人数 (走读/校外住宿)")
    private Long offCampusCount;
    
    @Schema(description = "特殊状态人数 (休学/停用)")
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
    
    @Schema(description = "异常详情列表 (前20条)")
    private List<String> errorDetails = new ArrayList<>();
}