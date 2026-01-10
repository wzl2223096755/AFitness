package com.wzl.fitness.modules.nutrition.repository;

import com.wzl.fitness.entity.NutritionRecord;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

/**
 * 营养模块数据访问接口
 * 提供营养模块专用的数据库查询方法
 * 
 * @see Requirements 1.2 - 领域模块包含独立的repository子包
 */
@Repository
public interface NutritionModuleRepository extends JpaRepository<NutritionRecord, Long> {
    
    /**
     * 根据用户ID和日期查找营养记录
     */
    @Query("SELECT nr FROM NutritionRecord nr WHERE nr.user.id = :userId AND nr.recordDate = :date ORDER BY nr.createdAt")
    List<NutritionRecord> findByUserIdAndDate(@Param("userId") Long userId, @Param("date") LocalDate date);
    
    /**
     * 根据用户ID和日期范围查找营养记录
     */
    @Query("SELECT nr FROM NutritionRecord nr WHERE nr.user.id = :userId AND nr.recordDate BETWEEN :startDate AND :endDate ORDER BY nr.recordDate ASC")
    List<NutritionRecord> findByUserIdAndDateRange(@Param("userId") Long userId, 
                                                    @Param("startDate") LocalDate startDate, 
                                                    @Param("endDate") LocalDate endDate);
    
    /**
     * 获取用户指定日期的总卡路里
     */
    @Query("SELECT COALESCE(SUM(nr.calories), 0) FROM NutritionRecord nr WHERE nr.user.id = :userId AND nr.recordDate = :date")
    Double sumCaloriesByUserIdAndDate(@Param("userId") Long userId, @Param("date") LocalDate date);
    
    /**
     * 获取用户指定日期的总蛋白质
     */
    @Query("SELECT COALESCE(SUM(nr.protein), 0) FROM NutritionRecord nr WHERE nr.user.id = :userId AND nr.recordDate = :date")
    Double sumProteinByUserIdAndDate(@Param("userId") Long userId, @Param("date") LocalDate date);
    
    /**
     * 获取用户指定日期的总碳水化合物
     */
    @Query("SELECT COALESCE(SUM(nr.carbs), 0) FROM NutritionRecord nr WHERE nr.user.id = :userId AND nr.recordDate = :date")
    Double sumCarbsByUserIdAndDate(@Param("userId") Long userId, @Param("date") LocalDate date);
    
    /**
     * 获取用户指定日期的总脂肪
     */
    @Query("SELECT COALESCE(SUM(nr.fat), 0) FROM NutritionRecord nr WHERE nr.user.id = :userId AND nr.recordDate = :date")
    Double sumFatByUserIdAndDate(@Param("userId") Long userId, @Param("date") LocalDate date);
    
    /**
     * 获取用户指定日期的总纤维
     */
    @Query("SELECT COALESCE(SUM(nr.fiber), 0) FROM NutritionRecord nr WHERE nr.user.id = :userId AND nr.recordDate = :date")
    Double sumFiberByUserIdAndDate(@Param("userId") Long userId, @Param("date") LocalDate date);
    
    /**
     * 获取用户指定日期的总糖分
     */
    @Query("SELECT COALESCE(SUM(nr.sugar), 0) FROM NutritionRecord nr WHERE nr.user.id = :userId AND nr.recordDate = :date")
    Double sumSugarByUserIdAndDate(@Param("userId") Long userId, @Param("date") LocalDate date);
    
    /**
     * 获取用户指定日期的总钠
     */
    @Query("SELECT COALESCE(SUM(nr.sodium), 0) FROM NutritionRecord nr WHERE nr.user.id = :userId AND nr.recordDate = :date")
    Double sumSodiumByUserIdAndDate(@Param("userId") Long userId, @Param("date") LocalDate date);
    
    /**
     * 获取用户指定日期的餐次数量
     */
    @Query("SELECT COUNT(DISTINCT nr.mealType) FROM NutritionRecord nr WHERE nr.user.id = :userId AND nr.recordDate = :date")
    Integer countMealsByUserIdAndDate(@Param("userId") Long userId, @Param("date") LocalDate date);
    
    /**
     * 获取用户指定日期的记录数量
     */
    @Query("SELECT COUNT(nr) FROM NutritionRecord nr WHERE nr.user.id = :userId AND nr.recordDate = :date")
    Integer countRecordsByUserIdAndDate(@Param("userId") Long userId, @Param("date") LocalDate date);
    
    /**
     * 获取用户最常吃的食物
     */
    @Query("SELECT nr.foodName, COUNT(nr) as frequency FROM NutritionRecord nr WHERE nr.user.id = :userId GROUP BY nr.foodName ORDER BY frequency DESC")
    List<Object[]> findMostFrequentFoodsByUserId(@Param("userId") Long userId, Pageable pageable);
    
    /**
     * 获取用户营养记录总数
     */
    @Query("SELECT COUNT(nr) FROM NutritionRecord nr WHERE nr.user.id = :userId")
    long countByUserId(@Param("userId") Long userId);
}
