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
    
    @Schema(description = "用户名 (学号/工号)")
    private String username;
    
    @Schema(description = "密码 (BCrypt 加密)")
    private String password;
    
    @Schema(description = "真实姓名")
    private String realName;
    
    @Schema(description = "性别 (1-男, 2-女)")
    private Integer sex;
    
    @Schema(description = "民族")
    private String ethnicity;
    
    @Schema(description = "手机号")
    private String phone;
    
    @Schema(description = "身份分类: 0-学生, 1-普通职工")
    private Integer userCategory;
    
    @Schema(description = "账户状态: 1-活跃, 0-归档")
    private Integer accountStatus;
}
