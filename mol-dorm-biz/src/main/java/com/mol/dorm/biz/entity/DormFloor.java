package com.mol.dorm.biz.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.mol.common.core.entity.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serial;

/**
 * 楼层实体类 - 资源树 Level 3
 */
@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
@TableName("dorm_floor")
@Schema(description = "楼层层级：细化性别防御与楼层生命周期控制")
public class DormFloor extends BaseEntity {
    @Serial
    private static final long serialVersionUID = 1L;
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    @Schema(description = "归属楼栋ID")
    private Long buildingId;
    
    @Schema(description = "物理楼层号 (如: 1, 2, 3...)")
    private Integer floorNum;
    
    /**
     * 🛡️ 楼层性别防火墙 (Gender):
     * 1: 男, 2: 女 (🛡️防刁民：混合楼中每一层必须性别纯粹)
     */
    @Schema(description = "性别限制: 1-男, 2-女")
    private Integer genderLimit;
    
    /**
     * 🚦 生命周期 (Lifecycle):
     * 20: 正常, 40: 整层封闭装修
     */
    @Schema(description = "状态: 20正常, 40装修, 0停止")
    private Integer status;
    
    @Version
    private Integer version;
}