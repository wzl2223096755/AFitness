package com.wzl.fitness.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * 告警配置类
 */
@Configuration
@ConfigurationProperties(prefix = "alert")
public class AlertConfig {
    
    /**
     * 是否启用告警
     */
    private boolean enabled = true;
    
    /**
     * 错误率阈值 (0.0 - 1.0)
     */
    private double errorRateThreshold = 0.05;
    
    /**
     * 响应时间阈值 (毫秒)
     */
    private long responseTimeThreshold = 2000;
    
    /**
     * 内存使用率阈值 (0.0 - 1.0)
     */
    private double memoryUsageThreshold = 0.85;
    
    /**
     * 连续错误次数阈值
     */
    private int consecutiveErrorsThreshold = 10;
    
    /**
     * 告警静默时间 (分钟)
     */
    private int silencePeriodMinutes = 5;
    
    /**
     * 通知配置
     */
    private NotificationConfig notification = new NotificationConfig();
    
    // Getters and Setters
    public boolean isEnabled() {
        return enabled;
    }
    
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
    
    public double getErrorRateThreshold() {
        return errorRateThreshold;
    }
    
    public void setErrorRateThreshold(double errorRateThreshold) {
        this.errorRateThreshold = errorRateThreshold;
    }
    
    public long getResponseTimeThreshold() {
        return responseTimeThreshold;
    }
    
    public void setResponseTimeThreshold(long responseTimeThreshold) {
        this.responseTimeThreshold = responseTimeThreshold;
    }
    
    public double getMemoryUsageThreshold() {
        return memoryUsageThreshold;
    }
    
    public void setMemoryUsageThreshold(double memoryUsageThreshold) {
        this.memoryUsageThreshold = memoryUsageThreshold;
    }
    
    public int getConsecutiveErrorsThreshold() {
        return consecutiveErrorsThreshold;
    }
    
    public void setConsecutiveErrorsThreshold(int consecutiveErrorsThreshold) {
        this.consecutiveErrorsThreshold = consecutiveErrorsThreshold;
    }
    
    public int getSilencePeriodMinutes() {
        return silencePeriodMinutes;
    }
    
    public void setSilencePeriodMinutes(int silencePeriodMinutes) {
        this.silencePeriodMinutes = silencePeriodMinutes;
    }
    
    public NotificationConfig getNotification() {
        return notification;
    }
    
    public void setNotification(NotificationConfig notification) {
        this.notification = notification;
    }
    
    /**
     * 通知配置内部类
     */
    public static class NotificationConfig {
        
        /**
         * 是否启用邮件通知
         */
        private boolean emailEnabled = false;
        
        /**
         * 邮件接收者列表
         */
        private String[] emailRecipients = {};
        
        /**
         * 是否启用Webhook通知
         */
        private boolean webhookEnabled = false;
        
        /**
         * Webhook URL
         */
        private String webhookUrl = "";
        
        /**
         * Webhook类型 (slack, dingtalk, wechat, custom)
         */
        private String webhookType = "custom";
        
        // Getters and Setters
        public boolean isEmailEnabled() {
            return emailEnabled;
        }
        
        public void setEmailEnabled(boolean emailEnabled) {
            this.emailEnabled = emailEnabled;
        }
        
        public String[] getEmailRecipients() {
            return emailRecipients;
        }
        
        public void setEmailRecipients(String[] emailRecipients) {
            this.emailRecipients = emailRecipients;
        }
        
        public boolean isWebhookEnabled() {
            return webhookEnabled;
        }
        
        public void setWebhookEnabled(boolean webhookEnabled) {
            this.webhookEnabled = webhookEnabled;
        }
        
        public String getWebhookUrl() {
            return webhookUrl;
        }
        
        public void setWebhookUrl(String webhookUrl) {
            this.webhookUrl = webhookUrl;
        }
        
        public String getWebhookType() {
            return webhookType;
        }
        
        public void setWebhookType(String webhookType) {
            this.webhookType = webhookType;
        }
    }
}
