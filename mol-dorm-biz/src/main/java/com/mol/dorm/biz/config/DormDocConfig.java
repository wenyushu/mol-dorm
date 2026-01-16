package com.mol.dorm.biz.config;

import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 宿舍业务模块接口文档配置
 * 替代 application-dorm.yml 中的 springdoc.group-configs
 */
@Configuration
public class DormDocConfig {
    
    @Bean
    public GroupedOpenApi dormApi() {
        return GroupedOpenApi.builder()
                .group("dorm") // 分组名称
                .pathsToMatch("/**")
                .packagesToScan("com.mol.dorm.biz.controller") // 扫描包路径
                .build();
    }
}