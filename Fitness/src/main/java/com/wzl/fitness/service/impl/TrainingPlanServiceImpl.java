package com.wzl.fitness.service.impl;

import com.wzl.fitness.dto.request.TrainingPlanRequestDTO;
import com.wzl.fitness.entity.TrainingPlan;
import com.wzl.fitness.entity.TrainingPlanDay;
import com.wzl.fitness.entity.TrainingPlanExercise;
import com.wzl.fitness.entity.User;
import com.wzl.fitness.exception.BusinessException;
import com.wzl.fitness.repository.TrainingPlanDayRepository;
import com.wzl.fitness.repository.TrainingPlanExerciseRepository;
import com.wzl.fitness.repository.TrainingPlanRepository;
import com.wzl.fitness.service.TrainingPlanService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class TrainingPlanServiceImpl implements TrainingPlanService {

    private final TrainingPlanRepository trainingPlanRepository;
    private final TrainingPlanDayRepository trainingPlanDayRepository;
    private final TrainingPlanExerciseRepository trainingPlanExerciseRepository;

    @Override
    @Transactional
    public TrainingPlan createPlan(TrainingPlan plan) {
        // ... (existing logic)
        return trainingPlanRepository.save(plan);
    }

    @Override
    @Transactional
    public TrainingPlan createPlanFromDto(TrainingPlanRequestDTO dto, User user) {
        TrainingPlan plan = TrainingPlan.builder()
                .user(user)
                .name(dto.getName())
                .goal(dto.getGoal())
                .level(dto.getLevel())
                .durationWeeks(dto.getDuration())
                .daysPerWeek(dto.getDaysPerWeek())
                .durationPerSession(dto.getDurationPerSession())
                .startDate(LocalDate.now())
                .endDate(LocalDate.now().plusWeeks(dto.getDuration()))
                .status("ACTIVE")
                .isWeekly(true)
                .build();

        if (dto.getWeeklyPlan() != null) {
            for (int w = 0; w < dto.getWeeklyPlan().size(); w++) {
                TrainingPlanRequestDTO.WeekDTO weekDto = dto.getWeeklyPlan().get(w);
                if (weekDto.getDays() != null) {
                    for (int d = 0; d < weekDto.getDays().size(); d++) {
                        TrainingPlanRequestDTO.DayDTO dayDto = weekDto.getDays().get(d);
                        
                        TrainingPlanDay day = TrainingPlanDay.builder()
                                .plan(plan)
                                .weekNumber(w + 1)
                                .dayOfWeek(d)
                                .dayName(getDayName(d))
                                .hasTraining(dayDto.getHasTraining())
                                .focus(dayDto.getFocus())
                                .build();
                        
                        if (dayDto.getExercises() != null) {
                            for (int e = 0; e < dayDto.getExercises().size(); e++) {
                                TrainingPlanRequestDTO.ExerciseDTO exDto = dayDto.getExercises().get(e);
                                TrainingPlanExercise exercise = TrainingPlanExercise.builder()
                                        .day(day)
                                        .name(exDto.getName())
                                        .sets(exDto.getSets())
                                        .reps(exDto.getReps())
                                        .weight(exDto.getWeight() != null ? exDto.getWeight() : 0.0)
                                        .restTime(exDto.getRest())
                                        .notes(exDto.getNotes())
                                        .orderIndex(e)
                                        .completed(false)
                                        .build();
                                day.getExercises().add(exercise);
                            }
                        }
                        plan.getDays().add(day);
                    }
                }
            }
        }

        return trainingPlanRepository.save(plan);
    }

    private String getDayName(int dayOfWeek) {
        String[] days = {"周一", "周二", "周三", "周四", "周五", "周六", "周日"};
        return (dayOfWeek >= 0 && dayOfWeek < 7) ? days[dayOfWeek] : "未知";
    }

    @Override
    @Transactional
    public TrainingPlan updatePlan(Long id, TrainingPlan planDetails) {
        TrainingPlan plan = trainingPlanRepository.findById(id)
                .orElseThrow(() -> new BusinessException("计划不存在"));
        
        plan.setName(planDetails.getName());
        plan.setDescription(planDetails.getDescription());
        plan.setStartDate(planDetails.getStartDate());
        plan.setEndDate(planDetails.getEndDate());
        plan.setStatus(planDetails.getStatus());
        plan.setIsWeekly(planDetails.getIsWeekly());
        
        // 更新关联的days和exercises比较复杂，这里简单处理：先删除旧的再添加新的
        // 在生产环境中应该使用更精细的更新逻辑
        plan.getDays().clear();
        if (planDetails.getDays() != null) {
            for (TrainingPlanDay dayDetails : planDetails.getDays()) {
                TrainingPlanDay newDay = TrainingPlanDay.builder()
                        .plan(plan)
                        .dayOfWeek(dayDetails.getDayOfWeek())
                        .dayName(dayDetails.getDayName())
                        .notes(dayDetails.getNotes())
                        .build();
                
                if (dayDetails.getExercises() != null) {
                    for (TrainingPlanExercise exDetails : dayDetails.getExercises()) {
                        TrainingPlanExercise newEx = TrainingPlanExercise.builder()
                                .day(newDay)
                                .name(exDetails.getName())
                                .sets(exDetails.getSets())
                                .reps(exDetails.getReps())
                                .durationMinutes(exDetails.getDurationMinutes())
                                .intensity(exDetails.getIntensity())
                                .targetMuscles(exDetails.getTargetMuscles())
                                .restTime(exDetails.getRestTime())
                                .notes(exDetails.getNotes())
                                .orderIndex(exDetails.getOrderIndex())
                                .build();
                        newDay.getExercises().add(newEx);
                    }
                }
                plan.getDays().add(newDay);
            }
        }
        
        return trainingPlanRepository.save(plan);
    }

    @Override
    @Transactional
    public void deletePlan(Long id) {
        trainingPlanRepository.deleteById(id);
    }

    @Override
    public Optional<TrainingPlan> getPlanById(Long id) {
        return trainingPlanRepository.findById(id);
    }

    @Override
    public List<TrainingPlan> getPlansByUser(User user) {
        return trainingPlanRepository.findByUserOrderByCreatedAtDesc(user);
    }

    @Override
    public Page<TrainingPlan> getPlansByUser(User user, Pageable pageable) {
        return trainingPlanRepository.findByUser(user, pageable);
    }

    @Override
    public List<TrainingPlan> getPlansByStatus(User user, String status) {
        return trainingPlanRepository.findByUserAndStatus(user, status);
    }

    @Override
    public List<TrainingPlan> getPlansByDateRange(User user, LocalDate startDate, LocalDate endDate) {
        return trainingPlanRepository.findByUserAndStartDateBetween(user, startDate, endDate);
    }

    @Override
    @Transactional
    public TrainingPlan saveWeeklyPlan(User user, TrainingPlan plan) {
        plan.setUser(user);
        plan.setIsWeekly(true);
        return createPlan(plan);
    }

    @Override
    @Transactional
    public void toggleExerciseCompletion(Long exerciseId) {
        TrainingPlanExercise exercise = trainingPlanExerciseRepository.findById(exerciseId)
                .orElseThrow(() -> new BusinessException("动作不存在"));
        exercise.setCompleted(!Boolean.TRUE.equals(exercise.getCompleted()));
        trainingPlanExerciseRepository.save(exercise);
    }
    
    @Override
    @Transactional
    public TrainingPlan activatePlan(Long id, User user) {
        TrainingPlan plan = trainingPlanRepository.findById(id)
                .orElseThrow(() -> new BusinessException("计划不存在"));
        
        if (!plan.getUser().getId().equals(user.getId())) {
            throw new BusinessException("无权操作此计划");
        }
        
        // 将用户其他激活的计划设为暂停
        List<TrainingPlan> activePlans = trainingPlanRepository.findByUserAndStatus(user, "ACTIVE");
        for (TrainingPlan activePlan : activePlans) {
            if (!activePlan.getId().equals(id)) {
                activePlan.setStatus("PAUSED");
                trainingPlanRepository.save(activePlan);
            }
        }
        
        plan.setStatus("ACTIVE");
        return trainingPlanRepository.save(plan);
    }
    
    @Override
    @Transactional
    public TrainingPlan pausePlan(Long id, User user) {
        TrainingPlan plan = trainingPlanRepository.findById(id)
                .orElseThrow(() -> new BusinessException("计划不存在"));
        
        if (!plan.getUser().getId().equals(user.getId())) {
            throw new BusinessException("无权操作此计划");
        }
        
        plan.setStatus("PAUSED");
        return trainingPlanRepository.save(plan);
    }
    
    @Override
    @Transactional
    public TrainingPlan completePlan(Long id, User user) {
        TrainingPlan plan = trainingPlanRepository.findById(id)
                .orElseThrow(() -> new BusinessException("计划不存在"));
        
        if (!plan.getUser().getId().equals(user.getId())) {
            throw new BusinessException("无权操作此计划");
        }
        
        plan.setStatus("COMPLETED");
        plan.setEndDate(LocalDate.now());
        return trainingPlanRepository.save(plan);
    }
    
    @Override
    @Transactional
    public TrainingPlan addDayToPlan(Long planId, User user, int dayOfWeek, String focus) {
        TrainingPlan plan = trainingPlanRepository.findById(planId)
                .orElseThrow(() -> new BusinessException("计划不存在"));
        
        if (!plan.getUser().getId().equals(user.getId())) {
            throw new BusinessException("无权操作此计划");
        }
        
        TrainingPlanDay day = TrainingPlanDay.builder()
                .plan(plan)
                .dayOfWeek(dayOfWeek)
                .dayName(getDayName(dayOfWeek))
                .hasTraining(true)
                .focus(focus)
                .build();
        
        plan.getDays().add(day);
        return trainingPlanRepository.save(plan);
    }
    
    @Override
    @Transactional
    public TrainingPlan addExerciseToDay(Long dayId, User user, String exerciseName, Integer sets, String reps, Double weight) {
        TrainingPlanDay day = trainingPlanDayRepository.findById(dayId)
                .orElseThrow(() -> new BusinessException("训练日不存在"));
        
        TrainingPlan plan = day.getPlan();
        if (!plan.getUser().getId().equals(user.getId())) {
            throw new BusinessException("无权操作此计划");
        }
        
        int orderIndex = day.getExercises().size();
        
        TrainingPlanExercise exercise = TrainingPlanExercise.builder()
                .day(day)
                .name(exerciseName)
                .sets(sets)
                .reps(reps)
                .weight(weight != null ? weight : 0.0)
                .orderIndex(orderIndex)
                .completed(false)
                .build();
        
        day.getExercises().add(exercise);
        trainingPlanDayRepository.save(day);
        
        return plan;
    }
    
    @Override
    public Optional<TrainingPlan> getActivePlan(User user) {
        List<TrainingPlan> activePlans = trainingPlanRepository.findByUserAndStatus(user, "ACTIVE");
        return activePlans.isEmpty() ? Optional.empty() : Optional.of(activePlans.get(0));
    }
    
    @Override
    @Transactional
    public TrainingPlan copyPlan(Long planId, User user, String newName) {
        TrainingPlan originalPlan = trainingPlanRepository.findById(planId)
                .orElseThrow(() -> new BusinessException("计划不存在"));
        
        TrainingPlan newPlan = TrainingPlan.builder()
                .user(user)
                .name(newName != null ? newName : originalPlan.getName() + " (副本)")
                .description(originalPlan.getDescription())
                .goal(originalPlan.getGoal())
                .level(originalPlan.getLevel())
                .durationWeeks(originalPlan.getDurationWeeks())
                .daysPerWeek(originalPlan.getDaysPerWeek())
                .durationPerSession(originalPlan.getDurationPerSession())
                .startDate(LocalDate.now())
                .status("PAUSED")
                .isWeekly(originalPlan.getIsWeekly())
                .build();
        
        // 复制训练日和动作
        for (TrainingPlanDay originalDay : originalPlan.getDays()) {
            TrainingPlanDay newDay = TrainingPlanDay.builder()
                    .plan(newPlan)
                    .dayOfWeek(originalDay.getDayOfWeek())
                    .weekNumber(originalDay.getWeekNumber())
                    .dayName(originalDay.getDayName())
                    .hasTraining(originalDay.getHasTraining())
                    .focus(originalDay.getFocus())
                    .notes(originalDay.getNotes())
                    .build();
            
            for (TrainingPlanExercise originalEx : originalDay.getExercises()) {
                TrainingPlanExercise newEx = TrainingPlanExercise.builder()
                        .day(newDay)
                        .name(originalEx.getName())
                        .sets(originalEx.getSets())
                        .reps(originalEx.getReps())
                        .weight(originalEx.getWeight())
                        .durationMinutes(originalEx.getDurationMinutes())
                        .intensity(originalEx.getIntensity())
                        .targetMuscles(originalEx.getTargetMuscles())
                        .restTime(originalEx.getRestTime())
                        .notes(originalEx.getNotes())
                        .orderIndex(originalEx.getOrderIndex())
                        .completed(false)
                        .build();
                newDay.getExercises().add(newEx);
            }
            newPlan.getDays().add(newDay);
        }
        
        return trainingPlanRepository.save(newPlan);
    }
}
