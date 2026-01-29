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
@SpringBootApplication
// 显式列出所有子模块的包路径
@ComponentScan(
        basePackages = {
                "com.mol.launcher",
                "com.mol.common",
                "com.mol.server",        // 确保扫到 AuthController
                "com.mol.dorm.biz"       // 确保扫到 宿舍业务
        },
        // 排除子模块的启动类，防止重复启动
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
    
//    // 调试代码，锚点测试
//    @org.springframework.context.annotation.Bean
//    public org.springframework.boot.CommandLineRunner commandLineRunner(org.springframework.context.ApplicationContext ctx) {
//        return args -> {
//            System.out.println("================= Bean 检查开始 =================");
//            String[] beanNames = ctx.getBeanDefinitionNames();
//            boolean hasAuth = false;
//            for (String beanName : beanNames) {
//                if (beanName.equalsIgnoreCase("authController")) {
//                    System.out.println("✅ 找到了 AuthController !!!");
//                    hasAuth = true;
//                }
//            }
//            if (!hasAuth) {
//                System.err.println("❌❌❌ 完蛋了！容器里根本没有 AuthController！请检查 pom.xml 依赖和包扫描！❌❌❌");
//            }
//            System.out.println("================= Bean 检查结束 =================");
//        };
//    }
//
//    // ⬇️⬇️⬇️ 【新增】打印所有 URL 接口映射 ⬇️⬇️⬇️
//    @org.springframework.context.annotation.Bean
//    public org.springframework.boot.CommandLineRunner printMappings(org.springframework.context.ApplicationContext ctx) {
//        return args -> {
//            System.out.println("================= 接口映射表 (HandlerMapping) =================");
//            try {
//                // 获取 Spring MVC 的核心映射组件
//                RequestMappingHandlerMapping mapping = ctx.getBean("requestMappingHandlerMapping", RequestMappingHandlerMapping.class);
//                Map<RequestMappingInfo, HandlerMethod> map = mapping.getHandlerMethods();
//
//                if (map.isEmpty()) {
//                    System.err.println("⚠️ 警告：没有任何接口被注册！");
//                } else {
//                    // 遍历并打印
//                    map.forEach((info, method) -> {
//                        String controllerName = method.getBeanType().getSimpleName();
//                        // 只打印 auth 相关的，避免日志太多
//                        if (controllerName.contains("Auth")) {
//                            System.out.println("🔍 发现接口: " + info + "  --->  " + controllerName);
//                        }
//                    });
//                }
//            } catch (Exception e) {
//                System.err.println("❌ 获取映射表失败: " + e.getMessage());
//            }
//            System.out.println("================= 检查结束 =================");
//        };
//    }
    
}