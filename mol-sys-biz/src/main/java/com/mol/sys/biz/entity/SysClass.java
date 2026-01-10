package com.mol.sys.biz.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.mol.common.core.entity.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 班级实体类
 * <p>
 * 对应表：sys_class
 *
 * @author mol
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_class")
@Schema(description = "班级信息")
public class SysClass extends BaseEntity {
    
    @Schema(description = "主键 ID")
    @TableId(type = IdType.AUTO)
    private Long id;
    
    @Schema(description = "所属专业 ID")
    private Long majorId;
    
    @Schema(description = "班级名称 (如: 25级网安1班)")
    private String name;
    
    @Schema(description = "年级 (如: 2025)")
    private Integer grade;
    
    @Schema(description = "删除标志 (0-正常, 1-删除)")
    private String delFlag;
}