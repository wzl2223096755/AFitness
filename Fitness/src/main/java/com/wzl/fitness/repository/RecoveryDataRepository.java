package com.wzl.fitness.repository;

import com.wzl.fitness.entity.RecoveryData;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 恢复数据Repository
 */
@Repository
public interface RecoveryDataRepository extends JpaRepository<RecoveryData, Long> {
    
    /**
     * 根据用户ID查找恢复数据
     */
    List<RecoveryData> findByUserId(Long userId);
    
    /**
     * 根据用户ID和时间范围查找恢复数据
     */
    List<RecoveryData> findByUserIdAndTimestampBetween(Long userId, LocalDateTime start, LocalDateTime end);
    
    /**
     * 根据用户ID分页查找恢复数据
     */
    Page<RecoveryData> findByUserId(Long userId, Pageable pageable);
    
    /**
     * 获取用户最近的恢复数据
     */
    @Query("SELECT rd FROM RecoveryData rd WHERE rd.user.id = :userId ORDER BY rd.timestamp DESC")
    List<RecoveryData> findLatestByUserId(@Param("userId") Long userId, Pageable pageable);
    
    /**
     * 计算用户平均恢复评分
     */
    @Query("SELECT AVG(rd.recoveryScore) FROM RecoveryData rd WHERE rd.user.id = :userId AND rd.timestamp BETWEEN :start AND :end")
    Double calculateAverageRecoveryScore(@Param("userId") Long userId, @Param("start") LocalDateTime start, @Param("end") LocalDateTime end);
    
    /**
     * 查找恢复评分低于阈值的记录
     */
    @Query("SELECT rd FROM RecoveryData rd WHERE rd.user.id = :userId AND rd.recoveryScore < :threshold ORDER BY rd.timestamp DESC")
    List<RecoveryData> findLowRecoveryRecords(@Param("userId") Long userId, @Param("threshold") Integer threshold);
}