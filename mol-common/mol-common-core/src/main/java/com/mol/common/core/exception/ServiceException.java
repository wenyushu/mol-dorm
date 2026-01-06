package com.mol.common.core.exception;

import lombok.NoArgsConstructor;

import java.io.Serial;

/**
 * 业务逻辑异常类
 * * <p>用途：</p>
 * 1. 用于在 Service 业务层中手动抛出已知的业务错误（如：余额不足、账号已存在）。
 * 2. 区别于不可控的系统异常（如：NullPointerException、SQL 语法错误）。
 * 3. 配合 GlobalExceptionHandler 实现友好的前端错误提示。
 * * @author mol
 */
@NoArgsConstructor
public class ServiceException extends RuntimeException {
    
    @Serial
    private static final long serialVersionUID = 1L;
    
    /**
     * 构造一个带有指定详细消息的业务异常
     * * @param message 异常提示消息（通常直接展示给前端用户看）
     */
    public ServiceException(String message) {
        super(message);
    }
    
    /**
     * 构造一个带有指定详细消息和原始异常原因的业务异常
     * * @param message 异常提示消息
     * @param cause   原始异常对象（用于保留异常堆栈）
     */
    public ServiceException(String message, Throwable cause) {
        super(message, cause);
    }
    
    /**
     * 构造一个带有原始异常原因的业务异常
     * * @param cause 原始异常对象
     */
    public ServiceException(Throwable cause) {
        super(cause);
    }
}