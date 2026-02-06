package com.mol.dorm.biz.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.mol.common.core.entity.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.math.BigDecimal;

@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
@TableName("dorm_meter")
@Schema(description = "统一仪表设备")
public class DormMeter extends BaseEntity {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    @Schema(description = "房间 ID")
    private Long roomId;
    
    @Schema(description = "设备编号(物理 ID)")
    private String meterNo;
    
    @Schema(description = "设备名称")
    private String name;
    
    /**
     * 10: 冷水表
     * 11: 热水表
     * 20: 照明电表 (限流小)
     * 21: 空调电表 (限流大)
     */
    @Schema(description = "类型: 10冷水 11热水 20照明 21空调")
    private Integer type;
    
    @Schema(description = "当前读数")
    private BigDecimal currentReading;
    
    /**
     * 🛡️ 恶性负载控制：
     * 比如 2000W。当寝室使用热得快等违规电器时，
     * 硬件或软件层根据此值触发自动跳闸（switchStatus=0）。
     */
    @Schema(description = "功率限制(W)")
    private Integer powerLimit;
    
    @Schema(description = "阀门状态: 1通 0断")
    private Integer switchStatus;
    
    /**
     * 🚦 设备生命周期：
     * 20: 在线, 50: 离线/故障, 0: 报废
     */
    @Schema(description = "设备状态: 20正常 50故障")
    private Integer status;
}