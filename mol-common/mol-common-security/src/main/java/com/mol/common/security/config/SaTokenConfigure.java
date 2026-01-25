package com.mol.common.security.config;

import cn.dev33.satoken.interceptor.SaInterceptor;
import cn.dev33.satoken.stp.StpUtil;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Sa-Token 全局权限拦截器配置
 * <p>
 * 核心原则：
 * 1. "关门打狗"：默认拦截所有请求 (/**)
 * 2. "精准开窗"：只放行必须匿名的接口 (如 /auth/login)，绝不使用模糊的 /auth/**
 *
 * @author mol
 */
@Configuration
public class SaTokenConfigure implements WebMvcConfigurer {
    
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // 注册 Sa-Token 拦截器
        registry.addInterceptor(new SaInterceptor(handler -> {
                    // 校验逻辑：直接检查是否登录
                    // 因为“白名单”已经在 excludePathPatterns 中处理了，所以能进入到这里的请求，全是需要鉴权的。
                    // 进入这里的请求，意味着没有命中 excludePathPatterns 白名单
                    StpUtil.checkLogin();
                }))
                // A. 拦截范围：全量拦截
                .addPathPatterns("/**")
                
                // B. 放行范围：白名单配置 (严谨且全面)
                .excludePathPatterns(
                    // ================== 1. 业务白名单 (精准匹配) ==================
                        // 匹配 ModulePathConfig 的前缀
                        "/server/auth/login",   // 系统管理员登录
                        "/server/auth/check",   // Token 检查
                        // "/server/auth/register", // 如果有注册功能

                        // 如果宿舍模块也有独立的登录接口 (虽然通常统一用 server 的，但以防万一)
                        "/dorm/auth/login",
                        
                    // ----------------- 2. Swagger / OpenAPI 文档 -----------------
                    // (Apifox 同步、浏览器查看文档必备)
                        "/swagger-ui.html",     // Swagger UI 入口
                        "/swagger-ui/**",       // Swagger UI 内部静态资源
                        "/v3/api-docs/**",      // OpenAPI v3 JSON 数据 (Apifox 核心依赖)
                        "/v3/api-docs",         // 兼容写法
                        "/swagger-resources/**",// Swagger 资源配置
                        "/webjars/**",          // WebJars 依赖的静态资源
                        "/doc.html",            // Knife4j 文档入口 (如果有引入)
                        
                    // ----------------- 3. 静态资源与系统默认 -----------------
                        "/favicon.ico",         // 浏览器自动请求的图标
                        "/error",               // Spring Boot 默认报错页面
                        "/assets/**",           // 常见静态资源目录
                        "/*.html",              // 根目录下的 HTML
                        "/**/*.css",            // CSS 样式
                        "/**/*.js",             // JS 脚本
                        "/**/*.png", "/**/*.jpg", "/**/*.jpeg", "/**/*.gif", // 图片
                        
                    // ----------------- 4. 监控与探针 (可选) -----------------
                        "/actuator/**",         // Spring Boot 健康检查 (生产环境建议加权)
                        "/druid/**"             // Druid 连接池监控 (如果有)
                );
    }
}