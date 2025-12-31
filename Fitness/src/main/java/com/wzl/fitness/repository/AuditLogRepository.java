package com.wzl.fitness.repository;

import com.wzl.fitness.entity.AuditLog;
import com.wzl.fitness.entity.AuditLog.AuditAction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 审计日志数据访问层
 */
@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {
    
    /**
     * 根据用户ID查询审计日志
     */
    Page<AuditLog> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);
    
    /**
     * 根据操作类型查询审计日志
     */
    Page<AuditLog> findByActionOrderByCreatedAtDesc(AuditAction action, Pageable pageable);
    
    /**
     * 根据用户名查询审计日志
     */
    Page<AuditLog> findByUsernameOrderByCreatedAtDesc(String username, Pageable pageable);
    
    /**
     * 查询指定时间范围内的审计日志
     */
    @Query("SELECT a FROM AuditLog a WHERE a.createdAt BETWEEN :startTime AND :endTime ORDER BY a.createdAt DESC")
    Page<AuditLog> findByTimeRange(
        @Param("startTime") LocalDateTime startTime, 
        @Param("endTime") LocalDateTime endTime, 
        Pageable pageable
    );
    
    /**
     * 查询用户最近的登录失败记录
     */
    @Query("SELECT a FROM AuditLog a WHERE a.username = :username AND a.action = 'LOGIN_FAILED' " +
           "AND a.createdAt > :since ORDER BY a.createdAt DESC")
    List<AuditLog> findRecentLoginFailures(
        @Param("username") String username, 
        @Param("since") LocalDateTime since
    );
    
    /**
     * 统计指定时间范围内的操作次数
     */
    @Query("SELECT COUNT(a) FROM AuditLog a WHERE a.action = :action AND a.createdAt > :since")
    long countByActionSince(@Param("action") AuditAction action, @Param("since") LocalDateTime since);
    
    /**
     * 查询指定IP的最近操作
     */
    List<AuditLog> findByIpAddressAndCreatedAtAfterOrderByCreatedAtDesc(
        String ipAddress, 
        LocalDateTime since
    );
}
