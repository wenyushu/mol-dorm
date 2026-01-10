package com.mol.launcher;

import com.mol.auth.MolAuthApplication;
import com.mol.dorm.biz.MolDormApplication;
import com.mol.sys.biz.MolSysApplication;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.annotation.ComponentScan;

/**
 * MOL-DORM 聚合启动类
 * 模仿 Pig 项目风格，实现本地开发环境一键启动所有子模块
 * 注意：不要在这里加 @SpringBootApplication 注解！
 */
@SpringBootApplication
// 1. 扫描所有模块的 Bean (Service, Component, Controller)
@ComponentScan("com.mol")
// 2. 扫描所有模块的 Mapper 接口
@MapperScan("com.mol.**.mapper")
public class MolLauncherApplication {
    
    public static void main(String[] args) {
        
        SpringApplication.run(MolLauncherApplication.class, args);
        System.out.println("#######################################");
        
        // 1. 设置全局系统属性，防止 JMX 冲突
        // 设置全局系统属性
        System.setProperty("spring.threads.virtual.enabled", "true");
        // 【核心修复】强制禁用 Spring Boot 的 Admin JMX 注册，防止名字冲突
        System.setProperty("spring.application.admin.enabled", "false");
        // 必须禁用 JMX，否则多个应用会争夺同一个 MBean 导致无法启动
        System.setProperty("spring.jmx.enabled", "false");
        
        System.out.println(">>> 正在一键启动 MOL-DORM 全量业务... ");
        
        // 2. 启动 认证中心 (端口 9000)
        // 它会加载 mol-auth 模块下的所有 yml 配置
        new SpringApplicationBuilder(MolAuthApplication.class)
                // - 指定加载 application-auth.yml
                .properties("spring.config.name:application-auth")
                .properties("spring.application.admin.enabled:false")
                .run(args);
        
        // 3. 启动 系统管理模块 (端口 8080)
        // 它会加载 mol-sys-biz 模块下的所有 yml 配置
        new SpringApplicationBuilder(MolSysApplication.class)
                // - 指定加载 application-sys.yml
                .properties("spring.config.name:application-sys")
                .properties("spring.application.admin.enabled:false")
                .run(args);
        
        // 4. 启动 宿舍业务模块 (端口 8081)
        // 它会加载 mol-dorm-biz 模块下的所有 yml 配置
        new SpringApplicationBuilder(MolDormApplication.class)
                // - 指定加载 application-dorm.yml
                .properties("spring.config.name:application-dorm")
                .properties("spring.application.admin.enabled:false")
                .run(args);
        
        // 锚点打印
        System.out.println(">>> 所有模块启动指令已发出，请等待控制台 Banner 刷新 <<<");
        System.out.println("################################################");
        System.out.println(">>> 所有服务已成功启动 <<<");
        printSuccessBanner();
    }
    
    private static void printSuccessBanner() {
        System.out.println("""
                
                -------------------------------------------------------
                \t(♥◠‿◠)ﾉﾞ  MOL-DORM 所有模块已成功聚合启动  ლ(´ڡ`ლ)ﾞ \s
                \t[认证中心]: http://localhost:9000/api/auth/swagger-ui/index.html
                \t[系统管理]: http://localhost:8080/api/sys/swagger-ui/index.html
                \t[宿舍业务]: http://localhost:8081/api/dorm/swagger-ui/index.html
                -------------------------------------------------------
                """);
    }
}