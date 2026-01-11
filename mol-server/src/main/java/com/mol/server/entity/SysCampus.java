package com.mol.server.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.mol.common.core.entity.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;

/**
 * 校区实体类
 *
 * @author mol
 */
@Data
@TableName("sys_campus")
@Schema(name = "SysCampus", description = "校区信息")
@EqualsAndHashCode(callSuper = true)
public class SysCampus extends BaseEntity {
    
    @Serial
    private static final long serialVersionUID = 1L;
    
    /**
     * 校区主键 ID
     */
    @TableId(type = IdType.ASSIGN_ID)
    @Schema(description = "主键 ID (雪花算法)", example = "1742563859210452994")
    private Long id;
    
    /**
     * 校区名称
     */
    @NotBlank(message = "校区名称不能为空")
    @Schema(description = "校区名称", example = "广州本部", requiredMode = Schema.RequiredMode.REQUIRED)
    private String campusName;
    
    /**
     * 校区唯一编码
     */
    @NotBlank(message = "校区编码不能为空")
    @Schema(description = "校区唯一编码", example = "GZ-BASE-001", requiredMode = Schema.RequiredMode.REQUIRED)
    private String campusCode;
    
    /**
     * 校区详细地址
     */
    @Schema(description = "校区地址", example = "广东省广州市天河区学院路 1 号")
    private String address;
    
    /**
     * 校区状态 (0-启用, 1-停用)
     * 使用 Pattern 校验，确保前端传来的只能是 0 或 1
     */
    @Pattern(regexp = "[01]", message = "状态值格式不正确")
    @Schema(description = "状态 (0-启用, 1-停用)", example = "0", defaultValue = "0")
    private String status;
}