package com.wzl.fitness.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * 仪表盘概览DTO
 * 聚合展示用户训练、营养、恢复等核心数据
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardOverview {
    
    /**
     * 数据日期范围开始
     */
    private LocalDate startDate;
    
    /**
     * 数据日期范围结束
     */
    private LocalDate endDate;
    
    // ==================== 训练概览 ====================
    
    /**
     * 本周训练次数
     */
    private Integer weeklyTrainingCount;
    
    /**
     * 本周总训练量(kg)
     */
    private Double weeklyTotalVolume;
    
    /**
     * 本周训练时长(分钟)
     */
    private Integer weeklyTrainingDuration;
    
    /**
     * 与上周训练量对比(%)
     */
    private Double volumeChangePercent;
    
    /**
     * 连续训练天数
     */
    private Integer trainingStreak;
    
    // ==================== 恢复状态 ====================
    
    /**
     * 最新恢复评分
     */
    private Integer latestRecoveryScore;
    
    /**
     * 恢复状态
     */
    private String recoveryStatus;
    
    /**
     * 推荐训练强度
     */
    private String recommendedIntensity;
    
    // ==================== 1RM进步 ====================
    
    /**
     * 主要动作1RM进步数据
     * key: 动作名称, value: 进步百分比
     */
    private Map<String, Double> oneRepMaxProgress;
    
    /**
     * 个人最佳记录列表
     */
    private List<PersonalRecord> personalRecords;
    
    // ==================== 营养状态 ====================
    
    /**
     * 今日卡路里摄入
     */
    private Double todayCalories;
    
    /**
     * 今日卡路里目标
     */
    private Double caloriesTarget;
    
    /**
     * 卡路里达成率(%)
     */
    private Double caloriesAchievementRate;
    
    /**
     * 今日蛋白质摄入(g)
     */
    private Double todayProtein;
    
    /**
     * 蛋白质目标(g)
     */
    private Double proteinTarget;
    
    /**
     * 蛋白质达成率(%)
     */
    private Double proteinAchievementRate;
    
    /**
     * 本周营养达标天数
     */
    private Integer weeklyNutritionGoalDays;
    
    // ==================== 快速统计 ====================
    
    /**
     * 本月训练总次数
     */
    private Integer monthlyTrainingCount;
    
    /**
     * 本月总训练量
     */
    private Double monthlyTotalVolume;
    
    /**
     * 最常训练的动作
     */
    private String mostFrequentExercise;
    
    /**
     * 下次建议训练的肌群
     */
    private String suggestedMuscleGroup;
    
    /**
     * 个人记录DTO
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PersonalRecord {
        private String exerciseName;
        private Double weight;
        private Integer reps;
        private Double estimatedOneRepMax;
        private LocalDate achievedDate;
    }
}
