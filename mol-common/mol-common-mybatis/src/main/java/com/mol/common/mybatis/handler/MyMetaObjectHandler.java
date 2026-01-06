package com.mol.common.mybatis.handler;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * MyBatis-Plus 自动填充处理器
 * <p>用于处理 BaseEntity 中的公共字段自动赋值，如创建时间、修改时间、创建人等</p>
 *
 * @author mol
 */
@Slf4j
@Component // 必须交给 Spring 管理，MyBatis-Plus 才能自动扫描到它
public class MyMetaObjectHandler implements MetaObjectHandler {
    
    /**
     * 插入时的填充策略 (Insert)
     * @param metaObject 元对象
     */
    @Override
    public void insertFill(MetaObject metaObject) {
        log.info("start insert fill ....");
        
        // 1. 自动填充创建时间 (createTime)
        // 参数说明：字段名(Java属性名)、字段类型、填充的值
        this.strictInsertFill(metaObject, "createTime", LocalDateTime.class, LocalDateTime.now());
        
        // 2. 自动填充修改时间 (updateTime)
        this.strictInsertFill(metaObject, "updateTime", LocalDateTime.class, LocalDateTime.now());
        
        // 3. 自动填充逻辑删除标记 (delFlag)
        // 默认设置为 '0' 表示正常存活
        this.strictInsertFill(metaObject, "delFlag", String.class, "0");
        
        // 4. 自动填充创建人和修改人 (createBy, updateBy)
        // 尝试获取当前登录人的 ID。
        // 如果获取不到（比如还没做登录），就填入 "-1" 作为默认值。
        String currentUserId = "-1";
        try {
            if (StpUtil.isLogin()) {
                currentUserId = StpUtil.getLoginIdAsString();
            }
        } catch (Exception e) {
            // 此时可能还没有集成 Sa-Token 或者上下文环境不全，直接使用默认值
        }
        
        this.strictInsertFill(metaObject, "createBy", String.class, currentUserId);
        this.strictInsertFill(metaObject, "updateBy", String.class, currentUserId);
    }
    
    /**
     * 更新时的填充策略 (Update)
     * @param metaObject 元对象
     */
    @Override
    public void updateFill(MetaObject metaObject) {
        log.info("start update fill ....");
        
        // 1. 自动刷新修改时间
        this.strictUpdateFill(metaObject, "updateTime", LocalDateTime.class, LocalDateTime.now());
        
        // 2. 自动刷新修改人
        String currentUserId = "-1";
        try {
            if (StpUtil.isLogin()) {
                currentUserId = StpUtil.getLoginIdAsString();
            }
        } catch (Exception e) {
            // 保持静默
        }
        this.strictUpdateFill(metaObject, "updateBy", String.class, currentUserId);
    }
}