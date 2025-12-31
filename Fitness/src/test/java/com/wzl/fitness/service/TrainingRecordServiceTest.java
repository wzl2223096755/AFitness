package com.wzl.fitness.service;

import com.wzl.fitness.entity.TrainingRecord;
import com.wzl.fitness.entity.User;
import com.wzl.fitness.repository.TrainingRecordRepository;
import com.wzl.fitness.service.impl.TrainingRecordServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * TrainingRecordService的单元测试类
 * 测试训练记录服务的业务逻辑
 */
@ExtendWith(MockitoExtension.class)
public class TrainingRecordServiceTest {

    @Mock
    private TrainingRecordRepository trainingRecordRepository;

    @InjectMocks
    private TrainingRecordServiceImpl trainingRecordService;

    private TrainingRecord testRecord;

    @BeforeEach
    void setUp() {
        testRecord = new TrainingRecord();
        testRecord.setId(1L);
        User testUser = new User();
        testUser.setId(1L);
        testRecord.setUser(testUser);
        testRecord.setExerciseName("卧推");
        testRecord.setSets(3);
        testRecord.setReps(12);
        testRecord.setWeight(60.0);
        testRecord.setTrainingDate(LocalDate.now());
        testRecord.setDuration(45);
        testRecord.setNotes("测试训练记录");
    }

    /**
     * 测试根据ID查找训练记录
     */
    @Test
    void testFindById() {
        // Given
        when(trainingRecordRepository.findById(1L)).thenReturn(Optional.of(testRecord));

        // When
        Optional<TrainingRecord> result = trainingRecordService.findById(1L);

        // Then
        assertTrue(result.isPresent());
        assertEquals("卧推", result.get().getExerciseName());
        verify(trainingRecordRepository, times(1)).findById(1L);
    }

    /**
     * 测试根据用户ID查找训练记录
     */
    @Test
    void testFindByUserId() {
        // Given
        List<TrainingRecord> records = Arrays.asList(testRecord);
        Pageable pageable = PageRequest.of(0, 10);
        Page<TrainingRecord> page = new PageImpl<>(records, pageable, records.size());
        
        when(trainingRecordRepository.findByUserIdOrderByTrainingDateDesc(1L, pageable))
            .thenReturn(page);

        // When
        Page<TrainingRecord> result = trainingRecordService.findByUserId(1L, 0, 10);

        // Then
        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        assertEquals("卧推", result.getContent().get(0).getExerciseName());
        verify(trainingRecordRepository, times(1))
            .findByUserIdOrderByTrainingDateDesc(1L, pageable);
    }

    /**
     * 测试创建训练记录
     */
    @Test
    void testCreateTrainingRecord() {
        // Given
        TrainingRecord newRecord = new TrainingRecord();
        User testUser = new User();
        testUser.setId(1L);
        newRecord.setUser(testUser);
        newRecord.setExerciseName("深蹲");
        newRecord.setSets(4);
        newRecord.setReps(10);
        newRecord.setWeight(80.0);
        newRecord.setTrainingDate(LocalDate.now());
        newRecord.setDuration(30);

        when(trainingRecordRepository.save(any(TrainingRecord.class))).thenReturn(testRecord);

        // When
        TrainingRecord result = trainingRecordService.createTrainingRecord(newRecord);

        // Then
        assertNotNull(result);
        assertEquals(testRecord.getId(), result.getId());
        verify(trainingRecordRepository, times(1)).save(any(TrainingRecord.class));
    }

    /**
     * 测试更新训练记录
     */
    @Test
    void testUpdateTrainingRecord() {
        // Given
        TrainingRecord updatedRecord = new TrainingRecord();
        updatedRecord.setExerciseName("硬拉");
        updatedRecord.setSets(5);
        updatedRecord.setReps(8);
        updatedRecord.setWeight(100.0);
        updatedRecord.setDuration(60);

        when(trainingRecordRepository.findById(1L)).thenReturn(Optional.of(testRecord));
        when(trainingRecordRepository.save(any(TrainingRecord.class))).thenReturn(testRecord);

        // When
        Optional<TrainingRecord> result = trainingRecordService.updateTrainingRecord(1L, updatedRecord);

        // Then
        assertTrue(result.isPresent());
        verify(trainingRecordRepository, times(1)).findById(1L);
        verify(trainingRecordRepository, times(1)).save(any(TrainingRecord.class));
    }

