package com.mol.common.core.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;

/**
 * 系统角色表
 * <p>
 * 对应数据库: sys_role
 * 用于定义系统的权限组，如: 超级管理员, 宿管, 辅导员等
 * </p>
 *
 * @author mol
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_role")
@Schema(description = "系统角色对象")
public class SysRole extends BaseEntity {
    
    @Serial
    private static final long serialVersionUID = 1L;
    
    @Schema(description = "角色 ID")
    @TableId(type = IdType.AUTO)
    private Long id;
    
    @Schema(description = "角色名称 (如: 宿管经理)")
    private String roleName;
    
    @Schema(description = "角色权限字符串 (如: dorm_manager)")
    private String roleKey;
    
    @Schema(description = "角色状态 (0:正常 1:停用)")
    private String status;
}