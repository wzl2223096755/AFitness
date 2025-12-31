package com.wzl.fitness.service;

import com.wzl.fitness.dto.request.NutritionGoalRequest;
import com.wzl.fitness.dto.response.NutritionRecommendation;
import com.wzl.fitness.entity.User;
import com.wzl.fitness.entity.UserNutritionGoal;

import java.util.Optional;

/**
 * 用户营养目标服务接口
 */
public interface UserNutritionGoalService {
    
    /**
     * 获取用户营养目标
     */
    Optional<UserNutritionGoal> getUserNutritionGoal(User user);
    
    /**
     * 设置或更新用户营养目标
     */
    UserNutritionGoal setUserNutritionGoal(User user, NutritionGoalRequest request);
    
    /**
     * 获取用户的推荐营养摄入（结合用户目标设置）
     */
    NutritionRecommendation getEffectiveRecommendation(User user);
    
    /**
     * 删除用户营养目标
     */
    void deleteUserNutritionGoal(User user);
}
