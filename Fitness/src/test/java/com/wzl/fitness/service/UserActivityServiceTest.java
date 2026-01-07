package com.wzl.fitness.service;

import com.wzl.fitness.dto.response.DailyActivityDTO;
import com.wzl.fitness.dto.response.UserActivityDTO;
import com.wzl.fitness.entity.Role;
import com.wzl.fitness.repository.NutritionRecordRepository;
import com.wzl.fitness.repository.TrainingRecordRepository;
import com.wzl.fitness.repository.UserRepository;
import com.wzl.fitness.service.impl.UserActivityServiceImpl;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigInteger;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * UserActivityService 单元测试类
 * 测试用户活跃度统计服务的业务逻辑
 */
@ExtendWith(MockitoExtension.class)
@org.mockito.junit.jupiter.MockitoSettings(strictness = org.mockito.quality.Strictness.LENIENT)
public class UserActivityServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private TrainingRecordRepository trainingRecordRepository;

    @Mock
    private NutritionRecordRepository nutritionRecordRepository;

    @Mock
    private EntityManager entityManager;

    @Mock
    private Query query;

    @InjectMocks
    private UserActivityServiceImpl userActivityService;

    @BeforeEach
    void setUp() {
        // 注入 EntityManager
        ReflectionTestUtils.setField(userActivityService, "entityManager", entityManager);
        
        // 设置通用的 mock 行为
        when(entityManager.createNativeQuery(anyString())).thenReturn(query);
        when(query.setParameter(anyString(), any())).thenReturn(query);
    }

    /**
     * 测试获取用户活跃度统计
     */
    @Test
    void testGetUserActivityStats() {
        // Given
        when(userRepository.count()).thenReturn(100L);
        when(userRepository.countByRole(any(Role.class))).thenReturn(50L);
        when(query.getSingleResult()).thenReturn(BigInteger.valueOf(10L));

        // When
        UserActivityDTO result = userActivityService.getUserActivityStats();

        // Then
        assertNotNull(result);
        assertEquals(100L, result.getTotalUsers());
        verify(userRepository, times(1)).count();
    }

    /**
     * 测试获取今日活跃用户数
     */
    @Test
    void testGetActiveUsersToday() {
        // Given
        when(query.getSingleResult()).thenReturn(BigInteger.valueOf(25L));

        // When
        Long result = userActivityService.getActiveUsersToday();

        // Then
        assertEquals(25L, result);
    }

    /**
     * 测试获取本周活跃用户数
     */
    @Test
    void testGetActiveUsersThisWeek() {
        // Given
        when(query.getSingleResult()).thenReturn(BigInteger.valueOf(150L));

        // When
        Long result = userActivityService.getActiveUsersThisWeek();

        // Then
        assertEquals(150L, result);
    }

    /**
     * 测试获取本月活跃用户数
     */
    @Test
    void testGetActiveUsersThisMonth() {
        // Given
        when(query.getSingleResult()).thenReturn(BigInteger.valueOf(500L));

        // When
        Long result = userActivityService.getActiveUsersThisMonth();

        // Then
        assertEquals(500L, result);
    }

    /**
     * 测试获取今日新用户数
     */
    @Test
    void testGetNewUsersToday() {
        // Given
        when(query.getSingleResult()).thenReturn(BigInteger.valueOf(5L));

        // When
        Long result = userActivityService.getNewUsersToday();

        // Then
        assertEquals(5L, result);
    }

    /**
     * 测试获取本周新用户数
     */
    @Test
    void testGetNewUsersThisWeek() {
        // Given
        when(query.getSingleResult()).thenReturn(BigInteger.valueOf(30L));

        // When
        Long result = userActivityService.getNewUsersThisWeek();

        // Then
        assertEquals(30L, result);
    }

    /**
     * 测试获取本月新用户数
     */
    @Test
    void testGetNewUsersThisMonth() {
        // Given
        when(query.getSingleResult()).thenReturn(BigInteger.valueOf(100L));

        // When
        Long result = userActivityService.getNewUsersThisMonth();

        // Then
        assertEquals(100L, result);
    }

    /**
     * 测试获取每日活跃度统计
     */
    @Test
    void testGetDailyActivityStats() {
        // Given
        LocalDate startDate = LocalDate.now().minusDays(6);
        LocalDate endDate = LocalDate.now();
        
        when(query.getSingleResult()).thenReturn(BigInteger.valueOf(10L));

        // When
        List<DailyActivityDTO> result = userActivityService.getDailyActivityStats(startDate, endDate);

        // Then
        assertNotNull(result);
        assertEquals(7, result.size()); // 7 days
    }

    /**
     * 测试查询异常时返回0
     */
    @Test
    void testGetActiveUsersTodayWithException() {
        // Given
        when(entityManager.createNativeQuery(anyString())).thenThrow(new RuntimeException("Database error"));

        // When
        Long result = userActivityService.getActiveUsersToday();

        // Then
        assertEquals(0L, result);
    }

    /**
     * 测试查询结果为null时返回0
     */
    @Test
    void testGetActiveUsersTodayWithNullResult() {
        // Given
        when(query.getSingleResult()).thenReturn(null);

        // When
        Long result = userActivityService.getActiveUsersToday();

        // Then
        assertEquals(0L, result);
    }

    /**
     * 测试用户活跃度统计包含角色分布
     */
    @Test
    void testGetUserActivityStatsWithRoleDistribution() {
        // Given
        when(userRepository.count()).thenReturn(100L);
        when(userRepository.countByRole(Role.USER)).thenReturn(80L);
        when(userRepository.countByRole(Role.ADMIN)).thenReturn(20L);
        when(query.getSingleResult()).thenReturn(BigInteger.valueOf(10L));

        // When
        UserActivityDTO result = userActivityService.getUserActivityStats();

        // Then
        assertNotNull(result);
        assertNotNull(result.getUsersByRole());
        verify(userRepository, atLeastOnce()).countByRole(any(Role.class));
    }

    /**
     * 测试每日活跃度统计包含训练和营养记录
     */
    @Test
    void testDailyActivityStatsIncludesRecords() {
        // Given
        LocalDate today = LocalDate.now();
        
        when(query.getSingleResult()).thenReturn(BigInteger.valueOf(15L));

        // When
        List<DailyActivityDTO> result = userActivityService.getDailyActivityStats(today, today);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        DailyActivityDTO dailyStats = result.get(0);
        assertEquals(today, dailyStats.getDate());
        assertNotNull(dailyStats.getActiveUsers());
        assertNotNull(dailyStats.getNewUsers());
        assertNotNull(dailyStats.getTrainingRecords());
        assertNotNull(dailyStats.getNutritionRecords());
    }
}
