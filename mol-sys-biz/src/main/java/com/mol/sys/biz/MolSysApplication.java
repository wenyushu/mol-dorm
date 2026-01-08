package com.mol.sys.biz;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.core.env.Environment;

import java.net.InetAddress;
import java.net.UnknownHostException;

// 系统管理模块启动类 (Sys-Biz)
@SpringBootApplication
@MapperScan("com.mol.sys.biz.mapper") // 精确到本模块的 mapper
@ComponentScan(basePackages = {"com.mol.sys.biz", "com.mol.common"})

public class MolSysApplication {
    
    public static void main(String[] args) throws UnknownHostException {
        
        // 虚拟线程
        System.setProperty("spring.threads.virtual.enabled", "true");

        // 1. 启动并获取上下文
        ConfigurableApplicationContext application = SpringApplication.run(MolSysApplication.class, args);
        
        // 2. 动态提取配置
        Environment env = application.getEnvironment();
        String ip = InetAddress.getLocalHost().getHostAddress();
        String port = env.getProperty("server.port", "8080");
        String path = env.getProperty("server.servlet.context-path", "");
        
        String hostUrl = "http://localhost:" + port + path;
        String externalUrl = "http://" + ip + ":" + port + path;

        // 3. 打印动态横幅
        System.out.println("""
                -------------------------------------------------------
                \t(♥◠‿◠)ﾉﾞ  Mol-Dorm 系统管理模块启动成功   ლ(´ڡ`ლ)ﾞ \s
                \t本地访问地址:  %s
                \t外部访问地址:  %s
                \t接口文档地址:  %s/swagger-ui/index.html
                \t数据源 JSON:   %s/v3/api-docs
                -------------------------------------------------------
                """.formatted(hostUrl, externalUrl, hostUrl, hostUrl));
    }
}