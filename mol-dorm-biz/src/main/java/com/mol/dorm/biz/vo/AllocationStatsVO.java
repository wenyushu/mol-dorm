package com.mol.dorm.biz.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * 分配体检报告视图对象
 * 🛡️ [防刁民审计]：全方位展示分配异常，不漏掉任何一个幽灵床位。
 */
@Data
@Schema(description = "校区分配体检报告")
public class AllocationStatsVO {
    
    @Schema(description = "校区名称")
    private String campusName;
    
    @Schema(description = "总学生人数")
    private Long totalStudents = 0L;
    
    @Schema(description = "需住校总人数")
    private Long needDormCount = 0L;
    
    @Schema(description = "已分配人数")
    private Long allocatedCount = 0L;
    
    @Schema(description = "未分配人数")
    private Long unallocatedCount = 0L;
    
    @Schema(description = "分配进度百分比 (如: 95.5%)")
    private String progressRate;
    
    // --- 🚨 异常统计项 (报错补全点) ---
    
    @Schema(description = "状态异常人数 (休学/停用但占床)")
    private Long suspendedCount = 0L; // 🟢 补上这个字段，解决报错！
    
    @Schema(description = "走读生人数")
    private Long offCampusCount = 0L; // 🟢 补上这个字段！
    
    @Schema(description = "幽灵床位总数 (占床但查无此人)")
    private Integer ghostBedCount = 0;
    
    @Schema(description = "超卖房间总数 (实住 > 容量)")
    private Integer oversoldRoomCount = 0;
    
    @Schema(description = "计数不同步房间数 (Room表与Bed表不一致)")
    private Integer syncErrorCount = 0;
    
    @Schema(description = "总异常风险点数量")
    private Integer errorCount = 0;
    
    @Schema(description = "异常明细列表")
    private List<String> errorDetails = new ArrayList<>();
}