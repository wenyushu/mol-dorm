package com.mol.dorm.biz.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.util.List;

/**
 * 房间展示视图对象 (用于管理端房态图)
 */
@Data
@Schema(description = "房间详情视图")
public class DormRoomVO {
    
    @Schema(description = "房间 ID")
    private Long id;
    
    @Schema(description = "所属楼宇名称")
    private String buildingName;
    
    @Schema(description = "所在楼层")
    private Integer floorNo;
    
    @Schema(description = "房间号 (如: 302)")
    private String roomNo;
    
    @Schema(description = "房间性别限制 (1-男, 2-女)")
    private Integer gender;
    
    @Schema(description = "性别描述 (男寝/女寝)")
    private String genderDesc;
    
    @Schema(description = "当前实住人数")
    private Integer currentNum;
    
    @Schema(description = "房间容量 (床位数)")
    private Integer capacity;
    
    @Schema(description = "房间生命周期状态 (20正常, 50维修, 80保留)")
    private Integer status;
    
    @Schema(description = "状态中文描述")
    private String statusDesc;
    
    @Schema(description = "状态渲染颜色 (如 #67C23A)")
    private String statusColor;
    
    @Schema(description = "性别标签颜色")
    private String genderColor;
    
    @Schema(description = "饱和度资源状态 (21空闲, 23未满, 26已满)")
    private Integer resStatus;
    
    
    
    @Schema(description = "床位实时分布列表")
    private List<BedInfo> bedList;
    
    @Data
    public static class BedInfo {
        @Schema(description = "床位 ID")
        private Long bedId;
        
        @Schema(description = "床位物理标签 (如: A床)")
        private String bedLabel;
        
        @Schema(description = "乐观锁版本号 (分配时回传)")
        private Integer version;
        
        @Schema(description = "入住者 ID")
        private Long occupantId;
        
        @Schema(description = "入住者姓名")
        private String occupantName;
        
        @Schema(description = "入住者编号 (学工号)")
        private String occupantNo;
        
        @Schema(description = "入住人类型 (0学生 1教工)")
        private Integer occupantType;
    }
}