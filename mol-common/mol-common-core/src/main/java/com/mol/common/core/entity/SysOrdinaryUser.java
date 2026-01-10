package com.mol.common.core.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 普通用户账户实体 (学生/教工)
 */
@Data
@TableName("sys_ordinary_user")
@EqualsAndHashCode(callSuper = true)
@Schema(description = "普通用户账户")
public class SysOrdinaryUser extends BaseEntity {
    
    @TableId(type = IdType.ASSIGN_ID)
    @Schema(description = "主键 ID")
    private Long id;
    
    @Schema(description = "学号/职工号 (系统自动生成，新增时无需填写)")
    private String username;
    
    @Schema(description = "密码 (默认 123456)")
    private String password;
    
    @Schema(description = "真实姓名")
    private String realName;
    
    @Schema(description = "性别 (1-男, 2-女)")
    private Integer sex;
    
    @Schema(description = "民族")
    private String ethnicity;
    
    @Schema(description = "手机号")
    private String phone;
    
    @Schema(description = "入学/入职年份 (例如 2025)", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer entryYear;
    
    @Schema(description = "培养层次 (仅学生需填): B-本科, Z-专科, ZB-专升本, Y-研究生, D-博士")
    private String eduLevel;
    
    @Schema(description = "所属学院 ID (必填)")
    private Long collegeId;
    
    @Schema(description = "所属专业 ID (学生必填)")
    private Long majorId;
    
    @Schema(description = "所属班级 ID (学生必填)")
    private Long classId;
    
    @Schema(description = "身份分类: 0-学生, 1-普通职工")
    private Integer userCategory;
    
    @Schema(description = "账户状态: 1-活跃, 0-归档")
    private Integer accountStatus;
}