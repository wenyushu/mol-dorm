package com.mol.server.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import com.mol.common.core.entity.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;

/**
 * 角色实体类
 * <p>
 * 对应表: sys_role
 * 定义系统中的身份：超级管理员、宿管、学生、辅导员等
 * </p>
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_role")
@Schema(description = "角色信息")
public class SysRole extends BaseEntity {
    
    @Serial
    private static final long serialVersionUID = 1L;
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    @Schema(description = "角色名称 (如: 宿管老师)")
    private String roleName;
    
    @Schema(description = "角色权限字符串 (如: dorm_manager)")
    private String roleKey;
    
    @Schema(description = "显示顺序")
    private Integer sort;
    
    @Schema(description = "角色状态 (0:正常 1:停用)")
    private String status;
    
    @Schema(description = "备注")
    private String remark;
    
    @Schema(description = "逻辑删除标志")
    @TableLogic
    private String delFlag;
}