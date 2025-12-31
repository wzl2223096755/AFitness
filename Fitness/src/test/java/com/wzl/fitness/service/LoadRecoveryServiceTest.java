package com.wzl.fitness.service;

import com.wzl.fitness.entity.RecoveryMetric;
import com.wzl.fitness.repository.FitnessDataRepository;
import com.wzl.fitness.service.impl.LoadRecoveryServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;

/**
 * LoadRecoveryService的单元测试类
 * 测试核心算法：1RM计算、恢复评分、训练量计算
 * 
 * Requirements: 2.3, 2.4, 8.1
 */
@ExtendWith(MockitoExtension.class)
public class LoadRecoveryServiceTest {

    @Mock
    private FitnessDataRepository fitnessDataRepository;

    @InjectMocks
    private LoadRecoveryServiceImpl loadRecoveryService;

    @Nested
    @DisplayName("1RM计算算法测试")
    class OneRepMaxCalculationTests {

        /**
         * 测试Epley公式: 1RM = 重量 * (1 + 次数 / 30.0)
         */
        @Test
        @DisplayName("Epley公式计算1RM - 标准输入")
        void testEpleyFormula() {
            // Given: 100kg, 5次
            Double weight = 100.0;
            Integer reps = 5;
            
            // When
            Double result = loadRecoveryService.calculateOneRepMax(weight, reps, "Epley");
            
            // Then: 100 * (1 + 5/30) = 100 * 1.1667 ≈ 116.67
            assertEquals(116.67, result, 0.01);
        }

        /**
         * 测试Brzycki公式: 1RM = 重量 * 36 / (37 - 次数)
         */
        @Test
        @DisplayName("Brzycki公式计算1RM - 标准输入")
        void testBrzyckiFormula() {
            // Given: 100kg, 5次
            Double weight = 100.0;
            Integer reps = 5;
            
            // When
            Double result = loadRecoveryService.calculateOneRepMax(weight, reps, "Brzycki");
            
            // Then: 100 * 36 / (37 - 5) = 100 * 36 / 32 = 112.5
            assertEquals(112.5, result, 0.01);
        }

        /**
         * 测试Lombardi公式: 1RM = 重量 * 次数^0.1
         */
        @Test
        @DisplayName("Lombardi公式计算1RM - 标准输入")
        void testLombardiFormula() {
            // Given: 100kg, 5次
            Double weight = 100.0;
            Integer reps = 5;
            
            // When
            Double result = loadRecoveryService.calculateOneRepMax(weight, reps, "Lombardi");
            
            // Then: 100 * 5^0.1 ≈ 100 * 1.1746 ≈ 117.46
            double expected = weight * Math.pow(reps, 0.1);
            assertEquals(expected, result, 0.01);
        }

        /**
         * 测试O'Conner公式: 1RM = 重量 * (1 + 次数 / 40.0)
         */
        @Test
        @DisplayName("OConner公式计算1RM - 标准输入")
        void testOConnerFormula() {
            // Given: 100kg, 5次
            Double weight = 100.0;
            Integer reps = 5;
            
            // When
            Double result = loadRecoveryService.calculateOneRepMax(weight, reps, "OConner");
            
            // Then: 100 * (1 + 5/40) = 100 * 1.125 = 112.5
            assertEquals(112.5, result, 0.01);
        }

        /**
         * 测试Mayhew公式: 1RM = 重量 * (100 / (48.8 + 53.8 * exp(-0.075 * 次数)))
         */
        @Test
        @DisplayName("Mayhew公式计算1RM - 标准输入")
        void testMayhewFormula() {
            // Given: 100kg, 5次
            Double weight = 100.0;
            Integer reps = 5;
            
            // When
            Double result = loadRecoveryService.calculateOneRepMax(weight, reps, "Mayhew");
            
            // Then: 100 * (100 / (48.8 + 53.8 * exp(-0.375)))
            double expected = weight * (100 / (48.8 + 53.8 * Math.exp(-0.075 * reps)));
            assertEquals(expected, result, 0.01);
        }

