package com.mol.apl.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "登录参数")
public class LoginBody {
    @Schema(description = "用户名 (学号/工号)", example = "23B0001")
    private String username;
    
    @Schema(description = "密码", example = "123456")
    private String password;
    
    @Schema(description = "用户类型: 0-管理员, 1-普通用户", example = "1")
    private Integer userType;
}
