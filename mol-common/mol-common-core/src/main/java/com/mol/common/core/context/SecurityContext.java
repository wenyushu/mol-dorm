package com.mol.common.core.context;


import com.alibaba.ttl.TransmittableThreadLocal;

/**
 * 安全审计上下文
 * 🛡️ 防刁民设计：在当前线程内“记住”谁在看，以及有没有权限看明文。
 */
public class SecurityContext {
    
    // 使用 TransmittableThreadLocal 确保在异步调用或线程池中也能正确传递安全参数
    private static final ThreadLocal<Boolean> CAN_VIEW_FULL = new TransmittableThreadLocal<>();
    private static final ThreadLocal<Long> VIEWER_ID = new TransmittableThreadLocal<>();
    
    /**
     * 设置是否有权查看完整敏感数据
     */
    public static void setCanViewFullDetail(boolean can) {
        CAN_VIEW_FULL.set(can);
    }
    
    /**
     * 获取权限标记
     */
    public static boolean canViewFullDetail() {
        return CAN_VIEW_FULL.get() != null && CAN_VIEW_FULL.get();
    }
    
    /**
     * 设置当前操作者（查看者）ID
     */
    public static void setViewerId(Long id) {
        VIEWER_ID.set(id);
    }
    
    /**
     * 获取当前操作者 ID
     */
    public static Long getViewerId() {
        return VIEWER_ID.get();
    }
    
    /**
     * 请求结束必须清理，防止内存泄露
     */
    public static void clear() {
        CAN_VIEW_FULL.remove();
        VIEWER_ID.remove();
    }
}