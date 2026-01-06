package com.mol.common.security.config;


import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Swagger / OpenAPI 3 全局配置类
 * 用于生成接口文档，并集成 Sa-Token 鉴权功能
 */
@Configuration
public class OpenApiConfig {
    
    @Bean
    public OpenAPI customOpenAPI() {
        // 1. 定义安全方案名称（在文档中显示的标识）
        // 这里的名称要和下面 addSecuritySchemes 一致
        final String securitySchemeName = "mol-token";
        
        return new OpenAPI()
                // 配置文档基本信息（标题、描述、版本、作者等）
                .info(new Info()
                        .title("MOL-DORM 宿舍管理系统 - API 接口文档")
                        .version("1.0.0")
                        .description("基于 Spring Boot 3 & mol-token 的接口文档"))
                // 2. 开启全局安全需求：让所有接口右上角都显示“锁”图标
                // 如果不加这一行，你需要在每个 Controller 方法上加注解，非常繁琐
                .addSecurityItem(new SecurityRequirement().addList(securitySchemeName))
                // 3. 定义安全方案（告知 Swagger 如何在 HTTP 请求中携带 Token）
                .components(new Components()
                        .addSecuritySchemes(securitySchemeName,
                                new SecurityScheme()
                                        .name("mol-token") // 注意：这里的名字必须和 sa-token.token-name 配置的一致
                                        .type(SecurityScheme.Type.APIKEY) // Sa-Token 通常用 ApiKey 模式
                                        .in(SecurityScheme.In.HEADER))); // Token 放在请求头 (Header) 中
    }
}
