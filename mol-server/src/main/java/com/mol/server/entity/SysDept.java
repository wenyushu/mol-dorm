package com.mol.server.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.mol.common.core.entity.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 部门实体
 * 对应表: sys_dept
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_dept")
@Schema(description = "部门实体")
public class SysDept extends BaseEntity {
    
    @Schema(description = "主键ID")
    @TableId(type = IdType.AUTO) // 对应数据库的 AUTO_INCREMENT
    private Long id;
    
    @Schema(description = "部门名称")
    private String name;
    
    @Schema(description = "部门编码")
    private String code;
    
    @Schema(description = "所属校区 ID")
    private Long campusId;
    
    @Schema(description = "部门简介")
    private String intro;
    
    @Schema(description = "父部门 ID")
    private Long parentId;
    
    @Schema(description = "排序")
    private Integer sort;
    
    @Schema(description = "状态 (0正常 1停用)")
    private String status;
}