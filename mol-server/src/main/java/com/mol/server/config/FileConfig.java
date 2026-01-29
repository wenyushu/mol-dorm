package com.mol.server.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * 文件资源映射配置
 * <p>
 * 作用：将本地磁盘路径映射为 HTTP URL，让前端能访问图片。
 * 例如：磁盘 D:/mol/upload/abc.jpg -> HTTP http://host/profile/upload/abc.jpg
 * </p>
 */
@Configuration
public class FileConfig implements WebMvcConfigurer {
    
    // 默认路径，生产环境请在 application.yml 中配置 mol.profile
    @Value("${mol.profile:D:/mol/upload}")
    private String profile;
    
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 映射路径：/profile/** -> 本地文件夹
        // 注意：file: 后面必须拼接路径，且 Windows下通常需要 file:/D:/... 但 Spring Boot 会自动处理 file:D:/...
        // 这里的配置确保了 file:D:/mol/upload/ 这样的格式
        registry.addResourceHandler("/profile/**")
                .addResourceLocations("file:" + profile + "/");
    }
}