    /**
     * 测试更新训练记录 - 记录不存在
     */
    @Test
    void testUpdateTrainingRecordNotFound() {
        // Given
        TrainingRecord updatedRecord = new TrainingRecord();
        updatedRecord.setExerciseName("硬拉");

        when(trainingRecordRepository.findById(999L)).thenReturn(Optional.empty());

        // When
        Optional<TrainingRecord> result = trainingRecordService.updateTrainingRecord(999L, updatedRecord);

        // Then
        assertFalse(result.isPresent());
        verify(trainingRecordRepository, times(1)).findById(999L);
        verify(trainingRecordRepository, never()).save(any(TrainingRecord.class));
    }

    /**
     * 测试删除训练记录
     */
    @Test
    void testDeleteTrainingRecord() {
        // Given
        when(trainingRecordRepository.existsById(1L)).thenReturn(true);

        // When
        boolean result = trainingRecordService.deleteTrainingRecord(1L);

        // Then
        assertTrue(result);
        verify(trainingRecordRepository, times(1)).existsById(1L);
        verify(trainingRecordRepository, times(1)).deleteById(1L);
    }

    /**
     * 测试删除训练记录 - 记录不存在
     */
    @Test
    void testDeleteTrainingRecordNotFound() {
        // Given
        when(trainingRecordRepository.existsById(999L)).thenReturn(false);

        // When
        boolean result = trainingRecordService.deleteTrainingRecord(999L);

        // Then
        assertFalse(result);
        verify(trainingRecordRepository, times(1)).existsById(999L);
        verify(trainingRecordRepository, never()).deleteById(999L);
    }

    /**
     * 测试根据日期范围查找训练记录
     */
    @Test
    void testFindByUserIdAndDateRange() {
        // Given
        LocalDate startDate = LocalDate.now().minusDays(7);
        LocalDate endDate = LocalDate.now();
        List<TrainingRecord> records = Arrays.asList(testRecord);
        User testUser = new User();
        testUser.setId(1L);

        when(trainingRecordRepository.findByUserIdAndTrainingDateBetweenOrderByTrainingDateDesc(
            eq(1L), eq(startDate), eq(endDate)))
            .thenReturn(records);

        // When
        List<TrainingRecord> result = trainingRecordService.findByUserIdAndDateRange(1L, startDate, endDate);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(trainingRecordRepository, times(1))
            .findByUserIdAndTrainingDateBetweenOrderByTrainingDateDesc(1L, startDate, endDate);
    }

    /**
     * 测试获取训练统计信息
     */
    @Test
    void testGetTrainingStats() {
        // Given
        LocalDate startDate = LocalDate.now().minusDays(7);
        LocalDate endDate = LocalDate.now();
        
        when(trainingRecordRepository.countByUserIdAndTrainingDateBetween(1L, startDate, endDate))
            .thenReturn(5L);
        when(trainingRecordRepository.sumVolumeByUserIdAndDateRange(1L, startDate, endDate))
            .thenReturn(1500.0);
        when(trainingRecordRepository.sumDurationByUserIdAndDateRange(1L, startDate, endDate))
            .thenReturn(240L);

        // When
        var stats = trainingRecordService.getTrainingStats(1L, startDate, endDate);

        // Then
        assertNotNull(stats);
        assertEquals(5, stats.getTotalRecords());
        assertEquals(1500.0, stats.getTotalVolume());
        assertEquals(240, stats.getTotalDuration());
        verify(trainingRecordRepository, times(1))
            .countByUserIdAndTrainingDateBetween(1L, startDate, endDate);
        verify(trainingRecordRepository, times(1))
            .sumVolumeByUserIdAndDateRange(1L, startDate, endDate);
        verify(trainingRecordRepository, times(1))
            .sumDurationByUserIdAndDateRange(1L, startDate, endDate);
    }

    /**
     * 测试获取用户最近训练记录
     */
    @Test
    void testGetRecentTrainingRecords() {
        // Given
        List<TrainingRecord> records = Arrays.asList(testRecord);
        when(trainingRecordRepository.findTop10ByUserIdOrderByTrainingDateDesc(1L))
            .thenReturn(records);

        // When
        List<TrainingRecord> result = trainingRecordService.getRecentTrainingRecords(1L);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(trainingRecordRepository, times(1))
            .findTop10ByUserIdOrderByTrainingDateDesc(1L);
    }

    /**
     * 测试获取训练记录总数
     */
    @Test
    void testCountByUserId() {
        // Given
        when(trainingRecordRepository.countByUserId(1L)).thenReturn(10L);

        // When
        long result = trainingRecordService.countByUserId(1L);

        // Then
        assertEquals(10L, result);
        verify(trainingRecordRepository, times(1)).countByUserId(1L);
    }
}