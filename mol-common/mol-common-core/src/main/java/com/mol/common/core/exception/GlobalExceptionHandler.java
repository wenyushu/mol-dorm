package com.mol.common.core.exception;

import cn.dev33.satoken.exception.NotLoginException;
import cn.dev33.satoken.exception.NotPermissionException;
import cn.dev33.satoken.exception.NotRoleException;
import cn.dev33.satoken.exception.NotSafeException;
import com.mol.common.core.util.R;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.stream.Collectors;

/**
 * 全局异常处理器 (最终完善版)
 * <p>
 * 策略调整：HTTP 状态码统一返回 200，由 JSON 中的 code 字段决定业务结果。
 * 包含：400(参数错), 401(未登录), 403(无权限), 500(系统崩)
 * </p>
 *
 * @author mol
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {
    
    /**
     * 拦截业务逻辑异常
     */
    @ExceptionHandler(ServiceException.class)
    public R<Void> handleServiceException(ServiceException e) {
        log.warn("业务拦截: {}", e.getMessage());
        return R.fail(e.getMessage());
    }
    
    /**
     * 拦截参数校验异常 (@RequestBody JSON参数)
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public R<Void> handleJsonValidException(MethodArgumentNotValidException e) {
        String message = e.getBindingResult().getFieldErrors().stream()
                .map(DefaultMessageSourceResolvable::getDefaultMessage)
                .collect(Collectors.joining(", "));
        log.warn("JSON参数校验失败: {}", message);
        return R.fail(message);
    }
    
    /**
     * 拦截参数校验异常 (普通表单参数)
     */
    @ExceptionHandler(BindException.class)
    public R<Void> handleBindException(BindException e) {
        String message = e.getAllErrors().stream()
                .map(DefaultMessageSourceResolvable::getDefaultMessage)
                .collect(Collectors.joining(", "));
        log.warn("表单参数校验失败: {}", message);
        return R.fail(message);
    }
    
    /**
     * 拦截未登录异常 (401)
     */
    @ExceptionHandler(NotLoginException.class)
    public R<Void> handleNotLoginException(NotLoginException e) {
        log.warn("认证失败: {}", e.getMessage());
        return R.fail(401, "登录已过期，请重新登录");
    }
    
    /**
     * 拦截二级认证异常
     * 返回特定状态码 (例如 402 或 自定义业务码)，告知前端需要弹窗输密码
     */
    @ExceptionHandler(NotSafeException.class)
    public R<Void> handleNotSafeException(NotSafeException e) {
        log.warn("二级认证拦截: {}", e.getMessage());
        // 建议约定一个 code，例如 901 代表 "需要二级认证"
        return R.fail(901, "此操作需要二级认证，请验证密码");
    }
    
    /**
     * 拦截无权限/无角色异常 (403)
     * 防止学生访问管理员接口时报 500
     */
    @ExceptionHandler({NotPermissionException.class, NotRoleException.class})
    public R<Void> handleNotPermissionException(Exception e) {
        log.warn("越权访问拦截: {}", e.getMessage());
        return R.fail(403, "您没有操作该功能的权限");
    }
    
    /**
     * 拦截系统兜底异常 (500)
     */
    @ExceptionHandler(Exception.class)
    public R<Void> handleGlobalException(Exception e) {
        log.error("系统严重错误 ex={}", e.getMessage(), e);
        return R.fail(500, "系统繁忙，请稍后重试");
    }
}