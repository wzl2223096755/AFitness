package com.wzl.fitness.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * 审计日志实体
 * 记录敏感操作的审计信息
 */
@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
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
    
    /**
     * 操作用户ID（可为空，如登录失败时）
     */
    @Column(name = "user_id")
    private Long userId;
    
    /**
     * 操作用户名
     */
    @Column(name = "username", length = 50)
    private String username;
    
    /**
     * 操作类型
     */
    @Column(name = "action", nullable = false, length = 50)
    @Enumerated(EnumType.STRING)
    private AuditAction action;
    
    /**
     * 操作描述
     */
    @Column(name = "description", length = 500)
    private String description;
    
    /**
     * 目标资源类型
     */
    @Column(name = "resource_type", length = 50)
    private String resourceType;
    
    /**
     * 目标资源ID
     */
    @Column(name = "resource_id")
    private Long resourceId;
    
    /**
     * 操作结果
     */
    @Column(name = "result", nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private AuditResult result;
    
    /**
     * 客户端IP地址
     */
    @Column(name = "ip_address", length = 50)
    private String ipAddress;
    
    /**
     * 用户代理（浏览器信息）
     */
    @Column(name = "user_agent", length = 500)
    private String userAgent;
    
    /**
     * 请求路径
     */
    @Column(name = "request_path", length = 200)
    private String requestPath;
    
    /**
     * 请求方法
     */
    @Column(name = "request_method", length = 10)
    private String requestMethod;
    
    /**
     * 错误信息（操作失败时）
     */
    @Column(name = "error_message", length = 1000)
    private String errorMessage;
    
    /**
     * 创建时间
     */
    @Column(name = "created_at")
    @CreationTimestamp
    private LocalDateTime createdAt;
    
    /**
     * 审计操作类型枚举
     */
    public enum AuditAction {
        LOGIN,              // 登录
        LOGIN_FAILED,       // 登录失败
        LOGOUT,             // 登出
        REGISTER,           // 注册
        PASSWORD_CHANGE,    // 修改密码
        PROFILE_UPDATE,     // 更新资料
        DATA_DELETE,        // 数据删除
        DATA_CREATE,        // 数据创建
        DATA_UPDATE,        // 数据更新
        DATA_RESTORE,       // 数据恢复
        ADMIN_ACTION        // 管理员操作
    }
    
    /**
     * 审计结果枚举
     */
    public enum AuditResult {
        SUCCESS,    // 成功
        FAILURE,    // 失败
        BLOCKED     // 被阻止（如限流）
    }
}
