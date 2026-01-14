package com.mol.dorm.biz.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.mol.common.core.entity.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;
import java.time.LocalDate;

/**
 * 寒暑假留校申请实体
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("biz_holiday_stay")
@Schema(description = "留校申请")
public class HolidayStay extends BaseEntity {
    
    @Serial
    private static final long serialVersionUID = 1L;
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    @Schema(description = "学生ID")
    private Long studentId;
    
    @Schema(description = "开始日期")
    private LocalDate startDate;
    
    @Schema(description = "结束日期")
    private LocalDate endDate;
    
    @Schema(description = "留校原因 (考研、比赛、科研等)")
    private String reason;
    
    // === 紧急联系人信息 (必填) ===
    
    @Schema(description = "紧急联系人姓名")
    private String emergencyName;
    
    @Schema(description = "与本人关系")
    private String emergencyRelation;
    
    @Schema(description = "紧急联系人电话")
    private String emergencyPhone;
    
    /**
     * 0-待审批, 1-已通过, 2-已驳回
     */
    @Schema(description = "审批状态")
    private Integer status;
    
    @Schema(description = "审批意见")
    private String auditMsg;
}