package com.wzl.fitness.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * 速率限制服务
 * 用于限制登录尝试次数，防止暴力破解
 */
@Service
@Slf4j
public class RateLimiterService {

    // 存储登录尝试次数
    private final Map<String, AttemptInfo> loginAttempts = new ConcurrentHashMap<>();
    
    // 最大尝试次数
    private static final int MAX_ATTEMPTS = 5;
    
    // 锁定时间（毫秒）
    private static final long LOCK_DURATION = TimeUnit.MINUTES.toMillis(15);
    
    // 尝试窗口时间（毫秒）
    private static final long ATTEMPT_WINDOW = TimeUnit.MINUTES.toMillis(5);

    /**
     * 检查是否被限制
     */
    public boolean isBlocked(String key) {
        AttemptInfo info = loginAttempts.get(key);
        if (info == null) {
            return false;
        }
        
        // 检查锁定是否过期
        if (info.isLocked && System.currentTimeMillis() - info.lockTime > LOCK_DURATION) {
            loginAttempts.remove(key);
            return false;
        }
        
        return info.isLocked;
    }

    /**
     * 记录失败的登录尝试
     */
    public void recordFailedAttempt(String key) {
        AttemptInfo info = loginAttempts.compute(key, (k, v) -> {
            if (v == null) {
                return new AttemptInfo();
            }
            
            // 检查是否在尝试窗口内
            if (System.currentTimeMillis() - v.firstAttemptTime > ATTEMPT_WINDOW) {
                // 窗口过期，重置
                return new AttemptInfo();
            }
            
            v.attemptCount++;
            
            // 检查是否达到最大尝试次数
            if (v.attemptCount >= MAX_ATTEMPTS) {
                v.isLocked = true;
                v.lockTime = System.currentTimeMillis();
                log.warn("账户 {} 因多次登录失败被锁定 {} 分钟", key, LOCK_DURATION / 60000);
            }
            
            return v;
        });
    }

    /**
     * 清除登录尝试记录（成功登录后调用）
     */
    public void clearAttempts(String key) {
        loginAttempts.remove(key);
    }

    /**
     * 获取剩余尝试次数
     */
    public int getRemainingAttempts(String key) {
        AttemptInfo info = loginAttempts.get(key);
        if (info == null) {
            return MAX_ATTEMPTS;
        }
        
        // 检查窗口是否过期
        if (System.currentTimeMillis() - info.firstAttemptTime > ATTEMPT_WINDOW) {
            return MAX_ATTEMPTS;
        }
        
        return Math.max(0, MAX_ATTEMPTS - info.attemptCount);
    }

    /**
     * 获取锁定剩余时间（秒）
     */
    public long getRemainingLockTime(String key) {
        AttemptInfo info = loginAttempts.get(key);
        if (info == null || !info.isLocked) {
            return 0;
        }
        
        long remaining = LOCK_DURATION - (System.currentTimeMillis() - info.lockTime);
        return Math.max(0, remaining / 1000);
    }

    /**
     * 尝试信息内部类
     */
    private static class AttemptInfo {
        int attemptCount = 1;
        long firstAttemptTime = System.currentTimeMillis();
        boolean isLocked = false;
        long lockTime = 0;
    }
}
