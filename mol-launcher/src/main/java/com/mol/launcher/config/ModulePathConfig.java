package com.mol.launcher.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.config.annotation.PathMatchConfigurer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * 模块路由自动前缀配置
 */
@Configuration
public class ModulePathConfig implements WebMvcConfigurer {
    
    @Override
    public void configurePathMatch(PathMatchConfigurer configurer) {
        // 系统模块包名前缀为：com.mol.server
        // 这样 com.mol.server 下的 Controller 会自动加上 /server 前缀
        configurer.addPathPrefix("/server", c ->
                c.getPackageName().startsWith("com.mol.server")
        );
        
        // 2. 宿舍业务模块 (宿舍业务模块包名是 com.mol.dorm.biz)
        configurer.addPathPrefix("/dorm", c ->
                c.getPackageName().startsWith("com.mol.dorm.biz")
        );
    }
}