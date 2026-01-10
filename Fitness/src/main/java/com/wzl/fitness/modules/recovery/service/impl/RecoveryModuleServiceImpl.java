package com.wzl.fitness.modules.recovery.service.impl;

import com.wzl.fitness.entity.RecoveryData;
import com.wzl.fitness.modules.recovery.dto.RecoveryStatusDTO;
import com.wzl.fitness.modules.recovery.dto.TrainingSuggestionDTO;
import com.wzl.fitness.modules.recovery.repository.RecoveryModuleRepository;
import com.wzl.fitness.modules.recovery.service.RecoveryModuleService;
import com.wzl.fitness.modules.training.api.TrainingModuleApi;
import com.wzl.fitness.modules.training.dto.TrainingRecordDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * 恢复评估模块服务实现
 * 
 * @see Requirements 1.2 - 领域模块包含独立的service子包
 * @see Requirements 6.2 - 支持通过配置文件启用/禁用模块
 */
@Service
@ConditionalOnProperty(
    prefix = "fitness.modules.recovery",
    name = "enabled",
    havingValue = "true",
    matchIfMissing = true
)
public class RecoveryModuleServiceImpl implements RecoveryModuleService {
    
    private static final Logger log = LoggerFactory.getLogger(RecoveryModuleServiceImpl.class);
    
    private final RecoveryModuleRepository recoveryModuleRepository;
    private final TrainingModuleApi trainingModuleApi;
    
    public RecoveryModuleServiceImpl(RecoveryModuleRepository recoveryModuleRepository,
                                     TrainingModuleApi trainingModuleApi) {
        this.recoveryModuleRepository = recoveryModuleRepository;
        this.trainingModuleApi = trainingModuleApi;
    }
    
    @Override
    public RecoveryStatusDTO getCurrentStatus(Long userId) {
        log.debug("获取用户 {} 的当前恢复状态", userId);
        
        // 获取最新的恢复数据
        Optional<RecoveryData> latestData = recoveryModuleRepository.findTopByUserIdOrderByTimestampDesc(userId);
        
        if (latestData.isEmpty()) {
            // 返回默认状态
            return buildDefaultStatus(userId);
        }
        
        RecoveryData data = latestData.get();
        return buildStatusFromData(userId, data);
    }
    
    @Override
    public RecoveryStatusDTO getStatusByDate(Long userId, LocalDate date) {
        log.debug("获取用户 {} 在 {} 的恢复状态", userId, date);
        
        LocalDateTime startOfDay = date.atStartOfDay();
        LocalDateTime endOfDay = date.plusDays(1).atStartOfDay();
        
        List<RecoveryData> dataList = recoveryModuleRepository
                .findByUserIdAndTimestampBetween(userId, startOfDay, endOfDay);
        
        if (dataList.isEmpty()) {
            return buildDefaultStatus(userId);
        }
        
        // 取最新的一条记录
        RecoveryData data = dataList.get(dataList.size() - 1);
        return buildStatusFromData(userId, data);
    }
    
    @Override
    public List<TrainingSuggestionDTO> getSuggestions(Long userId) {
        log.debug("生成用户 {} 的训练建议", userId);
        
        List<TrainingSuggestionDTO> suggestions = new ArrayList<>();
        
        // 获取当前恢复状态
        RecoveryStatusDTO status = getCurrentStatus(userId);
        int recoveryScore = status.getOverallScore() != null ? status.getOverallScore() : 50;
        
        // 获取最近训练记录
        List<TrainingRecordDTO> recentRecords = trainingModuleApi.getRecentRecords(userId, 5);
        
        // 根据恢复状态生成建议
        if (recoveryScore >= 80) {
            suggestions.add(TrainingSuggestionDTO.builder()
                    .suggestionType("INTENSITY")
                    .title("高强度训练日")
                    .description("恢复状态极佳，可以进行高强度训练或尝试突破个人记录")
                    .recommendedIntensity("HIGH")
                    .recommendedDuration(60)
                    .priority(1)
                    .isRestDay(false)
                    .build());
        } else if (recoveryScore >= 60) {
            suggestions.add(TrainingSuggestionDTO.builder()
                    .suggestionType("INTENSITY")
                    .title("正常训练日")
                    .description("恢复状态良好，可以进行正常强度训练")
                    .recommendedIntensity("MODERATE")
                    .recommendedDuration(45)
                    .priority(1)
                    .isRestDay(false)
                    .build());
        } else if (recoveryScore >= 40) {
            suggestions.add(TrainingSuggestionDTO.builder()
                    .suggestionType("INTENSITY")
                    .title("轻度训练日")
                    .description("恢复状态一般，建议降低训练强度")
                    .recommendedIntensity("LIGHT")
                    .recommendedDuration(30)
                    .priority(1)
                    .isRestDay(false)
                    .build());
        } else {
            suggestions.add(TrainingSuggestionDTO.builder()
                    .suggestionType("REST")
                    .title("休息日")
                    .description("恢复不足，建议今天休息或只进行轻度恢复训练")
                    .recommendedIntensity("REST")
                    .recommendedDuration(0)
                    .priority(1)
                    .isRestDay(true)
                    .build());
        }
        
        // 根据最近训练记录给出肌群建议
        if (!recentRecords.isEmpty()) {
            String lastExerciseType = recentRecords.get(0).getExerciseType();
            suggestions.add(generateMuscleGroupSuggestion(lastExerciseType));
        }
        
        // 添加恢复建议
        if (status.getSleepScore() != null && status.getSleepScore() < 60) {
            suggestions.add(TrainingSuggestionDTO.builder()
                    .suggestionType("RECOVERY")
                    .title("改善睡眠")
                    .description("睡眠质量较差，建议保证7-9小时睡眠，改善睡眠环境")
                    .priority(2)
                    .isRestDay(false)
                    .build());
        }
        
        if (status.getMuscleSorenessScore() != null && status.getMuscleSorenessScore() >= 7) {
            suggestions.add(TrainingSuggestionDTO.builder()
                    .suggestionType("RECOVERY")
                    .title("肌肉恢复")
                    .description("肌肉酸痛明显，建议进行泡沫轴放松或按摩")
                    .priority(2)
                    .isRestDay(false)
                    .build());
        }
        
        return suggestions;
    }
    
