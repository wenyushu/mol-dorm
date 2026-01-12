package com.mol.dorm.biz;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.core.env.Environment;

import java.net.InetAddress;
import java.net.UnknownHostException;

@SpringBootApplication
@ComponentScan("com.mol")
@MapperScan("com.mol.**.mapper")
public class MolDormApplication {
    
    public static void main(String[] args) throws UnknownHostException {
        // 【核心修复】强制指定读取 application-dorm.yml
        System.setProperty("spring.config.name", "application-dorm");
        
        System.setProperty("spring.threads.virtual.enabled", "true");
        
        ConfigurableApplicationContext application = SpringApplication.run(MolDormApplication.class, args);
        
        Environment env = application.getEnvironment();
        String port = env.getProperty("server.port", "8081");
        String path = env.getProperty("server.servlet.context-path", "");
        String hostUrl = "http://localhost:" + port + path;
        
        System.out.println("""
                -------------------------------------------------------
                \t(♥◠‿◠)ﾉﾞ  Mol-Dorm [宿舍业务模块-独立模式] 启动成功   ლ(´ڡ`ლ)ﾞ \s
                \t访问地址:  %s
                \t文档地址:  %s/swagger-ui/index.html
                -------------------------------------------------------
                """.formatted(hostUrl, hostUrl));
    }
}