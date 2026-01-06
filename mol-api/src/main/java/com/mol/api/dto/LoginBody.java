package com.mol.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 登录请求参数实体
 */
@Data
@Schema(description = "登录参数")
public class LoginBody {
    
    @Schema(description = "用户名", example = "admin", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "用户名不能为空!")
    private String username;
    
    @Schema(description = "密码", example = "123456", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "密码不能为空!")
    private String password;
    
    @Schema(description = "用户类型: 0-管理员, 1-普通用户", example = "0")
    @NotNull(message = "用户类型不能为空")
    private Integer userType;
}