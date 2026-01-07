package com.wzl.fitness.service;

import com.wzl.fitness.dto.response.AnalyticsDataResponse;
import com.wzl.fitness.dto.response.DashboardMetricsResponse;
import com.wzl.fitness.dto.response.TrainingRecordResponse;
import com.wzl.fitness.dto.response.UserStatsOverviewResponse;
import com.wzl.fitness.entity.RecoveryMetric;
import com.wzl.fitness.entity.TrainingRecord;
import com.wzl.fitness.entity.User;
import com.wzl.fitness.repository.BodyRecordRepository;
import com.wzl.fitness.repository.RecoveryMetricRepository;
import com.wzl.fitness.repository.TrainingRecordRepository;
import com.wzl.fitness.repository.UserRepository;
import com.wzl.fitness.service.impl.DashboardServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * DashboardService 单元测试类
 * 测试仪表盘服务的业务逻辑
 */
@ExtendWith(MockitoExtension.class)
@org.mockito.junit.jupiter.MockitoSettings(strictness = org.mockito.quality.Strictness.LENIENT)
public class DashboardServiceTest {

    @Mock
    private TrainingRecordRepository trainingRecordRepository;

    @Mock
    private RecoveryMetricRepository recoveryMetricRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private BodyRecordRepository bodyRecordRepository;

    @InjectMocks
    private DashboardServiceImpl dashboardService;

    private User testUser;
    private TrainingRecord testRecord;
    private RecoveryMetric testRecoveryMetric;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");

        testRecord = new TrainingRecord();
        testRecord.setId(1L);
        testRecord.setUser(testUser);
        testRecord.setExerciseName("卧推");
        testRecord.setSets(3);
        testRecord.setReps(12);
        testRecord.setWeight(60.0);
        testRecord.setTotalVolume(2160.0);
        testRecord.setTrainingDate(LocalDate.now());
        testRecord.setDuration(45);

