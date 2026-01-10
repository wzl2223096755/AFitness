package com.wzl.fitness.common;

import com.wzl.fitness.exception.BusinessException;
import com.wzl.fitness.security.CustomUserDetails;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * 基础控制器类
 * 
 * @deprecated 请使用 {@link com.wzl.fitness.shared.common.BaseController} 代替
 * 此类保留用于向后兼容，将在未来版本中移除
 */
@Deprecated
public abstract class BaseController {

    /**
     * 从请求属性中获取用户ID（由JwtAuthenticationFilter设置）
     */
    protected Long getUserIdFromRequest(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        if (userId == null) {
            throw new BusinessException("用户未认证");
        }
        return userId;
    }

    /**
     * 获取当前认证用户ID
     */
    protected Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof CustomUserDetails) {
            return ((CustomUserDetails) authentication.getPrincipal()).getId();
        }
        throw new SecurityException("用户未认证或认证信息无效");
    }

    /**
     * 获取当前认证用户名
     */
    protected String getCurrentUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof CustomUserDetails) {
            return ((CustomUserDetails) authentication.getPrincipal()).getUsername();
        }
        throw new SecurityException("用户未认证或认证信息无效");
    }

    /**
     * 检查当前用户是否有管理员权限
     */
    protected boolean isAdmin() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication != null && 
               authentication.getAuthorities().stream()
                   .anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN"));
    }

    /**
     * 检查资源访问权限（用户只能访问自己的资源，管理员可以访问所有资源）
     */
    protected void checkResourceAccess(Long resourceUserId) {
        if (!isAdmin() && !getCurrentUserId().equals(resourceUserId)) {
            throw new SecurityException("无权限访问该资源");
        }
    }

    /**
     * 成功响应
     */
    protected <T> ApiResponse<T> success(T data) {
        return ApiResponse.success(data);
    }

    /**
     * 成功响应（无数据）
     */
    protected ApiResponse<Void> success() {
        return ApiResponse.success("操作成功", null);
    }

    /**
     * 分页响应
     */
    protected <T> PageResponse<T> pageResponse(org.springframework.data.domain.Page<T> page) {
        return PageResponse.of(page);
    }
}