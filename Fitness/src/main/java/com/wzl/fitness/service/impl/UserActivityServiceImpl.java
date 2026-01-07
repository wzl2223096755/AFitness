package com.wzl.fitness.service.impl;

import com.wzl.fitness.dto.response.DailyActivityDTO;
import com.wzl.fitness.dto.response.UserActivityDTO;
import com.wzl.fitness.entity.Role;
import com.wzl.fitness.repository.NutritionRecordRepository;
import com.wzl.fitness.repository.TrainingRecordRepository;
import com.wzl.fitness.repository.UserRepository;
import com.wzl.fitness.service.UserActivityService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import java.math.BigInteger;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.TemporalAdjusters;
import java.util.*;

/**
 * 用户活跃度统计服务实现
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class UserActivityServiceImpl implements UserActivityService {

    private final UserRepository userRepository;
    private final TrainingRecordRepository trainingRecordRepository;
    private final NutritionRecordRepository nutritionRecordRepository;
    
    @PersistenceContext
    private EntityManager entityManager;

    @Override
    @Cacheable(value = "userActivity", key = "'stats'", unless = "#result == null")
    public UserActivityDTO getUserActivityStats() {
        log.info("计算用户活跃度统计");
        
        // 获取总用户数
        long totalUsers = userRepository.count();
        
        // 获取活跃用户数
        Long activeUsersToday = getActiveUsersToday();
        Long activeUsersWeek = getActiveUsersThisWeek();
        Long activeUsersMonth = getActiveUsersThisMonth();
        
        // 获取新用户数
        Long newUsersToday = getNewUsersToday();
        Long newUsersWeek = getNewUsersThisWeek();
        Long newUsersMonth = getNewUsersThisMonth();
        
        // 按角色统计用户数
        Map<String, Long> usersByRole = new HashMap<>();
        for (Role role : Role.values()) {
            Long count = userRepository.countByRole(role);
            usersByRole.put(role.name(), count != null ? count : 0L);
        }
        
        // 获取最近30天的每日活跃度
        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusDays(29);
        List<DailyActivityDTO> dailyActivity = getDailyActivityStats(startDate, endDate);
        
        return UserActivityDTO.builder()
                .totalUsers(totalUsers)
                .activeUsersToday(activeUsersToday)
                .activeUsersWeek(activeUsersWeek)
                .activeUsersMonth(activeUsersMonth)
                .newUsersToday(newUsersToday)
                .newUsersWeek(newUsersWeek)
                .newUsersMonth(newUsersMonth)
                .usersByRole(usersByRole)
                .dailyActivity(dailyActivity)
                .build();
    }

    @Override
    public List<DailyActivityDTO> getDailyActivityStats(LocalDate startDate, LocalDate endDate) {
        log.debug("获取每日活跃度统计: {} 到 {}", startDate, endDate);
        
        List<DailyActivityDTO> result = new ArrayList<>();
        LocalDate currentDate = startDate;
        
        while (!currentDate.isAfter(endDate)) {
            DailyActivityDTO dailyStats = getDailyStats(currentDate);
            result.add(dailyStats);
            currentDate = currentDate.plusDays(1);
        }
        
        return result;
    }

    @Override
    public Long getActiveUsersToday() {
        LocalDate today = LocalDate.now();
        return countActiveUsersByDateRange(today, today);
    }

    @Override
    public Long getActiveUsersThisWeek() {
        LocalDate today = LocalDate.now();
        LocalDate weekStart = today.with(TemporalAdjusters.previousOrSame(java.time.DayOfWeek.MONDAY));
        return countActiveUsersByDateRange(weekStart, today);
    }

    @Override
    public Long getActiveUsersThisMonth() {
        LocalDate today = LocalDate.now();
        LocalDate monthStart = today.with(TemporalAdjusters.firstDayOfMonth());
        return countActiveUsersByDateRange(monthStart, today);
    }

    @Override
    public Long getNewUsersToday() {
        LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
        LocalDateTime endOfDay = LocalDate.now().atTime(LocalTime.MAX);
        return countNewUsersByDateRange(startOfDay, endOfDay);
    }

    @Override
    public Long getNewUsersThisWeek() {
        LocalDate today = LocalDate.now();
        LocalDate weekStart = today.with(TemporalAdjusters.previousOrSame(java.time.DayOfWeek.MONDAY));
        LocalDateTime startDateTime = weekStart.atStartOfDay();
        LocalDateTime endDateTime = today.atTime(LocalTime.MAX);
        return countNewUsersByDateRange(startDateTime, endDateTime);
    }

    @Override
    public Long getNewUsersThisMonth() {
        LocalDate today = LocalDate.now();
        LocalDate monthStart = today.with(TemporalAdjusters.firstDayOfMonth());
        LocalDateTime startDateTime = monthStart.atStartOfDay();
        LocalDateTime endDateTime = today.atTime(LocalTime.MAX);
        return countNewUsersByDateRange(startDateTime, endDateTime);
    }
    
    /**
     * 获取指定日期的统计数据
     */
    private DailyActivityDTO getDailyStats(LocalDate date) {
        // 统计活跃用户（有登录记录的用户）
        Long activeUsers = countActiveUsersByDateRange(date, date);
        
        // 统计新注册用户
        LocalDateTime startOfDay = date.atStartOfDay();
        LocalDateTime endOfDay = date.atTime(LocalTime.MAX);
        Long newUsers = countNewUsersByDateRange(startOfDay, endOfDay);
        
        // 统计训练记录数
        Long trainingRecords = countTrainingRecordsByDate(date);
        
        // 统计营养记录数
        Long nutritionRecords = countNutritionRecordsByDate(date);
        
        return DailyActivityDTO.builder()
                .date(date)
                .activeUsers(activeUsers)
                .newUsers(newUsers)
                .trainingRecords(trainingRecords)
                .nutritionRecords(nutritionRecords)
                .build();
    }
    
    /**
     * 统计指定日期范围内的活跃用户数
     * 活跃用户定义：在该日期范围内有登录记录的用户
     */
    private Long countActiveUsersByDateRange(LocalDate startDate, LocalDate endDate) {
        try {
            LocalDateTime startDateTime = startDate.atStartOfDay();
            LocalDateTime endDateTime = endDate.atTime(LocalTime.MAX);
            
            String sql = "SELECT COUNT(DISTINCT u.id) FROM user_table u " +
                        "WHERE u.last_login_at BETWEEN :startDate AND :endDate";
            
            Query query = entityManager.createNativeQuery(sql);
            query.setParameter("startDate", startDateTime);
            query.setParameter("endDate", endDateTime);
            
            Object result = query.getSingleResult();
            return convertToLong(result);
        } catch (Exception e) {
            log.warn("统计活跃用户失败: {}", e.getMessage());
            return 0L;
        }
    }
    
    /**
     * 统计指定日期范围内的新注册用户数
     */
    private Long countNewUsersByDateRange(LocalDateTime startDateTime, LocalDateTime endDateTime) {
        try {
            String sql = "SELECT COUNT(*) FROM user_table u " +
                        "WHERE u.created_at BETWEEN :startDate AND :endDate";
            
            Query query = entityManager.createNativeQuery(sql);
            query.setParameter("startDate", startDateTime);
            query.setParameter("endDate", endDateTime);
            
            Object result = query.getSingleResult();
            return convertToLong(result);
        } catch (Exception e) {
            log.warn("统计新用户失败: {}", e.getMessage());
            return 0L;
        }
    }
    
    /**
     * 统计指定日期的训练记录数
     */
    private Long countTrainingRecordsByDate(LocalDate date) {
        try {
            String sql = "SELECT COUNT(*) FROM training_records tr " +
                        "WHERE tr.training_date = :date AND (tr.deleted = false OR tr.deleted IS NULL)";
            
            Query query = entityManager.createNativeQuery(sql);
            query.setParameter("date", date);
            
            Object result = query.getSingleResult();
            return convertToLong(result);
        } catch (Exception e) {
            log.warn("统计训练记录失败: {}", e.getMessage());
            return 0L;
        }
    }
    
    /**
     * 统计指定日期的营养记录数
     */
    private Long countNutritionRecordsByDate(LocalDate date) {
        try {
            String sql = "SELECT COUNT(*) FROM nutrition_records nr " +
                        "WHERE nr.record_date = :date AND (nr.deleted = false OR nr.deleted IS NULL)";
            
            Query query = entityManager.createNativeQuery(sql);
            query.setParameter("date", date);
            
            Object result = query.getSingleResult();
            return convertToLong(result);
        } catch (Exception e) {
            log.warn("统计营养记录失败: {}", e.getMessage());
            return 0L;
        }
    }
    
    /**
     * 将查询结果转换为 Long 类型
     */
    private Long convertToLong(Object result) {
        if (result == null) {
            return 0L;
        }
        if (result instanceof Long) {
            return (Long) result;
        }
        if (result instanceof BigInteger) {
            return ((BigInteger) result).longValue();
        }
        if (result instanceof Number) {
            return ((Number) result).longValue();
        }
        return 0L;
    }
}
