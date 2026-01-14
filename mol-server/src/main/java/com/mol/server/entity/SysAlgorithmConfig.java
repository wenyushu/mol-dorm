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
 * 系统核心算法配置实体
 * <p>
 * 用于动态调整分配算法的权重参数，无需重启服务器。
 * </p>
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_algorithm_config")
@Schema(description = "算法/系统参数配置")
public class SysAlgorithmConfig extends BaseEntity {
    
    @Serial
    private static final long serialVersionUID = 1L;
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    @Schema(description = "配置键 (如: weight_sleep_time)")
    private String configKey;
    
    @Schema(description = "配置值")
    private String configValue;
    
    @Schema(description = "配置说明")
    private String description;
}