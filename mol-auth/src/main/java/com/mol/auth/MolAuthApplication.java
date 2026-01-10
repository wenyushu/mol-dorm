package com.mol.auth;

import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.core.env.Environment;

import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * 认证中心启动类 (Mol-Auth)
 * * 职责：
 * 1. 处理用户登录校验、Token 签发。
 * 2. 拦截并校验令牌合法性（配合 Sa-Token）。
 */
@Slf4j
@SpringBootApplication
// 扫描所有模块的 Mapper (包括 sys 模块的 Mapper)
@MapperScan("com.mol.**.mapper")
// 扫描所有模块的 Bean (包括 sys 模块的 Service)
@ComponentScan(basePackages = {"com.mol"})
public class MolAuthApplication {
    
    public static void main(String[] args) throws UnknownHostException {
        // 1. 开启 Spring Boot 虚拟线程优化 (Java 21+)
        System.setProperty("spring.threads.virtual.enabled", "true");
        
        // 2. 启动应用
        ConfigurableApplicationContext application = SpringApplication.run(MolAuthApplication.class, args);
        
        // 3. 提取环境配置（从 application-auth.yml 中读取）
        Environment env = application.getEnvironment();
        String ip = InetAddress.getLocalHost().getHostAddress();
        
        // 获取端口：优先读取 yml 中的 server.port，若无则使用 9000 兜底
        String port = env.getProperty("server.port", "9000");
        
        // 获取上下文路径：读取 yml 中的 server.servlet.context-path (如 /api/auth)
        String path = env.getProperty("server.servlet.context-path", "");
        
        // 4. 预先构建基础访问 URL，减少后续逻辑的复杂度
        String startupBanner = getString(port, path, ip);
        
        System.out.println(startupBanner);
    }
    
    private static @NonNull String getString(String port, String path, String ip) {
        String hostUrl = "http://localhost:" + port + path;
        String externalUrl = "http://" + ip + ":" + port + path;
        
        // 5. 使用 Java 17 文本块打印整洁的启动报告
        // 注意：弃用了 Knife4j 的 doc.html，改用原生 Swagger UI 地址
        return """
                -------------------------------------------------------
                \t(♥◠‿◠)ﾉﾞ  Mol-Dorm 认证中心启动成功  ლ(´ڡ`ლ)ﾞ \s
                \t本地访问地址:  %s
                \t外部访问地址:  %s
                \t接口文档地址:  %s/swagger-ui/index.html
                \t数据源JSON:   %s/v3/api-docs
                -------------------------------------------------------
                """.formatted(hostUrl, externalUrl, hostUrl, hostUrl);
    }
}