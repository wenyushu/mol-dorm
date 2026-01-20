package com.mol.server.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

/**
 * 校区删除前置校验事件
 * 用于解耦 server 和 dorm 模块
 */
@Getter
public class CampusDeleteEvent extends ApplicationEvent {
    
    private final Long campusId;
    
    public CampusDeleteEvent(Object source, Long campusId) {
        super(source);
        this.campusId = campusId;
    }
}