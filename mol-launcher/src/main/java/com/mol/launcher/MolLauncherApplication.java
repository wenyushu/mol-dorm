package com.mol.launcher;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.core.env.Environment;
import java.net.UnknownHostException;

/**
 * MOL-DORM 聚合启动类 (模块化单体版)
 */
@SpringBootApplication
// 核心修复 1：必须开启全包扫描，否则找不到 Server 和 Dorm 的 Controller
// basePackages = "com.mol" 表示扫描 com.mol 下的所有包
@ComponentScan(
        basePackages = "com.mol",
        // 核心修复 2：排除子模块的独立启动类
        // 因为它们也有 @SpringBootApplication，不排除会报错或重复启动
        excludeFilters = @ComponentScan.Filter(
                type = FilterType.REGEX,
                pattern = "com\\.mol\\.(server|dorm\\.biz)\\.Mol.*Application"
        )
)
@MapperScan("com.mol.**.mapper")
public class MolLauncherApplication {
    
    public static void main(String[] args) throws UnknownHostException {
        // 1. 【强制】设置统一端口和路径 (最高优先级，覆盖 yml 配置)
        System.setProperty("server.port", "9090");
        System.setProperty("server.servlet.context-path", "/api");
        
        // 2. 开启虚拟线程 (Java 21+)
        System.setProperty("spring.threads.virtual.enabled", "true");
        
        // 3. 启动聚合上下文
        ConfigurableApplicationContext application = SpringApplication.run(MolLauncherApplication.class, args);
        
        // 4. 获取环境配置，打印信息
        Environment env = application.getEnvironment();
        String port = env.getProperty("server.port");
        String path = env.getProperty("server.servlet.context-path");
        if (path == null) path = ""; // 防止未配置时为 null
        
        String baseUrl = "http://localhost:" + port + path;
        
        // 5. 打印漂亮的启动日志
        System.out.printf("""
                #############################################################
                (♥◠‿◠)ﾉﾞ  MOL-DORM 宿舍管理系统 (聚合版) 启动成功   ლ(´ڡ`ლ)ﾞ
                
                后端服务地址:  %s
                -------------------------------------------------------------
                >>> Knife4j (推荐 - 增强版文档):
                %s/doc.html
                
                >>> Swagger UI (原生文档):
                %s/swagger-ui/index.html
                
                >>> OpenAPI JSON (Apifox 自动同步地址):
                %s/v3/api-docs
                -------------------------------------------------------------
                #############################################################
                %n""", baseUrl, baseUrl, baseUrl, baseUrl);
    }
}