        /**
         * 测试默认公式（Epley）
         */
        @Test
        @DisplayName("默认公式（无模型参数）使用Epley")
        void testDefaultFormula() {
            // Given
            Double weight = 100.0;
            Integer reps = 5;
            
            // When
            Double resultDefault = loadRecoveryService.calculateOneRepMax(weight, reps);
            Double resultEpley = loadRecoveryService.calculateOneRepMax(weight, reps, "Epley");
            
            // Then
            assertEquals(resultEpley, resultDefault);
        }

        /**
         * 测试空模型参数默认使用Epley
         */
        @Test
        @DisplayName("空模型参数默认使用Epley")
        void testEmptyModelDefaultsToEpley() {
            // Given
            Double weight = 100.0;
            Integer reps = 5;
            
            // When
            Double resultEmpty = loadRecoveryService.calculateOneRepMax(weight, reps, "");
            Double resultNull = loadRecoveryService.calculateOneRepMax(weight, reps, null);
            Double resultEpley = loadRecoveryService.calculateOneRepMax(weight, reps, "Epley");
            
            // Then
            assertEquals(resultEpley, resultEmpty);
            assertEquals(resultEpley, resultNull);
        }

        /**
         * 测试无效输入返回0
         */
        @Test
        @DisplayName("无效输入返回0")
        void testInvalidInputReturnsZero() {
            // When & Then
            assertEquals(0.0, loadRecoveryService.calculateOneRepMax(null, 5, "Epley"));
            assertEquals(0.0, loadRecoveryService.calculateOneRepMax(100.0, null, "Epley"));
            assertEquals(0.0, loadRecoveryService.calculateOneRepMax(100.0, 0, "Epley"));
            assertEquals(0.0, loadRecoveryService.calculateOneRepMax(100.0, -1, "Epley"));
        }

        /**
         * 测试1次重复时1RM等于重量
         */
        @Test
        @DisplayName("1次重复时1RM接近重量本身")
        void testSingleRepEqualsWeight() {
            // Given
            Double weight = 100.0;
            Integer reps = 1;
            
            // When
            Double resultEpley = loadRecoveryService.calculateOneRepMax(weight, reps, "Epley");
            
            // Then: 100 * (1 + 1/30) ≈ 103.33
            assertEquals(103.33, resultEpley, 0.01);
        }

        /**
         * 测试高次数输入
         */
        @Test
        @DisplayName("高次数输入（10次）")
        void testHighReps() {
            // Given
            Double weight = 100.0;
            Integer reps = 10;
            
            // When
            Double resultEpley = loadRecoveryService.calculateOneRepMax(weight, reps, "Epley");
            Double resultBrzycki = loadRecoveryService.calculateOneRepMax(weight, reps, "Brzycki");
            
            // Then
            // Epley: 100 * (1 + 10/30) = 133.33
            assertEquals(133.33, resultEpley, 0.01);
            // Brzycki: 100 * 36 / (37 - 10) = 133.33
            assertEquals(133.33, resultBrzycki, 0.01);
        }
    }

    @Nested
    @DisplayName("恢复评分算法测试")
    class RecoveryScoreCalculationTests {

        /**
         * 测试恢复评分计算 - 标准输入
         */
        @Test
        @DisplayName("恢复评分计算 - 标准输入")
        void testRecoveryScoreCalculation() {
            // Given
            RecoveryMetric recovery = new RecoveryMetric();
            recovery.setMuscleSoreness(2);    // 1-5, 低值表示恢复好
            recovery.setSleepQuality(4);       // 1-5, 高值表示睡眠好
            recovery.setSubjectiveEnergy(4);   // 1-5, 高值表示精力好
            
            // When
            Double score = loadRecoveryService.calculateRecoveryScore(recovery);
            
            // Then: (5 - 2) * 4 * (4 / 5.0) = 3 * 4 * 0.8 = 9.6
            assertEquals(9.6, score, 0.01);
        }

