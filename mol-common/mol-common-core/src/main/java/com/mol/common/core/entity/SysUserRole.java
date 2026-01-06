package com.mol.common.core.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 用户角色中间表
 * 对应数据库表：sys_user_role
 */
@Data
@TableName("sys_user_role")
@Schema(description = "用户角色关联")
public class SysUserRole implements Serializable {
    
    @Serial
    private static final long serialVersionUID = 1L;
    
    @Schema(description = "用户 ID")
    private Long userId;
    
    @Schema(description = "角色 ID")
    private Long roleId;
    
    /**
     * 用户类型: 0-管理员, 1-普通用户
     * 这是你数据库里设计的关键字段，用来区分 admin 表和 ordinary 表的 ID
     */
    @Schema(description = "用户类型: 0-管理员, 1-普通用户")
    private Integer userType;
}