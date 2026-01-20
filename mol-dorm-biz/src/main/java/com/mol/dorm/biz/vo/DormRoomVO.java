package com.mol.dorm.biz.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.util.List;

/**
 * 宿舍房间视图对象 (View Object)
 * 用于前端展示房间详情、床位分布和人员入住情况
 */
@Data
@Schema(description = "房间展示 VO")
public class DormRoomVO {
    
    @Schema(description = "房间 ID")
    private Long id;
    
    @Schema(description = "所属校区 ID")
    private Long campusId;
    
    @Schema(description = "所属楼栋 ID")
    private Long buildingId;
    
    @Schema(description = "所在楼层号")
    private Integer floorNo;
    
    @Schema(description = "房间号")
    private String roomNo;
    
    @Schema(description = "房间性别 (0-女 1-男)")
    private String gender;
    
    @Schema(description = "当前人数")
    private Integer currentNum;
    
    @Schema(description = "总床位数")
    private Integer capacity;
    
    @Schema(description = "状态 (10-正常 20-满员 40-维修...)")
    private Integer status;
    
    @Schema(description = "床位列表 (含入住人信息)")
    private List<BedInfo> bedList;
    
    /**
     * 内部类：床位详情
     */
    @Data
    public static class BedInfo {
        @Schema(description = "床位 ID")
        private Long bedId;
        
        @Schema(description = "床位标签 (如: 101-1)")
        private String bedLabel;
        
        @Schema(description = "床位方位/排序 (1-左上 2-左下...)")
        private Integer sortOrder;
        
        @Schema(description = "入住者 ID (可能是学生 ID，也可能是教工 ID)")
        private Long occupantId;
        
        @Schema(description = "入住者姓名 (张三 / 李老师)")
        private String occupantName;
        
        @Schema(description = "入住者编号 (学号 / 工号)")
        private String occupantNo;
        
        @Schema(description = "入住者类型: 0-学生 1-教职工/宿管")
        private Integer occupantType;
    }
}