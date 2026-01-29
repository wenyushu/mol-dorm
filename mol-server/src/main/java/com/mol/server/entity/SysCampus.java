package com.mol.server.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.mol.common.core.entity.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;
import java.math.BigDecimal;

/**
 * 校区实体类 (增加水电费单价配置)
 * <p>
 * 最高层级，状态控制影响全校区。
 * </p>
 */
@Data
@TableName("sys_campus")
@Schema(name = "SysCampus", description = "校区信息")
@EqualsAndHashCode(callSuper = true)
public class SysCampus extends BaseEntity {
    
    @Serial
    private static final long serialVersionUID = 1L;
    
    @TableId(type = IdType.ASSIGN_ID)
    @Schema(description = "主键 ID (雪花算法)")
    private Long id;
    
    @NotBlank(message = "校区名称不能为空")
    @Schema(description = "校区名称 (如: 广州本部)")
    private String campusName;
    
    @NotBlank(message = "校区编码不能为空")
    @Schema(description = "校区唯一编码 (如: GZ-01)")
    private String campusCode;
    
    @Schema(description = "校区详细地址")
    private String address;
    
    /**
     * 0: 停用 (该校区下所有业务冻结)
     * 1: 启用 (正常)
     */
    @NotNull(message = "状态不能为空")
    @Schema(description = "状态: 1-启用 0-停用")
    private Integer status;
    
    // ========== ✨ 新增：水电费计费标准 ==========
    
    @Schema(description = "冷水单价 (元/吨)")
    private BigDecimal priceWaterCold;
    
    @Schema(description = "热水单价 (元/吨)")
    private BigDecimal priceWaterHot;
    
    @Schema(description = "电费单价 (元/度)")
    private BigDecimal priceElectric;
}