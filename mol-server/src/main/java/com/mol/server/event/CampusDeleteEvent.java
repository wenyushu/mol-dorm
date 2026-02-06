package com.mol.server.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

/**
 * 校区删除事件
 * <p>
 * 当 SysCampusService 尝试删除校区时发布此事件。
 * 宿舍模块 (Dorm) 监听到此事件后，需检查该校区下是否有楼栋，如果有则抛异常阻断删除。
 * </p>
 */
@Getter
public class CampusDeleteEvent extends ApplicationEvent {
    
    private final Long campusId;
    
    public CampusDeleteEvent(Object source, Long campusId) {
        super(source);
        this.campusId = campusId;
    }
}