    @Override
    @Transactional
    public void updateRecoveryAfterTraining(Long userId, Long trainingRecordId, 
                                            Double totalVolume, String exerciseType) {
        log.info("训练完成后更新用户 {} 的恢复状态，训练记录ID: {}", userId, trainingRecordId);
        
        // 获取当前恢复数据
        Optional<RecoveryData> latestData = recoveryModuleRepository.findTopByUserIdOrderByTimestampDesc(userId);
        
        if (latestData.isPresent()) {
            RecoveryData data = latestData.get();
            
            // 根据训练量调整恢复评分
            int currentScore = data.getRecoveryScore() != null ? data.getRecoveryScore() : 50;
            int adjustment = calculateTrainingImpact(totalVolume);
            int newScore = Math.max(0, Math.min(100, currentScore + adjustment));
            
            data.setRecoveryScore(newScore);
            data.setNotes(String.format("训练后自动更新 - 训练类型: %s, 训练量: %.2f", exerciseType, totalVolume));
            
            recoveryModuleRepository.save(data);
            log.info("恢复评分已更新: {} -> {}", currentScore, newScore);
        }
    }
    
    @Override
    public int calculateRecoveryScore(Double sleepHours, Integer sleepQuality,
                                      Integer muscleSoreness, Integer stressLevel,
                                      Integer hrv, Integer restingHeartRate) {
        int score = 50; // 基础分
        
        // 睡眠时长评分 (最高+20分)
        if (sleepHours != null) {
            if (sleepHours >= 8) score += 20;
            else if (sleepHours >= 7) score += 15;
            else if (sleepHours >= 6) score += 5;
            else if (sleepHours < 5) score -= 15;
            else score -= 5;
        }
        
        // 睡眠质量评分 (最高+15分)
        if (sleepQuality != null) {
            score += (sleepQuality - 5) * 3; // 5分为基准
        }
        
        // 肌肉酸痛评分 (最高-20分)
        if (muscleSoreness != null) {
            score -= (muscleSoreness - 3) * 3; // 3分为基准
        }
        
        // 压力水平评分 (最高-15分)
        if (stressLevel != null) {
            score -= (stressLevel - 5) * 3;
        }
        
        // HRV评分 (最高+10分)
        if (hrv != null) {
            if (hrv >= 60) score += 10;
            else if (hrv >= 50) score += 5;
            else if (hrv < 40) score -= 10;
        }
        
        // 静息心率评分 (最高+5分)
        if (restingHeartRate != null) {
            if (restingHeartRate <= 55) score += 5;
            else if (restingHeartRate <= 65) score += 2;
            else if (restingHeartRate >= 80) score -= 5;
        }
        
        // 确保分数在0-100范围内
        return Math.max(0, Math.min(100, score));
    }
    
    @Override
    public String getRecommendedIntensity(int recoveryScore) {
        if (recoveryScore >= 80) return "INTENSE";
        if (recoveryScore >= 65) return "HIGH";
        if (recoveryScore >= 50) return "MODERATE";
        if (recoveryScore >= 30) return "LIGHT";
        return "REST";
    }
    
    // 私有辅助方法
    
