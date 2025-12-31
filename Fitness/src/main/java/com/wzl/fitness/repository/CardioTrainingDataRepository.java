package com.wzl.fitness.repository;

import com.wzl.fitness.entity.CardioTrainingData;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 有氧训练数据Repository
 */
@Repository
public interface CardioTrainingDataRepository extends JpaRepository<CardioTrainingData, Long> {
    
    /**
     * 根据用户ID查找有氧训练数据
     */
    List<CardioTrainingData> findByUserId(Long userId);
    
    /**
     * 根据用户ID和时间范围查找有氧训练数据
     */
    List<CardioTrainingData> findByUserIdAndTimestampBetween(Long userId, LocalDateTime start, LocalDateTime end);
    
    /**
     * 根据用户ID分页查找有氧训练数据
     */
    Page<CardioTrainingData> findByUserId(Long userId, Pageable pageable);
    
    /**
     * 根据用户ID和运动类型查找数据
     */
    List<CardioTrainingData> findByUserIdAndExerciseType(Long userId, String exerciseType);
    
    /**
     * 计算用户指定时间范围内的总运动时长
     */
    @Query("SELECT SUM(ctd.duration) FROM CardioTrainingData ctd WHERE ctd.user.id = :userId AND ctd.timestamp BETWEEN :start AND :end")
    Integer calculateTotalDurationByUserAndDateRange(@Param("userId") Long userId, @Param("start") LocalDateTime start, @Param("end") LocalDateTime end);
    
    /**
     * 计算用户指定时间范围内的总消耗卡路里
     */
    @Query("SELECT SUM(ctd.caloriesBurned) FROM CardioTrainingData ctd WHERE ctd.user.id = :userId AND ctd.timestamp BETWEEN :start AND :end")
    Double calculateTotalCaloriesByUserAndDateRange(@Param("userId") Long userId, @Param("start") LocalDateTime start, @Param("end") LocalDateTime end);
    
    /**
     * 获取用户最近的有氧训练记录
     */
    @Query("SELECT ctd FROM CardioTrainingData ctd WHERE ctd.user.id = :userId ORDER BY ctd.timestamp DESC")
    List<CardioTrainingData> findLatestByUserId(@Param("userId") Long userId, Pageable pageable);
}