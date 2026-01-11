package com.mol.server.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.mol.common.core.entity.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 学院实体类
 * <p>
 * 对应表：sys_college
 * 作用：存储学院信息，其中的 code 字段用于生成学号
 *
 * @author mol
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_college")
@Schema(description = "学院信息")
public class SysCollege extends BaseEntity {
    
    @Schema(description = "主键 ID")
    @TableId(type = IdType.AUTO) // 配合数据库的 AUTO_INCREMENT
    private Long id;
    
    @Schema(description = "所属校区 ID")
    private Long campusId;
    
    @Schema(description = "学院名称 (如: 网络安全学院)")
    private String name;
    
    @Schema(description = "学院代码 (如: 06, 必须是 2 位数字, 用于生成学号)")
    private String code;
    
    @Schema(description = "显示排序 (越小越靠前)")
    private Integer sort;
    
    @Schema(description = "删除标志 (0-正常, 1-删除)")
    private String delFlag;
}