package com.wzl.fitness.repository;

import com.wzl.fitness.entity.TrainingRecord;
import com.wzl.fitness.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.data.repository.query.Param;
import jakarta.persistence.QueryHint;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 训练记录数据访问层
 * 
 * <p>性能优化说明：</p>
 * <ul>
 *   <li>使用 @QueryHints 优化只读查询，减少内存占用</li>
 *   <li>复合索引：(user_id, training_date) 用于日期范围查询</li>
 *   <li>软删除过滤：通过 @SQLRestriction 自动过滤已删除记录</li>
 * </ul>
 */
public interface TrainingRecordRepository extends JpaRepository<TrainingRecord, Long> {
    // 基于User实体的查询方法
    List<TrainingRecord> findByUser(User user);
    Page<TrainingRecord> findByUser(User user, Pageable pageable);
    List<TrainingRecord> findByUserOrderByTrainingDateDesc(User user);
    Page<TrainingRecord> findByUserOrderByTrainingDateDesc(User user, Pageable pageable);
    List<TrainingRecord> findTop10ByUserOrderByTrainingDateDesc(User user);
    List<TrainingRecord> findByUserAndTrainingDateBetween(User user, LocalDate startDate, LocalDate endDate);
    List<TrainingRecord> findByUserAndTrainingDateBetweenOrderByTrainingDateDesc(User user, LocalDate startDate, LocalDate endDate);
    TrainingRecord findFirstByUserOrderByTrainingDateDesc(User user);
    List<TrainingRecord> findByUserAndTrainingDate(User user, LocalDate date);
    void deleteByUserAndId(User user, Long id);
    long countByUser(User user);
    
    // 基于userId的查询方法（为了向后兼容）
    @Query("SELECT tr FROM TrainingRecord tr WHERE tr.user.id = :userId")
    @QueryHints(@QueryHint(name = "org.hibernate.readOnly", value = "true"))
    List<TrainingRecord> findByUserId(@Param("userId") Long userId);
    
    @Query("SELECT tr FROM TrainingRecord tr WHERE tr.user.id = :userId ORDER BY tr.trainingDate DESC")
    @QueryHints(@QueryHint(name = "org.hibernate.readOnly", value = "true"))
    List<TrainingRecord> findByUserIdOrderByTrainingDateDesc(@Param("userId") Long userId);
    
    @Query("SELECT tr FROM TrainingRecord tr WHERE tr.user.id = :userId ORDER BY tr.trainingDate DESC")
    @QueryHints(@QueryHint(name = "org.hibernate.readOnly", value = "true"))
    Page<TrainingRecord> findByUserIdOrderByTrainingDateDesc(@Param("userId") Long userId, Pageable pageable);
    
    @Query("SELECT tr FROM TrainingRecord tr WHERE tr.user.id = :userId ORDER BY tr.trainingDate DESC")
    @QueryHints(@QueryHint(name = "org.hibernate.readOnly", value = "true"))
    List<TrainingRecord> findTop10ByUserIdOrderByTrainingDateDesc(@Param("userId") Long userId);
    
    @Query("SELECT tr FROM TrainingRecord tr WHERE tr.user.id = :userId AND tr.trainingDate BETWEEN :startDate AND :endDate")
    @QueryHints(@QueryHint(name = "org.hibernate.readOnly", value = "true"))
    List<TrainingRecord> findByUserIdAndTrainingDateBetween(@Param("userId") Long userId, @Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);
    
    @Query("SELECT tr FROM TrainingRecord tr WHERE tr.user.id = :userId AND tr.trainingDate BETWEEN :startDate AND :endDate ORDER BY tr.trainingDate DESC")
    @QueryHints(@QueryHint(name = "org.hibernate.readOnly", value = "true"))
    List<TrainingRecord> findByUserIdAndTrainingDateBetweenOrderByTrainingDateDesc(@Param("userId") Long userId, @Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);
    
