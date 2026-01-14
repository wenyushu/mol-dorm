package com.mol.server.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.mol.common.core.entity.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;

/**
 * 专业实体类
 * <p>
 * 对应表: sys_major
 * </p>
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_major")
@Schema(description = "专业信息")
public class SysMajor extends BaseEntity {
    
    @Serial
    private static final long serialVersionUID = 1L;
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    @Schema(description = "所属学院 ID")
    private Long collegeId;
    
    @Schema(description = "专业名称")
    private String name;
    
    @Schema(description = "专业简称/代码")
    private String shortName;
    
    @Schema(description = "排序优先级 (越小越靠前)")
    private Integer sort;
}