package com.wzl.fitness.modules.training;

import com.wzl.fitness.modules.training.event.TrainingCompletedEvent;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 训练完成事件单元测试
 * 
 * 测试TrainingCompletedEvent事件类的正确性
 * 
 * **Validates: Requirements 3.2, 8.1**
 * 
 * Feature: modular-architecture, Task 4.4
 */
@DisplayName("训练完成事件测试")
class TrainingCompletedEventTest {

    @Test
    @DisplayName("创建基本事件 - 包含必要字段")
    void createBasicEvent_ContainsRequiredFields() {
        // Given
        Long userId = 1L;
        Long trainingRecordId = 100L;
        Double totalVolume = 3200.0;
        String exerciseType = "深蹲";
        
        // When
        TrainingCompletedEvent event = new TrainingCompletedEvent(
                userId, trainingRecordId, totalVolume, exerciseType);
        
        // Then
        assertNotNull(event);
        assertEquals(userId, event.getUserId());
        assertEquals(trainingRecordId, event.getTrainingRecordId());
        assertEquals(totalVolume, event.getTotalVolume());
        assertEquals(exerciseType, event.getExerciseType());
        assertNotNull(event.getEventId());
        assertNotNull(event.getOccurredAt());
    }
    
    @Test
    @DisplayName("创建完整事件 - 包含所有字段")
    void createFullEvent_ContainsAllFields() {
        // Given
        Long userId = 1L;
        Long trainingRecordId = 100L;
        Double totalVolume = 3200.0;
        String exerciseType = "深蹲";
        Double trainingStress = 85.0;
        Integer duration = 60;
        
        // When
        TrainingCompletedEvent event = new TrainingCompletedEvent(
                userId, trainingRecordId, totalVolume, exerciseType, trainingStress, duration);
        
        // Then
        assertNotNull(event);
        assertEquals(userId, event.getUserId());
        assertEquals(trainingRecordId, event.getTrainingRecordId());
        assertEquals(totalVolume, event.getTotalVolume());
        assertEquals(exerciseType, event.getExerciseType());
        assertEquals(trainingStress, event.getTrainingStress());
        assertEquals(duration, event.getDuration());
    }
    
    @Test
    @DisplayName("事件ID唯一性")
    void eventId_IsUnique() {
        // Given & When
        TrainingCompletedEvent event1 = new TrainingCompletedEvent(1L, 100L, 3200.0, "深蹲");
        TrainingCompletedEvent event2 = new TrainingCompletedEvent(1L, 100L, 3200.0, "深蹲");
        
        // Then
        assertNotEquals(event1.getEventId(), event2.getEventId());
    }
    
    @Test
    @DisplayName("事件发生时间自动设置")
    void occurredAt_IsAutoSet() {
        // Given & When
        TrainingCompletedEvent event = new TrainingCompletedEvent(1L, 100L, 3200.0, "深蹲");
        
        // Then
        assertNotNull(event.getOccurredAt());
    }
    
    @Test
    @DisplayName("toString方法返回有意义的字符串")
    void toString_ReturnsReadableString() {
        // Given
        TrainingCompletedEvent event = new TrainingCompletedEvent(1L, 100L, 3200.0, "深蹲");
        
        // When
        String result = event.toString();
        
        // Then
        assertNotNull(result);
        assertTrue(result.contains("TrainingCompletedEvent"));
        assertTrue(result.contains("userId=1"));
        assertTrue(result.contains("trainingRecordId=100"));
        assertTrue(result.contains("totalVolume=3200"));
        assertTrue(result.contains("exerciseType=深蹲"));
    }
    
    @Test
    @DisplayName("基本事件的可选字段为null")
    void basicEvent_OptionalFieldsAreNull() {
        // Given & When
        TrainingCompletedEvent event = new TrainingCompletedEvent(1L, 100L, 3200.0, "深蹲");
        
        // Then
        assertNull(event.getTrainingStress());
        assertNull(event.getDuration());
    }
}
