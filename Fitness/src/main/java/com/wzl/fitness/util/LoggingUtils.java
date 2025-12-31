package com.wzl.fitness.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import java.util.HashMap;
import java.util.Map;

/**
 * 日志工具类
 * 提供结构化日志记录功能
 */
public class LoggingUtils {
    
    private static final Logger AUDIT_LOG = LoggerFactory.getLogger("AUDIT");
    
    /**
     * 记录审计日志
     * @param userId 用户ID
     * @param action 操作类型
     * @param resource 资源类型
     * @param resourceId 资源ID
     * @param details 详细信息
     */
    public static void audit(String userId, String action, String resource, 
                            String resourceId, String details) {
        try {
            MDC.put("userId", userId);
            MDC.put("action", action);
            MDC.put("resource", resource);
            MDC.put("resourceId", resourceId);
            
            AUDIT_LOG.info("User {} performed {} on {} (id: {}): {}", 
                userId, action, resource, resourceId, details);
        } finally {
            MDC.remove("action");
            MDC.remove("resource");
            MDC.remove("resourceId");
        }
    }
    
    /**
     * 记录审计日志（简化版）
     */
    public static void audit(String userId, String action, String message) {
        try {
            MDC.put("userId", userId);
            MDC.put("action", action);
            
            AUDIT_LOG.info("User {} - {}: {}", userId, action, message);
        } finally {
            MDC.remove("action");
        }
    }
    
    /**
     * 记录业务操作日志
     */
    public static void logBusinessOperation(Logger logger, String operation, 
                                           Map<String, Object> context) {
        StringBuilder sb = new StringBuilder();
        sb.append("Operation: ").append(operation);
        
        if (context != null && !context.isEmpty()) {
            sb.append(" | Context: ");
            context.forEach((key, value) -> 
                sb.append(key).append("=").append(value).append(", "));
            // 移除最后的逗号和空格
            sb.setLength(sb.length() - 2);
        }
        
        logger.info(sb.toString());
    }
    
    /**
     * 记录性能日志
     */
    public static void logPerformance(Logger logger, String operation, 
                                     long durationMs, boolean success) {
        if (durationMs > 1000) {
            logger.warn("Slow operation: {} took {}ms (success: {})", 
                operation, durationMs, success);
        } else {
            logger.debug("Operation: {} completed in {}ms (success: {})", 
                operation, durationMs, success);
        }
    }
    
    /**
     * 记录API调用日志
     */
    public static void logApiCall(Logger logger, String method, String uri, 
                                 int statusCode, long durationMs) {
        if (statusCode >= 500) {
            logger.error("API Error: {} {} - Status: {} - Duration: {}ms", 
                method, uri, statusCode, durationMs);
        } else if (statusCode >= 400) {
            logger.warn("API Client Error: {} {} - Status: {} - Duration: {}ms", 
                method, uri, statusCode, durationMs);
        } else if (durationMs > 1000) {
            logger.warn("Slow API: {} {} - Status: {} - Duration: {}ms", 
                method, uri, statusCode, durationMs);
        } else {
            logger.debug("API Call: {} {} - Status: {} - Duration: {}ms", 
                method, uri, statusCode, durationMs);
        }
    }
    
    /**
     * 记录异常日志（带上下文）
     */
    public static void logException(Logger logger, String operation, 
                                   Exception e, Map<String, Object> context) {
        StringBuilder sb = new StringBuilder();
        sb.append("Exception in ").append(operation);
        
        if (context != null && !context.isEmpty()) {
            sb.append(" | Context: ");
            context.forEach((key, value) -> 
                sb.append(key).append("=").append(value).append(", "));
            sb.setLength(sb.length() - 2);
        }
        
        logger.error(sb.toString(), e);
    }
    
    /**
     * 创建上下文Map的便捷方法
     */
    public static Map<String, Object> context(Object... keyValues) {
        Map<String, Object> map = new HashMap<>();
        for (int i = 0; i < keyValues.length - 1; i += 2) {
            map.put(String.valueOf(keyValues[i]), keyValues[i + 1]);
        }
        return map;
    }
    
    /**
     * 记录安全相关日志
     */
    public static void logSecurity(Logger logger, String event, String userId, 
                                  String details, boolean success) {
        try {
            MDC.put("securityEvent", event);
            MDC.put("userId", userId != null ? userId : "anonymous");
            
            if (success) {
                logger.info("Security Event: {} - User: {} - {}", event, userId, details);
            } else {
                logger.warn("Security Event Failed: {} - User: {} - {}", 
                    event, userId, details);
            }
        } finally {
            MDC.remove("securityEvent");
        }
    }
    
    /**
     * 记录数据变更日志
     */
    public static void logDataChange(Logger logger, String entity, String entityId, 
                                    String operation, String userId, 
                                    Object oldValue, Object newValue) {
        try {
            MDC.put("entity", entity);
            MDC.put("entityId", entityId);
            MDC.put("dataOperation", operation);
            MDC.put("userId", userId);
            
            logger.info("Data Change: {} {} (id: {}) by user {} - Old: {}, New: {}", 
                operation, entity, entityId, userId, oldValue, newValue);
        } finally {
            MDC.remove("entity");
            MDC.remove("entityId");
            MDC.remove("dataOperation");
        }
    }
}
