package com.mol.common.mybatis.handler;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import com.mol.common.core.util.LoginHelper;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * MyBatis-Plus 字段全量审计填充
 * 🛡️ 防刁民设计：确保每一条入库数据都有迹可循
 */
@Slf4j
@Component
public class MyMetaObjectHandler implements MetaObjectHandler {
    
    @Override
    public void insertFill(MetaObject metaObject) {
        
        log.debug("开始插入填充审计字段...");
        LocalDateTime now = LocalDateTime.now();
        String currentUserId = getUserIdStr();
        
        // 1. 自动填充时间戳 (直接调用 LocalDateTime.now())
        this.strictInsertFill(metaObject, "createTime", LocalDateTime.class, LocalDateTime.now());
        this.strictInsertFill(metaObject, "updateTime", LocalDateTime.class, LocalDateTime.now());

        // 2. 操作人填充 (如果没登录，默认填充 "system")
        String filler = currentUserId != null ? currentUserId : "system";
        this.strictInsertFill(metaObject, "createBy", String.class, filler);
        this.strictInsertFill(metaObject, "updateBy", String.class, filler);
        
        // 3. 基础逻辑字段填充
        this.strictInsertFill(metaObject, "delFlag", String.class, "0"); // 逻辑删除标识
        this.strictInsertFill(metaObject, "version", Integer.class, 0);   // 乐观锁版本号
    }
    
    @Override
    public void updateFill(MetaObject metaObject) {
        
        log.debug("开始更新填充审计字段...");
        
        // 更新时仅填充 updateTime 和 updateBy
        this.strictUpdateFill(metaObject, "updateTime", LocalDateTime.class, LocalDateTime.now());
        
        String currentUserId = getUserIdStr();
        if (currentUserId != null) {
            this.strictUpdateFill(metaObject, "updateBy", String.class, currentUserId);
        }
    }
    
    /**
     * 辅助方法：获取当前用户 ID 字符串
     */
    private String getUserIdStr() {
        try {
            Long userId = LoginHelper.getUserId();
            return userId != null ? String.valueOf(userId) : null;
        } catch (Exception e) {
            // 防止在非 Web 环境（如定时任务、异步线程）下报错导致业务中断
            return null;
        }
    }
}