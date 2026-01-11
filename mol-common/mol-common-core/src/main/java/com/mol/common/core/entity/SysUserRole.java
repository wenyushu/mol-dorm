package com.mol.common.core.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 用户和角色关联表
 * <p>
 * 对应数据库: sys_user_role
 * 这是一个纯关联表，通常不需要继承 BaseEntity (除非你有给关联表加审计字段的需求)
 * </p>
 *
 * @author mol
 */
@Data
@TableName("sys_user_role")
@Schema(description = "用户角色关联对象")
public class SysUserRole implements Serializable {
    
    @Serial
    private static final long serialVersionUID = 1L;
    
    @Schema(description = "用户 ID")
    private Long userId;
    
    @Schema(description = "角色 ID")
    private Long roleId;
}