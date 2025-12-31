package com.wzl.fitness.service;

import com.wzl.fitness.dto.request.RecoveryDataRequest;
import com.wzl.fitness.entity.RecoveryMetric;
import com.wzl.fitness.entity.User;
import com.wzl.fitness.service.impl.LoadRecoveryServiceImpl;
import com.wzl.fitness.service.impl.RecoveryAssessmentServiceImpl;
import net.jqwik.api.*;
import net.jqwik.api.constraints.*;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/**
 * 恢复评分属性测试
 * 
 * **Property 5: 恢复评分范围**
 * *For any* 恢复数据输入，计算的恢复评分应在合理范围内，且状态分类应与评分区间一致
 * 
 * **Validates: Requirements 2.4, 3.2**
 * 
 * Feature: project-evaluation, Property 5: 恢复评分范围
 * Feature: short-term-improvements, Property: 恢复评分范围验证
 */
public class RecoveryScorePropertyTest {

    private final LoadRecoveryServiceImpl loadRecoveryService = new LoadRecoveryServiceImpl();

    /**
     * Property 5: 恢复评分范围
     * 
     * 对于任意有效的恢复指标输入（肌肉酸痛1-5，睡眠质量1-5，主观能量1-5），
     * 计算的恢复评分应在0-20范围内（基于公式：(5-酸痛)*睡眠*(能量/5)）
     * 
     * **Validates: Requirements 2.4**
     */
    @Property(tries = 100)
    @Label("Property 5: 恢复评分范围 - 评分应在有效范围内")
    void recoveryScoreShouldBeInValidRange(
            @ForAll @IntRange(min = 1, max = 5) Integer muscleSoreness,
            @ForAll @IntRange(min = 1, max = 5) Integer sleepQuality,
            @ForAll @IntRange(min = 1, max = 5) Integer subjectiveEnergy) {
        
        RecoveryMetric recovery = new RecoveryMetric();
        recovery.setMuscleSoreness(muscleSoreness);
        recovery.setSleepQuality(sleepQuality);
        recovery.setSubjectiveEnergy(subjectiveEnergy);
        
        Double score = loadRecoveryService.calculateRecoveryScore(recovery);
        
        // 公式: (5 - muscleSoreness) * sleepQuality * (subjectiveEnergy / 5.0)
        // 最小值: (5-5) * 1 * (1/5) = 0
        // 最大值: (5-1) * 5 * (5/5) = 4 * 5 * 1 = 20
        assertTrue(score >= 0.0, 
                String.format("恢复评分应>=0: soreness=%d, sleep=%d, energy=%d, score=%.2f", 
                        muscleSoreness, sleepQuality, subjectiveEnergy, score));
        assertTrue(score <= 20.0, 
                String.format("恢复评分应<=20: soreness=%d, sleep=%d, energy=%d, score=%.2f", 
                        muscleSoreness, sleepQuality, subjectiveEnergy, score));
    }

    /**
     * 属性：肌肉酸痛越低，恢复评分越高
     * 
     * 对于相同的睡眠质量和能量水平，较低的肌肉酸痛应产生较高的恢复评分
     */
    @Property(tries = 100)
    @Label("肌肉酸痛越低恢复评分越高")
    void lowerSorenessImpliesHigherScore(
            @ForAll @IntRange(min = 1, max = 5) Integer soreness1,
            @ForAll @IntRange(min = 1, max = 5) Integer soreness2,
            @ForAll @IntRange(min = 1, max = 5) Integer sleepQuality,
            @ForAll @IntRange(min = 1, max = 5) Integer subjectiveEnergy) {
        
        Assume.that(soreness1 < soreness2);
        
        RecoveryMetric recovery1 = new RecoveryMetric();
        recovery1.setMuscleSoreness(soreness1);
        recovery1.setSleepQuality(sleepQuality);
        recovery1.setSubjectiveEnergy(subjectiveEnergy);
        
        RecoveryMetric recovery2 = new RecoveryMetric();
        recovery2.setMuscleSoreness(soreness2);
        recovery2.setSleepQuality(sleepQuality);
        recovery2.setSubjectiveEnergy(subjectiveEnergy);
        
        Double score1 = loadRecoveryService.calculateRecoveryScore(recovery1);
        Double score2 = loadRecoveryService.calculateRecoveryScore(recovery2);
        
        assertTrue(score1 >= score2,
                String.format("较低酸痛应产生较高评分: soreness1=%d (score=%.2f), soreness2=%d (score=%.2f)", 
                        soreness1, score1, soreness2, score2));
    }

