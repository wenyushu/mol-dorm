package com.mol.common.core.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 管理员账户实体
 */
@Data
@TableName("sys_admin_user")
@EqualsAndHashCode(callSuper = true)
@Schema(description = "管理员账户")
public class SysAdminUser extends BaseEntity {
    
    @TableId(type = IdType.ASSIGN_ID)
    @Schema(description = "主键 ID")
    private Long id;
    
    @Schema(description = "用户名")
    private String username;
    
    @Schema(description = "密码 (BCrypt 加密)")
    private String password;
    
    @Schema(description = "真实姓名")
    private String realName;
    
    @Schema(description = "手机号")
    private String phone;
    
    @Schema(description = "管理员级别: 2-超级管理员, 1-部门管理, 0-普通宿管")
    private Integer adminLevel;
    
    @Schema(description = "帐号状态 (0-正常, 1-停用)")
    private String status;
}