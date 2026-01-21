package com.mol.server.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 用户和角色关联实体
 * <p>
 * 对应表: sys_user_role
 * </p>
 */
@Data
@TableName("sys_user_role")
@Schema(description = "用户角色关联")
public class SysUserRole implements Serializable {
    
    @Serial
    private static final long serialVersionUID = 1L;
    
    // 因为是中间表，通常没有单一主键，这里只做映射
    // 如果 Mybatis-Plus 报错找不到主键，可以不加 @TableId，或者在数据库建一个自增 id
    
    @Schema(description = "用户 ID")
    private Long userId;
    
    @Schema(description = "角色 ID")
    private Long roleId;
}