    private RecoveryStatusDTO buildDefaultStatus(Long userId) {
        return RecoveryStatusDTO.builder()
                .userId(userId)
                .assessmentDate(LocalDate.now())
                .overallScore(50)
                .recoveryStatus("UNKNOWN")
                .recommendedIntensity("MODERATE")
                .estimatedRecoveryDays(1)
                .recoveryAdvice(List.of("请记录恢复数据以获取准确评估"))
                .build();
    }
    
    private RecoveryStatusDTO buildStatusFromData(Long userId, RecoveryData data) {
        int score = data.getRecoveryScore() != null ? data.getRecoveryScore() : 50;
        String status = getRecoveryStatus(score);
        String intensity = getRecommendedIntensity(score);
        
        return RecoveryStatusDTO.builder()
                .userId(userId)
                .assessmentDate(data.getTimestamp() != null ? data.getTimestamp().toLocalDate() : LocalDate.now())
                .overallScore(score)
                .recoveryStatus(status)
                .sleepScore(calculateSleepScore(data.getSleepHours(), data.getSleepQuality()))
                .sleepHours(data.getSleepHours())
                .muscleSorenessScore(data.getMuscleSoreness() != null ? data.getMuscleSoreness().intValue() : null)
                .stressScore(data.getStressLevel() != null ? data.getStressLevel().intValue() : null)
                .hrv(data.getHeartRateVariability())
                .restingHeartRate(data.getRestingHeartRate())
                .recommendedIntensity(intensity)
                .estimatedRecoveryDays(estimateRecoveryDays(score))
                .recoveryAdvice(generateRecoveryAdvice(data))
                .build();
    }
    
    private String getRecoveryStatus(int score) {
        if (score >= 80) return "EXCELLENT";
        if (score >= 60) return "GOOD";
        if (score >= 40) return "FAIR";
        if (score >= 20) return "POOR";
        return "CRITICAL";
    }
    
    private int calculateSleepScore(Double sleepHours, Integer sleepQuality) {
        int score = 50;
        
        if (sleepHours != null) {
            if (sleepHours >= 8) score += 25;
            else if (sleepHours >= 7) score += 15;
            else if (sleepHours < 6) score -= 20;
        }
        
        if (sleepQuality != null) {
            score += (sleepQuality - 5) * 5;
        }
        
        return Math.max(0, Math.min(100, score));
    }
    
    private int estimateRecoveryDays(int score) {
        if (score >= 80) return 0;
        if (score >= 60) return 1;
        if (score >= 40) return 2;
        if (score >= 20) return 3;
        return 4;
    }
    
    private int calculateTrainingImpact(Double totalVolume) {
        if (totalVolume == null) return 0;
        
        // 根据训练量调整恢复评分
        if (totalVolume > 10000) return -15; // 高训练量
        if (totalVolume > 5000) return -10;
        if (totalVolume > 2000) return -5;
        return -2; // 低训练量也会有轻微影响
    }
    
    private List<String> generateRecoveryAdvice(RecoveryData data) {
        List<String> advice = new ArrayList<>();
        
        if (data.getSleepHours() != null && data.getSleepHours() < 7) {
            advice.add("睡眠不足，建议保证7-9小时睡眠");
        }
        
        if (data.getMuscleSoreness() != null && data.getMuscleSoreness() >= 6) {
            advice.add("肌肉酸痛明显，建议进行泡沫轴放松或按摩");
        }
        
        if (data.getStressLevel() != null && data.getStressLevel() >= 7) {
            advice.add("压力水平较高，建议进行冥想或深呼吸练习");
        }
        
        if (data.getHeartRateVariability() != null && data.getHeartRateVariability() < 40) {
            advice.add("HRV偏低，身体可能处于应激状态，建议增加休息");
        }
        
        if (advice.isEmpty()) {
            advice.add("保持良好的作息和营养摄入");
        }
        
        return advice;
    }
    
    private TrainingSuggestionDTO generateMuscleGroupSuggestion(String lastExerciseType) {
        String targetMuscleGroup;
        String description;
        
        if (lastExerciseType == null) {
            targetMuscleGroup = "全身";
            description = "建议进行全身训练";
        } else if (lastExerciseType.contains("上肢") || lastExerciseType.contains("胸") || lastExerciseType.contains("背")) {
            targetMuscleGroup = "下肢";
            description = "最近进行了上肢训练，建议今天训练下肢，保持均衡发展";
        } else if (lastExerciseType.contains("下肢") || lastExerciseType.contains("腿")) {
            targetMuscleGroup = "上肢";
            description = "最近进行了下肢训练，建议今天训练上肢，保持均衡发展";
        } else {
            targetMuscleGroup = "核心";
            description = "建议进行核心训练，提高整体协调性";
        }
        
        return TrainingSuggestionDTO.builder()
                .suggestionType("MUSCLE_GROUP")
                .title("肌群建议")
                .description(description)
                .targetMuscleGroup(targetMuscleGroup)
                .priority(3)
                .isRestDay(false)
                .build();
    }
}
