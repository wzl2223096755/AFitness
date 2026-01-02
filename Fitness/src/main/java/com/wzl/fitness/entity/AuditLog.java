package com.wzl.fitness.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * 审计日志实体
 * 记录敏感操作的审计信息
 */
@Entity
@Table(name = "audit_logs", indexes = {
    @Index(name = "idx_audit_user_id", columnList = "user_id"),
    @Index(name = "idx_audit_action", columnList = "action"),
    @Index(name = "idx_audit_created_at", columnList = "created_at"),
    @Index(name = "idx_audit_user_action", columnList = "user_id, action")
})
public class AuditLog {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "user_id")
    private Long userId;
    
    @Column(name = "username", length = 50)
    private String username;
    
    @Column(name = "action", nullable = false, length = 50)
    @Enumerated(EnumType.STRING)
    private AuditAction action;
    
    @Column(name = "description", length = 500)
    private String description;
    
    @Column(name = "resource_type", length = 50)
    private String resourceType;
    
    @Column(name = "resource_id")
    private Long resourceId;
    
    @Column(name = "result", nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private AuditResult result;
    
    @Column(name = "ip_address", length = 50)
    private String ipAddress;
    
    @Column(name = "user_agent", length = 500)
    private String userAgent;
    
    @Column(name = "request_path", length = 200)
    private String requestPath;
    
    @Column(name = "request_method", length = 10)
    private String requestMethod;
    
    @Column(name = "error_message", length = 1000)
    private String errorMessage;
    
    @Column(name = "created_at")
    @CreationTimestamp
    private LocalDateTime createdAt;
    
    public enum AuditAction {
        LOGIN, LOGIN_FAILED, LOGOUT, REGISTER, PASSWORD_CHANGE,
        PROFILE_UPDATE, DATA_DELETE, DATA_CREATE, DATA_UPDATE,
        DATA_RESTORE, ADMIN_ACTION, DATA_EXPORT
    }
    
    public enum AuditResult {
        SUCCESS, FAILURE, BLOCKED
    }
    
    // Constructors
    public AuditLog() {}
    
    public AuditLog(Long id, Long userId, String username, AuditAction action, String description,
                    String resourceType, Long resourceId, AuditResult result, String ipAddress,
                    String userAgent, String requestPath, String requestMethod, String errorMessage,
                    LocalDateTime createdAt) {
        this.id = id;
        this.userId = userId;
        this.username = username;
        this.action = action;
        this.description = description;
        this.resourceType = resourceType;
        this.resourceId = resourceId;
        this.result = result;
        this.ipAddress = ipAddress;
        this.userAgent = userAgent;
        this.requestPath = requestPath;
        this.requestMethod = requestMethod;
        this.errorMessage = errorMessage;
        this.createdAt = createdAt;
    }
    
    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public AuditAction getAction() { return action; }
    public void setAction(AuditAction action) { this.action = action; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getResourceType() { return resourceType; }
    public void setResourceType(String resourceType) { this.resourceType = resourceType; }
    public Long getResourceId() { return resourceId; }
    public void setResourceId(Long resourceId) { this.resourceId = resourceId; }
    public AuditResult getResult() { return result; }
    public void setResult(AuditResult result) { this.result = result; }
    public String getIpAddress() { return ipAddress; }
    public void setIpAddress(String ipAddress) { this.ipAddress = ipAddress; }
    public String getUserAgent() { return userAgent; }
    public void setUserAgent(String userAgent) { this.userAgent = userAgent; }
    public String getRequestPath() { return requestPath; }
    public void setRequestPath(String requestPath) { this.requestPath = requestPath; }
    public String getRequestMethod() { return requestMethod; }
    public void setRequestMethod(String requestMethod) { this.requestMethod = requestMethod; }
    public String getErrorMessage() { return errorMessage; }
    public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    // Builder pattern
    public static AuditLogBuilder builder() { return new AuditLogBuilder(); }
    
    public static class AuditLogBuilder {
        private Long id;
        private Long userId;
        private String username;
        private AuditAction action;
        private String description;
        private String resourceType;
        private Long resourceId;
        private AuditResult result;
        private String ipAddress;
        private String userAgent;
        private String requestPath;
        private String requestMethod;
        private String errorMessage;
        private LocalDateTime createdAt;
        
        public AuditLogBuilder id(Long id) { this.id = id; return this; }
        public AuditLogBuilder userId(Long userId) { this.userId = userId; return this; }
        public AuditLogBuilder username(String username) { this.username = username; return this; }
        public AuditLogBuilder action(AuditAction action) { this.action = action; return this; }
        public AuditLogBuilder description(String description) { this.description = description; return this; }
        public AuditLogBuilder resourceType(String resourceType) { this.resourceType = resourceType; return this; }
        public AuditLogBuilder resourceId(Long resourceId) { this.resourceId = resourceId; return this; }
        public AuditLogBuilder result(AuditResult result) { this.result = result; return this; }
        public AuditLogBuilder ipAddress(String ipAddress) { this.ipAddress = ipAddress; return this; }
        public AuditLogBuilder userAgent(String userAgent) { this.userAgent = userAgent; return this; }
        public AuditLogBuilder requestPath(String requestPath) { this.requestPath = requestPath; return this; }
        public AuditLogBuilder requestMethod(String requestMethod) { this.requestMethod = requestMethod; return this; }
        public AuditLogBuilder errorMessage(String errorMessage) { this.errorMessage = errorMessage; return this; }
        public AuditLogBuilder createdAt(LocalDateTime createdAt) { this.createdAt = createdAt; return this; }
        
        public AuditLog build() {
            return new AuditLog(id, userId, username, action, description, resourceType, resourceId,
                    result, ipAddress, userAgent, requestPath, requestMethod, errorMessage, createdAt);
        }
    }
}
