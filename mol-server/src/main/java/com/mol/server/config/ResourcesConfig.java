package com.mol.server.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * 通用资源映射配置
 * 核心作用：将本地磁盘路径映射为 HTTP URL，让前端可以通过链接访问图片
 */
@Configuration
public class ResourcesConfig implements WebMvcConfigurer {
    
    // 读取 yml 中的路径配置
    // 双保险：如果 yml 里没配，则用默认路径：D:/mol/uploadPath
    @Value("${mol.profile:D:/mol/uploadPath}")
    private String localFilePath;
    
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 映射规则：/profile/** ->  本地磁盘路径
        // 例如：访问 http://localhost:9090/profile/avatar/abc.png
        // 实际指向：D:/mol/uploadPath/avatar/abc.png
        registry.addResourceHandler("/profile/**")
                .addResourceLocations("file:" + localFilePath + "/");
    }
}