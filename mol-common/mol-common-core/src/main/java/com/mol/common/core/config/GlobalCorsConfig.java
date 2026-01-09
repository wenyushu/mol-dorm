package com.mol.common.core.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * 全局跨域资源共享 (CORS) 配置
 * <p>
 * 作用：允许前端（如 Vue/React 运行在 localhost:5173）跨域访问后端接口（localhost:9000）
 * 如果不配置，浏览器会拦截请求并报 CORS 错误。
 *
 * @author mol
 */
@Configuration
public class GlobalCorsConfig implements WebMvcConfigurer {
    
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        // 1. 添加映射路径：拦截所有请求
        registry.addMapping("/**")
                // 2. 放行哪些原始域 (Origin)
                // 使用 allowCredentials(true) 时，必须指定具体的域名或使用 allowedOriginPatterns
                // "*" 代表允许所有域名访问，生产环境可改为具体的 "http://example.com"
                .allowedOriginPatterns("*")
                
                // 3. 放行哪些 HTTP 方法
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                
                // 4. 放行哪些请求头
                .allowedHeaders("*")
                
                // 5. 是否允许携带 Cookie / Token
                // 前后端分离项目中，鉴权通常依赖 Header 或 Cookie，所以必须开启
                .allowCredentials(true)
                
                // 6. 预检请求 (OPTIONS) 的缓存时间 (单位：秒)
                // 在这个时间内，浏览器不再发起预检请求，提升性能
                .maxAge(3600);
    }
}