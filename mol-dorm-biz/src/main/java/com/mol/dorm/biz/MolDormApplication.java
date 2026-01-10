package com.mol.dorm.biz;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.core.env.Environment;

import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * 宿舍业务模块启动类 (Dorm-Biz)
 * * 职责：
 * 1. 负责楼栋、房间、床位、水电、报修等核心业务。
 * 2. 运行端口：8081。
 * 3. 访问前缀：/api/dorm。
 */
@SpringBootApplication
@ComponentScan("com.mol")
// 核心修复 2：扫描所有模块的 Mapper (关键！这能扫到 com.mol.sys... 下的 Mapper)
@MapperScan("com.mol.**.mapper")
public class MolDormApplication {
    
    public static void main(String[] args)throws UnknownHostException {
        // 开启 Spring Boot 3.5+ 虚拟线程，优化宿舍分配等耗时业务的并发处理能力
        System.setProperty("spring.threads.virtual.enabled", "true");
        
        // 1. 启动并获取上下文
        ConfigurableApplicationContext application = SpringApplication.run(MolDormApplication.class, args);
        
        // 2. 动态提取配置
        Environment env = application.getEnvironment();
        String ip = InetAddress.getLocalHost().getHostAddress();
        String port = env.getProperty("server.port", "8081");
        String path = env.getProperty("server.servlet.context-path", "");
        
        String hostUrl = "http://localhost:" + port + path;
        String externalUrl = "http://" + ip + ":" + port + path;
        
        // 3. 打印动态横幅
        // 注意：Dorm 模块使用了分组扫描，JSON 路径建议加上 /dorm 后缀
        System.out.println("""
                -------------------------------------------------------
                \t(♥◠‿◠)ﾉﾞ  Mol-Dorm 宿舍业务模块启动成功   ლ(´ڡ`ლ)ﾞ \s
                \t本地访问地址:  %s
                \t外部访问地址:  %s
                \t接口文档地址:  %s/swagger-ui/index.html
                \t数据源 JSON:   %s/v3/api-docs/dorm
                -------------------------------------------------------
                """.formatted(hostUrl, externalUrl, hostUrl, hostUrl));
    }
}