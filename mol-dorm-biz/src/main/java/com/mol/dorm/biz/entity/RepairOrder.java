package com.mol.dorm.biz.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.mol.common.core.entity.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serial;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 报修工单实体
 * 🛡️ [资产联动版]：支持全自动熔断审计
 */
@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
@TableName("biz_repair_order")
@Schema(description = "报修工单")
public class RepairOrder extends BaseEntity {
    
    @Serial
    private static final long serialVersionUID = 1L;
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    @Schema(description = "工单号 (自动生成)")
    private String orderNo;
    
    @Schema(description = "房间 ID")
    private Long roomId;
    
    @Schema(description = "关联资产编号 (资产条码)")
    private String assetCode;
    
    @Schema(description = "申请人 ID")
    private Long applicantId;
    
    @Schema(description = "故障描述")
    private String description;
    
    @Schema(description = "报修图片 (URL 逗号分隔)")
    private String images;
    
    /**
     * 🚦 状态流转增强:
     * 0-待处理, 1-维修中, 2-已修复(待评价), 3-已评价(结束), 4-已驳回, 5-挂起(待大修)
     */
    @Schema(description = "状态: 0待指派, 1维修中, 2已完成, 3已评价, 4已驳回, 5待大修")
    private Integer status;
    
    /**
     * 维修耗材费用 (由师傅在完工时填报)
     */
    private BigDecimal materialCost;
    
    @Schema(description = "维修工 ID")
    private Long repairmanId;
    
    @Schema(description = "维修工姓名")
    private String repairmanName;
    
    @Schema(description = "维修工电话")
    private String repairmanPhone;
    
    @Schema(description = "指派时间")
    private LocalDateTime assignTime;
    
    @Schema(description = "实际完成时间")
    private LocalDateTime finishTime;
    
    @Schema(description = "评分 (1-5)")
    private Integer rating;
    
    @Schema(description = "评价内容")
    private String comment;
    
    @Schema(description = "备注/挂起原因/驳回原因")
    private String remark;
}