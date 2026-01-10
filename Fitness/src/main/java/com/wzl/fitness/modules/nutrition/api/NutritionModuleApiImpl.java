package com.wzl.fitness.modules.nutrition.api;

import com.wzl.fitness.modules.nutrition.dto.DailyNutritionDTO;
import com.wzl.fitness.modules.nutrition.dto.NutritionGoalDTO;
import com.wzl.fitness.modules.nutrition.service.NutritionModuleService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

/**
 * 营养模块API实现
 * 实现营养模块对外暴露的服务接口
 * 
 * @see Requirements 2.1 - 为每个领域模块定义独立的服务接口
 * @see Requirements 2.5 - 模块接口使用DTO进行数据传输
 * @see Requirements 6.2 - 支持通过配置文件启用/禁用模块
 */
@Component
@RequiredArgsConstructor
@Slf4j
@ConditionalOnProperty(
    prefix = "fitness.modules.nutrition",
    name = "enabled",
    havingValue = "true",
    matchIfMissing = true
)
public class NutritionModuleApiImpl implements NutritionModuleApi {
    
    private final NutritionModuleService nutritionModuleService;
    
    @Override
    public DailyNutritionDTO getDailyNutrition(Long userId, LocalDate date) {
        log.debug("NutritionModuleApi: 获取用户 {} 在 {} 的每日营养摄入", userId, date);
        
        if (userId == null) {
            throw new IllegalArgumentException("用户ID不能为空");
        }
        if (date == null) {
            date = LocalDate.now();
        }
        
        return nutritionModuleService.getDailyNutrition(userId, date);
    }
    
    @Override
    public NutritionGoalDTO getGoals(Long userId) {
        log.debug("NutritionModuleApi: 获取用户 {} 的营养目标", userId);
        
        if (userId == null) {
            throw new IllegalArgumentException("用户ID不能为空");
        }
        
        return nutritionModuleService.getGoals(userId);
    }
}
