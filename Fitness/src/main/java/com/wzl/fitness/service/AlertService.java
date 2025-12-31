package com.wzl.fitness.service;

import com.wzl.fitness.util.LoggingUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 告警服务
 * 监控关键指标并在超过阈值时发送告警通知
 */
@Service
public class AlertService {
    
    private static final Logger log = LoggerFactory.getLogger(AlertService.class);
    
    // 告警阈值配置
    @Value("${alert.error-rate-threshold:0.05}")
    private double errorRateThreshold; // 错误率阈值 (5%)
    
    @Value("${alert.response-time-threshold:2000}")
    private long responseTimeThreshold; // 响应时间阈值 (2000ms)
    
    @Value("${alert.memory-usage-threshold:0.85}")
    private double memoryUsageThreshold; // 内存使用率阈值 (85%)
    
    @Value("${alert.consecutive-errors-threshold:10}")
    private int consecutiveErrorsThreshold; // 连续错误次数阈值
    
    @Value("${alert.enabled:true}")
    private boolean alertEnabled;
    
    // 指标统计
    private final AtomicInteger totalRequests = new AtomicInteger(0);
    private final AtomicInteger errorRequests = new AtomicInteger(0);
    private final AtomicLong totalResponseTime = new AtomicLong(0);
    private final AtomicInteger consecutiveErrors = new AtomicInteger(0);
    private final Map<String, AlertState> alertStates = new ConcurrentHashMap<>();
    
    // 告警状态
    private static class AlertState {
        LocalDateTime lastAlertTime;
        int alertCount;
        boolean isActive;
        
        AlertState() {
            this.lastAlertTime = null;
            this.alertCount = 0;
            this.isActive = false;
        }
    }
    
    /**
     * 记录请求指标
     */
    public void recordRequest(long responseTimeMs, boolean isError) {
        totalRequests.incrementAndGet();
        totalResponseTime.addAndGet(responseTimeMs);
        
        if (isError) {
            errorRequests.incrementAndGet();
            consecutiveErrors.incrementAndGet();
            
            // 检查连续错误告警
            if (consecutiveErrors.get() >= consecutiveErrorsThreshold) {
                triggerAlert("CONSECUTIVE_ERRORS", 
                    String.format("连续错误次数达到 %d 次", consecutiveErrors.get()),
                    AlertLevel.CRITICAL);
            }
        } else {
            consecutiveErrors.set(0);
        }
        
        // 检查响应时间告警
        if (responseTimeMs > responseTimeThreshold) {
            triggerAlert("SLOW_RESPONSE", 
                String.format("响应时间 %dms 超过阈值 %dms", responseTimeMs, responseTimeThreshold),
                AlertLevel.WARNING);
        }
    }
    
    /**
     * 定时检查系统指标
     */
    @Scheduled(fixedRate = 60000) // 每分钟检查一次
    public void checkSystemMetrics() {
        if (!alertEnabled) {
            return;
        }
        
        // 检查错误率
        checkErrorRate();
        
        // 检查内存使用率
        checkMemoryUsage();
        
        // 重置统计数据
        resetMetrics();
    }
    
    /**
     * 检查错误率
     */
    private void checkErrorRate() {
        int total = totalRequests.get();
        int errors = errorRequests.get();
        
        if (total > 0) {
            double errorRate = (double) errors / total;
            
            if (errorRate > errorRateThreshold) {
                triggerAlert("HIGH_ERROR_RATE", 
                    String.format("错误率 %.2f%% 超过阈值 %.2f%%", 
                        errorRate * 100, errorRateThreshold * 100),
                    AlertLevel.CRITICAL);
            } else {
                resolveAlert("HIGH_ERROR_RATE");
            }
        }
    }
    
    /**
     * 检查内存使用率
     */
    private void checkMemoryUsage() {
        Runtime runtime = Runtime.getRuntime();
        long maxMemory = runtime.maxMemory();
        long usedMemory = runtime.totalMemory() - runtime.freeMemory();
        double memoryUsage = (double) usedMemory / maxMemory;
        
        if (memoryUsage > memoryUsageThreshold) {
            triggerAlert("HIGH_MEMORY_USAGE", 
                String.format("内存使用率 %.2f%% 超过阈值 %.2f%%", 
                    memoryUsage * 100, memoryUsageThreshold * 100),
                AlertLevel.WARNING);
        } else {
            resolveAlert("HIGH_MEMORY_USAGE");
        }
    }
    