        testRecoveryMetric = new RecoveryMetric();
        testRecoveryMetric.setId(1L);
        testRecoveryMetric.setUser(testUser);
        testRecoveryMetric.setRecordDate(LocalDate.now());
        testRecoveryMetric.setSleepQuality(4);
        testRecoveryMetric.setMuscleSoreness(2);
        testRecoveryMetric.setSubjectiveEnergy(4);
    }

    /**
     * 测试获取仪表盘指标概览 - 周视图
     */
    @Test
    void testGetMetricsOverviewWeek() {
        List<TrainingRecord> records = Arrays.asList(testRecord);
        List<RecoveryMetric> recoveryMetrics = Arrays.asList(testRecoveryMetric);
        
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(trainingRecordRepository.findByUserAndTrainingDateBetweenOrderByTrainingDateDesc(
                eq(testUser), any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(records);
        when(recoveryMetricRepository.findByUserAndRecordDateBetween(
                eq(testUser), any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(recoveryMetrics);

        DashboardMetricsResponse result = dashboardService.getMetricsOverview(1L, "week");

        assertNotNull(result);
        assertEquals(1, result.getWeeklyTrainingCount());
        verify(userRepository, atLeast(1)).findById(1L);
    }

    /**
     * 测试获取仪表盘指标概览 - 月视图
     */
    @Test
    void testGetMetricsOverviewMonth() {
        List<TrainingRecord> records = Arrays.asList(testRecord);
        List<RecoveryMetric> recoveryMetrics = Arrays.asList(testRecoveryMetric);
        
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(trainingRecordRepository.findByUserAndTrainingDateBetweenOrderByTrainingDateDesc(
                eq(testUser), any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(records);
        when(recoveryMetricRepository.findByUserAndRecordDateBetween(
                eq(testUser), any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(recoveryMetrics);

        DashboardMetricsResponse result = dashboardService.getMetricsOverview(1L, "month");

        assertNotNull(result);
        verify(userRepository, atLeast(1)).findById(1L);
    }

    /**
     * 测试获取用户统计概览
     */
    @Test
    void testGetUserStatsOverview() {
        List<TrainingRecord> currentRecords = Arrays.asList(testRecord);
        List<TrainingRecord> prevRecords = new ArrayList<>();
        
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(trainingRecordRepository.findByUserAndTrainingDateBetweenOrderByTrainingDateDesc(
                eq(testUser), any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(currentRecords)
                .thenReturn(prevRecords);

        UserStatsOverviewResponse result = dashboardService.getUserStatsOverview(1L);

        assertNotNull(result);
        assertEquals(1, result.getWeeklyTrainingCount());
        assertEquals(1, result.getWeeklyChange());
        verify(userRepository, atLeast(1)).findById(1L);
    }

    /**
     * 测试获取分析数据
     */
    @Test
    void testGetAnalyticsData() {
        List<TrainingRecord> currentRecords = Arrays.asList(testRecord);
        List<TrainingRecord> prevRecords = new ArrayList<>();
        List<RecoveryMetric> recoveryMetrics = Arrays.asList(testRecoveryMetric);
        
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(trainingRecordRepository.findByUserAndTrainingDateBetweenOrderByTrainingDateDesc(
                eq(testUser), any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(currentRecords)
                .thenReturn(prevRecords);
        when(recoveryMetricRepository.findByUserAndRecordDateBetween(
                eq(testUser), any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(recoveryMetrics);

        AnalyticsDataResponse result = dashboardService.getAnalyticsData(1L, "week");

        assertNotNull(result);
        assertEquals(1, result.getTotalWorkouts());
        verify(userRepository, atLeast(1)).findById(1L);
    }

    /**
     * 测试获取最近训练记录
     */
    @Test
    void testGetRecentTrainingRecords() {
        List<TrainingRecord> records = Arrays.asList(testRecord);
        
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(trainingRecordRepository.findTop10ByUserOrderByTrainingDateDesc(testUser))
                .thenReturn(records);

        List<TrainingRecordResponse> result = dashboardService.getRecentTrainingRecords(1L);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("卧推", result.get(0).getExerciseName());
        verify(trainingRecordRepository, times(1)).findTop10ByUserOrderByTrainingDateDesc(testUser);
    }

    /**
     * 测试获取训练趋势数据
     */
    @Test
    void testGetTrainingTrend() {
        List<TrainingRecord> records = Arrays.asList(testRecord);
        
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(trainingRecordRepository.findByUserAndTrainingDateBetweenOrderByTrainingDateDesc(
                eq(testUser), any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(records);

        Object result = dashboardService.getTrainingTrend(1L, "week");

        assertNotNull(result);
        verify(userRepository, atLeast(1)).findById(1L);
    }

    /**
     * 测试获取身体指标数据
     */
    @Test
    void testGetBodyMetrics() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(bodyRecordRepository.findByUserOrderByRecordTimeDesc(testUser))
                .thenReturn(new ArrayList<>());

        Object result = dashboardService.getBodyMetrics(1L, "week");

        assertNotNull(result);
        verify(bodyRecordRepository, times(1)).findByUserOrderByRecordTimeDesc(testUser);
    }

    /**
     * 测试获取训练分布数据
     */
    @Test
    void testGetTrainingDistribution() {
        List<TrainingRecord> records = Arrays.asList(testRecord);
        
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(trainingRecordRepository.findByUserAndTrainingDateBetweenOrderByTrainingDateDesc(
                eq(testUser), any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(records);

        Object result = dashboardService.getTrainingDistribution(1L, "week");

        assertNotNull(result);
        verify(userRepository, atLeast(1)).findById(1L);
    }

    /**
     * 测试用户不存在时抛出异常
     */
    @Test
    void testGetMetricsOverviewUserNotFound() {
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(Exception.class, () -> {
            dashboardService.getMetricsOverview(999L, "week");
        });
    }

    /**
     * 测试空训练记录时的处理
     */
    @Test
    void testGetMetricsOverviewEmptyRecords() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(trainingRecordRepository.findByUserAndTrainingDateBetweenOrderByTrainingDateDesc(
                eq(testUser), any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(new ArrayList<>());
        when(recoveryMetricRepository.findByUserAndRecordDateBetween(
                eq(testUser), any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(new ArrayList<>());

        DashboardMetricsResponse result = dashboardService.getMetricsOverview(1L, "week");

        assertNotNull(result);
        assertEquals(0, result.getWeeklyTrainingCount());
    }

    /**
     * 测试年视图的指标概览
     */
    @Test
    void testGetMetricsOverviewYear() {
        List<TrainingRecord> records = Arrays.asList(testRecord);
        List<RecoveryMetric> recoveryMetrics = Arrays.asList(testRecoveryMetric);
        
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(trainingRecordRepository.findByUserAndTrainingDateBetweenOrderByTrainingDateDesc(
                eq(testUser), any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(records);
        when(recoveryMetricRepository.findByUserAndRecordDateBetween(
                eq(testUser), any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(recoveryMetrics);

        DashboardMetricsResponse result = dashboardService.getMetricsOverview(1L, "year");

        assertNotNull(result);
        verify(userRepository, atLeast(1)).findById(1L);
    }

    /**
     * 测试获取周概览
     */
    @Test
    void testGetWeeklyOverview() {
        List<TrainingRecord> records = Arrays.asList(testRecord);
        List<TrainingRecord> prevRecords = new ArrayList<>();
        List<RecoveryMetric> recoveryMetrics = Arrays.asList(testRecoveryMetric);
        
        when(trainingRecordRepository.findByUserAndTrainingDateBetweenOrderByTrainingDateDesc(
                eq(testUser), any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(records)
                .thenReturn(prevRecords);
        when(recoveryMetricRepository.findByUserAndRecordDateBetween(
                eq(testUser), any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(recoveryMetrics);
        when(trainingRecordRepository.findByUserIdAndTrainingDateBetween(
                eq(1L), any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(new ArrayList<>());

        var result = dashboardService.getWeeklyOverview(testUser);

        assertNotNull(result);
    }
}
