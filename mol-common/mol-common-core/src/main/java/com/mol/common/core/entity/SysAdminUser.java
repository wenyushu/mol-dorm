package com.mol.common.core.entity;

import com.baomidou.mybatisplus.annotation.*;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;

/**
 * 系统管理员实体
 * (已升级为全字段档案模式，支持教职工住校管理)
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
    
    @Schema(description = "用户昵称")
    private String nickname;
    
    // 🟢 4. 性别：强制非空，只能是 0 或 1
    @NotNull(message = "性别不能为空")
    @Pattern(regexp = "[01]", message = "性别格式错误 (0-女 1-男)")
    @Schema(description = "性别 (0-女 1-男)")
    private String gender;
    
    @Schema(description = "头像地址")
    private String avatar;
    
    // 🟢 1. 核心身份信息 (新增)
    @NotBlank(message = "身份证号不能为空")
    @Schema(description = "身份证号")
    private String idCard;
    
    @NotBlank(message = "联系电话不能为空")
    @Schema(description = "联系电话")
    private String phone;
    
    @Schema(description = "电子邮箱")
    private String email;
    
    @NotBlank(message = "民族不能为空")
    @Schema(description = "民族")
    private String ethnicity;
    
    @NotBlank(message = "籍贯不能为空")
    @Schema(description = "籍贯")
    private String hometown;
    
    // 🟢 2. 紧急联系人 (非空)
    @NotBlank(message = "紧急联系人不能为空")
    @Schema(description = "紧急联系人姓名")
    private String emergencyContact;
    
    @NotBlank(message = "紧急联系电话不能为空")
    @Schema(description = "紧急联系人电话")
    private String emergencyPhone;
    
    @NotBlank(message = "紧急联系人关系不能为空")
    @Schema(description = "紧急联系人关系")
    private String emergencyRelation;
    
    // 🟢 3. 居住信息
    @NotNull(message = "居住类型不能为空")
    @Schema(description = "居住类型 (0:住校 1:校外)")
    private Integer residenceType;
    
    @Schema(description = "校外居住地址")
    private String currentAddress;
    
    // ----------- 状态控制 -----------
    
    @NotBlank(message = "状态不能为空")
    @Schema(description = "帐号状态 (0:正常 1:停用)")
    private String status;
    
    @Schema(description = "逻辑删除标志")
    @TableLogic
    private String delFlag;
}