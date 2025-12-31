package com.wzl.fitness.service.impl;

import com.wzl.fitness.dto.request.RecoveryDataRequest;
import com.wzl.fitness.dto.response.RecoveryAssessment;
import com.wzl.fitness.entity.RecoveryData;
import com.wzl.fitness.entity.TrainingRecord;
import com.wzl.fitness.entity.User;
import com.wzl.fitness.repository.RecoveryDataRepository;
import com.wzl.fitness.repository.TrainingRecordRepository;
import com.wzl.fitness.service.RecoveryAssessmentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * 恢复评估服务实现类
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class RecoveryAssessmentServiceImpl implements RecoveryAssessmentService {
    
    private final RecoveryDataRepository recoveryDataRepository;
    private final TrainingRecordRepository trainingRecordRepository;
    
    @Override
    @Transactional
    public RecoveryAssessment recordAndAssess(User user, RecoveryDataRequest request) {
        log.info("记录用户 {} 的恢复数据", user.getId());
        
        LocalDate recordDate = request.getRecordDate() != null ? request.getRecordDate() : LocalDate.now();
        
        // 保存恢复数据
        RecoveryData recoveryData = new RecoveryData();
        recoveryData.setUser(user);
        recoveryData.setTimestamp(recordDate.atStartOfDay());
        recoveryData.setSleepHours(request.getSleepHours());
        recoveryData.setSleepQuality(request.getSleepQuality() != null ? request.getSleepQuality() : 5);
        recoveryData.setMuscleSoreness(request.getMuscleSoreness() != null ? request.getMuscleSoreness().doubleValue() : 3.0);
        recoveryData.setStressLevel(request.getStressLevel() != null ? request.getStressLevel().doubleValue() : 5.0);
        recoveryData.setHeartRateVariability(request.getHrv() != null ? request.getHrv() : 50);
        recoveryData.setRestingHeartRate(request.getRestingHeartRate() != null ? request.getRestingHeartRate() : 60);
        recoveryData.setNotes(request.getNotes());
        
        // 计算恢复评分
        int overallScore = calculateOverallScore(request, user);
        recoveryData.setRecoveryScore(overallScore);
        
        recoveryDataRepository.save(recoveryData);
        
        return buildAssessment(request, user, recordDate, overallScore);
    }
    
    @Override
    public RecoveryAssessment getAssessment(User user, LocalDate date) {
        LocalDateTime startOfDay = date.atStartOfDay();
        LocalDateTime endOfDay = date.plusDays(1).atStartOfDay();
        
        Optional<RecoveryData> dataOpt = recoveryDataRepository
                .findByUserIdAndTimestampBetween(user.getId(), startOfDay, endOfDay)
                .stream().findFirst();
        
        if (dataOpt.isEmpty()) {
            // 返回默认评估
            return RecoveryAssessment.builder()
                    .assessmentDate(date)
                    .overallScore(50)
                    .recoveryStatus("UNKNOWN")
                    .recommendedIntensity("MODERATE")
                    .trainingAdvice(List.of("请先记录今日恢复数据以获取准确评估"))
                    .recoveryAdvice(List.of("建议每日记录恢复数据"))
                    .build();
        }
        
        RecoveryData data = dataOpt.get();
        RecoveryDataRequest request = RecoveryDataRequest.builder()
                .sleepHours(data.getSleepHours())
                .sleepQuality(data.getSleepQuality())
                .muscleSoreness(data.getMuscleSoreness() != null ? data.getMuscleSoreness().intValue() : null)
                .stressLevel(data.getStressLevel() != null ? data.getStressLevel().intValue() : null)
                .hrv(data.getHeartRateVariability())
                .restingHeartRate(data.getRestingHeartRate())
                .build();
        
        return buildAssessment(request, user, date, data.getRecoveryScore());
    }
    
    @Override
    public List<RecoveryAssessment> getAssessmentHistory(User user, LocalDate startDate, LocalDate endDate) {
        List<RecoveryAssessment> history = new ArrayList<>();
        
        LocalDate current = startDate;
        while (!current.isAfter(endDate)) {
            history.add(getAssessment(user, current));
            current = current.plusDays(1);
        }
        
        return history;
    }
    
    @Override
    public List<String> generateTrainingAdvice(User user, RecoveryAssessment assessment) {
        List<String> advice = new ArrayList<>();
        int score = assessment.getOverallScore();
        
        if (score >= 80) {
            advice.add("恢复状态极佳，可以进行高强度训练");
            advice.add("建议尝试突破个人记录或增加训练量");
            advice.add("可以进行复合动作和大重量训练");
        } else if (score >= 60) {
            advice.add("恢复状态良好，可以进行正常强度训练");
            advice.add("建议保持当前训练计划");
            advice.add("注意训练后的拉伸和放松");
        } else if (score >= 40) {
            advice.add("恢复状态一般，建议降低训练强度");
            advice.add("可以进行轻度有氧或技术训练");
            advice.add("避免大重量和高强度训练");
        } else if (score >= 20) {
            advice.add("恢复不足，建议以恢复训练为主");
            advice.add("可以进行瑜伽、拉伸或轻度活动");
            advice.add("重点关注睡眠和营养补充");
        } else {
            advice.add("严重疲劳，强烈建议休息");
            advice.add("今天应该完全休息，不进行任何训练");
            advice.add("如持续疲劳，建议咨询医生");
        }
        
        // 基于肌肉酸痛给出建议
        if (assessment.getMuscleSorenessScore() != null && assessment.getMuscleSorenessScore() >= 7) {
            advice.add("肌肉酸痛明显，避免训练相同肌群");
        }
        
        return advice;
    }
    
    @Override
    public List<String> generateRecoveryAdvice(User user, RecoveryAssessment assessment) {
        List<String> advice = new ArrayList<>();
        
        // 睡眠建议
        if (assessment.getSleepHours() != null && assessment.getSleepHours() < 7) {
            advice.add("睡眠不足，建议保证7-9小时睡眠");
        }
        if (assessment.getSleepScore() != null && assessment.getSleepScore() < 60) {
            advice.add("睡眠质量较差，建议改善睡眠环境，避免睡前使用电子设备");
        }
        
        // 压力建议
        if (assessment.getStressScore() != null && assessment.getStressScore() >= 7) {
            advice.add("压力水平较高，建议进行冥想或深呼吸练习");
        }
        
        // 肌肉恢复建议
        if (assessment.getMuscleSorenessScore() != null && assessment.getMuscleSorenessScore() >= 6) {
            advice.add("肌肉酸痛明显，建议进行泡沫轴放松或按摩");
            advice.add("可以尝试冷热交替浴促进恢复");
        }
        
        // 营养建议
        advice.add("确保摄入足够蛋白质（体重kg × 1.6-2.2g）");
        advice.add("保持充足水分摄入（体重kg × 33ml）");
        
        // HRV建议
        if (assessment.getHrv() != null && assessment.getHrv() < 40) {
            advice.add("HRV偏低，身体可能处于应激状态，建议增加休息");
        }
        
        return advice;
    }
    
    @Override
    public int calculateOverallScore(RecoveryDataRequest request, User user) {
        int score = 50; // 基础分
        
        // 睡眠时长评分 (最高+20分)
        if (request.getSleepHours() != null) {
            if (request.getSleepHours() >= 8) score += 20;
            else if (request.getSleepHours() >= 7) score += 15;
            else if (request.getSleepHours() >= 6) score += 5;
            else if (request.getSleepHours() < 5) score -= 15;
            else score -= 5;
        }
        
        // 睡眠质量评分 (最高+15分)
        if (request.getSleepQuality() != null) {
            score += (request.getSleepQuality() - 5) * 3; // 5分为基准
        }
        
        // 肌肉酸痛评分 (最高-20分)
        if (request.getMuscleSoreness() != null) {
            score -= (request.getMuscleSoreness() - 3) * 3; // 3分为基准
        }
        
        // 疲劳程度评分 (最高-15分)
        if (request.getFatigueLevel() != null) {
            score -= (request.getFatigueLevel() - 5) * 3;
        }
        
        // 压力水平评分 (最高-15分)
        if (request.getStressLevel() != null) {
            score -= (request.getStressLevel() - 5) * 3;
        }
        
        // HRV评分 (最高+10分)
        if (request.getHrv() != null) {
            if (request.getHrv() >= 60) score += 10;
            else if (request.getHrv() >= 50) score += 5;
            else if (request.getHrv() < 40) score -= 10;
        }
        
        // 静息心率评分 (最高+5分)
        if (request.getRestingHeartRate() != null) {
            if (request.getRestingHeartRate() <= 55) score += 5;
            else if (request.getRestingHeartRate() <= 65) score += 2;
            else if (request.getRestingHeartRate() >= 80) score -= 5;
        }
        
        // 考虑最近训练负荷
        score += calculateTrainingLoadImpact(user);
        
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
    
    private RecoveryAssessment buildAssessment(RecoveryDataRequest request, User user, LocalDate date, int overallScore) {
        String recoveryStatus = getRecoveryStatus(overallScore);
        String recommendedIntensity = getRecommendedIntensity(overallScore);
        
        RecoveryAssessment assessment = RecoveryAssessment.builder()
                .assessmentDate(date)
                .overallScore(overallScore)
                .recoveryStatus(recoveryStatus)
                .sleepHours(request.getSleepHours())
                .sleepScore(calculateSleepScore(request))
                .muscleSorenessScore(request.getMuscleSoreness())
                .fatigueScore(request.getFatigueLevel())
                .stressScore(request.getStressLevel())
                .hrv(request.getHrv())
                .restingHeartRate(request.getRestingHeartRate())
                .trainingLoadImpact(calculateTrainingLoadImpact(user))
                .recommendedIntensity(recommendedIntensity)
                .estimatedRecoveryDays(estimateRecoveryDays(overallScore))
                .build();
        
        assessment.setTrainingAdvice(generateTrainingAdvice(user, assessment));
        assessment.setRecoveryAdvice(generateRecoveryAdvice(user, assessment));
        
        return assessment;
    }
    
    private String getRecoveryStatus(int score) {
        if (score >= 80) return "EXCELLENT";
        if (score >= 60) return "GOOD";
        if (score >= 40) return "FAIR";
        if (score >= 20) return "POOR";
        return "CRITICAL";
    }
    
    private int calculateSleepScore(RecoveryDataRequest request) {
        int score = 50;
        
        if (request.getSleepHours() != null) {
            if (request.getSleepHours() >= 8) score += 25;
            else if (request.getSleepHours() >= 7) score += 15;
            else if (request.getSleepHours() < 6) score -= 20;
        }
        
        if (request.getSleepQuality() != null) {
            score += (request.getSleepQuality() - 5) * 5;
        }
        
        return Math.max(0, Math.min(100, score));
    }
    
    private int calculateTrainingLoadImpact(User user) {
        // 获取最近3天的训练记录
        LocalDate today = LocalDate.now();
        List<TrainingRecord> recentRecords = trainingRecordRepository
                .findByUserIdAndTrainingDateBetween(user.getId(), today.minusDays(3), today);
        
        if (recentRecords.isEmpty()) {
            return 5; // 没有训练记录，给予小幅加分
        }
        
        double totalVolume = recentRecords.stream()
                .mapToDouble(r -> r.getTotalVolume() != null ? r.getTotalVolume() : 0)
                .sum();
        
        // 根据训练量调整分数
        if (totalVolume > 10000) return -15; // 高训练量
        if (totalVolume > 5000) return -5;
        if (totalVolume > 2000) return 0;
        return 5; // 低训练量
    }
    
    private int estimateRecoveryDays(int score) {
        if (score >= 80) return 0;
        if (score >= 60) return 1;
        if (score >= 40) return 2;
        if (score >= 20) return 3;
        return 4;
    }
}
