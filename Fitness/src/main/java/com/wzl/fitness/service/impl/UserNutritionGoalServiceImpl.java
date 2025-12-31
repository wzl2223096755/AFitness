package com.wzl.fitness.service.impl;

import com.wzl.fitness.dto.request.NutritionGoalRequest;
import com.wzl.fitness.dto.response.NutritionRecommendation;
import com.wzl.fitness.entity.User;
import com.wzl.fitness.entity.UserNutritionGoal;
import com.wzl.fitness.repository.UserNutritionGoalRepository;
import com.wzl.fitness.service.NutritionCalculationService;
import com.wzl.fitness.service.UserNutritionGoalService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * 用户营养目标服务实现类
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class UserNutritionGoalServiceImpl implements UserNutritionGoalService {
    
    private final UserNutritionGoalRepository nutritionGoalRepository;
    private final NutritionCalculationService nutritionCalculationService;
    
    @Override
    public Optional<UserNutritionGoal> getUserNutritionGoal(User user) {
        return nutritionGoalRepository.findByUser(user);
    }
    
    @Override
    @Transactional
    public UserNutritionGoal setUserNutritionGoal(User user, NutritionGoalRequest request) {
        log.info("设置用户 {} 的营养目标", user.getId());
        
        UserNutritionGoal goal = nutritionGoalRepository.findByUser(user)
                .orElse(UserNutritionGoal.builder().user(user).build());
        
        // 更新训练目标和活动水平
        if (request.getTrainingGoal() != null) {
            goal.setTrainingGoal(request.getTrainingGoal());
        }
        if (request.getActivityLevel() != null) {
            goal.setActivityLevel(request.getActivityLevel());
        }
        
        // 更新自定义目标
        if (Boolean.TRUE.equals(request.getUseCustomTargets())) {
            goal.setUseCustomTargets(true);
            goal.setTargetCalories(request.getTargetCalories());
            goal.setTargetProtein(request.getTargetProtein());
            goal.setTargetCarbs(request.getTargetCarbs());
            goal.setTargetFat(request.getTargetFat());
        } else {
            goal.setUseCustomTargets(false);
            // 使用计算值
            NutritionRecommendation recommendation = nutritionCalculationService.calculateRecommendedIntake(
                    user, goal.getTrainingGoal(), goal.getActivityLevel());
            goal.setTargetCalories(recommendation.getRecommendedCalories());
            goal.setTargetProtein(recommendation.getRecommendedProtein());
            goal.setTargetCarbs(recommendation.getRecommendedCarbs());
            goal.setTargetFat(recommendation.getRecommendedFat());
        }
        
        return nutritionGoalRepository.save(goal);
    }
    
    @Override
    public NutritionRecommendation getEffectiveRecommendation(User user) {
        Optional<UserNutritionGoal> goalOpt = getUserNutritionGoal(user);
        
        if (goalOpt.isPresent()) {
            UserNutritionGoal goal = goalOpt.get();
            if (Boolean.TRUE.equals(goal.getUseCustomTargets())) {
                // 返回自定义目标
                return NutritionRecommendation.builder()
                        .recommendedCalories(goal.getTargetCalories())
                        .recommendedProtein(goal.getTargetProtein())
                        .recommendedCarbs(goal.getTargetCarbs())
                        .recommendedFat(goal.getTargetFat())
                        .trainingGoal(goal.getTrainingGoal())
                        .activityLevel(goal.getActivityLevel())
                        .bodyWeight(user.getWeight())
                        .build();
            } else {
                // 使用计算值
                return nutritionCalculationService.calculateRecommendedIntake(
                        user, goal.getTrainingGoal(), goal.getActivityLevel());
            }
        }
        
        // 没有设置目标，使用默认计算
        return nutritionCalculationService.calculateRecommendedIntake(user, "maintenance", "moderate");
    }
    
    @Override
    @Transactional
    public void deleteUserNutritionGoal(User user) {
        log.info("删除用户 {} 的营养目标", user.getId());
        nutritionGoalRepository.deleteByUser(user);
    }
}
