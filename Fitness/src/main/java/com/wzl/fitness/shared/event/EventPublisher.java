package com.wzl.fitness.shared.event;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

/**
 * 事件发布器
 * 用于发布领域事件，支持模块间的松耦合通信
 * 
 * 使用Spring的ApplicationEventPublisher作为底层实现，
 * 支持同步和异步事件处理
 */
@Component
public class EventPublisher {
    
    private static final Logger logger = LoggerFactory.getLogger(EventPublisher.class);
    
    private final ApplicationEventPublisher applicationEventPublisher;
    
    public EventPublisher(ApplicationEventPublisher applicationEventPublisher) {
        this.applicationEventPublisher = applicationEventPublisher;
    }
    
    /**
     * 发布领域事件
     * 
     * @param event 要发布的领域事件
     */
    public void publish(DomainEvent event) {
        if (event == null) {
            logger.warn("尝试发布空事件，已忽略");
            return;
        }
        
        logger.info("发布领域事件: {} [eventId={}, userId={}]", 
                event.getEventType(), 
                event.getEventId(), 
                event.getUserId());
        
        try {
            applicationEventPublisher.publishEvent(event);
            logger.debug("领域事件发布成功: {}", event.getEventId());
        } catch (Exception e) {
            logger.error("领域事件发布失败: {} - {}", event.getEventId(), e.getMessage(), e);
            throw new EventPublishException("事件发布失败: " + event.getEventType(), e);
        }
    }
    
    /**
     * 发布多个领域事件
     * 
     * @param events 要发布的领域事件数组
     */
    public void publishAll(DomainEvent... events) {
        if (events == null || events.length == 0) {
            return;
        }
        
        for (DomainEvent event : events) {
            publish(event);
        }
    }
    
    /**
     * 发布多个领域事件
     * 
     * @param events 要发布的领域事件集合
     */
    public void publishAll(Iterable<? extends DomainEvent> events) {
        if (events == null) {
            return;
        }
        
        for (DomainEvent event : events) {
            publish(event);
        }
    }
    
    /**
     * 事件发布异常
     */
    public static class EventPublishException extends RuntimeException {
        public EventPublishException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
