package com.mol.server.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "登录成功响应信息")
public class LoginVO {
    
    @Schema(description = "Token 名称 (Header Key)", example = "mol-token")
    private String tokenName;
    
    @Schema(description = "Token 值 (Header Value)", example = "eyJhbGciOiJIUzI1Ni...")
    private String tokenValue;
    
    // 明确告诉文档，这个字段输出是 string 类型
    @Schema(description = "用户 ID", example = "1", type = "string")
    private Long userId;
    
    @Schema(description = "真实姓名", example = "张三")
    private String realName;
    
    // 新增：昵称
    @Schema(description = "用户昵称", example = "阿祖")
    private String nickname;
    
    @Schema(description = "前端展示角色 (如: admin, student)", example = "student")
    private String role;
    
    // 明确告诉文档，这个字段允许为 null
    // (注意：nullable=true 在某些 Swagger 版本可能不生效，若不生效可直接在 Apifox 界面改)
    @Schema(description = "头像地址", example = "http://...", nullable = true)
    private String avatar;
    
    // 新增：告诉前端是否需要强制改密
    private Boolean needChangePwd;
}