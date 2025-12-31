package com.wzl.fitness.service.impl;

import com.wzl.fitness.entity.FitnessData;
import com.wzl.fitness.entity.User;
import com.wzl.fitness.repository.FitnessDataRepository;
import com.wzl.fitness.service.LoadRecoveryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class LoadRecoveryServiceImpl implements LoadRecoveryService {

    @Autowired
    private FitnessDataRepository fitnessDataRepository;

    @Override
    public FitnessData calculateTrainingLoad(FitnessData fitnessData) {
        // 计算训练量
        if (fitnessData.getWeight() != null && fitnessData.getSets() != null && fitnessData.getReps() != null) {
            double volume = fitnessData.getWeight() * fitnessData.getSets() * fitnessData.getReps();
            fitnessData.setTrainingVolume(volume);
        }

        // 计算1RM
        if (fitnessData.getWeight() != null && fitnessData.getReps() != null) {
            fitnessData.setOneRepMax(calculateOneRepMax(fitnessData.getWeight(), fitnessData.getReps()));
        }

        return fitnessData;
    }

    @Override
    public Map<String, Object> assessRecoveryStatus(Long userId, Integer sleepHours, Integer stressLevel) {
        Map<String, Object> result = new HashMap<>();
        
        // 获取最近的训练数据
        FitnessData lastActivity = fitnessDataRepository.findTopByUserIdOrderByTimestampDesc(userId);
        
        int recoveryScore = 50; // 基础分数
        
        // 基于睡眠时长调整
        if (sleepHours != null) {
            if (sleepHours >= 8) recoveryScore += 20;
            else if (sleepHours >= 6) recoveryScore += 10;
            else recoveryScore -= 20;
        }
        
        // 基于压力水平调整
        if (stressLevel != null) {
            if (stressLevel <= 3) recoveryScore += 10;
            else if (stressLevel <= 7) recoveryScore -= 10;
            else recoveryScore -= 20;
        }
        
        // 基于最近训练负荷调整
        if (lastActivity != null) {
            LocalDateTime lastTrainingTime = lastActivity.getTimestamp();
            LocalDateTime now = LocalDateTime.now();
            
            long hoursSinceLastTraining = java.time.Duration.between(lastTrainingTime, now).toHours();
            
            if (hoursSinceLastTraining < 24 && lastActivity.getTrainingVolume() != null && lastActivity.getTrainingVolume() > 1000) {
                recoveryScore -= 15;
            }
        }
        
        // 确保分数在0-100范围内
        recoveryScore = Math.max(0, Math.min(100, recoveryScore));
        
        // 确定恢复状态
        String recoveryStatus;
        if (recoveryScore >= 80) recoveryStatus = "恢复良好";
        else if (recoveryScore >= 60) recoveryStatus = "恢复一般";
        else if (recoveryScore >= 40) recoveryStatus = "轻度疲劳";
        else if (recoveryScore >= 20) recoveryStatus = "中度疲劳";
        else recoveryStatus = "严重疲劳";
        
        result.put("recoveryScore", recoveryScore);
        result.put("recoveryStatus", recoveryStatus);
        result.put("sleepHours", sleepHours);
        result.put("stressLevel", stressLevel);
        
        return result;
    }

    @Override
    public Map<String, Object> generateTrainingSuggestions(Long userId) {
        Map<String, Object> suggestions = new HashMap<>();
        
        // 获取用户最近的训练数据和恢复状态
        FitnessData lastActivity = fitnessDataRepository.findTopByUserIdOrderByTimestampDesc(userId);
        Map<String, Object> recoveryStatus = assessRecoveryStatus(userId, null, null);
        int recoveryScore = (int) recoveryStatus.get("recoveryScore");
        
        StringBuilder suggestionText = new StringBuilder();
        
        if (recoveryScore >= 80) {
            suggestionText.append("您的恢复状态良好，可以进行高强度训练。");
        } else if (recoveryScore >= 60) {
            suggestionText.append("您的恢复状态一般，建议进行中等强度训练。");
        } else if (recoveryScore >= 40) {
            suggestionText.append("您有轻度疲劳，建议降低训练强度，增加休息时间。");
        } else {
            suggestionText.append("您的疲劳程度较高，建议今天以休息为主，或进行低强度的恢复训练。");
        }
        
        // 基于最近的训练动作类型给出建议
        if (lastActivity != null && lastActivity.getExerciseType() != null) {
            String lastExerciseType = lastActivity.getExerciseType();
            suggestionText.append(" 您最近进行了").append(lastExerciseType).append("训练，");
            
            // 根据训练类型给出建议
            switch (lastExerciseType) {
                case "上肢":
                    suggestionText.append("建议今天可以考虑下肢训练，保持均衡发展。");
                    break;
                case "下肢":
                    suggestionText.append("建议今天可以考虑上肢训练，保持均衡发展。");
                    break;
                case "核心":
                    suggestionText.append("建议今天可以考虑全身训练，提高整体协调性。");
                    break;
                default:
                    suggestionText.append("建议注意不同肌群的均衡训练。");
            }
        }
        
        suggestions.put("suggestionText", suggestionText.toString());
        suggestions.put("recoveryScore", recoveryScore);
        
        return suggestions;
    }

    @Override
    public Double calculateOneRepMax(Double weight, Integer reps) {
        // 使用Epley公式作为默认计算方法
        return calculateOneRepMax(weight, reps, "Epley");
    }
    
    @Override
    public Double calculateOneRepMax(Double weight, Integer reps, String model) {
        if (weight == null || reps == null || reps <= 0) {
            return 0.0;
        }
        
        // 确保reps不会导致除零或无效计算
        reps = Math.max(1, reps);
        
        // 防止 model 为空
        if (model == null || model.isEmpty()) {
            model = "Epley";
        }
        
        switch (model.toLowerCase()) {
            case "brzycki":
                // Brzycki公式: 1RM = 重量 * 36 / (37 - 次数)
                // 适用于次数少于10次的情况，非常流行且准确
                return reps < 37 ? weight * 36.0 / (37.0 - reps) : weight * 10.0;
            case "lombardi":
                // Lombardi公式: 1RM = 重量 * 次数^0.1
                // 较不保守，适合高次数估算
                return weight * Math.pow(reps, 0.1);
            case "oconner":
                // O'Conner公式: 1RM = 重量 * (1 + 次数 / 40.0)
                // 1RM = W * (1 + 0.025 * R)
                return weight * (1 + reps / 40.0);
            case "mayhew":
                // Mayhew公式: 1RM = 重量 * (100 / (48.8 + 53.8 * Math.exp(-0.075 * 次数)))
                // 适用于全身性动作
                return weight * (100 / (48.8 + 53.8 * Math.exp(-0.075 * reps)));
            case "epley":
            default:
                // Epley公式: 1RM = 重量 * (1 + 次数 / 30.0)
                // 最通用的公式: 1RM = W * (1 + R/30)
                return weight * (1 + reps / 30.0);
        }
    }

    @Override
    public Double calculateVolume(Double weight, Integer sets, Integer reps) {
        if (weight == null || sets == null || reps == null) {
            return 0.0;
        }
        return weight * sets * reps;
    }

    @Override
    public Double calculateCalories(Double duration, Double intensity) {
        if (duration == null) {
            return 0.0;
        }
        // 基础公式：时长 * 强度系数 * 基础消耗(每分钟5卡)
        double factor = intensity != null ? intensity : 5.0;
        return duration * factor * 1.2; // 稍微增加系数
    }
    
    @Override
    public Double calculateTrainingStress(Double volume, Double rpe) {
        if (volume == null || rpe == null) {
            return 0.0;
        }
        return volume * (rpe / 10.0);
    }

    @Override
    public Double calculateFatigueIndex(Double trainingStress, com.wzl.fitness.entity.RecoveryMetric recovery) {
        if (trainingStress == null || recovery == null) {
            return 0.0;
        }
        Double recoveryScore = calculateRecoveryScore(recovery);
        return trainingStress / (recoveryScore + 0.1);
    }

    @Override
    public Double calculateRecoveryScore(com.wzl.fitness.entity.RecoveryMetric recovery) {
        if (recovery == null) {
            return 0.0;
        }
        // 基于肌肉酸痛、睡眠质量和主观能量水平计算恢复得分
        return (5 - (recovery.getMuscleSoreness() != null ? recovery.getMuscleSoreness() : 3)) * 
               (recovery.getSleepQuality() != null ? recovery.getSleepQuality() : 3) * 
               ((recovery.getSubjectiveEnergy() != null ? recovery.getSubjectiveEnergy() : 3) / 5.0);
    }

    @Autowired
    private com.wzl.fitness.repository.TrainingRecordRepository trainingRecordRepository;

    @Override
    public com.wzl.fitness.entity.TrainingRecord calculateTrainingSummary(com.wzl.fitness.entity.TrainingRecord trainingRecord) {
        if (trainingRecord == null || trainingRecord.getExerciseDetails() == null) {
            return trainingRecord;
        }
        Double totalVolume = 0.0;
        Double totalStress = 0.0;

        // 计算每个动作的训练量和训练压力
        for (com.wzl.fitness.entity.ExerciseDetail detail : trainingRecord.getExerciseDetails()) {
            Double volume = calculateVolume(detail.getWeight(), detail.getSets(), detail.getReps());
            detail.setVolume(volume);
            
            // 如果有RPE值，则计算训练压力
            if (detail.getRpe() != null) {
                Double stress = calculateTrainingStress(volume, detail.getRpe().doubleValue());
                totalStress += stress;
            }
            
            totalVolume += volume;
        }

        trainingRecord.setTotalVolume(totalVolume);
        trainingRecord.setTrainingStress(totalStress);
        
        return trainingRecord;
    }

    @Override
    public Map<String, Double> getLoadTrendByDate(Long userId, java.time.LocalDate startDate, java.time.LocalDate endDate) {
        List<com.wzl.fitness.entity.TrainingRecord> records = trainingRecordRepository.findByUserIdAndTrainingDateBetween(userId, startDate, endDate);
        Map<String, Double> trend = new HashMap<>();

        for (com.wzl.fitness.entity.TrainingRecord record : records) {
            trend.put(record.getTrainingDate().toString(), record.getTotalVolume());
        }

        return trend;
    }

    @Override
    public List<String> getSupportedOneRepMaxModels() {
        return Arrays.asList("Epley", "Brzycki", "Lombardi", "OConner", "Mayhew");
    }

    @Override
    public Map<String, Double> getLoadTrend(Long userId, LocalDateTime startDate, LocalDateTime endDate) {
        List<FitnessData> data = fitnessDataRepository.findByUserIdAndTimestampBetween(userId, startDate, endDate);
        Map<String, Double> trend = new HashMap<>();
        
        // 按日期分组计算每日训练量
        for (FitnessData fitnessData : data) {
            if (fitnessData.getTrainingVolume() == null || fitnessData.getTimestamp() == null) {
                continue;
            }
            
            String dateStr = fitnessData.getTimestamp().toLocalDate().toString();
            double volume = fitnessData.getTrainingVolume();
            
            if (trend.containsKey(dateStr)) {
                trend.put(dateStr, trend.get(dateStr) + volume);
            } else {
                trend.put(dateStr, volume);
            }
        }
        
        return trend;
    }
}