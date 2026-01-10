package com.wzl.fitness.modules.recovery.repository;

import com.wzl.fitness.entity.RecoveryData;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 恢复评估模块数据访问接口
 * 
 * 提供恢复数据的CRUD操作和查询功能
 * 
 * @see Requirements 1.2 - 领域模块包含独立的repository子包
 */
@Repository
public interface RecoveryModuleRepository extends JpaRepository<RecoveryData, Long> {
    
    /**
     * 根据用户ID查找恢复数据
     * 
     * @param userId 用户ID
     * @return 恢复数据列表
     */
    List<RecoveryData> findByUserId(Long userId);
    
    /**
     * 根据用户ID和时间范围查找恢复数据
     * 
     * @param userId 用户ID
     * @param start 开始时间
     * @param end 结束时间
     * @return 恢复数据列表
     */
    List<RecoveryData> findByUserIdAndTimestampBetween(Long userId, LocalDateTime start, LocalDateTime end);
    
    /**
     * 获取用户最新的恢复数据
     * 
     * @param userId 用户ID
     * @return 最新的恢复数据
     */
    Optional<RecoveryData> findTopByUserIdOrderByTimestampDesc(Long userId);
    
    /**
     * 获取用户最近的恢复数据列表
     * 
     * @param userId 用户ID
     * @param pageable 分页参数
     * @return 恢复数据列表
     */
    @Query("SELECT rd FROM RecoveryData rd WHERE rd.user.id = :userId ORDER BY rd.timestamp DESC")
    List<RecoveryData> findLatestByUserId(@Param("userId") Long userId, Pageable pageable);
    
    /**
     * 计算用户平均恢复评分
     * 
     * @param userId 用户ID
     * @param start 开始时间
     * @param end 结束时间
     * @return 平均恢复评分
     */
    @Query("SELECT AVG(rd.recoveryScore) FROM RecoveryData rd WHERE rd.user.id = :userId AND rd.timestamp BETWEEN :start AND :end")
    Double calculateAverageRecoveryScore(@Param("userId") Long userId, @Param("start") LocalDateTime start, @Param("end") LocalDateTime end);
    
    /**
     * 查找恢复评分低于阈值的记录
     * 
     * @param userId 用户ID
     * @param threshold 阈值
     * @return 低恢复评分记录列表
     */
    @Query("SELECT rd FROM RecoveryData rd WHERE rd.user.id = :userId AND rd.recoveryScore < :threshold ORDER BY rd.timestamp DESC")
    List<RecoveryData> findLowRecoveryRecords(@Param("userId") Long userId, @Param("threshold") Integer threshold);
}