    @Query("SELECT tr FROM TrainingRecord tr WHERE tr.user.id = :userId ORDER BY tr.trainingDate DESC")
    @QueryHints(@QueryHint(name = "org.hibernate.readOnly", value = "true"))
    TrainingRecord findFirstByUserIdOrderByTrainingDateDesc(@Param("userId") Long userId);
    
    @Query("SELECT COUNT(tr) FROM TrainingRecord tr WHERE tr.user.id = :userId")
    @QueryHints(@QueryHint(name = "org.hibernate.readOnly", value = "true"))
    long countByUserId(@Param("userId") Long userId);
    
    // 统计方法
    @Query("SELECT COUNT(tr) FROM TrainingRecord tr WHERE tr.user.id = :userId AND tr.trainingDate BETWEEN :startDate AND :endDate")
    @QueryHints(@QueryHint(name = "org.hibernate.readOnly", value = "true"))
    long countByUserIdAndTrainingDateBetween(@Param("userId") Long userId, @Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);
    
    @Query("SELECT SUM(tr.sets * tr.reps * tr.weight) FROM TrainingRecord tr WHERE tr.user.id = :userId AND tr.trainingDate BETWEEN :startDate AND :endDate")
    @QueryHints(@QueryHint(name = "org.hibernate.readOnly", value = "true"))
    Double sumVolumeByUserIdAndDateRange(@Param("userId") Long userId, @Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);
    
    @Query("SELECT SUM(tr.duration) FROM TrainingRecord tr WHERE tr.user.id = :userId AND tr.trainingDate BETWEEN :startDate AND :endDate")
    @QueryHints(@QueryHint(name = "org.hibernate.readOnly", value = "true"))
    Long sumDurationByUserIdAndDateRange(@Param("userId") Long userId, @Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);
    
    // ==================== 软删除相关方法 ====================
    
    /**
     * 查找已删除的训练记录（用于数据恢复）
     * 注意：此查询绕过 @SQLRestriction 过滤器
     */
    @Query(value = "SELECT * FROM training_records WHERE user_id = :userId AND deleted = true ORDER BY deleted_at DESC", nativeQuery = true)
    List<TrainingRecord> findDeletedByUserId(@Param("userId") Long userId);
    
    /**
     * 查找指定ID的记录（包括已删除的）
     * 用于数据恢复场景
     */
    @Query(value = "SELECT * FROM training_records WHERE id = :id", nativeQuery = true)
    Optional<TrainingRecord> findByIdIncludingDeleted(@Param("id") Long id);
    
    /**
     * 执行软删除
     */
    @Modifying
    @Query("UPDATE TrainingRecord tr SET tr.deleted = true, tr.deletedAt = :deletedAt, tr.deletedBy = :deletedBy WHERE tr.id = :id")
    int softDelete(@Param("id") Long id, @Param("deletedAt") LocalDateTime deletedAt, @Param("deletedBy") Long deletedBy);
    
    /**
     * 恢复已删除的记录
     */
    @Modifying
    @Query(value = "UPDATE training_records SET deleted = false, deleted_at = NULL, deleted_by = NULL WHERE id = :id", nativeQuery = true)
    int restore(@Param("id") Long id);
    
    /**
     * 批量恢复用户的已删除记录
     */
    @Modifying
    @Query(value = "UPDATE training_records SET deleted = false, deleted_at = NULL, deleted_by = NULL WHERE user_id = :userId AND deleted = true", nativeQuery = true)
    int restoreAllByUserId(@Param("userId") Long userId);
    
    /**
     * 永久删除已软删除的记录（清理操作）
     */
    @Modifying
    @Query(value = "DELETE FROM training_records WHERE deleted = true AND deleted_at < :cutoffDate", nativeQuery = true)
    int permanentlyDeleteOldRecords(@Param("cutoffDate") LocalDateTime cutoffDate);
    
    /**
     * 统计用户已删除的记录数
     */
    @Query(value = "SELECT COUNT(*) FROM training_records WHERE user_id = :userId AND deleted = true", nativeQuery = true)
    long countDeletedByUserId(@Param("userId") Long userId);
}