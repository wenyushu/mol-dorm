package com.mol.server;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.core.env.Environment;

import java.net.UnknownHostException;

@SpringBootApplication
@ComponentScan("com.mol")
@MapperScan("com.mol.**.mapper")
public class MolServerApplication {
    
    public static void main(String[] args) throws UnknownHostException {
        // 强制指定读取 application-server.yml (或者是 application-sys.yml)
        // 请确认你的 resources 下是 application-server.yml 还是 application-sys.yml
        System.setProperty("spring.config.name", "application-server");
        
        System.setProperty("spring.threads.virtual.enabled", "true");
        
        ConfigurableApplicationContext application = SpringApplication.run(MolServerApplication.class, args);
        
        Environment env = application.getEnvironment();
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