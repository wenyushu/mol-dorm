package com.mol.common.core.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;

/**
 * 系统管理员实体类
 * 对应数据库表：sys_admin_user
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_admin_user")
public class SysAdminUser extends BaseEntity {
    
    @Serial
    private static final long serialVersionUID = 1L;
    
    /**
     * 主键 ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /**
     * 用户名 (登录账号)
     */
    private String username;
    
    /**
     * 密码 (加密存储)
     */
    private String password;
    
    /**
     * 真实姓名
     */
    private String realName;
    
    /**
     * 手机号码
     */
    private String phone;
    
    /**
     * 角色类型
     * 0-超级管理员, 1-宿管人员, 2-辅导员 ...
     */
    private Integer roleType;
    
    /**
     * 所属部门ID (可选)
     */
    private Long deptId;
    
    /**
     * 状态 (0:正常, 1:停用)
     * 注意：del_flag 在 BaseEntity 里已经有了，这里不需要重复定义
     */
    private String status;
}