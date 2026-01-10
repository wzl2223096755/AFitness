package com.wzl.fitness.modules.nutrition.api;

import com.wzl.fitness.modules.nutrition.dto.DailyNutritionDTO;
import com.wzl.fitness.modules.nutrition.dto.NutritionGoalDTO;

import java.time.LocalDate;

/**
 * 营养模块对外接口
 * 定义营养模块对其他模块暴露的服务契约
 * 
 * @see Requirements 2.1 - 为每个领域模块定义独立的服务接口
 */
public interface NutritionModuleApi {
    
    /**
     * 获取用户每日营养摄入
     * 
     * @param userId 用户ID
     * @param date 日期
     * @return 每日营养摄入DTO
     */
    DailyNutritionDTO getDailyNutrition(Long userId, LocalDate date);
    
    /**
     * 获取营养目标
     * 
     * @param userId 用户ID
     * @return 营养目标DTO
     */
    NutritionGoalDTO getGoals(Long userId);
}