    /**
     * 触发告警
     */
    public void triggerAlert(String alertType, String message, AlertLevel level) {
        if (!alertEnabled) {
            return;
        }
        
        AlertState state = alertStates.computeIfAbsent(alertType, k -> new AlertState());
        
        // 防止告警风暴：同一类型告警5分钟内只发送一次
        if (state.lastAlertTime != null && 
            state.lastAlertTime.plusMinutes(5).isAfter(LocalDateTime.now())) {
            return;
        }
        
        state.lastAlertTime = LocalDateTime.now();
        state.alertCount++;
        state.isActive = true;
        
        // 记录告警日志
        switch (level) {
            case CRITICAL:
                log.error("🚨 CRITICAL ALERT [{}]: {}", alertType, message);
                break;
            case WARNING:
                log.warn("⚠️ WARNING ALERT [{}]: {}", alertType, message);
                break;
            case INFO:
                log.info("ℹ️ INFO ALERT [{}]: {}", alertType, message);
                break;
        }
        
        // 发送告警通知
        sendAlertNotification(alertType, message, level);
        
        // 记录审计日志
        LoggingUtils.audit("SYSTEM", "ALERT_TRIGGERED", 
            String.format("Alert: %s - Level: %s - Message: %s", alertType, level, message));
    }
    
    /**
     * 解除告警
     */
    public void resolveAlert(String alertType) {
        AlertState state = alertStates.get(alertType);
        if (state != null && state.isActive) {
            state.isActive = false;
            log.info("✅ ALERT RESOLVED [{}]", alertType);
            
            // 发送告警解除通知
            sendAlertResolvedNotification(alertType);
        }
    }
    
    /**
     * 发送告警通知
     * 可以扩展为发送邮件、短信、Webhook等
     */
    private void sendAlertNotification(String alertType, String message, AlertLevel level) {
        // 这里可以集成各种通知渠道
        // 1. 邮件通知
        // 2. 短信通知
        // 3. Webhook (Slack, DingTalk, WeChat Work等)
        // 4. PagerDuty
        
        // 当前实现：记录到日志
        log.info("Alert notification sent: type={}, level={}, message={}", 
            alertType, level, message);
        
        // TODO: 实现具体的通知渠道
        // sendEmail(alertType, message, level);
        // sendWebhook(alertType, message, level);
    }
    
    /**
     * 发送告警解除通知
     */
    private void sendAlertResolvedNotification(String alertType) {
        log.info("Alert resolved notification sent: type={}", alertType);
    }
    
    /**
     * 重置指标统计
     */
    private void resetMetrics() {
        totalRequests.set(0);
        errorRequests.set(0);
        totalResponseTime.set(0);
    }
    
    /**
     * 获取当前指标
     */
    public Map<String, Object> getCurrentMetrics() {
        int total = totalRequests.get();
        int errors = errorRequests.get();
        long totalTime = totalResponseTime.get();
        
        Runtime runtime = Runtime.getRuntime();
        long maxMemory = runtime.maxMemory();
        long usedMemory = runtime.totalMemory() - runtime.freeMemory();
        
        return Map.of(
            "totalRequests", total,
            "errorRequests", errors,
            "errorRate", total > 0 ? (double) errors / total : 0,
            "avgResponseTime", total > 0 ? totalTime / total : 0,
            "consecutiveErrors", consecutiveErrors.get(),
            "memoryUsage", (double) usedMemory / maxMemory,
            "activeAlerts", alertStates.entrySet().stream()
                .filter(e -> e.getValue().isActive)
                .map(Map.Entry::getKey)
                .toList()
        );
    }
    
    /**
     * 手动触发测试告警
     */
    public void triggerTestAlert() {
        triggerAlert("TEST_ALERT", "这是一条测试告警消息", AlertLevel.INFO);
    }
    
    /**
     * 告警级别枚举
     */
    public enum AlertLevel {
        INFO,
        WARNING,
        CRITICAL
    }
}
