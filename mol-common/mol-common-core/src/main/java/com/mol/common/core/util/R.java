package com.mol.common.core.util;

import com.mol.common.core.constant.CommonConstants;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import lombok.experimental.Accessors;

import java.io.Serial;
import java.io.Serializable;

/**
 * 响应信息主体包装类
 * 用于前后端交互的最外层数据结构
 *
 * @param <T> 数据体的泛型
 * @author mol
 */
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true) // 开启链式调用：new R().setCode(0).setMsg("ok")
@Schema(description = "响应信息主体")
public class R<T> implements Serializable {
    
    @Serial
    private static final long serialVersionUID = 1L;
    
    @Getter
    @Setter
    @Schema(description = "业务响应码（0-成功，1-失败）")
    private int code;
    
    @Getter
    @Setter
    @Schema(description = "响应消息描述")
    private String msg;
    
    @Getter
    @Setter
    @Schema(description = "业务数据体")
    private T data;
    
    /**
     * 静态快捷方法：成功返回 (不带数据)
     */
    public static <T> R<T> ok() {
        return restResult(null, CommonConstants.SUCCESS, "操作成功");
    }
    
    /**
     * 静态快捷方法：成功返回 (带数据)
     */
    public static <T> R<T> ok(T data) {
        return restResult(data, CommonConstants.SUCCESS, "操作成功");
    }
    
    /**
     * 静态快捷方法：失败返回 (不带数据)
     */
    public static <T> R<T> failed() {
        return restResult(null, CommonConstants.FAIL, "操作失败");
    }
    
    /**
     * 静态快捷方法：失败返回 (自定义错误消息)
     */
    public static <T> R<T> failed(String msg) {
        return restResult(null, CommonConstants.FAIL, msg);
    }
    
    /**
     * 构造返回结果的核心方法
     * * @param data 业务数据
     * @param code 响应码
     * @param msg  提示消息
     */
    private static <T> R<T> restResult(T data, int code, String msg) {
        R<T> apiResult = new R<>();
        apiResult.setCode(code);
        apiResult.setData(data);
        apiResult.setMsg(msg);
        return apiResult;
    }
}