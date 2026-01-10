package com.wzl.fitness.modules.nutrition.service;

import com.wzl.fitness.entity.NutritionRecord;
import com.wzl.fitness.modules.nutrition.dto.DailyNutritionDTO;
import com.wzl.fitness.modules.nutrition.dto.NutritionGoalDTO;
import com.wzl.fitness.modules.nutrition.dto.NutritionRecordDTO;

import java.time.LocalDate;
import java.util.List;

/**
 * 营养模块服务接口
 * 定义营养模块内部的业务逻辑方法
 * 
 * @see Requirements 1.2 - 领域模块包含独立的service子包
 */
public interface NutritionModuleService {
    
    /**
     * 获取用户每日营养摄入汇总
     * 
     * @param userId 用户ID
     * @param date 日期
     * @return 每日营养摄入DTO
     */
    DailyNutritionDTO getDailyNutrition(Long userId, LocalDate date);
    
    /**
     * 获取用户营养目标
     * 
     * @param userId 用户ID
     * @return 营养目标DTO
     */
    NutritionGoalDTO getGoals(Long userId);
    
    /**
     * 获取用户指定日期的营养记录列表
     * 
     * @param userId 用户ID
     * @param date 日期
     * @return 营养记录DTO列表
     */
    List<NutritionRecordDTO> getRecordsByDate(Long userId, LocalDate date);
    
    /**
     * 获取用户指定日期范围的营养记录列表
     * 
     * @param userId 用户ID
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 营养记录DTO列表
     */
    List<NutritionRecordDTO> getRecordsByDateRange(Long userId, LocalDate startDate, LocalDate endDate);
    
    /**
     * 将营养记录实体转换为DTO
     * 
     * @param record 营养记录实体
     * @return 营养记录DTO
     */
    NutritionRecordDTO convertToDTO(NutritionRecord record);
    
    /**
     * 将营养记录实体列表转换为DTO列表
     * 
     * @param records 营养记录实体列表
     * @return 营养记录DTO列表
     */
    List<NutritionRecordDTO> convertToDTOList(List<NutritionRecord> records);
}
