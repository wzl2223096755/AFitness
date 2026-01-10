package com.wzl.fitness.shared.event;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * 领域事件基类
 * 所有领域事件都应继承此类
 * 
 * 领域事件用于模块间的松耦合通信，支持事件驱动架构
 */
public abstract class DomainEvent {
    
    /**
     * 事件唯一标识
     */
    private final String eventId;
    
    /**
     * 事件发生时间
     */
    private final LocalDateTime occurredAt;
    
    /**
     * 关联的用户ID（可选）
     */
    private final Long userId;
    
    /**
     * 事件类型名称
     */
    private final String eventType;
    
    /**
     * 创建领域事件
     * @param userId 关联的用户ID
     */
    protected DomainEvent(Long userId) {
        this.eventId = UUID.randomUUID().toString();
        this.occurredAt = LocalDateTime.now();
        this.userId = userId;
        this.eventType = this.getClass().getSimpleName();
    }
    
    /**
     * 创建领域事件（无用户关联）
     */
    protected DomainEvent() {
        this(null);
    }
    
    /**
     * 获取事件唯一标识
     */
    public String getEventId() {
        return eventId;
    }
    
    /**
     * 获取事件发生时间
     */
    public LocalDateTime getOccurredAt() {
        return occurredAt;
    }
    
    /**
     * 获取关联的用户ID
     */
    public Long getUserId() {
        return userId;
    }
    
    /**
     * 获取事件类型名称
     */
    public String getEventType() {
        return eventType;
    }
    
    @Override
    public String toString() {
        return String.format("%s[eventId=%s, occurredAt=%s, userId=%s]",
                eventType, eventId, occurredAt, userId);
    }
}
