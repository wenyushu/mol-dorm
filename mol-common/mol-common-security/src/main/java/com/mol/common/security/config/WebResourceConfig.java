package com.mol.common.security.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web 静态资源映射配置
 * 核心作用：1. 文档映射  2. 图片上传后的 Web 访问映射
 */
@Slf4j
@Configuration
public class WebResourceConfig implements WebMvcConfigurer {
    
    @Value("${mol.profile:D:/mol/upload}")
    private String localPath; // 必须和 yml 里的 mol.profile 保持一致
    
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        
        log.info("⚙️ 正在初始化静态资源映射...");
        log.info("📂 文件存储物理路径: {}", localPath);
        
        // 1. 映射 Knife4j 文档页面 (doc.html)
        registry.addResourceHandler("/doc.html")
                .addResourceLocations("classpath:/META-INF/resources/");
        
        // 2. 映射 Swagger 的静态资源 (webjars)
        registry.addResourceHandler("/webjars/**")
                .addResourceLocations("classpath:/META-INF/resources/webjars/");
        
        /* 3. 🛡️ 【核心：图片访问映射】
         * 逻辑：SysFileService 返回的 URL 是 "/profile/lost_found/..."
         * 匹配：前端请求 /api/profile/** 时，Spring 会去 localPath 物理目录下找文件
         */
        registry.addResourceHandler("/profile/**")
                .addResourceLocations("file:" + localPath + "/");
        
        log.info("✅ 资源映射就绪: /profile/** -> {}", localPath);
    }
}