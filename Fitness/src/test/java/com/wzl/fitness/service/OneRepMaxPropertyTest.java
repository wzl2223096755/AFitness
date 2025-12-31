package com.wzl.fitness.service;

import com.wzl.fitness.service.impl.LoadRecoveryServiceImpl;
import net.jqwik.api.*;
import net.jqwik.api.constraints.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 1RM计算属性测试
 * 
 * **Property 4: 1RM估算一致性**
 * *For any* 有效的重量和次数输入，使用不同公式（Epley、Brzycki、Lombardi）计算的1RM值应在合理误差范围内（±15%）
 * 
 * **Validates: Requirements 2.3**
 * 
 * Feature: project-evaluation, Property 4: 1RM估算一致性
 */
public class OneRepMaxPropertyTest {

    private final LoadRecoveryServiceImpl loadRecoveryService = new LoadRecoveryServiceImpl();

    /**
     * Property 4: 1RM估算一致性
     * 
     * 对于任意有效的重量(20-300kg)和次数(1-12)输入，
     * 使用不同公式（Epley、Brzycki、Lombardi）计算的1RM值应在合理误差范围内（±15%）
     * 
     * **Validates: Requirements 2.3**
     */
    @Property(tries = 100)
    @Label("Property 4: 1RM估算一致性 - 不同公式计算结果应在±15%误差范围内")
    void differentFormulasProduceSimilarResults(
            @ForAll @DoubleRange(min = 20.0, max = 300.0) Double weight,
            @ForAll @IntRange(min = 1, max = 12) Integer reps) {
        
        // 计算各公式的1RM
        Double epley = loadRecoveryService.calculateOneRepMax(weight, reps, "Epley");
        Double brzycki = loadRecoveryService.calculateOneRepMax(weight, reps, "Brzycki");
        Double lombardi = loadRecoveryService.calculateOneRepMax(weight, reps, "Lombardi");
        
        // 计算平均值作为参考
        Double average = (epley + brzycki + lombardi) / 3.0;
        
        // 验证每个公式的结果与平均值的偏差不超过15%
        double tolerance = 0.15;
        
        assertTrue(Math.abs(epley - average) / average <= tolerance,
                String.format("Epley公式偏差过大: weight=%.1f, reps=%d, epley=%.2f, avg=%.2f", 
                        weight, reps, epley, average));
        
        assertTrue(Math.abs(brzycki - average) / average <= tolerance,
                String.format("Brzycki公式偏差过大: weight=%.1f, reps=%d, brzycki=%.2f, avg=%.2f", 
                        weight, reps, brzycki, average));
        
        assertTrue(Math.abs(lombardi - average) / average <= tolerance,
                String.format("Lombardi公式偏差过大: weight=%.1f, reps=%d, lombardi=%.2f, avg=%.2f", 
                        weight, reps, lombardi, average));
    }

    /**
     * 属性：1RM应始终大于或等于实际举起的重量
     * 
     * 对于任意有效输入，计算的1RM应该大于等于实际举起的重量
     */
    @Property(tries = 100)
    @Label("1RM应始终大于或等于实际举起的重量")
    void oneRepMaxShouldBeGreaterThanOrEqualToWeight(
            @ForAll @DoubleRange(min = 1.0, max = 500.0) Double weight,
            @ForAll @IntRange(min = 1, max = 30) Integer reps) {
        
        String[] models = {"Epley", "Brzycki", "Lombardi", "OConner", "Mayhew"};
        
        for (String model : models) {
            Double oneRepMax = loadRecoveryService.calculateOneRepMax(weight, reps, model);
            assertTrue(oneRepMax >= weight,
                    String.format("1RM应大于等于重量: model=%s, weight=%.1f, reps=%d, 1RM=%.2f", 
                            model, weight, reps, oneRepMax));
        }
    }

    /**
     * 属性：次数增加时，1RM估算值应该增加
     * 
     * 对于相同重量，更多的次数意味着更高的1RM估算
     */
    @Property(tries = 100)
    @Label("次数增加时1RM估算值应该增加")
    void moreRepsImpliesHigherOneRepMax(
            @ForAll @DoubleRange(min = 20.0, max = 200.0) Double weight,
            @ForAll @IntRange(min = 1, max = 15) Integer reps1,
            @ForAll @IntRange(min = 1, max = 15) Integer reps2) {
        
        Assume.that(reps1 < reps2);
        
        String[] models = {"Epley", "Brzycki", "Lombardi", "OConner", "Mayhew"};
        
        for (String model : models) {
            Double oneRepMax1 = loadRecoveryService.calculateOneRepMax(weight, reps1, model);
            Double oneRepMax2 = loadRecoveryService.calculateOneRepMax(weight, reps2, model);
            
            assertTrue(oneRepMax2 >= oneRepMax1,
                    String.format("更多次数应产生更高1RM: model=%s, weight=%.1f, reps1=%d (1RM=%.2f), reps2=%d (1RM=%.2f)", 
                            model, weight, reps1, oneRepMax1, reps2, oneRepMax2));
        }
    }

    /**
     * 属性：重量增加时，1RM估算值应该成比例增加
     * 
     * 对于相同次数，重量翻倍应该导致1RM也翻倍
     */
    @Property(tries = 100)
    @Label("重量增加时1RM应成比例增加")
    void weightScalesLinearly(
            @ForAll @DoubleRange(min = 20.0, max = 150.0) Double weight,
            @ForAll @IntRange(min = 1, max = 15) Integer reps,
            @ForAll @DoubleRange(min = 1.5, max = 3.0) Double multiplier) {
        
        String[] models = {"Epley", "Brzycki", "Lombardi", "OConner", "Mayhew"};
        
        for (String model : models) {
            Double oneRepMax1 = loadRecoveryService.calculateOneRepMax(weight, reps, model);
            Double oneRepMax2 = loadRecoveryService.calculateOneRepMax(weight * multiplier, reps, model);
            
            // 允许0.01%的浮点误差
            double expectedRatio = multiplier;
            double actualRatio = oneRepMax2 / oneRepMax1;
            
            assertTrue(Math.abs(actualRatio - expectedRatio) < 0.0001,
                    String.format("1RM应与重量成比例: model=%s, weight=%.1f, multiplier=%.2f, expected=%.4f, actual=%.4f", 
                            model, weight, multiplier, expectedRatio, actualRatio));
        }
    }

    /**
     * 属性：无效输入应返回0
     */
    @Property(tries = 50)
    @Label("无效输入应返回0")
    void invalidInputsReturnZero(
            @ForAll @IntRange(min = -10, max = 0) Integer invalidReps) {
        
        Double result = loadRecoveryService.calculateOneRepMax(100.0, invalidReps, "Epley");
        assertEquals(0.0, result, "无效次数应返回0");
    }
}
