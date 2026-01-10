package com.mol.sys.biz.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 用户和角色关联表
 * 对应数据库：sys_user_role
 */
@Data
@NoArgsConstructor  // 生成无参构造
@AllArgsConstructor // 生成全参构造 -> 方便 new SysUserRole(userId, roleId)
@TableName("sys_user_role")
public class SysUserRole implements Serializable {
    
    // 不需要 @TableId，因为关联表通常使用联合主键，或者干脆没有单列主键
    // MyBatis-Plus 操作这种表时，通常使用 Wrapper 条件删除/查询
    
    /**
     * 用户 ID
     */
    private Long userId;
    
    /**
     * 角色 ID
     */
    private Long roleId;
}