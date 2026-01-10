package com.wzl.fitness.modules.nutrition.service.impl;

import com.wzl.fitness.entity.NutritionRecord;
import com.wzl.fitness.entity.UserNutritionGoal;
import com.wzl.fitness.modules.nutrition.dto.DailyNutritionDTO;
import com.wzl.fitness.modules.nutrition.dto.NutritionGoalDTO;
import com.wzl.fitness.modules.nutrition.dto.NutritionRecordDTO;
import com.wzl.fitness.modules.nutrition.repository.NutritionModuleRepository;
import com.wzl.fitness.modules.nutrition.service.NutritionModuleService;
import com.wzl.fitness.repository.UserNutritionGoalRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 营养模块服务实现
 * 实现营养模块内部的业务逻辑
 * 
 * @see Requirements 1.2 - 领域模块包含独立的service子包
 * @see Requirements 6.2 - 支持通过配置文件启用/禁用模块
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
@ConditionalOnProperty(
    prefix = "fitness.modules.nutrition",
    name = "enabled",
    havingValue = "true",
    matchIfMissing = true
)
public class NutritionModuleServiceImpl implements NutritionModuleService {
    
    private final NutritionModuleRepository nutritionModuleRepository;
    private final UserNutritionGoalRepository userNutritionGoalRepository;
    
    @Override
    @Cacheable(value = "dailyNutrition", key = "'daily:' + #userId + ':' + #date")
    public DailyNutritionDTO getDailyNutrition(Long userId, LocalDate date) {
        log.debug("获取用户 {} 在 {} 的每日营养摄入", userId, date);
        
        // 获取当日营养记录
        List<NutritionRecord> records = nutritionModuleRepository.findByUserIdAndDate(userId, date);
        List<NutritionRecordDTO> recordDTOs = convertToDTOList(records);
        
        // 计算营养汇总
        Double totalCalories = nutritionModuleRepository.sumCaloriesByUserIdAndDate(userId, date);
        Double totalProtein = nutritionModuleRepository.sumProteinByUserIdAndDate(userId, date);
        Double totalCarbs = nutritionModuleRepository.sumCarbsByUserIdAndDate(userId, date);
        Double totalFat = nutritionModuleRepository.sumFatByUserIdAndDate(userId, date);
        Double totalFiber = nutritionModuleRepository.sumFiberByUserIdAndDate(userId, date);
        Double totalSugar = nutritionModuleRepository.sumSugarByUserIdAndDate(userId, date);
        Double totalSodium = nutritionModuleRepository.sumSodiumByUserIdAndDate(userId, date);
        Integer mealCount = nutritionModuleRepository.countMealsByUserIdAndDate(userId, date);
        
        return DailyNutritionDTO.builder()
                .userId(userId)
                .date(date)
                .totalCalories(totalCalories != null ? totalCalories : 0.0)
                .totalProtein(totalProtein != null ? totalProtein : 0.0)
                .totalCarbs(totalCarbs != null ? totalCarbs : 0.0)
                .totalFat(totalFat != null ? totalFat : 0.0)
                .totalFiber(totalFiber != null ? totalFiber : 0.0)
                .totalSugar(totalSugar != null ? totalSugar : 0.0)
                .totalSodium(totalSodium != null ? totalSodium : 0.0)
                .mealCount(mealCount != null ? mealCount : 0)
                .records(recordDTOs)
                .build();
    }
    
    @Override
    @Cacheable(value = "nutritionGoals", key = "'goals:' + #userId")
    public NutritionGoalDTO getGoals(Long userId) {
        log.debug("获取用户 {} 的营养目标", userId);
        
        return userNutritionGoalRepository.findByUserId(userId)
                .map(this::convertGoalToDTO)
                .orElse(getDefaultGoals(userId));
    }
    
    @Override
    public List<NutritionRecordDTO> getRecordsByDate(Long userId, LocalDate date) {
        log.debug("获取用户 {} 在 {} 的营养记录", userId, date);
        List<NutritionRecord> records = nutritionModuleRepository.findByUserIdAndDate(userId, date);
        return convertToDTOList(records);
    }
    
    @Override
    public List<NutritionRecordDTO> getRecordsByDateRange(Long userId, LocalDate startDate, LocalDate endDate) {
        log.debug("获取用户 {} 从 {} 到 {} 的营养记录", userId, startDate, endDate);
        List<NutritionRecord> records = nutritionModuleRepository.findByUserIdAndDateRange(userId, startDate, endDate);
        return convertToDTOList(records);
    }
    
    @Override
    public NutritionRecordDTO convertToDTO(NutritionRecord record) {
        if (record == null) {
            return null;
        }
        
        return NutritionRecordDTO.builder()
                .id(record.getId())
                .userId(record.getUser() != null ? record.getUser().getId() : null)
                .recordDate(record.getRecordDate())
                .mealType(record.getMealType())
                .foodName(record.getFoodName())
                .calories(record.getCalories())
                .protein(record.getProtein())
                .carbs(record.getCarbs())
                .fat(record.getFat())
                .fiber(record.getFiber())
                .sugar(record.getSugar())
                .sodium(record.getSodium())
                .amount(record.getAmount())
                .notes(record.getNotes())
                .createdAt(record.getCreatedAt())
                .build();
    }
    
    @Override
    public List<NutritionRecordDTO> convertToDTOList(List<NutritionRecord> records) {
        if (records == null) {
            return List.of();
        }
        return records.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    /**
     * 将营养目标实体转换为DTO
     */
    private NutritionGoalDTO convertGoalToDTO(UserNutritionGoal goal) {
        return NutritionGoalDTO.builder()
                .id(goal.getId())
                .userId(goal.getUser() != null ? goal.getUser().getId() : null)
                .targetCalories(goal.getTargetCalories())
                .targetProtein(goal.getTargetProtein())
                .targetCarbs(goal.getTargetCarbs())
                .targetFat(goal.getTargetFat())
                .goalType(goal.getTrainingGoal())
                .createdAt(goal.getCreatedAt())
                .updatedAt(goal.getUpdatedAt())
                .build();
    }
    
    /**
     * 获取默认营养目标
     */
    private NutritionGoalDTO getDefaultGoals(Long userId) {
        return NutritionGoalDTO.builder()
                .userId(userId)
                .targetCalories(2000.0)
                .targetProtein(50.0)
                .targetCarbs(250.0)
                .targetFat(65.0)
                .targetFiber(25.0)
                .targetSugar(50.0)
                .targetSodium(2300.0)
                .goalType("maintenance")
                .build();
    }
}
