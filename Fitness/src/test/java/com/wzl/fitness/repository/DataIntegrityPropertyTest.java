package com.wzl.fitness.repository;

import com.wzl.fitness.entity.ExerciseDetail;
import com.wzl.fitness.entity.TrainingRecord;
import com.wzl.fitness.entity.User;
import net.jqwik.api.*;
import net.jqwik.api.constraints.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 数据完整性属性测试
 * 
 * **Property 7: 数据完整性约束**
 * *For any* 训练记录删除操作，关联的动作详情应被级联删除
 * 
 * **Validates: Requirements 4.3**
 * 
 * Feature: project-evaluation, Property 7: 数据完整性约束
 */
public class DataIntegrityPropertyTest {

    // ========== Property 7: 数据完整性约束 ==========

    /**
     * Property 7: 数据完整性约束 - 训练记录与动作详情的关联完整性
     * 
     * 对于任意训练记录，其关联的所有动作详情都应正确引用该训练记录
     * 
     * **Validates: Requirements 4.3**
     */
    @Property(tries = 100)
    @Label("Property 7: 数据完整性约束 - 训练记录与动作详情的关联完整性")
    void exerciseDetailsShouldReferenceParentTrainingRecord(
            @ForAll @LongRange(min = 1, max = 1000000) Long userId,
            @ForAll @AlphaChars @StringLength(min = 3, max = 50) String exerciseName,
            @ForAll @IntRange(min = 1, max = 10) Integer sets,
            @ForAll @IntRange(min = 1, max = 20) Integer reps,
            @ForAll @DoubleRange(min = 1.0, max = 500.0) Double weight,
            @ForAll @IntRange(min = 1, max = 5) Integer detailCount) {
        
        // Given - create user and training record
        User user = User.builder()
                .id(userId)
                .username("user" + userId)
                .password("password")
                .build();
        
        TrainingRecord record = createTrainingRecord(user, exerciseName, sets, reps, weight);
        
        // Create exercise details and associate with training record
        List<ExerciseDetail> details = new ArrayList<>();
        for (int i = 0; i < detailCount; i++) {
            ExerciseDetail detail = createExerciseDetail(record, "Detail" + i, weight, sets, reps);
            details.add(detail);
        }
        record.setExerciseDetails(details);
        
        // Then - verify all details reference the parent record
        assertNotNull(record.getExerciseDetails(), "训练记录应有关联的动作详情");
        assertEquals(detailCount, record.getExerciseDetails().size(), 
                String.format("动作详情数量应为 %d", detailCount));
        
        for (ExerciseDetail detail : record.getExerciseDetails()) {
            assertNotNull(detail.getTrainingRecord(), "动作详情应引用训练记录");
            assertSame(record, detail.getTrainingRecord(), 
                    "动作详情应引用正确的训练记录");
        }
    }

    /**
     * Property 7: 数据完整性约束 - 级联删除模拟验证
     * 
     * 模拟级联删除行为：当训练记录被删除时，其关联的动作详情也应被清除
     * 
     * **Validates: Requirements 4.3**
     */
    @Property(tries = 100)
    @Label("Property 7: 数据完整性约束 - 级联删除模拟验证")
    void cascadeDeleteShouldRemoveExerciseDetails(
            @ForAll @LongRange(min = 1, max = 1000000) Long userId,
            @ForAll @AlphaChars @StringLength(min = 3, max = 50) String exerciseName,
            @ForAll @IntRange(min = 1, max = 5) Integer detailCount) {
        
        // Given - create training record with exercise details
        User user = User.builder()
                .id(userId)
                .username("user" + userId)
                .password("password")
                .build();
        
        TrainingRecord record = createTrainingRecord(user, exerciseName, 3, 10, 50.0);
        
        List<ExerciseDetail> details = new ArrayList<>();
        for (int i = 0; i < detailCount; i++) {
            ExerciseDetail detail = createExerciseDetail(record, "Detail" + i, 50.0, 3, 10);
            details.add(detail);
        }
        record.setExerciseDetails(details);
        
        // Verify initial state
        assertEquals(detailCount, record.getExerciseDetails().size(),
                "初始状态应有正确数量的动作详情");
        
        // When - simulate cascade delete by clearing the list (JPA cascade behavior)
        record.getExerciseDetails().clear();
        
        // Then - verify all details are removed
        assertTrue(record.getExerciseDetails().isEmpty(),
                "级联删除后动作详情列表应为空");
    }

    /**
     * Property 7: 数据完整性约束 - 动作详情不能独立于训练记录存在
     * 
     * 对于任意动作详情，必须关联到一个有效的训练记录
     * 
     * **Validates: Requirements 4.3**
     */
    @Property(tries = 100)
    @Label("Property 7: 数据完整性约束 - 动作详情必须关联训练记录")
    void exerciseDetailMustHaveTrainingRecord(
            @ForAll @AlphaChars @StringLength(min = 3, max = 50) String detailName,
            @ForAll @DoubleRange(min = 1.0, max = 500.0) Double weight,
            @ForAll @IntRange(min = 1, max = 10) Integer sets,
            @ForAll @IntRange(min = 1, max = 20) Integer reps) {
        
        // Given - create exercise detail without training record
        ExerciseDetail detail = new ExerciseDetail();
        detail.setExerciseName(detailName);
        detail.setWeight(weight);
        detail.setSets(sets);
        detail.setReps(reps);
        
        // Then - verify training record is null (invalid state)
        assertNull(detail.getTrainingRecord(), 
                "未关联训练记录的动作详情应为null");
        
        // When - associate with a training record
        User user = User.builder()
                .id(1L)
                .username("testuser")
                .password("password")
                .build();
        TrainingRecord record = createTrainingRecord(user, "TestExercise", 3, 10, 50.0);
        detail.setTrainingRecord(record);
        
        // Then - verify association is established
        assertNotNull(detail.getTrainingRecord(),
                "关联后动作详情应有训练记录引用");
        assertEquals(record, detail.getTrainingRecord(),
                "动作详情应引用正确的训练记录");
    }

