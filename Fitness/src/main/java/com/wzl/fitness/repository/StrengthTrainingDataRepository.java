package com.wzl.fitness.repository;

import com.wzl.fitness.entity.StrengthTrainingData;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 力量训练数据Repository
 */
@Repository
public interface StrengthTrainingDataRepository extends JpaRepository<StrengthTrainingData, Long> {
    
    /**
     * 根据用户ID查找力量训练数据
     */
    List<StrengthTrainingData> findByUserId(Long userId);
    
    /**
     * 根据用户ID和时间范围查找力量训练数据
     */
    List<StrengthTrainingData> findByUserIdAndTimestampBetween(Long userId, LocalDateTime start, LocalDateTime end);
    
    /**
     * 根据用户ID分页查找力量训练数据
     */
    Page<StrengthTrainingData> findByUserId(Long userId, Pageable pageable);
    
    /**
     * 根据用户ID和动作类型查找数据
     */
    List<StrengthTrainingData> findByUserIdAndExerciseType(Long userId, String exerciseType);
    
    /**
     * 查找用户特定动作的最大重量
     */
    @Query("SELECT MAX(std.weight) FROM StrengthTrainingData std WHERE std.user.id = :userId AND std.exerciseName = :exerciseName")
    Double findMaxWeightByUserAndExercise(@Param("userId") Long userId, @Param("exerciseName") String exerciseName);
    
    /**
     * 计算用户指定时间范围内的训练总量
     */
    @Query("SELECT SUM(std.trainingVolume) FROM StrengthTrainingData std WHERE std.user.id = :userId AND std.timestamp BETWEEN :start AND :end")
    Double calculateTotalVolumeByUserAndDateRange(@Param("userId") Long userId, @Param("start") LocalDateTime start, @Param("end") LocalDateTime end);
    
    /**
     * 获取用户最近的力量训练记录
     */
    @Query("SELECT std FROM StrengthTrainingData std WHERE std.user.id = :userId ORDER BY std.timestamp DESC")
    List<StrengthTrainingData> findLatestByUserId(@Param("userId") Long userId, Pageable pageable);
}