package com.mol.sys.biz;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 系统管理模块启动类 (Sys-Biz)
 * * <p>注意：在多模块架构中，必须显式配置组件扫描路径，
 * 否则 Spring 无法加载到其他模块（如 mol-common-mybatis）中的配置类。</p>
 *
 * @author mol
 */
@SpringBootApplication
// 关键：扫描 mol 根包下的所有组件
// 这样才能扫到 common 模块里的 GlobalExceptionHandler, MybatisPlusConfiguration 等
@MapperScan("com.mol.**.mapper") // 使用通配符，确保扫描到所有模块下的 mapper 包
public class MolSysApplication {
    
    public static void main(String[] args) {
        // 设置虚拟线程优化（虽然 yml 配了，代码里也可以显式确认一下，针对 SpringBoot 3.5.x）
        System.setProperty("spring.threads.virtual.enabled", "true");
        
        SpringApplication.run(MolSysApplication.class, args);
        
        System.out.println("-------------------------------------------------------");
        System.out.println(" (♥◠‿◠)ﾉﾞ  Mol-Dorm 系统管理模块启动成功   ლ(´ڡ`ლ)ﾞ  ");
        System.out.println(" 接口文档: http://localhost:8080/api/sys/doc.html       ");
        System.out.println("-------------------------------------------------------");
    }
}