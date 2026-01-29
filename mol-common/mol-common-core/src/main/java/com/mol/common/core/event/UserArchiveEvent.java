package com.mol.common.core.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

/**
 * 用户归档/异动事件
 * <p>
 * 当用户发生退学、休学、毕业等异动时发布此事件。
 * 其他模块（如宿舍、财务）监听此事件进行后续处理。
 * </p>
 */
@Getter
public class UserArchiveEvent extends ApplicationEvent {
    
    /**
     * 被归档的用户 ID
     */
    private final Long userId;
    
    /**
     * 归档类型 Code (参考 ArchiveTypeEnum)
     */
    private final Integer archiveType;
    
    public UserArchiveEvent(Object source, Long userId, Integer archiveType) {
        super(source);
        this.userId = userId;
        this.archiveType = archiveType;
    }
}