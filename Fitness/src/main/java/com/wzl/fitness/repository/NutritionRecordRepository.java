package com.wzl.fitness.repository;

import com.wzl.fitness.entity.NutritionRecord;
import com.wzl.fitness.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 营养记录数据访问接口
 * 支持软删除功能
 */
@Repository
public interface NutritionRecordRepository extends JpaRepository<NutritionRecord, Long> {
    
    /**
     * 根据用户和日期查找营养记录
     */
    List<NutritionRecord> findByUserAndRecordDateOrderByCreatedAt(User user, LocalDate recordDate);
    
    /**
     * 根据用户和日期查找营养记录，按餐次排序
     */
    List<NutritionRecord> findByUserAndRecordDateOrderByMealTypeAsc(User user, LocalDate recordDate);
    
    /**
     * 根据用户、日期和餐次查找营养记录
     */
    List<NutritionRecord> findByUserAndRecordDateAndMealTypeOrderByCreatedAt(User user, LocalDate recordDate, String mealType);
    
    /**
     * 根据用户、日期和餐次查找营养记录
     */
    List<NutritionRecord> findByUserAndRecordDateAndMealType(User user, LocalDate recordDate, String mealType);
    
    /**
     * 根据用户和日期范围查找营养记录
     */
    List<NutritionRecord> findByUserAndRecordDateBetweenOrderByRecordDateAsc(User user, LocalDate startDate, LocalDate endDate);
    
    /**
     * 根据用户和日期范围查找营养记录，按日期和餐次排序
     */
    List<NutritionRecord> findByUserAndRecordDateBetweenOrderByRecordDateAscMealTypeAsc(User user, LocalDate startDate, LocalDate endDate);
    
    /**
     * 根据用户查找营养记录，按日期降序排序
     */
    Page<NutritionRecord> findByUserOrderByRecordDateDesc(User user, Pageable pageable);
    
    /**
     * 根据ID和用户查找营养记录
     */
    Optional<NutritionRecord> findByIdAndUser(Long id, User user);
    
    /**
     * 根据用户、日期范围和餐次查找营养记录
     */
    List<NutritionRecord> findByUserAndRecordDateBetweenAndMealTypeOrderByRecordDateAsc(User user, LocalDate startDate, LocalDate endDate, String mealType);
    
    /**
     * 根据用户和食物名称查找营养记录
     */
    Page<NutritionRecord> findByUserAndFoodNameContainingIgnoreCase(User user, String foodName, Pageable pageable);
    
    /**
     * 获取用户指定日期的营养摄入统计
     */
    @Query("SELECT SUM(nr.calories) as totalCalories, SUM(nr.protein) as totalProtein, " +
           "SUM(nr.carbs) as totalCarbs, SUM(nr.fat) as totalFat " +
           "FROM NutritionRecord nr WHERE nr.user = :user AND nr.recordDate = :date")
    Object[] getNutritionStatsByDate(@Param("user") User user, @Param("date") LocalDate date);
    
    /**
     * 获取用户指定日期范围的营养摄入统计
     */
    @Query("SELECT nr.recordDate, SUM(nr.calories) as totalCalories, SUM(nr.protein) as totalProtein, " +
           "SUM(nr.carbs) as totalCarbs, SUM(nr.fat) as totalFat " +
           "FROM NutritionRecord nr WHERE nr.user = :user AND nr.recordDate BETWEEN :startDate AND :endDate " +
           "GROUP BY nr.recordDate ORDER BY nr.recordDate ASC")
    List<Object[]> getNutritionStatsByDateRange(@Param("user") User user, 
                                               @Param("startDate") LocalDate startDate, 
                                               @Param("endDate") LocalDate endDate);
    
    /**
     * 获取用户最近常吃的食物
     */
    @Query("SELECT nr.foodName, COUNT(nr) as frequency " +
           "FROM NutritionRecord nr WHERE nr.user = :user " +
           "GROUP BY nr.foodName ORDER BY frequency DESC")
    List<Object[]> getMostFrequentFoods(@Param("user") User user, Pageable pageable);
    
    /**
     * 检查用户是否已记录指定日期的饮食
     */
    boolean existsByUserAndRecordDate(User user, LocalDate date);
    
    /**
     * 获取用户营养记录总数
     */
    long countByUser(User user);
    
    /**
     * 根据用户和日期删除营养记录
     */
    void deleteByUserAndRecordDate(User user, LocalDate date);
    
    /**
     * 根据用户、日期和餐次获取营养记录
     */
    @Query("SELECT nr FROM NutritionRecord nr WHERE nr.user = :user AND nr.recordDate = :date AND nr.mealType = :mealType")
    List<NutritionRecord> findByUserAndDateAndMealType(@Param("user") User user, 
                                                       @Param("date") LocalDate date, 
                                                       @Param("mealType") String mealType);
    
    // ==================== 软删除相关方法 ====================
    
    /**
     * 查找用户已删除的营养记录（用于数据恢复）
     */
    @Query(value = "SELECT * FROM nutrition_records WHERE user_id = :userId AND deleted = true ORDER BY deleted_at DESC", nativeQuery = true)
    List<NutritionRecord> findDeletedByUserId(@Param("userId") Long userId);
    
    /**
     * 查找指定ID的记录（包括已删除的）
     */
    @Query(value = "SELECT * FROM nutrition_records WHERE id = :id", nativeQuery = true)
    Optional<NutritionRecord> findByIdIncludingDeleted(@Param("id") Long id);
    
    /**
     * 执行软删除
     */
    @Modifying
    @Query("UPDATE NutritionRecord nr SET nr.deleted = true, nr.deletedAt = :deletedAt, nr.deletedBy = :deletedBy WHERE nr.id = :id")
    int softDelete(@Param("id") Long id, @Param("deletedAt") LocalDateTime deletedAt, @Param("deletedBy") Long deletedBy);
    
    /**
     * 恢复已删除的记录
     */
    @Modifying
    @Query(value = "UPDATE nutrition_records SET deleted = false, deleted_at = NULL, deleted_by = NULL WHERE id = :id", nativeQuery = true)
    int restore(@Param("id") Long id);
    
    /**
     * 批量恢复用户的已删除记录
     */
    @Modifying
    @Query(value = "UPDATE nutrition_records SET deleted = false, deleted_at = NULL, deleted_by = NULL WHERE user_id = :userId AND deleted = true", nativeQuery = true)
    int restoreAllByUserId(@Param("userId") Long userId);
    
    /**
     * 永久删除已软删除的记录（清理操作）
     */
    @Modifying
    @Query(value = "DELETE FROM nutrition_records WHERE deleted = true AND deleted_at < :cutoffDate", nativeQuery = true)
    int permanentlyDeleteOldRecords(@Param("cutoffDate") LocalDateTime cutoffDate);
    
    /**
     * 统计用户已删除的记录数
     */
    @Query(value = "SELECT COUNT(*) FROM nutrition_records WHERE user_id = :userId AND deleted = true", nativeQuery = true)
    long countDeletedByUserId(@Param("userId") Long userId);
}