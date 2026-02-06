package com.mol.server.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.mol.common.core.entity.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serial;
import java.math.BigDecimal;

/**
 * 校区实体类 - 资源树 Level 1
 */
@Data
@Accessors(chain = true) // 保持与父类 BaseEntity 的链式风格一致
@TableName("sys_campus")
@EqualsAndHashCode(callSuper = true)
@Schema(description = "校区层级：管理全校水电单价与全局业务生命周期")
public class SysCampus extends BaseEntity {
    @Serial
    private static final long serialVersionUID = 1L;
    
    @TableId(type = IdType.AUTO)
    @Schema(description = "主键ID")
    private Long id;
    
    @Schema(description = "校区名称 (如: 广州校区南校园)")
    private String campusName;
    
    @Schema(description = "校区唯一编码 (如: SYSU-GZ-S)")
    private String campusCode;
    
    @Schema(description = "校区详细地址")
    private String address;
    
    /**
     * 🚦 生命周期 (Lifecycle):
     * 20: 正常 (业务全开)
     * 30: 暂停使用 (🛡️防刁民：封校期间禁止任何入住变动)
     */
    @Schema(description = "状态: 20-正常, 30-暂停")
    private Integer status;
    
    @Schema(description = "冷水单价(元/吨)")
    private BigDecimal priceWaterCold;
    
    @Schema(description = "热水单价(元/吨)")
    private BigDecimal priceWaterHot;
    
    @Schema(description = "电费单价(元/度)")
    private BigDecimal priceElectric;
    
    @Version
    @Schema(description = "乐观锁版本号")
    private Integer version;
    
    @TableLogic
    @Schema(description = "逻辑删除标志 (0正常 1删除)")
    private String delFlag;
}