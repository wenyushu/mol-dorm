package com.mol.common.core.aspect;

import cn.dev33.satoken.stp.StpUtil;
import com.mol.common.core.constant.RoleConstants;
import com.mol.common.core.context.SecurityContext;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import jakarta.servlet.http.HttpServletResponse;

/**
 * 数据访问安全审计切面
 * 🛡️ 防刁民设计：强制记录访问足迹 + 动态判定脱敏权限
 */
@Slf4j
@Aspect
@Component
public class DataAuditAspect {
    
    /**
     * 定义拦截点：所有业务模块 service 包下获取详情的方法
     */
    @Pointcut("execution(* com.mol..service..*.get*Detail(..))")
    public void auditPointcut() {}
    
    @Before("auditPointcut()")
    public void doBefore(JoinPoint joinPoint) {
        if (!StpUtil.isLogin()) return;
        
        // 1. 获取当前登录人并存入上下文
        Long loginId = StpUtil.getLoginIdAsLong();
        SecurityContext.setViewerId(loginId);
        
        // 2. 判定是否为“管理员级别”查看者
        // 🛡️ 只有超管和宿管默认给明文权限，普通学生/室友逻辑在 Service 层通过 setCanViewFullDetail(true) 动态覆盖
        boolean isAdmin = StpUtil.hasRole(RoleConstants.SUPER_ADMIN)
                || StpUtil.hasRole(RoleConstants.DORM_MANAGER);
        SecurityContext.setCanViewFullDetail(isAdmin);
        
        // 3. 强制审计日志：记录谁在调用什么方法
        log.warn("📢 [安全审计] 操作员 ID: {} | 访问方法: {}", loginId, joinPoint.getSignature().toShortString());
        
        // 4. 在响应头埋入标记（方便前端生成水印）
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes != null) {
            HttpServletResponse response = attributes.getResponse();
            if (response != null) {
                response.addHeader("X-Viewer-Id", String.valueOf(loginId));
                response.addHeader("Access-Control-Expose-Headers", "X-Viewer-Id"); // 确保前端能读到这个 Header
            }
        }
    }
    
    @After("auditPointcut()")
    public void doAfter() {
        // 请求完成，销毁上下文
        SecurityContext.clear();
    }
}