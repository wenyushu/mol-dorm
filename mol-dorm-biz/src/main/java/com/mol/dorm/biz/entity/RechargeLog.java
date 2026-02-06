package com.mol.dorm.biz.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.mol.common.core.entity.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
@TableName("biz_recharge_log")
@Schema(description = "充值流水记录 - 财务审计专用")
public class RechargeLog extends BaseEntity {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    @Schema(description = "流水号 (唯一索引，用于幂等控制)")
    private String orderNo;
    
    @Schema(description = "房间 ID")
    private Long roomId;
    
    @Schema(description = "操作用户 ID (谁执行的动账)")
    private Long userId;
    
    @Schema(description = "充值金额")
    private BigDecimal amount;
    
    @Schema(description = "变动前余额 (快照)")
    private BigDecimal beforeBalance;
    
    @Schema(description = "变动后余额 (快照)")
    private BigDecimal afterBalance;
    
    @Schema(description = "支付方式 (ONLINE/CASH/ADMIN)")
    private String payType;
    
    @Schema(description = "状态: 1成功 0失败")
    private Integer status;
    
}