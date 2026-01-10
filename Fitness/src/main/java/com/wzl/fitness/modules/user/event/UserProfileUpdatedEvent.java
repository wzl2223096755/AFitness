package com.wzl.fitness.modules.user.event;

import com.wzl.fitness.shared.event.DomainEvent;
import lombok.Getter;

/**
 * 用户资料更新事件
 * 当用户资料更新时发布此事件
 * 
 * @see Requirements 3.1 - 事件总线提供发布/订阅机制
 */
@Getter
public class UserProfileUpdatedEvent extends DomainEvent {
    
    private final String username;
    private final String updatedField;
    
    public UserProfileUpdatedEvent(Long userId, String username, String updatedField) {
        super(userId);
        this.username = username;
        this.updatedField = updatedField;
    }
}