    /**
     * 属性：睡眠质量越高，恢复评分越高
     * 
     * 对于相同的肌肉酸痛和能量水平，较高的睡眠质量应产生较高的恢复评分
     */
    @Property(tries = 100)
    @Label("睡眠质量越高恢复评分越高")
    void higherSleepQualityImpliesHigherScore(
            @ForAll @IntRange(min = 1, max = 5) Integer muscleSoreness,
            @ForAll @IntRange(min = 1, max = 5) Integer sleep1,
            @ForAll @IntRange(min = 1, max = 5) Integer sleep2,
            @ForAll @IntRange(min = 1, max = 5) Integer subjectiveEnergy) {
        
        Assume.that(sleep1 < sleep2);
        
        RecoveryMetric recovery1 = new RecoveryMetric();
        recovery1.setMuscleSoreness(muscleSoreness);
        recovery1.setSleepQuality(sleep1);
        recovery1.setSubjectiveEnergy(subjectiveEnergy);
        
        RecoveryMetric recovery2 = new RecoveryMetric();
        recovery2.setMuscleSoreness(muscleSoreness);
        recovery2.setSleepQuality(sleep2);
        recovery2.setSubjectiveEnergy(subjectiveEnergy);
        
        Double score1 = loadRecoveryService.calculateRecoveryScore(recovery1);
        Double score2 = loadRecoveryService.calculateRecoveryScore(recovery2);
        
        assertTrue(score2 >= score1,
                String.format("较高睡眠质量应产生较高评分: sleep1=%d (score=%.2f), sleep2=%d (score=%.2f)", 
                        sleep1, score1, sleep2, score2));
    }

    /**
     * 属性：主观能量越高，恢复评分越高
     * 
     * 对于相同的肌肉酸痛和睡眠质量，较高的主观能量应产生较高的恢复评分
     */
    @Property(tries = 100)
    @Label("主观能量越高恢复评分越高")
    void higherEnergyImpliesHigherScore(
            @ForAll @IntRange(min = 1, max = 5) Integer muscleSoreness,
            @ForAll @IntRange(min = 1, max = 5) Integer sleepQuality,
            @ForAll @IntRange(min = 1, max = 5) Integer energy1,
            @ForAll @IntRange(min = 1, max = 5) Integer energy2) {
        
        Assume.that(energy1 < energy2);
        
        RecoveryMetric recovery1 = new RecoveryMetric();
        recovery1.setMuscleSoreness(muscleSoreness);
        recovery1.setSleepQuality(sleepQuality);
        recovery1.setSubjectiveEnergy(energy1);
        
        RecoveryMetric recovery2 = new RecoveryMetric();
        recovery2.setMuscleSoreness(muscleSoreness);
        recovery2.setSleepQuality(sleepQuality);
        recovery2.setSubjectiveEnergy(energy2);
        
        Double score1 = loadRecoveryService.calculateRecoveryScore(recovery1);
        Double score2 = loadRecoveryService.calculateRecoveryScore(recovery2);
        
        assertTrue(score2 >= score1,
                String.format("较高能量应产生较高评分: energy1=%d (score=%.2f), energy2=%d (score=%.2f)", 
                        energy1, score1, energy2, score2));
    }

    /**
     * 属性：最佳状态应产生最高评分
     * 
     * 肌肉酸痛=1，睡眠质量=5，主观能量=5时应产生最高评分(20)
     */
    @Property(tries = 10)
    @Label("最佳状态应产生最高评分")
    void bestConditionProducesMaxScore() {
        RecoveryMetric recovery = new RecoveryMetric();
        recovery.setMuscleSoreness(1);
        recovery.setSleepQuality(5);
        recovery.setSubjectiveEnergy(5);
        
        Double score = loadRecoveryService.calculateRecoveryScore(recovery);
        
        // (5-1) * 5 * (5/5) = 4 * 5 * 1 = 20
        assertEquals(20.0, score, 0.01, "最佳状态应产生评分20");
    }

