package com.mol.launcher;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.core.env.Environment;

import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * MOL-DORM 聚合启动类 (模块化单体版)
 * <p>
 * 核心修正：
 * 1. 只启动一个 Spring Context (端口 9090)
 * 2. 通过 @ComponentScan 自动扫描 Sys 和 Dorm 模块
 * 3. 通过 config.import 自动加载子模块配置
 * </p>
 */
@SpringBootApplication
// 扫描所有模块的 Bean (Sys, Dorm, Common)
@ComponentScan("com.mol")
// 扫描所有模块的 Mapper
@MapperScan("com.mol.**.mapper")
public class MolLauncherApplication {
    
    public static void main(String[] args) throws UnknownHostException {
        // 开启虚拟线程
        System.setProperty("spring.threads.virtual.enabled", "true");
        
        // 启动！
        ConfigurableApplicationContext application = SpringApplication.run(MolLauncherApplication.class, args);
        
        // 打印信息
        Environment env = application.getEnvironment();
        String ip = InetAddress.getLocalHost().getHostAddress();
        String port = env.getProperty("server.port", "9090"); // 获取实际运行端口
        String path = env.getProperty("server.servlet.context-path", "");
        
        String localUrl = "http://localhost:" + port + path;
        String externalUrl = "http://" + ip + ":" + port + path;
        
        System.out.println("""
            #############################################################
            (♥◠‿◠)ﾉﾞ  MOL-DORM 宿舍管理系统 (聚合版) 启动成功   ლ(´ڡ`ლ)ﾞ
            
            应用模式: 模块化单体 (Modular Monolith)
            运行端口: %s
            -------------------------------------------------------------
            >>> 统一接口文档 (Knife4j/Swagger):
            %s/swagger-ui/index.html
            
            >>> 核心模块分组 (自动路由前缀已生效):
            [系统管理]: %s/swagger-ui/index.html?urls.primaryName=server
            [宿舍业务]: %s/swagger-ui/index.html?urls.primaryName=dorm
            -------------------------------------------------------------
            本地访问: %s
            外部访问: %s
            #############################################################
            """.formatted(port, localUrl, localUrl, localUrl, localUrl, externalUrl));
    }
}