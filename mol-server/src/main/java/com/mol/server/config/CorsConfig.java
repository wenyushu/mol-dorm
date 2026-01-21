package com.mol.server.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

/**
 * 跨域配置 (CORS)
 * <p>
 * 解决前端报错: "Access-Control-Allow-Origin" missing 或 "header 'satoken' is not allowed"
 * </p>
 *
 * @author mol
 */
@Configuration
public class CorsConfig {
    
    @Bean
    public CorsFilter corsFilter() {
        CorsConfiguration config = new CorsConfiguration();
        
        // 1. 允许任何来源 (生产环境建议改为具体的域名，如 "http://localhost:8080")
        // SpringBoot 2.4+ 使用 setAllowedOriginPatterns 替代 setAllowedOrigins
        config.addAllowedOriginPattern("*");
        
        // 2. 允许任何请求头 (重点：这包含了我们的 'satoken')
        config.addAllowedHeader("*");
        
        // 3. 允许任何方法 (GET, POST, PUT, DELETE...)
        config.addAllowedMethod("*");
        
        // 4. 允许携带凭证 (如果未来需要 Cookie 交互，这个必须为 true)
        config.setAllowCredentials(true);
        
        // 5. 暴露哪些头部给前端 (前端 js 可以读取到的响应头)
        config.addExposedHeader("Authorization");
        config.addExposedHeader("satoken"); // 允许前端读取我们返回的 token 头(如果需要)
        
        // 注册配置
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return new CorsFilter(source);
    }
}