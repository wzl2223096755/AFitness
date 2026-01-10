package com.wzl.fitness.modules.user.event;

import com.wzl.fitness.shared.event.DomainEvent;
import lombok.Getter;

/**
 * 用户注册事件
 * 当新用户注册成功时发布此事件
 * 
 * @see Requirements 3.1 - 事件总线提供发布/订阅机制
 */
@Getter
public class UserRegisteredEvent extends DomainEvent {
    
    private final String username;
    private final String email;
    
    public UserRegisteredEvent(Long userId, String username, String email) {
        super(userId);
        this.username = username;
        this.email = email;
    }
}
