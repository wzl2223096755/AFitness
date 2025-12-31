package com.wzl.fitness.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

/**
 * 请求追踪过滤器
 * 为每个请求生成唯一的追踪ID，用于日志关联和问题排查
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class RequestTracingFilter extends OncePerRequestFilter {
    
    private static final Logger log = LoggerFactory.getLogger(RequestTracingFilter.class);
    
    // MDC键名常量
    public static final String TRACE_ID = "traceId";
    public static final String SPAN_ID = "spanId";
    public static final String REQUEST_ID = "requestId";
    public static final String CLIENT_IP = "clientIp";
    public static final String REQUEST_URI = "requestUri";
    public static final String REQUEST_METHOD = "requestMethod";
    public static final String USER_ID = "userId";
    
    // 请求头名称
    private static final String X_TRACE_ID = "X-Trace-Id";
    private static final String X_SPAN_ID = "X-Span-Id";
    private static final String X_REQUEST_ID = "X-Request-Id";
    
    @Override
    protected void doFilterInternal(HttpServletRequest request, 
                                    HttpServletResponse response, 
                                    FilterChain filterChain) throws ServletException, IOException {
        
        long startTime = System.currentTimeMillis();
        
        try {
            // 设置追踪信息到MDC
            setupMDC(request);
            
            // 将追踪ID添加到响应头
            response.setHeader(X_TRACE_ID, MDC.get(TRACE_ID));
            response.setHeader(X_REQUEST_ID, MDC.get(REQUEST_ID));
            
            // 记录请求开始
            if (log.isDebugEnabled() && !isStaticResource(request)) {
                log.debug("Request started: {} {} from {}", 
                    request.getMethod(), 
                    request.getRequestURI(),
                    getClientIp(request));
            }
            
            // 继续过滤器链
            filterChain.doFilter(request, response);
            
        } finally {
            // 记录请求完成
            long duration = System.currentTimeMillis() - startTime;
            
            if (!isStaticResource(request)) {
                if (response.getStatus() >= 400) {
                    log.warn("Request completed: {} {} - Status: {} - Duration: {}ms",
                        request.getMethod(),
                        request.getRequestURI(),
                        response.getStatus(),
                        duration);
                } else if (log.isDebugEnabled()) {
                    log.debug("Request completed: {} {} - Status: {} - Duration: {}ms",
                        request.getMethod(),
                        request.getRequestURI(),
                        response.getStatus(),
                        duration);
                }
            }
            
            // 清理MDC
            clearMDC();
        }
    }
    
    /**
     * 设置MDC追踪信息
     */
    private void setupMDC(HttpServletRequest request) {
        // 获取或生成追踪ID
        String traceId = request.getHeader(X_TRACE_ID);
        if (traceId == null || traceId.isEmpty()) {
            traceId = generateTraceId();
        }
        
        // 获取或生成Span ID
        String spanId = request.getHeader(X_SPAN_ID);
        if (spanId == null || spanId.isEmpty()) {
            spanId = generateSpanId();
        }
        
        // 获取或生成请求ID
        String requestId = request.getHeader(X_REQUEST_ID);
        if (requestId == null || requestId.isEmpty()) {
            requestId = generateRequestId();
        }
        
        // 设置MDC
        MDC.put(TRACE_ID, traceId);
        MDC.put(SPAN_ID, spanId);
        MDC.put(REQUEST_ID, requestId);
        MDC.put(CLIENT_IP, getClientIp(request));
        MDC.put(REQUEST_URI, request.getRequestURI());
        MDC.put(REQUEST_METHOD, request.getMethod());
    }
    
    /**
     * 清理MDC
     */
    private void clearMDC() {
        MDC.remove(TRACE_ID);
        MDC.remove(SPAN_ID);
        MDC.remove(REQUEST_ID);
        MDC.remove(CLIENT_IP);
        MDC.remove(REQUEST_URI);
        MDC.remove(REQUEST_METHOD);
        MDC.remove(USER_ID);
    }
    
    /**
     * 生成追踪ID
     */
    private String generateTraceId() {
        return UUID.randomUUID().toString().replace("-", "").substring(0, 16);
    }
    
    /**
     * 生成Span ID
     */
    private String generateSpanId() {
        return UUID.randomUUID().toString().replace("-", "").substring(0, 8);
    }
    
    /**
     * 生成请求ID
     */
    private String generateRequestId() {
        return UUID.randomUUID().toString().replace("-", "");
    }
    
    /**
     * 获取客户端IP地址
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
        // 如果是多个代理，取第一个IP
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        return ip;
    }
    
    /**
     * 判断是否为静态资源请求
     */
    private boolean isStaticResource(HttpServletRequest request) {
        String uri = request.getRequestURI();
        return uri.endsWith(".css") || 
               uri.endsWith(".js") || 
               uri.endsWith(".png") || 
               uri.endsWith(".jpg") || 
               uri.endsWith(".gif") || 
               uri.endsWith(".ico") ||
               uri.endsWith(".svg") ||
               uri.endsWith(".woff") ||
               uri.endsWith(".woff2") ||
               uri.endsWith(".ttf") ||
               uri.startsWith("/static/") ||
               uri.startsWith("/assets/");
    }
    
    /**
     * 设置用户ID到MDC（供其他组件调用）
     */
    public static void setUserId(String userId) {
        if (userId != null) {
            MDC.put(USER_ID, userId);
        }
    }
    
    /**
     * 获取当前追踪ID
     */
    public static String getCurrentTraceId() {
        return MDC.get(TRACE_ID);
    }
    
    /**
     * 获取当前请求ID
     */
    public static String getCurrentRequestId() {
        return MDC.get(REQUEST_ID);
    }
}
