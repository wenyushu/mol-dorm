package com.mol.server.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "登录表单")
public class LoginBody {
    @Schema(description = "账号/学号")
    private String username;
    
    @Schema(description = "密码")
    private String password;
    
    @Schema(description = "用户类型: admin / student")
    private String userType;
}