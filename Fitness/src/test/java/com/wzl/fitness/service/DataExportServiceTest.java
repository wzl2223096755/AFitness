package com.wzl.fitness.service;

import com.wzl.fitness.entity.NutritionRecord;
import com.wzl.fitness.entity.TrainingRecord;
import com.wzl.fitness.entity.User;
import com.wzl.fitness.repository.NutritionRecordRepository;
import com.wzl.fitness.repository.TrainingRecordRepository;
import com.wzl.fitness.repository.UserRepository;
import com.wzl.fitness.service.impl.DataExportServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * DataExportService 单元测试类
 * 测试数据导出服务的业务逻辑
 */
@ExtendWith(MockitoExtension.class)
public class DataExportServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private TrainingRecordRepository trainingRecordRepository;

    @Mock
    private NutritionRecordRepository nutritionRecordRepository;

    @InjectMocks
    private DataExportServiceImpl dataExportService;

    private User testUser;
    private TrainingRecord testTrainingRecord;
    private NutritionRecord testNutritionRecord;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");
        testUser.setEmail("test@example.com");

        testTrainingRecord = new TrainingRecord();
        testTrainingRecord.setId(1L);
        testTrainingRecord.setUser(testUser);
        testTrainingRecord.setExerciseName("卧推");
        testTrainingRecord.setSets(3);
        testTrainingRecord.setReps(12);
        testTrainingRecord.setWeight(60.0);
        testTrainingRecord.setTotalVolume(2160.0);
        testTrainingRecord.setTrainingDate(LocalDate.now());
        testTrainingRecord.setDuration(45);

        testNutritionRecord = new NutritionRecord();
        testNutritionRecord.setId(1L);
        testNutritionRecord.setUser(testUser);
        testNutritionRecord.setFoodName("鸡胸肉");
        testNutritionRecord.setCalories(165.0);
        testNutritionRecord.setProtein(31.0);
        testNutritionRecord.setCarbs(0.0);
        testNutritionRecord.setFat(3.6);
        testNutritionRecord.setRecordDate(LocalDate.now());
    }

    /**
     * 测试导出用户数据
     */
    @Test
    void testExportUsersToExcel() {
        // Given
        List<User> users = Arrays.asList(testUser);
        when(userRepository.findAll()).thenReturn(users);

        // When
        byte[] result = dataExportService.exportUsersToExcel();

        // Then
        assertNotNull(result);
        assertTrue(result.length > 0);
        verify(userRepository, times(1)).findAll();
    }

    /**
     * 测试导出训练记录数据
     */
    @Test
    void testExportTrainingRecordsToExcel() {
        // Given
        LocalDate startDate = LocalDate.now().minusDays(30);
        LocalDate endDate = LocalDate.now();
        List<TrainingRecord> records = Arrays.asList(testTrainingRecord);
        
        when(trainingRecordRepository.findByUserIdAndTrainingDateBetweenOrderByTrainingDateDesc(
                eq(1L), eq(startDate), eq(endDate)))
                .thenReturn(records);

        // When
        byte[] result = dataExportService.exportTrainingRecordsToExcel(1L, startDate, endDate);

        // Then
        assertNotNull(result);
        assertTrue(result.length > 0);
        verify(trainingRecordRepository, times(1))
            .findByUserIdAndTrainingDateBetweenOrderByTrainingDateDesc(1L, startDate, endDate);
    }

    /**
     * 测试导出营养记录数据
     */
    @Test
    void testExportNutritionRecordsToExcel() {
        // Given
        LocalDate startDate = LocalDate.now().minusDays(30);
        LocalDate endDate = LocalDate.now();
        List<NutritionRecord> records = Arrays.asList(testNutritionRecord);
        
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(nutritionRecordRepository.findByUserAndRecordDateBetweenOrderByRecordDateAscMealTypeAsc(
                eq(testUser), eq(startDate), eq(endDate)))
                .thenReturn(records);

        // When
        byte[] result = dataExportService.exportNutritionRecordsToExcel(1L, startDate, endDate);

        // Then
        assertNotNull(result);
        assertTrue(result.length > 0);
        verify(nutritionRecordRepository, times(1))
            .findByUserAndRecordDateBetweenOrderByRecordDateAscMealTypeAsc(testUser, startDate, endDate);
    }

    /**
     * 测试导出空用户数据
     */
    @Test
    void testExportUsersToExcelEmpty() {
        // Given
        when(userRepository.findAll()).thenReturn(Arrays.asList());

        // When
        byte[] result = dataExportService.exportUsersToExcel();

        // Then
        assertNotNull(result);
        // 即使没有数据，也应该返回有效的 Excel 文件（包含表头）
        assertTrue(result.length > 0);
    }

    /**
     * 测试导出空训练记录数据
     */
    @Test
    void testExportTrainingRecordsToExcelEmpty() {
        // Given
        LocalDate startDate = LocalDate.now().minusDays(30);
        LocalDate endDate = LocalDate.now();
        
        when(trainingRecordRepository.findByUserIdAndTrainingDateBetweenOrderByTrainingDateDesc(
                eq(1L), eq(startDate), eq(endDate)))
                .thenReturn(Arrays.asList());

        // When
        byte[] result = dataExportService.exportTrainingRecordsToExcel(1L, startDate, endDate);

        // Then
        assertNotNull(result);
        assertTrue(result.length > 0);
    }

    /**
     * 测试导出空营养记录数据
     */
    @Test
    void testExportNutritionRecordsToExcelEmpty() {
        // Given
        LocalDate startDate = LocalDate.now().minusDays(30);
        LocalDate endDate = LocalDate.now();
        
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(nutritionRecordRepository.findByUserAndRecordDateBetweenOrderByRecordDateAscMealTypeAsc(
                eq(testUser), eq(startDate), eq(endDate)))
                .thenReturn(Arrays.asList());

        // When
        byte[] result = dataExportService.exportNutritionRecordsToExcel(1L, startDate, endDate);

        // Then
        assertNotNull(result);
        assertTrue(result.length > 0);
    }

    /**
     * 测试导出训练记录数据 - 日期范围为同一天
     */
    @Test
    void testExportTrainingRecordsToExcelSameDay() {
        // Given
        LocalDate today = LocalDate.now();
        List<TrainingRecord> records = Arrays.asList(testTrainingRecord);
        
        when(trainingRecordRepository.findByUserIdAndTrainingDateBetweenOrderByTrainingDateDesc(
                eq(1L), eq(today), eq(today)))
                .thenReturn(records);

        // When
        byte[] result = dataExportService.exportTrainingRecordsToExcel(1L, today, today);

        // Then
        assertNotNull(result);
        assertTrue(result.length > 0);
    }

    /**
     * 测试导出多条训练记录
     */
    @Test
    void testExportMultipleTrainingRecords() {
        // Given
        LocalDate startDate = LocalDate.now().minusDays(7);
        LocalDate endDate = LocalDate.now();
        
        TrainingRecord record2 = new TrainingRecord();
        record2.setId(2L);
        record2.setUser(testUser);
        record2.setExerciseName("深蹲");
        record2.setSets(4);
        record2.setReps(10);
        record2.setWeight(80.0);
        record2.setTotalVolume(3200.0);
        record2.setTrainingDate(LocalDate.now().minusDays(1));
        record2.setDuration(50);
        
        List<TrainingRecord> records = Arrays.asList(testTrainingRecord, record2);
        
        when(trainingRecordRepository.findByUserIdAndTrainingDateBetweenOrderByTrainingDateDesc(
                eq(1L), eq(startDate), eq(endDate)))
                .thenReturn(records);

        // When
        byte[] result = dataExportService.exportTrainingRecordsToExcel(1L, startDate, endDate);

        // Then
        assertNotNull(result);
        assertTrue(result.length > 0);
    }

    /**
     * 测试导出多条营养记录
     */
    @Test
    void testExportMultipleNutritionRecords() {
        // Given
        LocalDate startDate = LocalDate.now().minusDays(7);
        LocalDate endDate = LocalDate.now();
        
        NutritionRecord record2 = new NutritionRecord();
        record2.setId(2L);
        record2.setUser(testUser);
        record2.setFoodName("糙米饭");
        record2.setCalories(216.0);
        record2.setProtein(5.0);
        record2.setCarbs(45.0);
        record2.setFat(1.8);
        record2.setRecordDate(LocalDate.now().minusDays(1));
        
        List<NutritionRecord> records = Arrays.asList(testNutritionRecord, record2);
        
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(nutritionRecordRepository.findByUserAndRecordDateBetweenOrderByRecordDateAscMealTypeAsc(
                eq(testUser), eq(startDate), eq(endDate)))
                .thenReturn(records);

        // When
        byte[] result = dataExportService.exportNutritionRecordsToExcel(1L, startDate, endDate);

        // Then
        assertNotNull(result);
        assertTrue(result.length > 0);
    }

    /**
     * 测试导出多个用户数据
     */
    @Test
    void testExportMultipleUsersToExcel() {
        // Given
        User user2 = new User();
        user2.setId(2L);
        user2.setUsername("testuser2");
        user2.setEmail("test2@example.com");
        
        List<User> users = Arrays.asList(testUser, user2);
        when(userRepository.findAll()).thenReturn(users);

        // When
        byte[] result = dataExportService.exportUsersToExcel();

        // Then
        assertNotNull(result);
        assertTrue(result.length > 0);
    }
    
    /**
     * 测试导出系统统计数据
     */
    @Test
    void testExportSystemStatsToExcel() {
        // Given
        when(userRepository.count()).thenReturn(10L);
        when(userRepository.countByRole(any())).thenReturn(5L);
        when(trainingRecordRepository.count()).thenReturn(100L);

        // When
        byte[] result = dataExportService.exportSystemStatsToExcel();

        // Then
        assertNotNull(result);
        assertTrue(result.length > 0);
    }
}
