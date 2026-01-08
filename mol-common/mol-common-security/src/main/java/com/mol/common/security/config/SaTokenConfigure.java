package com.mol.common.security.config;

import cn.dev33.satoken.interceptor.SaInterceptor;
import cn.dev33.satoken.router.SaRouter;
import cn.dev33.satoken.stp.StpUtil;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Sa-Token 权限拦截器配置
 * * @author mol
 */
@Configuration
public class SaTokenConfigure implements WebMvcConfigurer {
    
    /**
     * 注册 Sa-Token 路由拦截器
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new SaInterceptor(handler -> {
            
            // 1. 获取路由匹配管理器，进行权限校验
            SaRouter.match("/**")
                    // 2. 放行登录、注册等认证相关接口 (匹配 mol-auth 模块)
                    .notMatch("/auth/**")

                    // 3. 放行 Swagger 标准资源 / OpenAPI 相关资源
                    .notMatch("/swagger-ui.html")
                    .notMatch("/swagger-ui/**")
                    // 放行 OpenAPI 的 JSON 数据接口 (这是 Apifox 同步数据的核心)
                    // 确保它不被拦截，Apifox 才能自动刷新接口
                    .notMatch("/v3/api-docs/**")     // 放行 OpenAPI 3 默认路径
                    .notMatch("/*/api-docs/**")      // 放行带分组的路径 (如 /sys/api-docs)
                    .notMatch("/swagger-resources/**")
                    
                    // 4. 静态资源（由于不再使用 Knife4j，doc.html 可以删掉，但保留 webjars 没坏处）
                    .notMatch("/webjars/**")
                    .notMatch("/favicon.ico")
                    
                    // 5. 除上述外，所有接口必须登录（执行登录检查）才能访问
                    .check(r -> StpUtil.checkLogin());
            
        })).addPathPatterns("/**")
                // 重点：在 Spring 层面也进行一次物理排除，提高性能并减少误拦截
                .excludePathPatterns(
                        "/swagger-ui.html",
                        "/swagger-ui/**",
                        "/v3/api-docs/**",
                        "/*/api-docs/**",
                        "/swagger-resources/**",
                        "/webjars/**",
                        "/favicon.ico",
                        "/auth/**"
                );
    }
}