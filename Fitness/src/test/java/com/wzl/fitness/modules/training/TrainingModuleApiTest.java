package com.wzl.fitness.modules.training;

import com.wzl.fitness.entity.TrainingRecord;
import com.wzl.fitness.modules.training.api.TrainingModuleApi;
import com.wzl.fitness.modules.training.api.TrainingModuleApiImpl;
import com.wzl.fitness.modules.training.dto.TrainingLoadDTO;
import com.wzl.fitness.modules.training.dto.TrainingRecordDTO;
import com.wzl.fitness.modules.training.dto.TrainingStatsDTO;
import com.wzl.fitness.modules.training.service.TrainingModuleService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * 训练模块API单元测试
 * 
 * 测试TrainingModuleApi接口实现的正确性
 * 
 * **Validates: Requirements 2.1, 2.5, 8.1**
 * 
 * Feature: modular-architecture, Task 4.4
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("训练模块API测试")
class TrainingModuleApiTest {

    @Mock
    private TrainingModuleService trainingModuleService;
    
    private TrainingModuleApi trainingModuleApi;
    
    private TrainingRecord testRecord;
    private TrainingRecordDTO testRecordDTO;
    private TrainingLoadDTO testLoadDTO;
    private TrainingStatsDTO testStatsDTO;
    
    @BeforeEach
    void setUp() {
        trainingModuleApi = new TrainingModuleApiImpl(trainingModuleService);
        
        // 创建测试训练记录
        testRecord = new TrainingRecord();
        testRecord.setId(1L);
        testRecord.setExerciseName("深蹲");
        testRecord.setSets(4);
        testRecord.setReps(8);
        testRecord.setWeight(100.0);
        testRecord.setTrainingDate(LocalDate.now());
        testRecord.setDuration(60);
        
        // 创建测试DTO
        testRecordDTO = TrainingRecordDTO.builder()
                .id(1L)
                .userId(1L)
                .exerciseName("深蹲")
                .exerciseType("力量训练")
                .sets(4)
                .reps(8)
                .weight(100.0)
                .totalVolume(3200.0)
                .trainingDate(LocalDate.now())
                .createdAt(LocalDateTime.now())
                .build();
        
        // 创建测试负荷DTO
        testLoadDTO = TrainingLoadDTO.builder()
                .userId(1L)
                .startDate(LocalDate.now().minusDays(7))
                .endDate(LocalDate.now())
                .totalVolume(15000.0)
                .averageVolume(2142.86)
                .totalSessions(7)
                .acuteLoad(2500.0)
                .chronicLoad(2000.0)
                .acuteChronicRatio(1.25)
                .loadStatus("适中")
                .build();
        
        // 创建测试统计DTO
        testStatsDTO = TrainingStatsDTO.builder()
                .userId(1L)
                .totalRecords(100L)
                .totalVolume(500000.0)
                .totalDuration(6000L)
                .totalSessions(50)
                .averageVolumePerSession(10000.0)
                .mostFrequentExercise("深蹲")
                .personalBestWeight(150.0)
                .build();
    }
    
    @Test
    @DisplayName("getRecentRecords - 返回最近训练记录列表")
    void getRecentRecords_ReturnsRecordList() {
        // Given
        List<TrainingRecord> records = Arrays.asList(testRecord);
        List<TrainingRecordDTO> dtos = Arrays.asList(testRecordDTO);
        
        when(trainingModuleService.getRecentRecords(1L, 10)).thenReturn(records);
        when(trainingModuleService.convertToDTOList(records)).thenReturn(dtos);
        
        // When
        List<TrainingRecordDTO> result = trainingModuleApi.getRecentRecords(1L, 10);
        
        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("深蹲", result.get(0).getExerciseName());
        verify(trainingModuleService).getRecentRecords(1L, 10);
        verify(trainingModuleService).convertToDTOList(records);
    }
    
    @Test
    @DisplayName("getRecentRecords - 无记录时返回空列表")
    void getRecentRecords_WhenNoRecords_ReturnsEmptyList() {
        // Given
        when(trainingModuleService.getRecentRecords(1L, 10)).thenReturn(Collections.emptyList());
        when(trainingModuleService.convertToDTOList(Collections.emptyList())).thenReturn(Collections.emptyList());
        
        // When
        List<TrainingRecordDTO> result = trainingModuleApi.getRecentRecords(1L, 10);
        
        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }
    
    @Test
    @DisplayName("getRecentRecords - limit参数正确传递")
    void getRecentRecords_LimitParameterPassedCorrectly() {
        // Given
        when(trainingModuleService.getRecentRecords(anyLong(), anyInt())).thenReturn(Collections.emptyList());
        when(trainingModuleService.convertToDTOList(any())).thenReturn(Collections.emptyList());
        
        // When
        trainingModuleApi.getRecentRecords(1L, 5);
        trainingModuleApi.getRecentRecords(1L, 20);
        
        // Then
        verify(trainingModuleService).getRecentRecords(1L, 5);
        verify(trainingModuleService).getRecentRecords(1L, 20);
    }
    
