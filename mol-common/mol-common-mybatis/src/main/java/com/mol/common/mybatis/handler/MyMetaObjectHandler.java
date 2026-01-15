package com.mol.common.mybatis.handler;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import com.mol.common.core.util.LoginHelper;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * MyBatis-Plus 字段自动填充
 *
 * @author mol
 */
@Slf4j
@Component
public class MyMetaObjectHandler implements MetaObjectHandler {
    
    @Override
    public void insertFill(MetaObject metaObject) {
        // 1. 自动填充时间 (直接调用 LocalDateTime.now())
        this.strictInsertFill(metaObject, "createTime", LocalDateTime.class, LocalDateTime.now());
        this.strictInsertFill(metaObject, "updateTime", LocalDateTime.class, LocalDateTime.now());
        
        // 2. 自动填充操作人
        // 直接调用 getUserIdStr() 获取值，而不是传递 Lambda
        this.strictInsertFill(metaObject, "createBy", String.class, getUserIdStr());
        this.strictInsertFill(metaObject, "updateBy", String.class, getUserIdStr());
    }
    
    @Override
    public void updateFill(MetaObject metaObject) {
        // 更新时仅填充 updateTime 和 updateBy
        this.strictUpdateFill(metaObject, "updateTime", LocalDateTime.class, LocalDateTime.now());
        // 直接调用 getUserIdStr()
        this.strictUpdateFill(metaObject, "updateBy", String.class, getUserIdStr());
    }
    
    /**
     * 辅助方法：获取当前用户 ID 字符串
     */
    private String getUserIdStr() {
        // 这里的 LoginHelper 依赖之前创建的工具类
        Long userId = LoginHelper.getUserId();
        return userId != null ? String.valueOf(userId) : null;
    }
}