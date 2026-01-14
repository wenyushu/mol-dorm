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
 * 班级实体类
 * <p>
 * 对应表: biz_class (注意表名可能是 biz_class 而不是 sys_class)
 * </p>
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("biz_class")
@Schema(description = "班级信息")
public class SysClass extends BaseEntity {
    
    @Serial
    private static final long serialVersionUID = 1L;
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    @Schema(description = "所属专业 ID")
    private Long majorId;
    
    @Schema(description = "年级 (如: 2022)")
    private Integer grade;
    
    @Schema(description = "班级名称 (如: 软件工程 1 班 )")
    private String className;
    
    @Schema(description = "班级全称 (如: 2024 级软件工程 1 班 )")
    private String fullName;
}