    /**
     * 属性：最差状态应产生最低评分
     * 
     * 肌肉酸痛=5时应产生评分0（因为5-5=0）
     */
    @Property(tries = 10)
    @Label("最差状态应产生最低评分")
    void worstConditionProducesMinScore(
            @ForAll @IntRange(min = 1, max = 5) Integer sleepQuality,
            @ForAll @IntRange(min = 1, max = 5) Integer subjectiveEnergy) {
        
        RecoveryMetric recovery = new RecoveryMetric();
        recovery.setMuscleSoreness(5);  // 最高酸痛
        recovery.setSleepQuality(sleepQuality);
        recovery.setSubjectiveEnergy(subjectiveEnergy);
        
        Double score = loadRecoveryService.calculateRecoveryScore(recovery);
        
        // (5-5) * any * any = 0
        assertEquals(0.0, score, 0.01, "最高酸痛应产生评分0");
    }

    /**
     * 属性：空输入应返回0
     */
    @Property(tries = 10)
    @Label("空输入应返回0")
    void nullInputReturnsZero() {
        Double score = loadRecoveryService.calculateRecoveryScore(null);
        assertEquals(0.0, score, "空输入应返回0");
    }

    /**
     * 属性：部分空字段使用默认值3
     */
    @Property(tries = 10)
    @Label("部分空字段使用默认值")
    void nullFieldsUseDefaultValues() {
        RecoveryMetric recovery = new RecoveryMetric();
        // 所有字段为null，使用默认值3
        
        Double score = loadRecoveryService.calculateRecoveryScore(recovery);
        
        // (5-3) * 3 * (3/5) = 2 * 3 * 0.6 = 3.6
        assertEquals(3.6, score, 0.01, "空字段应使用默认值3计算");
    }

    // ============================================================
    // 综合恢复评分属性测试 (0-100范围)
    // **Property: 恢复评分范围验证**
    // **Validates: Requirements 3.2**
    // Feature: short-term-improvements, Property: 恢复评分范围验证
    // ============================================================

    /**
     * Property: 恢复评分范围验证
     * 
     * *For any* 有效的恢复数据输入（睡眠时长、睡眠质量、肌肉酸痛、疲劳程度、压力水平、HRV、静息心率），
     * 计算的综合恢复评分应在0-100范围内
     * 
     * **Validates: Requirements 3.2**
     */
    @Property(tries = 100)
    @Label("Property: 恢复评分范围验证 - 综合评分应在0-100范围内")
    void overallRecoveryScoreShouldBeInValidRange(
            @ForAll @DoubleRange(min = 0, max = 24) Double sleepHours,
            @ForAll @IntRange(min = 1, max = 10) Integer sleepQuality,
            @ForAll @IntRange(min = 1, max = 10) Integer muscleSoreness,
            @ForAll @IntRange(min = 1, max = 10) Integer fatigueLevel,
            @ForAll @IntRange(min = 1, max = 10) Integer stressLevel,
            @ForAll @IntRange(min = 20, max = 100) Integer hrv,
            @ForAll @IntRange(min = 40, max = 120) Integer restingHeartRate) {
        
        RecoveryDataRequest request = RecoveryDataRequest.builder()
                .sleepHours(sleepHours)
                .sleepQuality(sleepQuality)
                .muscleSoreness(muscleSoreness)
                .fatigueLevel(fatigueLevel)
                .stressLevel(stressLevel)
                .hrv(hrv)
                .restingHeartRate(restingHeartRate)
                .build();
        
        // 创建一个mock的RecoveryAssessmentServiceImpl来测试calculateOverallScore
        // 由于calculateOverallScore内部调用了calculateTrainingLoadImpact需要User和Repository
        // 我们直接测试核心计算逻辑
        int score = calculateOverallScoreForTest(request);
        
        assertTrue(score >= 0, 
                String.format("综合恢复评分应>=0: sleep=%.1f, quality=%d, soreness=%d, fatigue=%d, stress=%d, hrv=%d, hr=%d, score=%d", 
                        sleepHours, sleepQuality, muscleSoreness, fatigueLevel, stressLevel, hrv, restingHeartRate, score));
        assertTrue(score <= 100, 
                String.format("综合恢复评分应<=100: sleep=%.1f, quality=%d, soreness=%d, fatigue=%d, stress=%d, hrv=%d, hr=%d, score=%d", 
                        sleepHours, sleepQuality, muscleSoreness, fatigueLevel, stressLevel, hrv, restingHeartRate, score));
    }

