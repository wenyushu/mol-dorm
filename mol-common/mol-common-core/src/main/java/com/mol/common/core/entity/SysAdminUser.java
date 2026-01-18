package com.mol.common.core.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;

/**
 * 系统管理员实体
 * <p>
 * 对应表: sys_admin_user
 * 包含基础审计字段继承
 * </p>
 *
 * @author mol
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_admin_user")
@Schema(description = "系统管理员对象")
public class SysAdminUser extends BaseEntity {
    
    @Serial
    private static final long serialVersionUID = 1L;
    
    @Schema(description = "主键 ID")
    @TableId(type = IdType.AUTO)
    private Long id;
    
    @Schema(description = "登录账号")
    private String username;
    
    @Schema(description = "加密密码")
    private String password;
    
    @Schema(description = "真实姓名")
    private String realName;
    
    // 🟢 新增字段
    @Schema(description = "用户昵称")
    private String nickname;
    
    @Schema(description = "联系电话")
    private String phone;
    
    @Schema(description = "电子邮箱")
    private String email;
    
    @Schema(description = "头像地址")
    private String avatar;
    
    /**
     * 帐号状态 (0:正常 1:停用)
     * 数据库 char(1)
     */
    @Schema(description = "帐号状态 (0:正常 1:停用)")
    private String status;
    
    @Schema(description = "逻辑删除标志")
    @TableLogic
    private String delFlag;
}