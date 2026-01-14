package com.mol.server.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.mol.common.core.entity.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 违规用电/安全警报日志
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("biz_electric_violation_log")
@Schema(description = "违规电器监测日志")
public class ElectricViolationLog extends BaseEntity {
    
    @Serial
    private static final long serialVersionUID = 1L;
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    @Schema(description = "房间 ID")
    private Long roomId;
    
    @Schema(description = "违规类型: 1-功率超载 2-恶性负载(阻性) 3-夜间不归")
    private Integer violationType;
    
    @Schema(description = "违规时的功率数值(W)")
    private BigDecimal powerVal;
    
    @Schema(description = "检测时间")
    private LocalDateTime detectedTime;
    
    @Schema(description = "处理备注")
    private String remark;
}