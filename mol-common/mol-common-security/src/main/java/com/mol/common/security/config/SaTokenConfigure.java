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
                    
                    // 3. 放行 Swagger / SpringDoc / Knife4j 相关的所有静态资源和接口地址
                    // 注意：由于配置了 context-path，拦截器中的路径是相对于 context-path 之后的
                    .notMatch("/swagger-ui.html")
                    .notMatch("/swagger-ui/**")
                    .notMatch("/v3/api-docs/**")
                    .notMatch("/doc.html")       // 预留 Knife4j 路径
                    .notMatch("/webjars/**")
                    
                    // 4. 除上述外，所有接口必须登录才能访问
                    .check(r -> StpUtil.checkLogin());
            
        })).addPathPatterns("/**");
    }
}