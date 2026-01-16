package com.mol.server.config;

import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 系统模块接口文档配置
 * 替代 application-server.yml 中的 springdoc.group-configs
 */
@Configuration
public class SysDocConfig {
    
    @Bean
    public GroupedOpenApi sysApi() {
        return GroupedOpenApi.builder()
                .group("sys") // 分组名称
                .pathsToMatch("/**")
                .packagesToScan("com.mol.server.controller") // 扫描包路径
                .build();
    }
}