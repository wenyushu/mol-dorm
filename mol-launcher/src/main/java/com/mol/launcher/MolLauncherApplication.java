package com.mol.launcher;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.core.env.Environment;

import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * MOL-DORM 聚合启动类 (模块化单体版)
 */
@SpringBootApplication
@ComponentScan(
        basePackages = "com.mol",
        // 💡 最佳实践：排除子模块的启动类，防止它们重复加载造成干扰
        excludeFilters = @ComponentScan.Filter(
                type = FilterType.REGEX,
                pattern = "com\\.mol\\..*\\.biz\\.Mol.*Application"
        )
)
@MapperScan("com.mol.**.mapper")
public class MolLauncherApplication {
    
    public static void main(String[] args) throws UnknownHostException {
        // 1. 【核武器】强制覆盖端口和 ContextPath
        // 无论 yaml 里写什么，这里说了算！
        System.setProperty("server.port", "9090");
        System.setProperty("server.servlet.context-path", "/api");
        
        // 2. 开启虚拟线程
        System.setProperty("spring.threads.virtual.enabled", "true");
        
        // 3. 启动
        ConfigurableApplicationContext application = SpringApplication.run(MolLauncherApplication.class, args);
        
        // 4. 打印信息
        Environment env = application.getEnvironment();
        String ip = InetAddress.getLocalHost().getHostAddress();
        String port = env.getProperty("server.port");
        String path = env.getProperty("server.servlet.context-path");
        
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
            
            >>> 核心模块分组:
            [系统管理]: %s/swagger-ui/index.html?urls.primaryName=sys
            [宿舍业务]: %s/swagger-ui/index.html?urls.primaryName=dorm
            -------------------------------------------------------------
            本地访问: %s
            外部访问: %s
            #############################################################
            """.formatted(port, localUrl, localUrl, localUrl, localUrl, externalUrl));
    }
}