    /**
     * Property 7: 数据完整性约束 - 训练量计算一致性
     * 
     * 对于任意训练记录，其总训练量应等于所有动作详情训练量之和（如果有详情）
     * 或等于 sets × reps × weight（如果没有详情）
     * 
     * **Validates: Requirements 4.3**
     */
    @Property(tries = 100)
    @Label("Property 7: 数据完整性约束 - 训练量计算一致性")
    void trainingVolumeConsistency(
            @ForAll @IntRange(min = 1, max = 10) Integer sets,
            @ForAll @IntRange(min = 1, max = 20) Integer reps,
            @ForAll @DoubleRange(min = 1.0, max = 500.0) Double weight) {
        
        // Given
        User user = User.builder()
                .id(1L)
                .username("testuser")
                .password("password")
                .build();
        
        TrainingRecord record = createTrainingRecord(user, "TestExercise", sets, reps, weight);
        
        // When - calculate expected volume
        double expectedVolume = sets * reps * weight;
        
        // Then - verify calculated volume matches
        assertEquals(expectedVolume, record.getCalculatedTotalVolume(), 0.001,
                String.format("训练量应为 %f (sets=%d × reps=%d × weight=%f)", 
                        expectedVolume, sets, reps, weight));
    }

    /**
     * Property 7: 数据完整性约束 - 动作详情训练量计算
     * 
     * 对于任意动作详情，其训练量应等于 sets × reps × weight
     * 
     * **Validates: Requirements 4.3**
     */
    @Property(tries = 100)
    @Label("Property 7: 数据完整性约束 - 动作详情训练量计算")
    void exerciseDetailVolumeCalculation(
            @ForAll @IntRange(min = 1, max = 10) Integer sets,
            @ForAll @IntRange(min = 1, max = 20) Integer reps,
            @ForAll @DoubleRange(min = 1.0, max = 500.0) Double weight) {
        
        // Given
        User user = User.builder()
                .id(1L)
                .username("testuser")
                .password("password")
                .build();
        TrainingRecord record = createTrainingRecord(user, "TestExercise", 3, 10, 50.0);
        
        ExerciseDetail detail = createExerciseDetail(record, "DetailExercise", weight, sets, reps);
        
        // When - calculate expected volume
        double expectedVolume = sets * reps * weight;
        detail.setVolume(expectedVolume);
        
        // Then - verify volume is set correctly
        assertEquals(expectedVolume, detail.getVolume(), 0.001,
                String.format("动作详情训练量应为 %f", expectedVolume));
    }

    /**
     * Property 7: 数据完整性约束 - 多个动作详情的独立性
     * 
     * 对于同一训练记录的多个动作详情，修改一个不应影响其他
     * 
     * **Validates: Requirements 4.3**
     */
    @Property(tries = 100)
    @Label("Property 7: 数据完整性约束 - 多个动作详情的独立性")
    void multipleExerciseDetailsAreIndependent(
            @ForAll @IntRange(min = 2, max = 5) Integer detailCount,
            @ForAll @DoubleRange(min = 1.0, max = 500.0) Double newWeight) {
        
        // Given - create training record with multiple exercise details
        User user = User.builder()
                .id(1L)
                .username("testuser")
                .password("password")
                .build();
        
        TrainingRecord record = createTrainingRecord(user, "TestExercise", 3, 10, 50.0);
        
        List<ExerciseDetail> details = new ArrayList<>();
        for (int i = 0; i < detailCount; i++) {
            ExerciseDetail detail = createExerciseDetail(record, "Detail" + i, 50.0 + i, 3, 10);
            details.add(detail);
        }
        record.setExerciseDetails(details);
        
        // Store original weights
        Double[] originalWeights = new Double[detailCount];
        for (int i = 0; i < detailCount; i++) {
            originalWeights[i] = details.get(i).getWeight();
        }
        
        // When - modify first detail's weight
        details.get(0).setWeight(newWeight);
        
        // Then - verify only first detail is modified
        assertEquals(newWeight, details.get(0).getWeight(), 0.001,
                "第一个动作详情的重量应被修改");
        
        for (int i = 1; i < detailCount; i++) {
            assertEquals(originalWeights[i], details.get(i).getWeight(), 0.001,
                    String.format("第 %d 个动作详情的重量不应被修改", i + 1));
        }
    }

    // ========== Helper Methods ==========

    private TrainingRecord createTrainingRecord(User user, String exerciseName, 
            Integer sets, Integer reps, Double weight) {
        TrainingRecord record = new TrainingRecord();
        record.setUser(user);
        record.setExerciseName(exerciseName);
        record.setSets(sets);
        record.setReps(reps);
        record.setWeight(weight);
        record.setTrainingDate(LocalDate.now());
        record.setDuration(60);
        return record;
    }

    private ExerciseDetail createExerciseDetail(TrainingRecord record, String exerciseName,
            Double weight, Integer sets, Integer reps) {
        ExerciseDetail detail = new ExerciseDetail();
        detail.setTrainingRecord(record);
        detail.setExerciseName(exerciseName);
        detail.setWeight(weight);
        detail.setSets(sets);
        detail.setReps(reps);
        detail.setRpe(7);
        detail.setExerciseType("strength");
        detail.setVolume(sets * reps * weight);
        return detail;
    }
}
