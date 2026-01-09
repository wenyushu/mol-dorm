package com.mol.common.core.exception;

import cn.dev33.satoken.exception.NotLoginException;
import com.mol.common.core.util.R;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 全局异常处理器
 * 通过 AOP 切面拦截 Controller 抛出的异常，并转换为标准的 R 格式返回给前端
 *
 * @author mol
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {
    
    /**
     * 处理业务逻辑异常 (ServiceException)
     * 当 Service 层手动抛出业务错误时进入此方法
     */
    @ExceptionHandler(ServiceException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST) // 设定 HTTP 状态码为 400
    public R<String> handleServiceException(ServiceException e) {
        log.error("业务逻辑异常：{}", e.getMessage());
        return R.failed(e.getMessage());
    }
    
    /**
     * 处理参数校验异常
     * 拦截 @Valid 或 @Validated 校验失败时抛出的异常
     */
    @ExceptionHandler({MethodArgumentNotValidException.class, BindException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public R<String> handleBodyValidException(MethodArgumentNotValidException e) {
        // 获取所有校验失败的字段信息
        List<FieldError> fieldErrors = e.getBindingResult().getFieldErrors();
        // 将多个错误提示合并为逗号分隔的字符串
        String message = fieldErrors.stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining(", "));
        log.warn("客户端请求参数校验失败: {}", message);
        return R.failed(message);
    }
    
    /**
     * 处理系统未捕获的异常 (兜底)
     * 拦截代码中未预料到的 RuntimeException 或 Exception
     */
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR) // 设定 HTTP 状态码为 500
    public R<String> handleGlobalException(Exception e) {
        // 打印完整的堆栈信息，方便后台排查问题
        log.error("系统运行出现未捕获异常 ex={}", e.getMessage(), e);
        return R.failed("系统内部错误，请联系管理员");
    }
    
    
    /**
     * 拦截未登录异常
     * 返回 401 状态码，告知前端跳转登录页
     */
    @ExceptionHandler(NotLoginException.class)
    public R<Void> handleNotLoginException(NotLoginException e) {
        // 打印简短日志即可，无需打印堆栈
        log.warn("用户未登录或 Token 失效: {}", e.getMessage());
        return R.fail(401, "Token 已失效，请重新登录");
    }
}