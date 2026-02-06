package com.mol.server.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField; // 记得导入
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.mol.common.core.entity.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;

/**
 * 班级实体类
 * 对应表: biz_class
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("biz_class") // 确认表名正确
@Schema(description = "班级信息")
public class SysClass extends BaseEntity {
    
    @Serial
    private static final long serialVersionUID = 1L;
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    @Schema(description = "所属专业 ID")
    private Long majorId;
    
    @Schema(description = "年级 (如: 2024)")
    private Integer grade;
    
    @Schema(description = "班级名称 (如: 软件工程 1 班)")
    private String className;
    
    // ✨✨✨ 新增字段 ✨✨✨
    @Schema(description = "培养层次 (自动继承自专业)", example = "本科")
    private String educationLevel;
    
    // ✨ 新增关键字段：辅导员 ID (用于我的宿舍页面显示)
    @Schema(description = "辅导员 ID")
    private Long counselorId;
    
    // ⚠️ 数据库没有这个字段，必须加 exist=false，否则报错
    @Schema(description = "班级全称 (业务拼接)")
    @TableField(exist = false)
    private String fullName;
}