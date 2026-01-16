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
 * <p>
 * 核心逻辑：
 * 1. 只启动一个 Spring Context (端口 9090)。
 * 2. 自动扫描 com.mol 下所有的 Service/Controller/Mapper。
 * 3. 结果：Sys 和 Dorm 的 Bean 都在同一个容器里，可以互相 @Autowired。
 * </p>
 */
@SpringBootApplication(scanBasePackages = "com.mol")
@ComponentScan(
        basePackages = "com.mol",
        // ⚠️ 关键：排除掉子模块的独立启动类，防止它们干扰聚合启动
        excludeFilters = @ComponentScan.Filter(
                type = FilterType.REGEX,
                pattern = "com\\.mol\\..*\\.biz\\.Mol.*Application"
        )
)
@MapperScan("com.mol.**.mapper")
public class MolLauncherApplication {
    
    public static void main(String[] args) throws UnknownHostException {
        // 1. 【强制】设置统一端口和路径 (覆盖子模块配置)
        System.setProperty("server.port", "9090");
        System.setProperty("server.servlet.context-path", "/api");
        
        // 2. 开启虚拟线程
        System.setProperty("spring.threads.virtual.enabled", "true");
        
        // 3. 启动聚合上下文 (只运行这一次！)
        ConfigurableApplicationContext application = SpringApplication.run(MolLauncherApplication.class, args);
        
        // 4. 打印信息
        Environment env = application.getEnvironment();
        String ip = InetAddress.getLocalHost().getHostAddress();
        String port = env.getProperty("server.port");
        String path = env.getProperty("server.servlet.context-path");
        String localUrl = "http://localhost:" + port + path;
        
        System.out.printf("""
                #############################################################
                (♥◠‿◠)ﾉﾞ  MOL-DORM 宿舍管理系统 (聚合版) 启动成功   ლ(´ڡ`ლ)ﾞ
                
                架构模式: 模块化单体 (所有模块在同一进程内运行，Service可直接注入)
                运行端口: %s
                -------------------------------------------------------------
                >>> 统一接口文档 (Knife4j/Swagger):
                %s/swagger-ui/index.html
                
                >>> 模块分组 (自动路由前缀已生效):
                [系统管理]: %s/swagger-ui/index.html?urls.primaryName=sys
                [宿舍业务]: %s/swagger-ui/index.html?urls.primaryName=dorm
                -------------------------------------------------------------
                #############################################################
                %n""", port, localUrl, localUrl, localUrl);
    }
}