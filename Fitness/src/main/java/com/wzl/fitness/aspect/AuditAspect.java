package com.wzl.fitness.aspect;

import com.wzl.fitness.annotation.Auditable;
import com.wzl.fitness.entity.AuditLog;
import com.wzl.fitness.entity.AuditLog.AuditAction;
import com.wzl.fitness.entity.AuditLog.AuditResult;
import com.wzl.fitness.service.AuditLogService;
import jakarta.servlet.http.HttpServletRequest;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.lang.reflect.Method;

/**
 * 审计切面
 */
@Aspect
@Component
public class AuditAspect {
    
    private static final Logger log = LoggerFactory.getLogger(AuditAspect.class);
    private final AuditLogService auditLogService;
    
    public AuditAspect(AuditLogService auditLogService) {
        this.auditLogService = auditLogService;
    }
    
    /**
     * 环绕通知：拦截所有标记了@Auditable注解的方法
     */
    @Around("@annotation(com.wzl.fitness.annotation.Auditable)")
    public Object audit(ProceedingJoinPoint joinPoint) throws Throwable {
        // 获取方法签名和注解
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        Auditable auditable = method.getAnnotation(Auditable.class);
        
        // 获取当前用户信息
        String username = getCurrentUsername();
        Long userId = getCurrentUserId();
        
        // 构建审计日志
        AuditLog.AuditLogBuilder logBuilder = AuditLog.builder()
            .userId(userId)
            .username(username)
            .action(auditable.action())
            .description(auditable.description().isEmpty() ? 
                getDefaultDescription(auditable.action()) : auditable.description())
            .resourceType(auditable.resourceType());
        
        // 尝试从参数中获取资源ID
        Long resourceId = extractResourceId(joinPoint.getArgs());
        if (resourceId != null) {
            logBuilder.resourceId(resourceId);
        }
        
        try {
            // 执行目标方法
            Object result = joinPoint.proceed();
            
            // 记录成功日志
            logBuilder.result(AuditResult.SUCCESS);
            auditLogService.log(logBuilder.build());
            
            return result;
        } catch (Exception e) {
            // 记录失败日志
            logBuilder.result(AuditResult.FAILURE)
                .errorMessage(e.getMessage());
            auditLogService.log(logBuilder.build());
            
            throw e;
        }
    }
    
    /**
     * 获取当前登录用户名
     */
    private String getCurrentUsername() {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getPrincipal())) {
                return auth.getName();
            }
        } catch (Exception e) {
            log.debug("无法获取当前用户名: {}", e.getMessage());
        }
        return null;
    }
    
    /**
     * 获取当前登录用户ID
     */
    private Long getCurrentUserId() {
        try {
            ServletRequestAttributes attributes = 
                (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes != null) {
                HttpServletRequest request = attributes.getRequest();
                Object userId = request.getAttribute("userId");
                if (userId instanceof Long) {
                    return (Long) userId;
                }
            }
        } catch (Exception e) {
            log.debug("无法获取当前用户ID: {}", e.getMessage());
        }
        return null;
    }
    
    /**
     * 从方法参数中提取资源ID
     */
    private Long extractResourceId(Object[] args) {
        if (args == null || args.length == 0) {
            return null;
        }
        
        // 尝试找到Long类型的参数作为资源ID
        for (Object arg : args) {
            if (arg instanceof Long) {
                return (Long) arg;
            }
        }
        return null;
    }
    
    /**
     * 获取默认操作描述
     */
    private String getDefaultDescription(AuditAction action) {
        return switch (action) {
            case LOGIN -> "用户登录";
            case LOGIN_FAILED -> "登录失败";
            case LOGOUT -> "用户登出";
            case REGISTER -> "用户注册";
            case PASSWORD_CHANGE -> "修改密码";
            case PROFILE_UPDATE -> "更新资料";
            case DATA_DELETE -> "删除数据";
            case DATA_CREATE -> "创建数据";
            case DATA_UPDATE -> "更新数据";
            case DATA_RESTORE -> "恢复数据";
            case ADMIN_ACTION -> "管理员操作";
            case DATA_EXPORT -> "数据导出";
        };
    }
}
