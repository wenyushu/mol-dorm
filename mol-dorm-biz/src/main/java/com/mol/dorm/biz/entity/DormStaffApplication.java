package com.mol.dorm.biz.entity;

import com.baomidou.mybatisplus.annotation.*;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 教职工住宿申请实体
 */
@Data
@TableName("dorm_staff_application")
@Schema(description = "教职工住宿申请")
public class DormStaffApplication implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    
    @Schema(description = "教工 ID")
    private Long userId;
    
    @Schema(description = "申请类型: 0-新入住 1-退宿 2-换房")
    private Integer applyType;
    
    @Schema(description = "期望户型")
    private String targetRoomType;
    
    @Schema(description = "申请原因")
    private String reason;
    
    @Schema(description = "状态: 0-待审批 1-通过 2-驳回")
    private Integer status;
    
    @Schema(description = "审批备注/驳回原因")
    private String remark;
    
    @Schema(description = "创建时间")
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    
    @Schema(description = "更新时间")
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}