        /**
         * 测试恢复评分 - 最佳状态
         */
        @Test
        @DisplayName("恢复评分 - 最佳状态")
        void testRecoveryScoreBestCase() {
            // Given
            RecoveryMetric recovery = new RecoveryMetric();
            recovery.setMuscleSoreness(1);    // 最低酸痛
            recovery.setSleepQuality(5);       // 最佳睡眠
            recovery.setSubjectiveEnergy(5);   // 最高精力
            
            // When
            Double score = loadRecoveryService.calculateRecoveryScore(recovery);
            
            // Then: (5 - 1) * 5 * (5 / 5.0) = 4 * 5 * 1 = 20
            assertEquals(20.0, score, 0.01);
        }

        /**
         * 测试恢复评分 - 最差状态
         */
        @Test
        @DisplayName("恢复评分 - 最差状态")
        void testRecoveryScoreWorstCase() {
            // Given
            RecoveryMetric recovery = new RecoveryMetric();
            recovery.setMuscleSoreness(5);    // 最高酸痛
            recovery.setSleepQuality(1);       // 最差睡眠
            recovery.setSubjectiveEnergy(1);   // 最低精力
            
            // When
            Double score = loadRecoveryService.calculateRecoveryScore(recovery);
            
            // Then: (5 - 5) * 1 * (1 / 5.0) = 0 * 1 * 0.2 = 0
            assertEquals(0.0, score, 0.01);
        }

        /**
         * 测试恢复评分 - 空输入
         */
        @Test
        @DisplayName("恢复评分 - 空输入返回0")
        void testRecoveryScoreNullInput() {
            // When & Then
            assertEquals(0.0, loadRecoveryService.calculateRecoveryScore(null));
        }

        /**
         * 测试恢复评分 - 部分空字段使用默认值
         */
        @Test
        @DisplayName("恢复评分 - 部分空字段使用默认值")
        void testRecoveryScoreWithNullFields() {
            // Given
            RecoveryMetric recovery = new RecoveryMetric();
            // 所有字段为null，使用默认值3
            
            // When
            Double score = loadRecoveryService.calculateRecoveryScore(recovery);
            
            // Then: (5 - 3) * 3 * (3 / 5.0) = 2 * 3 * 0.6 = 3.6
            assertEquals(3.6, score, 0.01);
        }
    }

    @Nested
    @DisplayName("训练量计算测试")
    class TrainingVolumeCalculationTests {

        /**
         * 测试训练量计算 - 标准输入
         */
        @Test
        @DisplayName("训练量计算 - 标准输入")
        void testVolumeCalculation() {
            // Given: 100kg, 3组, 10次
            Double weight = 100.0;
            Integer sets = 3;
            Integer reps = 10;
            
            // When
            Double volume = loadRecoveryService.calculateVolume(weight, sets, reps);
            
            // Then: 100 * 3 * 10 = 3000
            assertEquals(3000.0, volume, 0.01);
        }

        /**
         * 测试训练量计算 - 空输入
         */
        @Test
        @DisplayName("训练量计算 - 空输入返回0")
        void testVolumeCalculationNullInput() {
            // When & Then
            assertEquals(0.0, loadRecoveryService.calculateVolume(null, 3, 10));
            assertEquals(0.0, loadRecoveryService.calculateVolume(100.0, null, 10));
            assertEquals(0.0, loadRecoveryService.calculateVolume(100.0, 3, null));
        }

        /**
         * 测试训练量计算 - 边界值
         */
        @Test
        @DisplayName("训练量计算 - 边界值")
        void testVolumeCalculationBoundary() {
            // Given: 最小有效输入
            Double weight = 0.0;
            Integer sets = 1;
            Integer reps = 1;
            
            // When
            Double volume = loadRecoveryService.calculateVolume(weight, sets, reps);
            
            // Then
            assertEquals(0.0, volume, 0.01);
        }

