package com.mol.server;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.core.env.Environment;

import java.net.InetAddress;
import java.net.UnknownHostException;

// 系统管理模块独立启动类 (仅用于单独测试 Sys 模块)
@SpringBootApplication
@ComponentScan("com.mol")
@MapperScan("com.mol.**.mapper")
public class MolSysApplication {
    
    public static void main(String[] args) throws UnknownHostException {
        // 开启虚拟线程优化
        System.setProperty("spring.threads.virtual.enabled", "true");
        
        ConfigurableApplicationContext application = SpringApplication.run(MolSysApplication.class, args);
        
        Environment env = application.getEnvironment();
        String ip = InetAddress.getLocalHost().getHostAddress();
        String port = env.getProperty("server.port", "8080");
        String path = env.getProperty("server.servlet.context-path", "");
        String hostUrl = "http://localhost:" + port + path;
        
        System.out.println("""
                -------------------------------------------------------
                \t(♥◠‿◠)ﾉﾞ  Mol-Dorm [系统模块-独立模式] 启动成功   ლ(´ڡ`ლ)ﾞ \s
                \t访问地址:  %s
                \t文档地址:  %s/swagger-ui/index.html
                -------------------------------------------------------
                """.formatted(hostUrl, hostUrl));
    }
}