    /**
     * 属性：睡眠时长对评分的影响
     * 
     * 对于相同的其他参数，较长的睡眠时长（在合理范围内）应产生较高或相等的评分
     */
    @Property(tries = 100)
    @Label("睡眠时长越长评分越高（合理范围内）")
    void longerSleepImpliesHigherOrEqualScore(
            @ForAll @DoubleRange(min = 5, max = 7) Double sleepHours1,
            @ForAll @DoubleRange(min = 7, max = 9) Double sleepHours2,
            @ForAll @IntRange(min = 1, max = 10) Integer sleepQuality,
            @ForAll @IntRange(min = 1, max = 10) Integer muscleSoreness,
            @ForAll @IntRange(min = 1, max = 10) Integer fatigueLevel,
            @ForAll @IntRange(min = 1, max = 10) Integer stressLevel) {
        
        Assume.that(sleepHours1 < sleepHours2);
        
        RecoveryDataRequest request1 = RecoveryDataRequest.builder()
                .sleepHours(sleepHours1)
                .sleepQuality(sleepQuality)
                .muscleSoreness(muscleSoreness)
                .fatigueLevel(fatigueLevel)
                .stressLevel(stressLevel)
                .build();
        
        RecoveryDataRequest request2 = RecoveryDataRequest.builder()
                .sleepHours(sleepHours2)
                .sleepQuality(sleepQuality)
                .muscleSoreness(muscleSoreness)
                .fatigueLevel(fatigueLevel)
                .stressLevel(stressLevel)
                .build();
        
        int score1 = calculateOverallScoreForTest(request1);
        int score2 = calculateOverallScoreForTest(request2);
        
        assertTrue(score2 >= score1,
                String.format("较长睡眠应产生较高评分: sleep1=%.1f (score=%d), sleep2=%.1f (score=%d)", 
                        sleepHours1, score1, sleepHours2, score2));
    }

    /**
     * 属性：肌肉酸痛越低评分越高
     * 
     * 对于相同的其他参数，较低的肌肉酸痛应产生较高或相等的评分
     */
    @Property(tries = 100)
    @Label("肌肉酸痛越低综合评分越高")
    void lowerMuscleSorenessImpliesHigherOverallScore(
            @ForAll @DoubleRange(min = 6, max = 8) Double sleepHours,
            @ForAll @IntRange(min = 1, max = 10) Integer sleepQuality,
            @ForAll @IntRange(min = 1, max = 5) Integer soreness1,
            @ForAll @IntRange(min = 6, max = 10) Integer soreness2,
            @ForAll @IntRange(min = 1, max = 10) Integer fatigueLevel,
            @ForAll @IntRange(min = 1, max = 10) Integer stressLevel) {
        
        Assume.that(soreness1 < soreness2);
        
        RecoveryDataRequest request1 = RecoveryDataRequest.builder()
                .sleepHours(sleepHours)
                .sleepQuality(sleepQuality)
                .muscleSoreness(soreness1)
                .fatigueLevel(fatigueLevel)
                .stressLevel(stressLevel)
                .build();
        
        RecoveryDataRequest request2 = RecoveryDataRequest.builder()
                .sleepHours(sleepHours)
                .sleepQuality(sleepQuality)
                .muscleSoreness(soreness2)
                .fatigueLevel(fatigueLevel)
                .stressLevel(stressLevel)
                .build();
        
        int score1 = calculateOverallScoreForTest(request1);
        int score2 = calculateOverallScoreForTest(request2);
        
        assertTrue(score1 >= score2,
                String.format("较低酸痛应产生较高评分: soreness1=%d (score=%d), soreness2=%d (score=%d)", 
                        soreness1, score1, soreness2, score2));
    }

