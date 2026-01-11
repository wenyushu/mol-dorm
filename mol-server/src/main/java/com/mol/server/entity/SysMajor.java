package com.mol.server.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.mol.common.core.entity.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 专业实体类
 * <p>
 * 对应表：sys_major
 *
 * @author mol
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_major")
@Schema(description = "专业信息")
public class SysMajor extends BaseEntity {
    
    @Schema(description = "主键 ID")
    @TableId(type = IdType.AUTO)
    private Long id;
    
    @Schema(description = "所属学院 ID")
    private Long collegeId;
    
    @Schema(description = "专业名称 (如: 网络工程)")
    private String name;
    
    @Schema(description = "删除标志 (0-正常, 1-删除)")
    private String delFlag;
}