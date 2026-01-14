package com.mol.dorm.biz.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.mol.common.core.entity.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;
import java.time.LocalDateTime;

/**
 * 报修工单实体
 */
@Data
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
    
    @Schema(description = "申请人 ID")
    private Long applicantId;
    
    @Schema(description = "故障描述")
    private String description;
    
    @Schema(description = "报修图片 (URL逗号分隔)")
    private String images;
    
    /**
     * 0-待处理, 1-维修中, 2-已修复(待评价), 3-已评价(结束), 4-已驳回
     */
    @Schema(description = "状态")
    private Integer status;
    
    @Schema(description = "维修工 ID")
    private Long repairmanId;
    
    @Schema(description = "完成时间")
    private LocalDateTime finishTime;
    
    @Schema(description = "评分 (1-5)")
    private Integer rating;
    
    @Schema(description = "评价内容")
    private String comment;
}