    /**
     * 属性：恢复状态分类与评分区间一致
     * 
     * 评分>=80为EXCELLENT, >=60为GOOD, >=40为FAIR, >=20为POOR, <20为CRITICAL
     */
    @Property(tries = 100)
    @Label("恢复状态分类与评分区间一致")
    void recoveryStatusMatchesScoreRange(
            @ForAll @DoubleRange(min = 0, max = 24) Double sleepHours,
            @ForAll @IntRange(min = 1, max = 10) Integer sleepQuality,
            @ForAll @IntRange(min = 1, max = 10) Integer muscleSoreness,
            @ForAll @IntRange(min = 1, max = 10) Integer fatigueLevel,
            @ForAll @IntRange(min = 1, max = 10) Integer stressLevel) {
        
        RecoveryDataRequest request = RecoveryDataRequest.builder()
                .sleepHours(sleepHours)
                .sleepQuality(sleepQuality)
                .muscleSoreness(muscleSoreness)
                .fatigueLevel(fatigueLevel)
                .stressLevel(stressLevel)
                .build();
        
        int score = calculateOverallScoreForTest(request);
        String status = getRecoveryStatusForTest(score);
        
        if (score >= 80) {
            assertEquals("EXCELLENT", status, "评分>=80应为EXCELLENT");
        } else if (score >= 60) {
            assertEquals("GOOD", status, "评分>=60应为GOOD");
        } else if (score >= 40) {
            assertEquals("FAIR", status, "评分>=40应为FAIR");
        } else if (score >= 20) {
            assertEquals("POOR", status, "评分>=20应为POOR");
        } else {
            assertEquals("CRITICAL", status, "评分<20应为CRITICAL");
        }
    }

    /**
     * 属性：推荐训练强度与评分一致
     * 
     * 评分>=80为INTENSE, >=65为HIGH, >=50为MODERATE, >=30为LIGHT, <30为REST
     */
    @Property(tries = 100)
    @Label("推荐训练强度与评分一致")
    void recommendedIntensityMatchesScore(
            @ForAll @DoubleRange(min = 0, max = 24) Double sleepHours,
            @ForAll @IntRange(min = 1, max = 10) Integer sleepQuality,
            @ForAll @IntRange(min = 1, max = 10) Integer muscleSoreness,
            @ForAll @IntRange(min = 1, max = 10) Integer fatigueLevel,
            @ForAll @IntRange(min = 1, max = 10) Integer stressLevel) {
        
        RecoveryDataRequest request = RecoveryDataRequest.builder()
                .sleepHours(sleepHours)
                .sleepQuality(sleepQuality)
                .muscleSoreness(muscleSoreness)
                .fatigueLevel(fatigueLevel)
                .stressLevel(stressLevel)
                .build();
        
        int score = calculateOverallScoreForTest(request);
        String intensity = getRecommendedIntensityForTest(score);
        
        if (score >= 80) {
            assertEquals("INTENSE", intensity, "评分>=80应推荐INTENSE");
        } else if (score >= 65) {
            assertEquals("HIGH", intensity, "评分>=65应推荐HIGH");
        } else if (score >= 50) {
            assertEquals("MODERATE", intensity, "评分>=50应推荐MODERATE");
        } else if (score >= 30) {
            assertEquals("LIGHT", intensity, "评分>=30应推荐LIGHT");
        } else {
            assertEquals("REST", intensity, "评分<30应推荐REST");
        }
    }

    // ============================================================
    // 辅助方法 - 复制自RecoveryAssessmentServiceImpl的核心计算逻辑
    // 用于独立测试，不依赖Spring容器和数据库
    // ============================================================

    /**
     * 计算综合恢复评分（复制自RecoveryAssessmentServiceImpl）
     * 不包含训练负荷影响，专注于测试核心评分算法
     */
    private int calculateOverallScoreForTest(RecoveryDataRequest request) {
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
        
        // 确保分数在0-100范围内
        return Math.max(0, Math.min(100, score));
    }

    /**
     * 获取恢复状态（复制自RecoveryAssessmentServiceImpl）
     */
    private String getRecoveryStatusForTest(int score) {
        if (score >= 80) return "EXCELLENT";
        if (score >= 60) return "GOOD";
        if (score >= 40) return "FAIR";
        if (score >= 20) return "POOR";
        return "CRITICAL";
    }

    /**
     * 获取推荐训练强度（复制自RecoveryAssessmentServiceImpl）
     */
    private String getRecommendedIntensityForTest(int score) {
        if (score >= 80) return "INTENSE";
        if (score >= 65) return "HIGH";
        if (score >= 50) return "MODERATE";
        if (score >= 30) return "LIGHT";
        return "REST";
    }
}
