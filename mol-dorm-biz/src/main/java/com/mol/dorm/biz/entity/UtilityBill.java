package com.mol.dorm.biz.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.mol.common.core.entity.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 水电费账单实体类
 * <p>
 * 对应表: biz_utility_bill
 * 记录每个房间每月的消耗和费用。
 * </p>
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("biz_utility_bill")
@Schema(description = "水电费账单")
public class UtilityBill extends BaseEntity {
    
    @Serial
    private static final long serialVersionUID = 1L;
    
    @Schema(description = "主键 ID")
    @TableId(type = IdType.AUTO)
    private Long id;
    
    @Schema(description = "房间 ID")
    private Long roomId;
    
    @Schema(description = "账单月份 (格式: 2024-06)")
    private String month;
    
    // --- 用量数据 ---
    
    @Schema(description = "冷水用量 (吨)")
    private BigDecimal waterCold;
    
    @Schema(description = "热水用量 (吨)")
    private BigDecimal waterHot;
    
    @Schema(description = "用电量 (度/kWh)")
    private BigDecimal electricUsage;
    
    // --- 费用数据 ---
    
    @Schema(description = "冷水费 (元)")
    private BigDecimal costWaterCold;
    
    @Schema(description = "热水费 (元)")
    private BigDecimal costWaterHot;
    
    @Schema(description = "电费 (元)")
    private BigDecimal costElectric;
    
    @Schema(description = "总金额 (元)")
    private BigDecimal totalCost;
    
    // --- 状态控制 ---
    
    @Schema(description = "缴费状态: 0-未缴 1-已缴 2-逾期")
    private Integer paymentStatus;
    
    @Schema(description = "支付时间")
    private LocalDateTime payTime;
    
    @Schema(description = "乐观锁版本号")
    @Version
    private Integer version;
    
    @Schema(description = "逻辑删除标志")
    @TableLogic
    private String delFlag;
}