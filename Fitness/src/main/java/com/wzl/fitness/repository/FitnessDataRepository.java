package com.wzl.fitness.repository;

import com.wzl.fitness.entity.FitnessData;
import com.wzl.fitness.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 健身数据仓库接口（已废弃）
 * 
 * @deprecated 此仓库使用已废弃的 {@link FitnessData} 实体。
 * 建议使用以下替代仓库：
 * <ul>
 *   <li>力量训练数据：使用 {@link StrengthTrainingDataRepository}</li>
 *   <li>恢复状态数据：使用 {@link RecoveryDataRepository}</li>
 * </ul>
 * 
 * @see StrengthTrainingDataRepository
 * @see RecoveryDataRepository
 */
@Deprecated(since = "1.1.0", forRemoval = true)
public interface FitnessDataRepository extends JpaRepository<FitnessData, Long> {
    // 基础查询方法
    List<FitnessData> findByUserId(Long userId);
    
    // 修复遍历错误：添加排序和分页查询，避免内存遍历
    List<FitnessData> findByUserIdOrderByTimestampDesc(Long userId);
    
    List<FitnessData> findTop10ByUserIdOrderByTimestampDesc(Long userId);
    
    List<FitnessData> findByUserIdAndTimestampBetween(Long userId, LocalDateTime start, LocalDateTime end);
    
    List<FitnessData> findByUserIdAndTimestamp(Long userId, LocalDateTime timestamp);
    
    FitnessData findTopByUserIdOrderByTimestampDesc(Long userId);
    
    List<FitnessData> findByUser(User user);
    
    List<FitnessData> findByUserOrderByTimestampDesc(User user);
    
    // 数据清理相关查询方法
    @Query("SELECT f FROM FitnessData f WHERE f.timestamp < :cutoffDate")
    List<FitnessData> findOlderThan(@Param("cutoffDate") LocalDateTime cutoffDate);
    
    // 删除了heartRate相关查询，因为FitnessData实体中没有heartRate字段
    
    @Query("SELECT f FROM FitnessData f WHERE f.weight < :weight")
    List<FitnessData> findByWeightLessThan(@Param("weight") Double weight);
    
    @Query("SELECT f FROM FitnessData f WHERE f.sets < :sets")
    List<FitnessData> findBySetsLessThan(@Param("sets") Integer sets);
    
    @Query("SELECT f FROM FitnessData f WHERE f.reps < :reps")
    List<FitnessData> findByRepsLessThan(@Param("reps") Integer reps);
    
    @Query("SELECT f FROM FitnessData f WHERE f.trainingVolume < :trainingVolume")
    List<FitnessData> findByTrainingVolumeLessThan(@Param("trainingVolume") Double trainingVolume);
    
    @Query("SELECT f FROM FitnessData f WHERE f.user IS NULL")
    List<FitnessData> findOrphanedData();
    
    @Query("SELECT COUNT(f) FROM FitnessData f WHERE f.timestamp < :cutoffDate")
    long countOlderThan(@Param("cutoffDate") LocalDateTime cutoffDate);
    
    @Query("SELECT COUNT(f) FROM FitnessData f WHERE f.weight < :weight")
    long countByWeightLessThan(@Param("weight") Double weight);
    
    @Query("SELECT COUNT(f) FROM FitnessData f WHERE f.sets < :sets")
    long countBySetsLessThan(@Param("sets") Integer sets);
    
    @Query("SELECT COUNT(f) FROM FitnessData f WHERE f.reps < :reps")
    long countByRepsLessThan(@Param("reps") Integer reps);
    
    @Query("SELECT COUNT(f) FROM FitnessData f WHERE f.trainingVolume < :trainingVolume")
    long countByTrainingVolumeLessThan(@Param("trainingVolume") Double trainingVolume);
    
    @Query("SELECT COUNT(f) FROM FitnessData f WHERE f.user IS NULL")
    long countOrphanedData();
    
    // 简化版本：查找所有数据，重复检查在服务层处理
    @Query("SELECT f FROM FitnessData f ORDER BY f.user.id, f.timestamp")
    List<FitnessData> findAllForDuplicateCheck();
    
    // 修复：添加根据设备ID查询的方法
    @Query("SELECT f FROM FitnessData f WHERE f.user.id IN " +
           "(SELECT ud.userId FROM UserDevice ud WHERE ud.deviceId = :deviceId)")
    List<FitnessData> findByDeviceId(@Param("deviceId") Long deviceId);
    
    @Query("SELECT f FROM FitnessData f WHERE f.user.id IN " +
           "(SELECT ud.userId FROM UserDevice ud WHERE ud.deviceId = :deviceId) " +
           "AND f.timestamp BETWEEN :start AND :end")
    List<FitnessData> findByDeviceIdAndTimestampBetween(@Param("deviceId") Long deviceId, 
                                                        @Param("start") LocalDateTime start, 
                                                        @Param("end") LocalDateTime end);
}