    @Test
    @DisplayName("calculateLoad - 返回训练负荷DTO")
    void calculateLoad_ReturnsLoadDTO() {
        // Given
        LocalDate startDate = LocalDate.now().minusDays(7);
        LocalDate endDate = LocalDate.now();
        
        when(trainingModuleService.calculateLoad(1L, startDate, endDate)).thenReturn(testLoadDTO);
        
        // When
        TrainingLoadDTO result = trainingModuleApi.calculateLoad(1L, startDate, endDate);
        
        // Then
        assertNotNull(result);
        assertEquals(1L, result.getUserId());
        assertEquals(15000.0, result.getTotalVolume());
        assertEquals(1.25, result.getAcuteChronicRatio());
        assertEquals("适中", result.getLoadStatus());
        verify(trainingModuleService).calculateLoad(1L, startDate, endDate);
    }
    
    @Test
    @DisplayName("calculateLoad - 日期参数正确传递")
    void calculateLoad_DateParametersPassedCorrectly() {
        // Given
        LocalDate startDate = LocalDate.of(2024, 1, 1);
        LocalDate endDate = LocalDate.of(2024, 1, 31);
        
        when(trainingModuleService.calculateLoad(anyLong(), any(), any())).thenReturn(testLoadDTO);
        
        // When
        trainingModuleApi.calculateLoad(1L, startDate, endDate);
        
        // Then
        verify(trainingModuleService).calculateLoad(1L, startDate, endDate);
    }
    
    @Test
    @DisplayName("getStats - 返回训练统计DTO")
    void getStats_ReturnsStatsDTO() {
        // Given
        when(trainingModuleService.getStats(1L)).thenReturn(testStatsDTO);
        
        // When
        TrainingStatsDTO result = trainingModuleApi.getStats(1L);
        
        // Then
        assertNotNull(result);
        assertEquals(1L, result.getUserId());
        assertEquals(100L, result.getTotalRecords());
        assertEquals(500000.0, result.getTotalVolume());
        assertEquals("深蹲", result.getMostFrequentExercise());
        assertEquals(150.0, result.getPersonalBestWeight());
        verify(trainingModuleService).getStats(1L);
    }
    
    @Test
    @DisplayName("getStats - 不同用户ID正确传递")
    void getStats_UserIdPassedCorrectly() {
        // Given
        when(trainingModuleService.getStats(anyLong())).thenReturn(testStatsDTO);
        
        // When
        trainingModuleApi.getStats(1L);
        trainingModuleApi.getStats(2L);
        trainingModuleApi.getStats(100L);
        
        // Then
        verify(trainingModuleService).getStats(1L);
        verify(trainingModuleService).getStats(2L);
        verify(trainingModuleService).getStats(100L);
    }
    
    @Test
    @DisplayName("API返回的是DTO而非Entity")
    void apiReturnsDTO_NotEntity() {
        // Given
        List<TrainingRecord> records = Arrays.asList(testRecord);
        List<TrainingRecordDTO> dtos = Arrays.asList(testRecordDTO);
        
        when(trainingModuleService.getRecentRecords(1L, 10)).thenReturn(records);
        when(trainingModuleService.convertToDTOList(records)).thenReturn(dtos);
        
        // When
        List<TrainingRecordDTO> result = trainingModuleApi.getRecentRecords(1L, 10);
        
        // Then
        assertNotNull(result);
        assertFalse(result.isEmpty());
        // 验证返回类型是DTO
        assertTrue(result.get(0) instanceof TrainingRecordDTO);
    }
    
    @Test
    @DisplayName("TrainingLoadDTO包含所有必要字段")
    void trainingLoadDTO_ContainsAllRequiredFields() {
        // Given
        when(trainingModuleService.calculateLoad(anyLong(), any(), any())).thenReturn(testLoadDTO);
        
        // When
        TrainingLoadDTO result = trainingModuleApi.calculateLoad(1L, LocalDate.now().minusDays(7), LocalDate.now());
        
        // Then
        assertNotNull(result.getUserId());
        assertNotNull(result.getStartDate());
        assertNotNull(result.getEndDate());
        assertNotNull(result.getTotalVolume());
        assertNotNull(result.getAverageVolume());
        assertNotNull(result.getTotalSessions());
        assertNotNull(result.getAcuteLoad());
        assertNotNull(result.getChronicLoad());
        assertNotNull(result.getAcuteChronicRatio());
        assertNotNull(result.getLoadStatus());
    }
}
