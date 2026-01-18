package com.mol.server.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@Schema(description = "个人资料修改参数")
public class UserProfileBody {
    
    @Size(min = 1, max = 64, message = "昵称长度不能超过64个字符")
    @Schema(description = "昵称", example = "我是昵称")
    private String nickname;
    
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    @Schema(description = "手机号", example = "13800138000")
    private String phone;
    
    @Size(max = 255, message = "头像路径过长")
    @Schema(description = "头像地址", example = "https://...")
    private String avatar;
    
    @Schema(description = "邮箱", example = "test@mol.com")
    private String email;
}