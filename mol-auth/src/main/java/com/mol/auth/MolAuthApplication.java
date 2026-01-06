package com.mol.auth;

import lombok.extern.slf4j.Slf4j;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.core.env.Environment;

import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * 认证中心启动类
 * * 【设计职责】
 * 1. 负责用户登录、注销、令牌校验（基于 Sa-Token）。
 * 2. 作为系统的安全网关，分发 Token 给前端及其他业务模块。
 * 3. 运行端口通常为 9090。
 */
@Slf4j
@SpringBootApplication
/*
  重点 1：多模块 Mapper 扫描
  扫描 com.mol 包下所有子模块中以 .mapper 结尾的包。
  这样 mol-auth 才能调用 mol-common-mybatis 里的数据库接口。
 */
@MapperScan("com.mol.**.mapper")
/*
  重点 2：全模块组件扫描
  默认只扫当前包（com.mol.auth），这里扩大到 com.mol，
  确保能够识别到 mol-common-security 模块里的 StpInterfaceImpl 等权限验证组件。
 */
@ComponentScan(basePackages = "com.mol")
public class MolAuthApplication {
    
    /**
     * 程序主入口
     * @param args 命令行参数
     * @throws UnknownHostException 当无法获取本机 IP 时抛出
     */
    public static void main(String[] args) throws UnknownHostException {
        
        // 1. 启动 Spring 应用，并获取“应用上下文（ApplicationContext）”
        // 这里必须使用 ConfigurableApplicationContext 接收返回值，后面才能从里面提取配置信息
        ConfigurableApplicationContext application = SpringApplication.run(MolAuthApplication.class, args);
        
        // 2. 获取 Spring 环境配置对象（Environment）
        // 它可以读取我们在 application.yml 里配置的所有信息
        Environment env = application.getEnvironment();
        
        // 3. 获取关键网络参数
        // 获取本机 IP（用于生成可以在局域网内访问的链接）
        String ip = InetAddress.getLocalHost().getHostAddress();
        // 获取 server.port 配置，如果没有配置则默认使用 8080（Sa-Token 模块通常设为 9090）
        String port = env.getProperty("server.port", "8080");
        // 获取 server.servlet.context-path 配置（即接口的前缀，如 /api/auth）
        String path = env.getProperty("server.servlet.context-path", "");
        
        // 4. 构建启动成功的控制台欢迎信息
        // 使用 Java 17 的 Text Blocks (三个双引号) 语法，支持多行排版
        String startupBanner = """
                -------------------------------------------------------
                \t(♥◠‿◠)ﾉﾞ  Mol-Dorm 认证中心启动成功  ლ(´ڡ`ლ)ﾞ \s
                \t本地访问: http://localhost:{}{}
                \t外部访问: http://{}:{}{}
                \t接口文档: http://localhost:{}{}/swagger-ui/index.html
                \t数据源URL: http://localhost:{}{}/v3/api-docs
                -------------------------------------------------------
                """;
        
        // 5. 按照顺序替换占位符并打印
        // 这里的逻辑是将字符串中所有的 {} 依次替换为真实的端口、IP 和路径
        System.out.println(startupBanner
                .replaceFirst("\\{}", port).replaceFirst("\\{}", path)
                .replaceFirst("\\{}", ip).replaceFirst("\\{}", port).replaceFirst("\\{}", path)
                .replaceFirst("\\{}", port).replaceFirst("\\{}", path)
                .replaceFirst("\\{}", port).replaceFirst("\\{}", path)
        );
    }
}