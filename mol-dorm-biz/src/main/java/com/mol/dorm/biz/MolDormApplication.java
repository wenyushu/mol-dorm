package com.mol.dorm.biz;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.core.env.Environment;

import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * 宿舍业务模块启动类 (Dorm-Biz)
 * * 职责：
 * 1. 负责楼栋、房间、床位、水电、报修等核心业务。
 * 2. 运行端口：8081。
 * 3. 访问前缀：/api/dorm。
 */
@SpringBootApplication
/*
 * 重点 1：精确 Mapper 扫描
 * 必须只扫描 com.mol.dorm.biz.mapper，防止在聚合启动时
 * 扫到其他模块的 Mapper 导致 SQL 映射冲突或 Bean 重复定义。
 */
@MapperScan("com.mol.dorm.biz.mapper")
/*
 * 重点 2：精确组件扫描 (隔离关键)
 * 1. "com.mol.dorm.biz": 扫描本模块的 Controller, Service, Component。
 * 2. "com.mol.common": 扫描公共模块的配置（如 OpenApiConfig, SaTokenConfigure, GlobalExceptionHandler）。
 * 这样既能保证业务独立，又能共用基础安全和文档配置。
 */
@ComponentScan(basePackages = {"com.mol.dorm.biz", "com.mol.common"})
public class MolDormApplication {
    
    public static void main(String[] args)throws UnknownHostException {
        // 开启 Spring Boot 3.5+ 虚拟线程，优化宿舍分配等耗时业务的并发处理能力
        System.setProperty("spring.threads.virtual.enabled", "true");
        
        // 1. 启动并获取上下文
        ConfigurableApplicationContext application = SpringApplication.run(MolDormApplication.class, args);
        
        // 2. 动态提取配置
        Environment env = application.getEnvironment();
        String ip = InetAddress.getLocalHost().getHostAddress();
        String port = env.getProperty("server.port", "8081");
        String path = env.getProperty("server.servlet.context-path", "");
        
        String hostUrl = "http://localhost:" + port + path;
        String externalUrl = "http://" + ip + ":" + port + path;
        
        // 3. 打印动态横幅
        // 注意：Dorm 模块使用了分组扫描，JSON 路径建议加上 /dorm 后缀
        System.out.println("""
                -------------------------------------------------------
                \t(♥◠‿◠)ﾉﾞ  Mol-Dorm 宿舍业务模块启动成功   ლ(´ڡ`ლ)ﾞ \s
                \t本地访问地址:  %s
                \t外部访问地址:  %s
                \t接口文档地址:  %s/swagger-ui/index.html
                \t数据源 JSON:   %s/v3/api-docs/dorm
                -------------------------------------------------------
                """.formatted(hostUrl, externalUrl, hostUrl, hostUrl));
    }
}