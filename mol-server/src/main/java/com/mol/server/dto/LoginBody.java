package com.mol.server.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 登录请求体
 * 🛡️ 防刁民设计：增加 @Size 校验，防止有人传 1MB 的超长字符串把数据库查崩
 */
@Data
@Schema(description = "用户登录参数")
public class LoginBody {
    
    @NotBlank(message = "账号不能为空")
    @Size(min = 2, max = 64, message = "账号长度需在2-64字符之间")
    @Schema(description = "登录账号 (学号/工号/管理员名)", example = "2024001")
    private String username;
    
    @NotBlank(message = "密码不能为空")
    @Size(min = 5, max = 32, message = "密码长度需在5-32字符之间")
    @Schema(description = "密码", example = "123456")
    private String password;
    
    @Schema(description = "用户类型: admin-管理员, ordinary-普通用户(默认)", example = "admin")
    private String userType = "ordinary"; // 默认普通用户，防止空指针
}