package com.mol.launcher.config;

import com.mol.server.controller.AuthController; // 记得导入 AuthController 类
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.PathMatchConfigurer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * 模块路由自动前缀配置
 * 核心目标：解决模块间路径冲突，同时保留部分核心接口的短路径
 */
@Configuration
public class ModulePathConfig implements WebMvcConfigurer {
    
    @Override
    public void configurePathMatch(PathMatchConfigurer configurer) {
        
        // =========================================================
        // 1. 系统模块 (com.mol.server) -> 统一添加 /sys 前缀
        // =========================================================
        // 效果：
        // SysUserController -> /sys/user/list
        // SysRoleController -> /sys/role/list
        // AuthController    -> /auth/login (被排除了，保持原样！)
        // =========================================================
        configurer.addPathPrefix("/sys", clazz -> {
            // 规则：是 server 包下的 Controller
            if (clazz.getPackageName().startsWith("com.mol.server")) {
                // 特例：如果是 AuthController，不加前缀！
                // (getSimpleName 比对类名，或者用 equals 判断 class)
                return !clazz.getSimpleName().equals("AuthController");
            }
            return false;
        });
        
        // =========================================================
        // 2. 宿舍业务模块 (com.mol.dorm.biz) -> 统一添加 /dorm 前缀
        // =========================================================
        // 效果：
        // RoomController -> /dorm/room/list
        // BuildController -> /dorm/build/list
        // =========================================================
        configurer.addPathPrefix("/dorm", clazz ->
                clazz.getPackageName().startsWith("com.mol.dorm.biz")
        );
    }
}