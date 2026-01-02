package com.wzl.fitness.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import java.time.LocalDateTime;

/**
 * 用户营养目标实体类
 */
@Entity
@Table(name = "user_nutrition_goals")
public class UserNutritionGoal {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;
    
    @Column(name = "training_goal", length = 50)
    private String trainingGoal = "maintenance";
    
    @Column(name = "activity_level", length = 20)
    private String activityLevel = "moderate";
    
    @Column(name = "target_calories")
    private Double targetCalories;
    
    @Column(name = "target_protein")
    private Double targetProtein;
    
    @Column(name = "target_carbs")
    private Double targetCarbs;
    
    @Column(name = "target_fat")
    private Double targetFat;
    
    @Column(name = "use_custom_targets")
    private Boolean useCustomTargets = false;
    
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    public UserNutritionGoal() {}
    
    public UserNutritionGoal(Long id, User user, String trainingGoal, String activityLevel, Double targetCalories,
                             Double targetProtein, Double targetCarbs, Double targetFat, Boolean useCustomTargets,
                             LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.user = user;
        this.trainingGoal = trainingGoal;
        this.activityLevel = activityLevel;
        this.targetCalories = targetCalories;
        this.targetProtein = targetProtein;
        this.targetCarbs = targetCarbs;
        this.targetFat = targetFat;
        this.useCustomTargets = useCustomTargets;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }
    
    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
    public String getTrainingGoal() { return trainingGoal; }
    public void setTrainingGoal(String trainingGoal) { this.trainingGoal = trainingGoal; }
    public String getActivityLevel() { return activityLevel; }
    public void setActivityLevel(String activityLevel) { this.activityLevel = activityLevel; }
    public Double getTargetCalories() { return targetCalories; }
    public void setTargetCalories(Double targetCalories) { this.targetCalories = targetCalories; }
    public Double getTargetProtein() { return targetProtein; }
    public void setTargetProtein(Double targetProtein) { this.targetProtein = targetProtein; }
    public Double getTargetCarbs() { return targetCarbs; }
    public void setTargetCarbs(Double targetCarbs) { this.targetCarbs = targetCarbs; }
    public Double getTargetFat() { return targetFat; }
    public void setTargetFat(Double targetFat) { this.targetFat = targetFat; }
    public Boolean getUseCustomTargets() { return useCustomTargets; }
    public void setUseCustomTargets(Boolean useCustomTargets) { this.useCustomTargets = useCustomTargets; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    
    // Builder
    public static UserNutritionGoalBuilder builder() { return new UserNutritionGoalBuilder(); }
    
    public static class UserNutritionGoalBuilder {
        private Long id;
        private User user;
        private String trainingGoal = "maintenance";
        private String activityLevel = "moderate";
        private Double targetCalories;
        private Double targetProtein;
        private Double targetCarbs;
        private Double targetFat;
        private Boolean useCustomTargets = false;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
        
        public UserNutritionGoalBuilder id(Long v) { this.id = v; return this; }
        public UserNutritionGoalBuilder user(User v) { this.user = v; return this; }
        public UserNutritionGoalBuilder trainingGoal(String v) { this.trainingGoal = v; return this; }
        public UserNutritionGoalBuilder activityLevel(String v) { this.activityLevel = v; return this; }
        public UserNutritionGoalBuilder targetCalories(Double v) { this.targetCalories = v; return this; }
        public UserNutritionGoalBuilder targetProtein(Double v) { this.targetProtein = v; return this; }
        public UserNutritionGoalBuilder targetCarbs(Double v) { this.targetCarbs = v; return this; }
        public UserNutritionGoalBuilder targetFat(Double v) { this.targetFat = v; return this; }
        public UserNutritionGoalBuilder useCustomTargets(Boolean v) { this.useCustomTargets = v; return this; }
        public UserNutritionGoalBuilder createdAt(LocalDateTime v) { this.createdAt = v; return this; }
        public UserNutritionGoalBuilder updatedAt(LocalDateTime v) { this.updatedAt = v; return this; }
        
        public UserNutritionGoal build() {
            return new UserNutritionGoal(id, user, trainingGoal, activityLevel, targetCalories, targetProtein,
                    targetCarbs, targetFat, useCustomTargets, createdAt, updatedAt);
        }
    }
}
