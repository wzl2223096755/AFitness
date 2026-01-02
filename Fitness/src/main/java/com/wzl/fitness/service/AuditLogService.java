package com.wzl.fitness.service;

import com.wzl.fitness.entity.AuditLog;
import com.wzl.fitness.entity.AuditLog.AuditAction;
import com.wzl.fitness.entity.AuditLog.AuditResult;
import com.wzl.fitness.repository.AuditLogRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.LocalDateTime;

/**
 * 审计日志服务
 */
@Service
public class AuditLogService {
    
    private static final Logger log = LoggerFactory.getLogger(AuditLogService.class);
    private final AuditLogRepository auditLogRepository;
    
    public AuditLogService(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }
    
    /**
     * 记录登录成功
     */
    public void logLoginSuccess(Long userId, String username) {
        log(AuditLog.builder()
            .userId(userId)
            .username(username)
            .action(AuditAction.LOGIN)
            .result(AuditResult.SUCCESS)
            .description("用户登录成功")
            .build());
    }
    
    /**
     * 记录登录失败
     */
    public void logLoginFailure(String username, String reason) {
        log(AuditLog.builder()
            .username(username)
            .action(AuditAction.LOGIN_FAILED)
            .result(AuditResult.FAILURE)
            .description("用户登录失败")
            .errorMessage(reason)
            .build());
    }
    
    /**
     * 记录登出
     */
    public void logLogout(Long userId, String username) {
        log(AuditLog.builder()
            .userId(userId)
            .username(username)
            .action(AuditAction.LOGOUT)
            .result(AuditResult.SUCCESS)
            .description("用户登出")
            .build());
    }
    
    /**
     * 记录注册
     */
    public void logRegister(Long userId, String username) {
        log(AuditLog.builder()
            .userId(userId)
            .username(username)
            .action(AuditAction.REGISTER)
            .result(AuditResult.SUCCESS)
            .description("新用户注册")
            .build());
    }
    
    /**
     * 记录密码修改
     */
    public void logPasswordChange(Long userId, String username, boolean success, String errorMessage) {
        log(AuditLog.builder()
            .userId(userId)
            .username(username)
            .action(AuditAction.PASSWORD_CHANGE)
            .result(success ? AuditResult.SUCCESS : AuditResult.FAILURE)
            .description(success ? "密码修改成功" : "密码修改失败")
            .errorMessage(errorMessage)
            .build());
    }
    
    /**
     * 记录资料更新
     */
    public void logProfileUpdate(Long userId, String username) {
        log(AuditLog.builder()
            .userId(userId)
            .username(username)
            .action(AuditAction.PROFILE_UPDATE)
            .result(AuditResult.SUCCESS)
            .description("用户资料更新")
            .build());
    }
    
    /**
     * 记录数据删除
     */
    public void logDataDelete(Long userId, String username, String resourceType, Long resourceId) {
        log(AuditLog.builder()
            .userId(userId)
            .username(username)
            .action(AuditAction.DATA_DELETE)
            .result(AuditResult.SUCCESS)
            .description("删除" + resourceType)
            .resourceType(resourceType)
            .resourceId(resourceId)
            .build());
    }
    
    /**
     * 记录数据恢复
     */
    public void logDataRestore(Long userId, String username, String resourceType, Long resourceId) {
        log(AuditLog.builder()
            .userId(userId)
            .username(username)
            .action(AuditAction.DATA_RESTORE)
            .result(AuditResult.SUCCESS)
            .description("恢复" + resourceType)
            .resourceType(resourceType)
            .resourceId(resourceId)
            .build());
    }
    
    /**
     * 记录批量数据恢复
     */
    public void logBatchDataRestore(Long userId, String username, String resourceType, int count) {
        log(AuditLog.builder()
            .userId(userId)
            .username(username)
            .action(AuditAction.DATA_RESTORE)
            .result(AuditResult.SUCCESS)
            .description("批量恢复" + resourceType + "，共" + count + "条记录")
            .resourceType(resourceType)
            .build());
    }
    
    /**
     * 记录数据创建
     */
    public void logDataCreate(Long userId, String username, String resourceType, Long resourceId) {
        log(AuditLog.builder()
            .userId(userId)
            .username(username)
            .action(AuditAction.DATA_CREATE)
            .result(AuditResult.SUCCESS)
            .description("创建" + resourceType)
            .resourceType(resourceType)
            .resourceId(resourceId)
            .build());
    }
    
    /**
     * 记录管理员操作
     */
    public void logAdminAction(Long userId, String username, String description, 
                               String resourceType, Long resourceId) {
        log(AuditLog.builder()
            .userId(userId)
            .username(username)
            .action(AuditAction.ADMIN_ACTION)
            .result(AuditResult.SUCCESS)
            .description(description)
            .resourceType(resourceType)
            .resourceId(resourceId)
            .build());
    }
    
    /**
     * 通用日志记录方法
     */
    @Async
    public void log(AuditLog auditLog) {
        try {
            // 填充请求信息
            fillRequestInfo(auditLog);
            
            // 保存日志
            auditLogRepository.save(auditLog);
            
            log.debug("审计日志已记录: action={}, user={}, result={}", 
                auditLog.getAction(), auditLog.getUsername(), auditLog.getResult());
        } catch (Exception e) {
            // 审计日志记录失败不应影响主业务
            log.error("审计日志记录失败: {}", e.getMessage(), e);
        }
    }
    
    /**
     * 填充HTTP请求信息
     */
    private void fillRequestInfo(AuditLog auditLog) {
        try {
            ServletRequestAttributes attributes = 
                (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes != null) {
                HttpServletRequest request = attributes.getRequest();
                auditLog.setIpAddress(getClientIp(request));
                auditLog.setUserAgent(request.getHeader("User-Agent"));
                auditLog.setRequestPath(request.getRequestURI());
                auditLog.setRequestMethod(request.getMethod());
            }
        } catch (Exception e) {
            log.debug("无法获取请求信息: {}", e.getMessage());
        }
    }
    
    /**
     * 获取客户端真实IP
     */
    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_CLIENT_IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_X_FORWARDED_FOR");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        // 多个代理时取第一个IP
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        return ip;
    }
    
    /**
     * 查询用户的审计日志
     */
    public Page<AuditLog> getUserAuditLogs(Long userId, int page, int size) {
        return auditLogRepository.findByUserIdOrderByCreatedAtDesc(userId, PageRequest.of(page, size));
    }
    
    /**
     * 查询指定操作类型的审计日志
     */
    public Page<AuditLog> getAuditLogsByAction(AuditAction action, int page, int size) {
        return auditLogRepository.findByActionOrderByCreatedAtDesc(action, PageRequest.of(page, size));
    }
    
    /**
     * 查询指定时间范围的审计日志
     */
    public Page<AuditLog> getAuditLogsByTimeRange(LocalDateTime start, LocalDateTime end, int page, int size) {
        return auditLogRepository.findByTimeRange(start, end, PageRequest.of(page, size));
    }
}
