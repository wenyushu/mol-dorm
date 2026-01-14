package com.mol.dorm.biz.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.mol.common.core.entity.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("dorm_change_request")
@Schema(description = "调宿/换宿申请")
public class DormChangeRequest extends BaseEntity {
    
    @Serial
    private static final long serialVersionUID = 1L;
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    @Schema(description = "申请类型 (1:单人调宿 2:双人互换)")
    private Integer type;
    
    @Schema(description = "申请人 ID")
    private Long userId;
    
    @Schema(description = "互换目标学生 ID (类型为 2 时必填)")
    private Long swapStudentId;
    
    @Schema(description = "原房间 ID")
    private Long originRoomId;
    
    @Schema(description = "原床位 ID")
    private Long originBedId;
    
    @Schema(description = "意向目标房间 ID")
    private Long targetRoomId;
    
    @Schema(description = "申请原因")
    private String reason;
    
    @Schema(description = "审核状态 (0:待审 1:通过 2:驳回)")
    private Integer status;
    
    @Schema(description = "审批意见")
    private String auditRemark;
}