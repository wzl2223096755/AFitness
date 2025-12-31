package com.wzl.fitness.service;

import com.wzl.fitness.dto.response.NutritionStatsResponse;
import com.wzl.fitness.dto.request.NutritionRecordRequest;
import com.wzl.fitness.dto.response.NutritionRecordDTO;
import com.wzl.fitness.entity.NutritionRecord;
import com.wzl.fitness.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * 营养记录服务接口
 */
public interface NutritionService {
    
    /**
     * 添加营养记录
     */
    NutritionRecord addNutritionRecord(User user, NutritionRecordRequest request);
    
    /**
     * 更新营养记录
     */
    NutritionRecord updateNutritionRecord(Long id, NutritionRecordRequest request);
    
    /**
     * 删除营养记录
     */
    void deleteNutritionRecord(Long id, User user);
    
    /**
     * 获取营养记录
     */
    Optional<NutritionRecord> getNutritionRecord(Long id, User user);
    
    /**
     * 获取用户指定日期的营养记录
     */
    List<NutritionRecordDTO> getNutritionRecordsByDate(User user, LocalDate date);
    
    /**
     * 获取用户指定日期范围的营养记录
     */
    List<NutritionRecordDTO> getNutritionRecordsByDateRange(User user, LocalDate startDate, LocalDate endDate);
    
    /**
     * 获取用户营养记录（分页）
     */
    Page<NutritionRecordDTO> getNutritionRecordsPaginated(User user, Pageable pageable);
    
    /**
     * 获取用户指定日期的营养统计
     */
    NutritionStatsResponse getNutritionStatsByDate(User user, LocalDate date);
    
    /**
     * 获取用户指定日期范围的营养统计
     */
    List<NutritionStatsResponse> getNutritionStatsByDateRange(User user, LocalDate startDate, LocalDate endDate);
    
    /**
     * 获取用户营养建议
     */
    List<String> getNutritionAdvice(User user, LocalDate date);
    
    /**
     * 获取用户最近常吃的食物
     */
    List<String> getMostFrequentFoods(User user, int limit);
    
    /**
     * 计算营养素占比
     */
    void calculateNutritionPercentages(NutritionStatsResponse stats);
    
    /**
     * 验证营养记录数据
     */
    boolean validateNutritionRecord(NutritionRecordRequest request);
    
    /**
     * 导出营养数据
     */
    byte[] exportNutritionData(User user, LocalDate startDate, LocalDate endDate);
}