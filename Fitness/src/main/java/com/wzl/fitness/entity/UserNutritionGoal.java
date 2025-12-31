package com.wzl.fitness.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * 用户营养目标实体类
 * 存储用户的个性化营养目标设置
 */
@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "user_nutrition_goals")
public class UserNutritionGoal {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;
    
    /**
     * 训练目标: fat_loss, muscle_gain, maintenance
     */
    @Column(name = "training_goal", length = 50)
    @Builder.Default
    private String trainingGoal = "maintenance";
    
    /**
     * 活动水平: sedentary, light, moderate, active, very_active
     */
    @Column(name = "activity_level", length = 20)
    @Builder.Default
    private String activityLevel = "moderate";
    
    /**
     * 目标每日卡路里摄入
     */
    @Column(name = "target_calories")
    private Double targetCalories;
    
    /**
     * 目标每日蛋白质摄入(g)
     */
    @Column(name = "target_protein")
    private Double targetProtein;
    
    /**
     * 目标每日碳水化合物摄入(g)
     */
    @Column(name = "target_carbs")
    private Double targetCarbs;
    
    /**
     * 目标每日脂肪摄入(g)
     */
    @Column(name = "target_fat")
    private Double targetFat;
    
    /**
     * 是否使用自定义目标（否则使用计算值）
     */
    @Column(name = "use_custom_targets")
    @Builder.Default
    private Boolean useCustomTargets = false;
    
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
