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
    @Size(min = 3, max = 64, message = "账号长度需在 3-64 字符之间")
    @Schema(description = "登录账号 (学号/工号/管理员名)", example = "admin")
    private String username;
    
    @NotBlank(message = "密码不能为空")
    @Size(min = 6, max = 32, message = "密码长度需在 6-32 字符之间")
    @Schema(description = "密码", example = "123456")
    private String password;
    
    // 修改建议：更新描述，明确该字段在自动探测模式下是可选的
    @Schema(description = "用户类型 (可选): 系统会自动根据账号识别是管理员还是学生，前端可不传", example = "admin")
    private String userType = "ordinary"; // 默认普通用户，以防止空指针
    
    // 新增：记住我 (true-7天免登录, false-关闭浏览器失效)
    @Schema(description = "是否记住我")
    private Boolean rememberMe;
    
    // 新增：设备类型 (用于互斥登录区分设备)
    // 建议值：PC, APP, WEB
    @Schema(description = "登录设备标识(PC/APP/WEB)", example = "PC")
    private String device;
}