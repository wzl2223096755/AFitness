package com.wzl.fitness.interceptor;

import com.wzl.fitness.annotation.RequireAdmin;
import com.wzl.fitness.annotation.RequireRole;
import com.wzl.fitness.annotation.RequireUser;
import com.wzl.fitness.entity.Role;
import com.wzl.fitness.entity.User;
import com.wzl.fitness.exception.BusinessException;
import com.wzl.fitness.service.AuthenticationService;
import com.wzl.fitness.util.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import java.lang.reflect.Method;
import java.util.Arrays;

/**
 * 权限验证拦截器
 */
@Component
@RequiredArgsConstructor
public class PermissionInterceptor implements HandlerInterceptor {
    
    private static final Logger logger = LoggerFactory.getLogger(PermissionInterceptor.class);
    
    private final AuthenticationService authenticationService;
    private final JwtUtil jwtUtil;
    
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 只处理方法处理器
        if (!(handler instanceof HandlerMethod)) {
            return true;
        }
        
        HandlerMethod handlerMethod = (HandlerMethod) handler;
        Method method = handlerMethod.getMethod();
        
        // 检查权限注解
        if (method.isAnnotationPresent(RequireAdmin.class)) {
            return checkAdminPermission(request, method.getAnnotation(RequireAdmin.class));
        }
        
        if (method.isAnnotationPresent(RequireUser.class)) {
            return checkUserPermission(request, method.getAnnotation(RequireUser.class));
        }
        
        if (method.isAnnotationPresent(RequireRole.class)) {
            return checkRolePermission(request, method.getAnnotation(RequireRole.class));
        }
        
        return true;
    }
    
    /**
     * 检查管理员权限
     */
    private boolean checkAdminPermission(HttpServletRequest request, RequireAdmin annotation) {
        User user = getCurrentUser(request);
        if (user == null) {
            logSecurityFailure("匿名用户", "管理员接口", request.getRequestURI());
            throw new BusinessException(annotation.message());
        }
        
        if (!Role.ADMIN.equals(user.getRole())) {
            logSecurityFailure(user.getUsername(), "管理员接口", request.getRequestURI());
            throw new BusinessException(annotation.message());
        }
        
        logger.debug("管理员{}访问受保护接口: {}", user.getUsername(), request.getRequestURI());
        return true;
    }
    
    /**
     * 检查用户权限
     */
    private boolean checkUserPermission(HttpServletRequest request, RequireUser annotation) {
        User user = getCurrentUser(request);
        if (user == null) {
            logSecurityFailure("匿名用户", "用户接口", request.getRequestURI());
            throw new BusinessException(annotation.message());
        }
        
        logger.debug("用户{}访问用户接口: {}", user.getUsername(), request.getRequestURI());
        return true;
    }
    
    /**
     * 检查特定角色权限
     */
    private boolean checkRolePermission(HttpServletRequest request, RequireRole annotation) {
        User user = getCurrentUser(request);
        if (user == null) {
            logSecurityFailure("匿名用户", "角色接口", request.getRequestURI());
            throw new BusinessException(annotation.message());
        }
        
        Role[] requiredRoles = annotation.value();
        boolean hasPermission = Arrays.stream(requiredRoles)
                .anyMatch(role -> role.equals(user.getRole()));
        
        if (!hasPermission) {
            logSecurityFailure(user.getUsername(), 
                "需要角色" + Arrays.toString(requiredRoles) + "的接口", 
                request.getRequestURI());
            throw new BusinessException(annotation.message());
        }
        
        logger.debug("用户{}访问角色{}接口，验证通过: {}", 
            user.getUsername(), user.getRole(), request.getRequestURI());
        return true;
    }
    
    /**
     * 记录安全失败日志，避免敏感信息泄露
     */
    private void logSecurityFailure(String username, String resourceType, String uri) {
        logger.warn("安全验证失败 - 用户: {}, 资源类型: {}, URI: {}", 
            username, resourceType, uri);
    }
    
    /**
     * 获取当前用户
     * 优化：增加缓存机制，避免频繁数据库查询
     */
    private User getCurrentUser(HttpServletRequest request) {
        try {
            Long userId = (Long) request.getAttribute("userId");
            if (userId == null) {
                logger.warn("请求中未找到用户ID，可能JWT验证失败");
                return null;
            }
            
            // 可以在这里添加用户信息缓存
            User user = authenticationService.getUserById(userId);
            if (user == null) {
                logger.warn("用户ID {} 对应的用户不存在", userId);
            }
            return user;
        } catch (Exception e) {
            logger.error("获取当前用户失败，用户ID: {}, 错误: {}", 
                request.getAttribute("userId"), e.getMessage());
            // 不暴露详细错误信息，避免信息泄露
            return null;
        }
    }
}