        /**
         * 测试训练量计算 - 大数值
         */
        @Test
        @DisplayName("训练量计算 - 大数值")
        void testVolumeCalculationLargeValues() {
            // Given: 大重量训练
            Double weight = 200.0;
            Integer sets = 5;
            Integer reps = 12;
            
            // When
            Double volume = loadRecoveryService.calculateVolume(weight, sets, reps);
            
            // Then: 200 * 5 * 12 = 12000
            assertEquals(12000.0, volume, 0.01);
        }
    }

    @Nested
    @DisplayName("训练压力计算测试")
    class TrainingStressCalculationTests {

        /**
         * 测试训练压力计算 - 标准输入
         */
        @Test
        @DisplayName("训练压力计算 - 标准输入")
        void testTrainingStressCalculation() {
            // Given: 训练量3000, RPE 8
            Double volume = 3000.0;
            Double rpe = 8.0;
            
            // When
            Double stress = loadRecoveryService.calculateTrainingStress(volume, rpe);
            
            // Then: 3000 * (8 / 10) = 2400
            assertEquals(2400.0, stress, 0.01);
        }

        /**
         * 测试训练压力计算 - 空输入
         */
        @Test
        @DisplayName("训练压力计算 - 空输入返回0")
        void testTrainingStressNullInput() {
            // When & Then
            assertEquals(0.0, loadRecoveryService.calculateTrainingStress(null, 8.0));
            assertEquals(0.0, loadRecoveryService.calculateTrainingStress(3000.0, null));
        }

        /**
         * 测试训练压力计算 - 最大RPE
         */
        @Test
        @DisplayName("训练压力计算 - 最大RPE")
        void testTrainingStressMaxRpe() {
            // Given: 训练量3000, RPE 10
            Double volume = 3000.0;
            Double rpe = 10.0;
            
            // When
            Double stress = loadRecoveryService.calculateTrainingStress(volume, rpe);
            
            // Then: 3000 * (10 / 10) = 3000
            assertEquals(3000.0, stress, 0.01);
        }
    }

    @Nested
    @DisplayName("热量消耗计算测试")
    class CaloriesCalculationTests {

        /**
         * 测试热量消耗计算 - 标准输入
         */
        @Test
        @DisplayName("热量消耗计算 - 标准输入")
        void testCaloriesCalculation() {
            // Given: 60分钟, 强度5
            Double duration = 60.0;
            Double intensity = 5.0;
            
            // When
            Double calories = loadRecoveryService.calculateCalories(duration, intensity);
            
            // Then: 60 * 5 * 1.2 = 360
            assertEquals(360.0, calories, 0.01);
        }

        /**
         * 测试热量消耗计算 - 空时长
         */
        @Test
        @DisplayName("热量消耗计算 - 空时长返回0")
        void testCaloriesNullDuration() {
            // When & Then
            assertEquals(0.0, loadRecoveryService.calculateCalories(null, 5.0));
        }

        /**
         * 测试热量消耗计算 - 空强度使用默认值
         */
        @Test
        @DisplayName("热量消耗计算 - 空强度使用默认值")
        void testCaloriesNullIntensity() {
            // Given: 60分钟, 空强度（默认5）
            Double duration = 60.0;
            
            // When
            Double calories = loadRecoveryService.calculateCalories(duration, null);
            
            // Then: 60 * 5 * 1.2 = 360
            assertEquals(360.0, calories, 0.01);
        }
    }

    @Nested
    @DisplayName("支持的1RM模型测试")
    class SupportedModelsTests {

        /**
         * 测试获取支持的1RM模型列表
         */
        @Test
        @DisplayName("获取支持的1RM模型列表")
        void testGetSupportedModels() {
            // When
            var models = loadRecoveryService.getSupportedOneRepMaxModels();
            
            // Then
            assertNotNull(models);
            assertEquals(5, models.size());
            assertTrue(models.contains("Epley"));
            assertTrue(models.contains("Brzycki"));
            assertTrue(models.contains("Lombardi"));
            assertTrue(models.contains("OConner"));
            assertTrue(models.contains("Mayhew"));
        }
    }
}
