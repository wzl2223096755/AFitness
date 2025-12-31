package com.wzl.fitness.service;

import com.wzl.fitness.dto.request.TrainingPlanRequestDTO;
import com.wzl.fitness.entity.TrainingPlan;
import com.wzl.fitness.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface TrainingPlanService {
    TrainingPlan createPlan(TrainingPlan plan);
    TrainingPlan createPlanFromDto(TrainingPlanRequestDTO dto, User user);
    TrainingPlan updatePlan(Long id, TrainingPlan plan);
    void deletePlan(Long id);
    Optional<TrainingPlan> getPlanById(Long id);
    List<TrainingPlan> getPlansByUser(User user);
    Page<TrainingPlan> getPlansByUser(User user, Pageable pageable);
    List<TrainingPlan> getPlansByStatus(User user, String status);
    List<TrainingPlan> getPlansByDateRange(User user, LocalDate startDate, LocalDate endDate);
    TrainingPlan saveWeeklyPlan(User user, TrainingPlan plan);
    void toggleExerciseCompletion(Long exerciseId);
    
    /**
     * 启用训练计划
     */
    TrainingPlan activatePlan(Long id, User user);
    
    /**
     * 暂停训练计划
     */
    TrainingPlan pausePlan(Long id, User user);
    
    /**
     * 完成训练计划
     */
    TrainingPlan completePlan(Long id, User user);
    
    /**
     * 为训练计划添加训练日
     */
    TrainingPlan addDayToPlan(Long planId, User user, int dayOfWeek, String focus);
    
    /**
     * 为训练日添加动作
     */
    TrainingPlan addExerciseToDay(Long dayId, User user, String exerciseName, Integer sets, String reps, Double weight);
    
    /**
     * 获取用户当前激活的训练计划
     */
    Optional<TrainingPlan> getActivePlan(User user);
    
    /**
     * 复制训练计划
     */
    TrainingPlan copyPlan(Long planId, User user, String newName);
}
