package com.wzl.fitness.modules.training.repository;

import com.wzl.fitness.entity.TrainingRecord;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import jakarta.persistence.QueryHint;
import java.time.LocalDate;
import java.util.List;

/**
 * 训练模块仓储接口
 * 提供训练模块所需的数据访问方法
 * 
 * @see Requirements 1.2 - 领域模块包含独立的repository子包
 */
@Repository
public interface TrainingModuleRepository extends JpaRepository<TrainingRecord, Long> {
    
    /**
     * 获取用户最近的训练记录
     */
    @Query("SELECT tr FROM TrainingRecord tr WHERE tr.user.id = :userId ORDER BY tr.trainingDate DESC")
    @QueryHints(@QueryHint(name = "org.hibernate.readOnly", value = "true"))
    List<TrainingRecord> findRecentByUserId(@Param("userId") Long userId, Pageable pageable);
    
    /**
     * 获取用户指定日期范围内的训练记录
     */
    @Query("SELECT tr FROM TrainingRecord tr WHERE tr.user.id = :userId AND tr.trainingDate BETWEEN :startDate AND :endDate ORDER BY tr.trainingDate DESC")
    @QueryHints(@QueryHint(name = "org.hibernate.readOnly", value = "true"))
    List<TrainingRecord> findByUserIdAndDateRange(
            @Param("userId") Long userId, 
            @Param("startDate") LocalDate startDate, 
            @Param("endDate") LocalDate endDate);
    
    /**
     * 统计用户指定日期范围内的训练记录数
     */
    @Query("SELECT COUNT(tr) FROM TrainingRecord tr WHERE tr.user.id = :userId AND tr.trainingDate BETWEEN :startDate AND :endDate")
    @QueryHints(@QueryHint(name = "org.hibernate.readOnly", value = "true"))
    long countByUserIdAndDateRange(
            @Param("userId") Long userId, 
            @Param("startDate") LocalDate startDate, 
            @Param("endDate") LocalDate endDate);
    
    /**
     * 计算用户指定日期范围内的总训练量
     */
    @Query("SELECT COALESCE(SUM(tr.sets * tr.reps * tr.weight), 0) FROM TrainingRecord tr WHERE tr.user.id = :userId AND tr.trainingDate BETWEEN :startDate AND :endDate")
    @QueryHints(@QueryHint(name = "org.hibernate.readOnly", value = "true"))
    Double sumVolumeByUserIdAndDateRange(
            @Param("userId") Long userId, 
            @Param("startDate") LocalDate startDate, 
            @Param("endDate") LocalDate endDate);
    
    /**
     * 计算用户指定日期范围内的总训练时长
     */
    @Query("SELECT COALESCE(SUM(tr.duration), 0) FROM TrainingRecord tr WHERE tr.user.id = :userId AND tr.trainingDate BETWEEN :startDate AND :endDate")
    @QueryHints(@QueryHint(name = "org.hibernate.readOnly", value = "true"))
    Long sumDurationByUserIdAndDateRange(
            @Param("userId") Long userId, 
            @Param("startDate") LocalDate startDate, 
            @Param("endDate") LocalDate endDate);
    
    /**
     * 统计用户的总训练记录数
     */
    @Query("SELECT COUNT(tr) FROM TrainingRecord tr WHERE tr.user.id = :userId")
    @QueryHints(@QueryHint(name = "org.hibernate.readOnly", value = "true"))
    long countByUserId(@Param("userId") Long userId);
    
    /**
     * 计算用户的总训练量
     */
    @Query("SELECT COALESCE(SUM(tr.sets * tr.reps * tr.weight), 0) FROM TrainingRecord tr WHERE tr.user.id = :userId")
    @QueryHints(@QueryHint(name = "org.hibernate.readOnly", value = "true"))
    Double sumVolumeByUserId(@Param("userId") Long userId);
    
    /**
     * 计算用户的总训练时长
     */
    @Query("SELECT COALESCE(SUM(tr.duration), 0) FROM TrainingRecord tr WHERE tr.user.id = :userId")
    @QueryHints(@QueryHint(name = "org.hibernate.readOnly", value = "true"))
    Long sumDurationByUserId(@Param("userId") Long userId);
    
    /**
     * 获取用户最常训练的动作
     */
    @Query("SELECT tr.exerciseName FROM TrainingRecord tr WHERE tr.user.id = :userId GROUP BY tr.exerciseName ORDER BY COUNT(tr) DESC")
    @QueryHints(@QueryHint(name = "org.hibernate.readOnly", value = "true"))
    List<String> findMostFrequentExerciseByUserId(@Param("userId") Long userId, Pageable pageable);
    
    /**
     * 获取用户的最大重量
     */
    @Query("SELECT COALESCE(MAX(tr.weight), 0) FROM TrainingRecord tr WHERE tr.user.id = :userId")
    @QueryHints(@QueryHint(name = "org.hibernate.readOnly", value = "true"))
    Double findMaxWeightByUserId(@Param("userId") Long userId);
    
    /**
     * 获取用户指定日期范围内的训练天数
     */
    @Query("SELECT COUNT(DISTINCT tr.trainingDate) FROM TrainingRecord tr WHERE tr.user.id = :userId AND tr.trainingDate BETWEEN :startDate AND :endDate")
    @QueryHints(@QueryHint(name = "org.hibernate.readOnly", value = "true"))
    int countDistinctTrainingDaysByUserIdAndDateRange(
            @Param("userId") Long userId, 
            @Param("startDate") LocalDate startDate, 
            @Param("endDate") LocalDate endDate);
}
