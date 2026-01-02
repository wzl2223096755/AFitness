package com.wzl.fitness.service.impl;

import com.wzl.fitness.exception.BusinessException;
import com.wzl.fitness.common.ResponseCode;
import com.wzl.fitness.dto.response.DashboardMetricsResponse;
import com.wzl.fitness.dto.response.UserStatsOverviewResponse;
import com.wzl.fitness.dto.response.AnalyticsDataResponse;
import com.wzl.fitness.dto.response.TrainingRecordResponse;
import com.wzl.fitness.service.DashboardService;
import com.wzl.fitness.entity.User;
import com.wzl.fitness.entity.TrainingRecord;
import com.wzl.fitness.entity.RecoveryMetric;
import com.wzl.fitness.repository.TrainingRecordRepository;
import com.wzl.fitness.repository.RecoveryMetricRepository;
import com.wzl.fitness.repository.UserRepository;
import com.wzl.fitness.repository.BodyRecordRepository;
import com.wzl.fitness.entity.BodyRecord;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 仪表盘服务实现类
 */
@Service
@Slf4j
public class DashboardServiceImpl implements DashboardService {

    private static final Logger log = LoggerFactory.getLogger(DashboardServiceImpl.class);

    private final TrainingRecordRepository trainingRecordRepository;
    private final RecoveryMetricRepository recoveryMetricRepository;
    private final UserRepository userRepository;
    private final BodyRecordRepository bodyRecordRepository;
    
    public DashboardServiceImpl(TrainingRecordRepository trainingRecordRepository, 
                               RecoveryMetricRepository recoveryMetricRepository,
                               UserRepository userRepository,
                               BodyRecordRepository bodyRecordRepository) {
        this.trainingRecordRepository = trainingRecordRepository;
        this.recoveryMetricRepository = recoveryMetricRepository;
        this.userRepository = userRepository;
        this.bodyRecordRepository = bodyRecordRepository;
    }
    
    /**
     * 根据ID获取用户
     */
    private User getUserById(Long userId) {
        return userRepository.findById(userId)
            .orElseThrow(() -> new BusinessException(ResponseCode.USER_NOT_FOUND.getCode(), "未找到指定用户，ID: " + userId));
    }

    @Override
    public DashboardMetricsResponse getMetricsOverview(Long userId, String timeRange) {
        log.info("获取仪表盘指标概览，用户ID: {}, 时间范围: {}", userId, timeRange);
        
        User currentUser = getUserById(userId);
        LocalDate[] dateRange = getDateRange(timeRange);
        LocalDate startDate = dateRange[0];
        LocalDate endDate = dateRange[1];
        
        // 从数据库获取训练记录
        List<TrainingRecord> trainingRecords = trainingRecordRepository
            .findByUserAndTrainingDateBetweenOrderByTrainingDateDesc(currentUser, startDate, endDate);
        
        // 从数据库获取恢复指标
        List<RecoveryMetric> recoveryMetrics = recoveryMetricRepository
            .findByUserAndRecordDateBetween(currentUser, startDate, endDate);
        
        // 计算指标数据
        DashboardMetricsResponse metrics = new DashboardMetricsResponse();
        metrics.setWeeklyTrainingCount(trainingRecords.size());
        metrics.setTotalVolume(calculateTotalVolume(trainingRecords));
        metrics.setRecoveryScore(calculateRecoveryScore(recoveryMetrics));
        metrics.setGoalCompletionRate(calculateGoalCompletionRate(trainingRecords, timeRange));
        metrics.setSleepQuality(calculateAverageSleepQuality(recoveryMetrics));
        metrics.setMuscleFatigue(calculateAverageMuscleSoreness(recoveryMetrics));
        metrics.setMentalState(calculateAverageSubjectiveEnergy(recoveryMetrics));
        
        // 计算周变化（与上一周比较）
        LocalDate[] prevDateRange = getPreviousDateRange(timeRange);
        List<TrainingRecord> prevTrainingRecords = trainingRecordRepository
            .findByUserAndTrainingDateBetweenOrderByTrainingDateDesc(currentUser, prevDateRange[0], prevDateRange[1]);
        metrics.setWeeklyChange(trainingRecords.size() - prevTrainingRecords.size());
        
        // 设置目标数据
        List<Map<String, Object>> goals = new ArrayList<>();
        goals.add(createGoal(1, "训练次数", trainingRecords.size(), getTargetTrainingCount(timeRange)));
        goals.add(createGoal(2, "总重量", metrics.getTotalVolume(), getTargetTotalVolume(timeRange)));
        goals.add(createGoal(3, "有氧时长", calculateTotalDuration(trainingRecords), getTargetAerobicDuration(timeRange)));
        goals.add(createGoal(4, "拉伸次数", calculateStretchingCount(trainingRecords), getTargetStretchingCount(timeRange)));
        metrics.setGoals(goals);
        
        return metrics;
    }

    @Override
    public UserStatsOverviewResponse getUserStatsOverview(Long userId) {
        log.info("获取用户统计概览，用户ID: {}", userId);
        
        User currentUser = getUserById(userId);
        
        // 获取本周数据
        LocalDate[] currentWeekRange = getDateRange("week");
        List<TrainingRecord> currentWeekRecords = trainingRecordRepository
            .findByUserAndTrainingDateBetweenOrderByTrainingDateDesc(currentUser, currentWeekRange[0], currentWeekRange[1]);
        
        // 获取上周数据
        LocalDate[] prevWeekRange = getPreviousDateRange("week");
        List<TrainingRecord> prevWeekRecords = trainingRecordRepository
            .findByUserAndTrainingDateBetweenOrderByTrainingDateDesc(currentUser, prevWeekRange[0], prevWeekRange[1]);
            
        UserStatsOverviewResponse stats = new UserStatsOverviewResponse();
        
        // 1. 训练次数
        stats.setWeeklyTrainingCount(currentWeekRecords.size());
        stats.setWeeklyChange(currentWeekRecords.size() - prevWeekRecords.size());
        
        // 2. 训练时长 (小时)
        double currentHours = currentWeekRecords.stream().mapToDouble(r -> r.getDuration() / 60.0).sum();
        double prevHours = prevWeekRecords.stream().mapToDouble(r -> r.getDuration() / 60.0).sum();
        stats.setTotalTrainingHours(Math.round(currentHours * 10.0) / 10.0);
        stats.setTrainingHoursChange(Math.round((currentHours - prevHours) * 10.0) / 10.0);
        
        // 3. 消耗卡路里
        int currentCalories = currentWeekRecords.stream().mapToInt(TrainingRecord::getCaloriesBurned).sum();
        int prevCalories = prevWeekRecords.stream().mapToInt(TrainingRecord::getCaloriesBurned).sum();
        stats.setTotalCalories(currentCalories);
        stats.setCaloriesChange(currentCalories - prevCalories);
        
        // 4. 目标完成率
        int currentRate = calculateGoalCompletionRate(currentWeekRecords, "week");
        int prevRate = calculateGoalCompletionRate(prevWeekRecords, "week");
        stats.setGoalCompletionRate(currentRate);
        stats.setGoalChange(currentRate - prevRate);
        
        return stats;
    }

    @Override
    public AnalyticsDataResponse getAnalyticsData(Long userId, String timeRange) {
        log.info("获取分析数据，用户ID: {}, 时间范围: {}", userId, timeRange);
        
        User currentUser = getUserById(userId);
        LocalDate[] currentRange = getDateRange(timeRange);
        List<TrainingRecord> currentRecords = trainingRecordRepository
            .findByUserAndTrainingDateBetweenOrderByTrainingDateDesc(currentUser, currentRange[0], currentRange[1]);
            
        LocalDate[] prevRange = getPreviousDateRange(timeRange);
        List<TrainingRecord> prevRecords = trainingRecordRepository
            .findByUserAndTrainingDateBetweenOrderByTrainingDateDesc(currentUser, prevRange[0], prevRange[1]);
            
        AnalyticsDataResponse analytics = new AnalyticsDataResponse();
        
        // 1. 基础统计
        long currentVolume = calculateTotalVolume(currentRecords);
        long prevVolume = calculateTotalVolume(prevRecords);
        int currentDuration = currentRecords.stream().mapToInt(TrainingRecord::getDuration).sum();
        int prevDuration = prevRecords.stream().mapToInt(TrainingRecord::getDuration).sum();
        
        analytics.setTotalWorkouts(currentRecords.size());
        analytics.setTotalVolume(currentVolume);
        analytics.setTotalDuration(currentDuration);
        
        // 平均强度 (基于重量/1RM的简化计算，这里暂用平均重量)
        double avgIntensity = currentRecords.stream()
            .mapToDouble(TrainingRecord::getWeight)
            .average()
            .orElse(0.0);
        analytics.setAvgIntensity(Math.round(avgIntensity * 10.0) / 10.0);
        
        // 2. 增长率计算
        analytics.setWorkoutIncrease(currentRecords.size() - prevRecords.size());
        analytics.setVolumeIncrease((int)(currentVolume - prevVolume));
        analytics.setDurationChange(currentDuration - prevDuration);
        
        double prevAvgIntensity = prevRecords.stream()
            .mapToDouble(TrainingRecord::getWeight)
            .average()
            .orElse(0.0);
        analytics.setIntensityIncrease((int)(avgIntensity - prevAvgIntensity));
        
        // 3. 性能详情 (根据实际数据生成建议)
        List<Map<String, Object>> performanceDetails = new ArrayList<>();
        performanceDetails.add(createPerformanceDetail("力量表现", calculateScore(currentVolume, prevVolume), 
            currentVolume >= prevVolume ? "您的力量水平在持续提升，继续保持当前训练强度" : "近期训练量有所下降，建议适当增加负荷"));
        
        long enduranceScore = calculateScore((long)currentDuration, (long)prevDuration);
        performanceDetails.add(createPerformanceDetail("耐力水平", (int)enduranceScore, 
            enduranceScore >= 80 ? "耐力表现优秀，建议继续保持有氧比例" : "耐力表现良好，建议增加训练时长"));
            
        performanceDetails.add(createPerformanceDetail("恢复能力", calculateRecoveryScore(userId, currentRange), 
            "根据近期恢复指标，您的身体恢复状态良好"));
            
        analytics.setPerformanceDetails(performanceDetails);
        
        return analytics;
    }

    /**
     * 计算得分 (基于当前与之前的比例，基准分为80)
     */
    private int calculateScore(Long current, Long previous) {
        if (previous == 0) return current > 0 ? 80 : 0;
        double ratio = (double) current / previous;
        int score = (int) (80 * ratio);
        return Math.min(100, Math.max(40, score));
    }

    /**
     * 计算特定时间范围的恢复分数
     */
    private int calculateRecoveryScore(Long userId, LocalDate[] range) {
        User user = getUserById(userId);
        List<RecoveryMetric> metrics = recoveryMetricRepository
            .findByUserAndRecordDateBetween(user, range[0], range[1]);
        return calculateRecoveryScore(metrics);
    }

    @Override
    public List<TrainingRecordResponse> getRecentTrainingRecords(Long userId) {
        log.info("获取最近训练记录，用户ID: {}", userId);
        
        User currentUser = getUserById(userId);
        List<TrainingRecord> recentRecords = trainingRecordRepository
            .findTop10ByUserOrderByTrainingDateDesc(currentUser);
        
        // 转换为DTO - 不加载懒加载的集合属性
        return recentRecords.stream().map(record -> {
            TrainingRecordResponse response = new TrainingRecordResponse();
            response.setId(record.getId());
            response.setExerciseName(record.getExerciseName());
            response.setWeight(record.getWeight());
            response.setSets(record.getSets());
            response.setReps(record.getReps());
            response.setTotalVolume(record.getCalculatedTotalVolume().doubleValue());
            response.setTrainingDate(record.getTrainingDate());
            response.setDuration(record.getDuration());
            response.setNotes(record.getNotes());
            response.setCreatedAt(record.getCreatedAt());
            // 不设置 exerciseDetails，避免懒加载异常
            return response;
        }).collect(Collectors.toList());
    }

    @Override
    public Object getTrainingTrend(Long userId, String timeRange) {
        log.info("获取训练趋势数据，用户ID: {}, 时间范围: {}", userId, timeRange);
        
        User currentUser = getUserById(userId);
        LocalDate[] dateRange = getDateRange(timeRange);
        LocalDate startDate = dateRange[0];
        LocalDate endDate = dateRange[1];
        
        // 获取时间范围内的所有训练记录
        List<TrainingRecord> trainingRecords = trainingRecordRepository
            .findByUserAndTrainingDateBetweenOrderByTrainingDateDesc(currentUser, startDate, endDate);
        
        // 准备日期列表
        List<String> labels = getDateList(timeRange);
        List<Long> volumes = new ArrayList<>();
        List<Long> durations = new ArrayList<>();
        
        if ("year".equals(timeRange)) {
            // 按月份分组 (YYYY-MM)
            Map<String, List<TrainingRecord>> recordsByMonth = trainingRecords.stream()
                .collect(Collectors.groupingBy(r -> r.getTrainingDate().toString().substring(0, 7)));
                
            for (String monthStr : labels) {
                List<TrainingRecord> monthRecords = recordsByMonth.getOrDefault(monthStr, Collections.emptyList());
                volumes.add(monthRecords.stream().mapToLong(r -> r.getCalculatedTotalVolume().longValue()).sum());
                durations.add(monthRecords.stream().mapToLong(TrainingRecord::getDuration).sum());
            }
        } else {
            // 按日期分组 (YYYY-MM-DD)
            Map<LocalDate, List<TrainingRecord>> recordsByDate = trainingRecords.stream()
                .collect(Collectors.groupingBy(TrainingRecord::getTrainingDate));
                
            for (String dateStr : labels) {
                LocalDate date = LocalDate.parse(dateStr);
                List<TrainingRecord> dayRecords = recordsByDate.getOrDefault(date, Collections.emptyList());
                volumes.add(dayRecords.stream().mapToLong(r -> r.getCalculatedTotalVolume().longValue()).sum());
                durations.add(dayRecords.stream().mapToLong(TrainingRecord::getDuration).sum());
            }
        }
        
        Map<String, Object> trend = new HashMap<>();
        trend.put("dates", labels);
        trend.put("volumes", volumes);
        trend.put("durations", durations);
        
        return trend;
    }

    @Override
    public Object getBodyMetrics(Long userId, String timeRange) {
        log.info("获取身体指标数据，用户ID: {}, 时间范围: {}", userId, timeRange);
        
        User currentUser = getUserById(userId);
        List<BodyRecord> bodyRecords = bodyRecordRepository.findByUserOrderByRecordTimeDesc(currentUser);
        
        List<String> dates = new ArrayList<>();
        List<Double> weights = new ArrayList<>();
        List<Double> bodyFats = new ArrayList<>();
        List<Double> muscleMasses = new ArrayList<>();
        
        // 仅取最近的10条记录或按时间范围过滤
        bodyRecords.stream().limit(10).forEach(record -> {
            dates.add(record.getRecordTime().toLocalDate().toString());
            weights.add(record.getWeight());
            bodyFats.add(record.getBodyFat());
            muscleMasses.add(record.getMuscleMass());
        });
        
        // 倒序排列，使图表从旧到新展示
        Collections.reverse(dates);
        Collections.reverse(weights);
        Collections.reverse(bodyFats);
        Collections.reverse(muscleMasses);
        
        Map<String, Object> metrics = new HashMap<>();
        metrics.put("dates", dates);
        metrics.put("weights", weights);
        metrics.put("bodyFat", bodyFats);
        metrics.put("muscleMass", muscleMasses);
        
        return metrics;
    }

    @Override
    public Object getTrainingDistribution(Long userId, String timeRange) {
        log.info("获取训练分布数据，用户ID: {}, 时间范围: {}", userId, timeRange);
        
        User currentUser = getUserById(userId);
        LocalDate[] dateRange = getDateRange(timeRange);
        LocalDate startDate = dateRange[0];
        LocalDate endDate = dateRange[1];
        
        // 获取时间范围内的所有训练记录
        List<TrainingRecord> trainingRecords = trainingRecordRepository
            .findByUserAndTrainingDateBetweenOrderByTrainingDateDesc(currentUser, startDate, endDate);
        
        // 按训练部位分组
        Map<String, Long> distribution = new HashMap<>();
        distribution.put("chest", 0L);
        distribution.put("back", 0L);
        distribution.put("legs", 0L);
        distribution.put("shoulders", 0L);
        distribution.put("arms", 0L);
        
        // 根据训练动作名称确定训练部位并累加训练量
        for (TrainingRecord record : trainingRecords) {
            String bodyPart = determineBodyPart(record.getExerciseName());
            Long currentVolume = distribution.getOrDefault(bodyPart, 0L);
            distribution.put(bodyPart, currentVolume + record.getCalculatedTotalVolume().longValue());
        }
        
        return distribution;
    }
    
    /**
     * 辅助方法：计算总训练量
     */
    private long calculateTotalVolume(List<TrainingRecord> records) {
        return records.stream()
            .mapToLong(r -> r.getCalculatedTotalVolume().longValue())
            .sum();
    }
    
    /**
     * 辅助方法：计算恢复分数
     */
    private int calculateRecoveryScore(List<RecoveryMetric> metrics) {
        if (metrics.isEmpty()) return 85; // 默认分数
        
        double avgSleep = metrics.stream().mapToInt(RecoveryMetric::getSleepQuality).average().orElse(3.0);
        double avgEnergy = metrics.stream().mapToInt(RecoveryMetric::getSubjectiveEnergy).average().orElse(3.0);
        double avgSoreness = metrics.stream().mapToInt(RecoveryMetric::getMuscleSoreness).average().orElse(2.0);
        
        // 简单计算公式
        double score = (avgSleep * 20) + (avgEnergy * 15) - (avgSoreness * 10) + 30;
        return (int) Math.min(100, Math.max(0, score));
    }
    
    /**
     * 辅助方法：计算目标完成率
     */
    private int calculateGoalCompletionRate(List<TrainingRecord> records, String timeRange) {
        int target = getTargetTrainingCount(timeRange);
        if (target == 0) return 100;
        return Math.min(100, (records.size() * 100) / target);
    }
    
    /**
     * 辅助方法：获取目标训练次数
     */
    private int getTargetTrainingCount(String timeRange) {
        int multiplier = getTimeRangeMultiplier(timeRange);
        return 4 * multiplier; // 假设每周4次
    }
    
    /**
     * 辅助方法：获取目标总训练量
     */
    private long getTargetTotalVolume(String timeRange) {
        int multiplier = getTimeRangeMultiplier(timeRange);
        return 20000L * multiplier; // 假设每周20000kg
    }
    
    /**
     * 辅助方法：获取目标有氧时长
     */
    private int getTargetAerobicDuration(String timeRange) {
        int multiplier = getTimeRangeMultiplier(timeRange);
        return 60 * multiplier; // 假设每周60分钟
    }
    
    /**
     * 辅助方法：获取目标拉伸次数
     */
    private int getTargetStretchingCount(String timeRange) {
        int multiplier = getTimeRangeMultiplier(timeRange);
        return 3 * multiplier; // 假设每周3次
    }
    
    /**
     * 辅助方法：计算总训练时长
     */
    private int calculateTotalDuration(List<TrainingRecord> records) {
        return records.stream().mapToInt(TrainingRecord::getDuration).sum();
    }
    
    /**
     * 辅助方法：计算拉伸次数 (简化：假设有氧且名称包含拉伸)
     */
    private int calculateStretchingCount(List<TrainingRecord> records) {
        return (int) records.stream()
            .filter(r -> r.getExerciseName().contains("拉伸"))
            .count();
    }
    
    /**
     * 辅助方法：计算平均睡眠质量
     */
    private double calculateAverageSleepQuality(List<RecoveryMetric> metrics) {
        return metrics.stream().mapToDouble(RecoveryMetric::getSleepQuality).average().orElse(0.0);
    }
    
    /**
     * 辅助方法：计算平均肌肉酸痛
     */
    private double calculateAverageMuscleSoreness(List<RecoveryMetric> metrics) {
        return metrics.stream().mapToDouble(RecoveryMetric::getMuscleSoreness).average().orElse(0.0);
    }
    
    /**
     * 辅助方法：计算平均主观精力
     */
    private double calculateAverageSubjectiveEnergy(List<RecoveryMetric> metrics) {
        return metrics.stream().mapToDouble(RecoveryMetric::getSubjectiveEnergy).average().orElse(0.0);
    }
    
    /**
     * 获取日期范围
     */
    private LocalDate[] getDateRange(String timeRange) {
        LocalDate now = LocalDate.now();
        LocalDate start;
        switch (timeRange) {
            case "week":
                start = now.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
                break;
            case "month":
                start = now.with(TemporalAdjusters.firstDayOfMonth());
                break;
            case "year":
                start = now.with(TemporalAdjusters.firstDayOfYear());
                break;
            default:
                start = now.minusDays(7);
        }
        return new LocalDate[]{start, now};
    }
    
    /**
     * 获取上一周期的日期范围
     */
    private LocalDate[] getPreviousDateRange(String timeRange) {
        LocalDate[] currentRange = getDateRange(timeRange);
        LocalDate start = currentRange[0];
        LocalDate end = currentRange[1];
        
        switch (timeRange) {
            case "week":
                return new LocalDate[]{start.minusWeeks(1), start.minusDays(1)};
            case "month":
                return new LocalDate[]{start.minusMonths(1), start.minusDays(1)};
            case "year":
                return new LocalDate[]{start.minusYears(1), start.minusDays(1)};
            default:
                return new LocalDate[]{start.minusDays(7), start.minusDays(1)};
        }
    }
    
    /**
     * 确定训练部位 (简化逻辑)
     */
    private String determineBodyPart(String exerciseName) {
        if (exerciseName == null) return "other";
        String name = exerciseName.toLowerCase();
        if (name.contains("推") || name.contains("胸")) return "chest";
        if (name.contains("划船") || name.contains("背")) return "back";
        if (name.contains("蹲") || name.contains("腿")) return "legs";
        if (name.contains("举") || name.contains("肩")) return "shoulders";
        if (name.contains("弯举") || name.contains("臂")) return "arms";
        return "other";
    }

    /**
     * 根据时间范围获取倍数
     */
    private int getTimeRangeMultiplier(String timeRange) {
        switch (timeRange) {
            case "week":
                return 1;
            case "month":
                return 4;
            case "year":
                return 52;
            default:
                return 1;
        }
    }
    
    /**
     * 创建目标数据
     */
    private Map<String, Object> createGoal(int id, String name, Object progress, Object target) {
        Map<String, Object> goal = new HashMap<>();
        goal.put("id", id);
        goal.put("name", name);
        goal.put("progress", progress);
        goal.put("target", target);
        return goal;
    }
    
    /**
     * 创建性能详情数据
     */
    private Map<String, Object> createPerformanceDetail(String name, int score, String description) {
        Map<String, Object> detail = new HashMap<>();
        detail.put("name", name);
        detail.put("score", score);
        detail.put("description", description);
        return detail;
    }
    
    /**
     * 获取日期列表
     */
    private List<String> getDateList(String timeRange) {
        List<String> dates = new ArrayList<>();
        LocalDate now = LocalDate.now();
        
        if ("week".equals(timeRange)) {
            LocalDate startOfWeek = now.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
            for (int i = 0; i < 7; i++) {
                dates.add(startOfWeek.plusDays(i).toString());
            }
        } else if ("month".equals(timeRange)) {
            LocalDate startOfMonth = now.with(TemporalAdjusters.firstDayOfMonth());
            for (int i = 0; i < now.getDayOfMonth(); i++) {
                dates.add(startOfMonth.plusDays(i).toString());
            }
        } else if ("year".equals(timeRange)) {
            LocalDate startOfYear = now.with(TemporalAdjusters.firstDayOfYear());
            for (int i = 0; i < 12; i++) {
                dates.add(startOfYear.plusMonths(i).toString().substring(0, 7));
            }
        }
        return dates;
    }

    // ==================== 新增方法实现 ====================
    
    @Override
    public com.wzl.fitness.dto.response.DashboardOverview getDashboardOverview(User user, LocalDate startDate, LocalDate endDate) {
        log.info("获取用户 {} 的仪表盘概览，日期范围: {} - {}", user.getId(), startDate, endDate);
        
        // 获取训练记录
        List<TrainingRecord> records = trainingRecordRepository
                .findByUserAndTrainingDateBetweenOrderByTrainingDateDesc(user, startDate, endDate);
        
        // 获取上一周期数据用于对比
        long daysDiff = endDate.toEpochDay() - startDate.toEpochDay();
        LocalDate prevStart = startDate.minusDays(daysDiff + 1);
        LocalDate prevEnd = startDate.minusDays(1);
        List<TrainingRecord> prevRecords = trainingRecordRepository
                .findByUserAndTrainingDateBetweenOrderByTrainingDateDesc(user, prevStart, prevEnd);
        
        // 计算训练量
        double currentVolume = records.stream()
                .mapToDouble(r -> r.getTotalVolume() != null ? r.getTotalVolume() : 0)
                .sum();
        double prevVolume = prevRecords.stream()
                .mapToDouble(r -> r.getTotalVolume() != null ? r.getTotalVolume() : 0)
                .sum();
        
        // 计算训练时长
        int totalDuration = records.stream().mapToInt(TrainingRecord::getDuration).sum();
        
        // 计算训练量变化百分比
        double volumeChange = prevVolume > 0 ? ((currentVolume - prevVolume) / prevVolume) * 100 : 0;
        
        // 计算连续训练天数
        int streak = calculateTrainingStreak(user);
        
        // 获取恢复数据
        List<RecoveryMetric> recoveryMetrics = recoveryMetricRepository
                .findByUserAndRecordDateBetween(user, startDate, endDate);
        int recoveryScore = calculateRecoveryScore(recoveryMetrics);
        String recoveryStatus = getRecoveryStatusText(recoveryScore);
        String recommendedIntensity = getRecommendedIntensityText(recoveryScore);
        
        // 计算1RM进步
        Map<String, Double> oneRepMaxProgress = calculateOneRepMaxProgress(user, startDate, endDate);
        
        // 获取个人记录
        List<com.wzl.fitness.dto.response.DashboardOverview.PersonalRecord> personalRecords = getPersonalRecords(user, records);
        
        // 获取最常训练的动作
        String mostFrequent = records.stream()
                .collect(Collectors.groupingBy(TrainingRecord::getExerciseName, Collectors.counting()))
                .entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse("无");
        
        // 建议训练的肌群
        String suggestedMuscle = suggestNextMuscleGroup(records);
        
        return com.wzl.fitness.dto.response.DashboardOverview.builder()
                .startDate(startDate)
                .endDate(endDate)
                .weeklyTrainingCount(records.size())
                .weeklyTotalVolume(Math.round(currentVolume * 10) / 10.0)
                .weeklyTrainingDuration(totalDuration)
                .volumeChangePercent(Math.round(volumeChange * 10) / 10.0)
                .trainingStreak(streak)
                .latestRecoveryScore(recoveryScore)
                .recoveryStatus(recoveryStatus)
                .recommendedIntensity(recommendedIntensity)
                .oneRepMaxProgress(oneRepMaxProgress)
                .personalRecords(personalRecords)
                .mostFrequentExercise(mostFrequent)
                .suggestedMuscleGroup(suggestedMuscle)
                .build();
    }
    
    @Override
    public com.wzl.fitness.dto.response.DashboardOverview getWeeklyOverview(User user) {
        LocalDate today = LocalDate.now();
        LocalDate startOfWeek = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        return getDashboardOverview(user, startOfWeek, today);
    }
    
    @Override
    public List<com.wzl.fitness.dto.response.DashboardOverview.PersonalRecord> getOneRepMaxProgress(
            User user, String exerciseName, LocalDate startDate, LocalDate endDate) {
        
        List<TrainingRecord> records = trainingRecordRepository
                .findByUserAndTrainingDateBetweenOrderByTrainingDateDesc(user, startDate, endDate)
                .stream()
                .filter(r -> exerciseName == null || exerciseName.equals(r.getExerciseName()))
                .collect(Collectors.toList());
        
        return getPersonalRecords(user, records);
    }
    
    // 私有辅助方法
    
    private int calculateTrainingStreak(User user) {
        LocalDate today = LocalDate.now();
        int streak = 0;
        LocalDate checkDate = today;
        
        while (true) {
            List<TrainingRecord> dayRecords = trainingRecordRepository
                    .findByUserIdAndTrainingDateBetween(user.getId(), checkDate, checkDate);
            if (dayRecords.isEmpty()) {
                break;
            }
            streak++;
            checkDate = checkDate.minusDays(1);
            if (streak > 365) break; // 防止无限循环
        }
        
        return streak;
    }
    
    private String getRecoveryStatusText(int score) {
        if (score >= 80) return "极佳";
        if (score >= 60) return "良好";
        if (score >= 40) return "一般";
        if (score >= 20) return "较差";
        return "需休息";
    }
    
    private String getRecommendedIntensityText(int score) {
        if (score >= 80) return "高强度";
        if (score >= 60) return "中高强度";
        if (score >= 40) return "中等强度";
        if (score >= 20) return "低强度";
        return "休息";
    }
    
    private Map<String, Double> calculateOneRepMaxProgress(User user, LocalDate startDate, LocalDate endDate) {
        Map<String, Double> progress = new HashMap<>();
        
        // 获取当前周期的记录
        List<TrainingRecord> currentRecords = trainingRecordRepository
                .findByUserAndTrainingDateBetweenOrderByTrainingDateDesc(user, startDate, endDate);
        
        // 获取上一周期的记录
        long daysDiff = endDate.toEpochDay() - startDate.toEpochDay();
        LocalDate prevStart = startDate.minusDays(daysDiff + 1);
        LocalDate prevEnd = startDate.minusDays(1);
        List<TrainingRecord> prevRecords = trainingRecordRepository
                .findByUserAndTrainingDateBetweenOrderByTrainingDateDesc(user, prevStart, prevEnd);
        
        // 按动作分组计算最大1RM
        Map<String, Double> currentMax = currentRecords.stream()
                .collect(Collectors.groupingBy(
                        TrainingRecord::getExerciseName,
                        Collectors.collectingAndThen(
                                Collectors.maxBy(Comparator.comparingDouble(r -> calculateEstimated1RM(r))),
                                opt -> opt.map(this::calculateEstimated1RM).orElse(0.0)
                        )
                ));
        
        Map<String, Double> prevMax = prevRecords.stream()
                .collect(Collectors.groupingBy(
                        TrainingRecord::getExerciseName,
                        Collectors.collectingAndThen(
                                Collectors.maxBy(Comparator.comparingDouble(r -> calculateEstimated1RM(r))),
                                opt -> opt.map(this::calculateEstimated1RM).orElse(0.0)
                        )
                ));
        
        // 计算进步百分比
        for (String exercise : currentMax.keySet()) {
            double current = currentMax.get(exercise);
            double prev = prevMax.getOrDefault(exercise, current);
            if (prev > 0) {
                double changePercent = ((current - prev) / prev) * 100;
                progress.put(exercise, Math.round(changePercent * 10) / 10.0);
            }
        }
        
        return progress;
    }
    
    private double calculateEstimated1RM(TrainingRecord record) {
        if (record.getWeight() == null || record.getReps() == null || record.getReps() <= 0) {
            return 0.0;
        }
        // Epley公式
        return record.getWeight() * (1 + record.getReps() / 30.0);
    }
    
    private List<com.wzl.fitness.dto.response.DashboardOverview.PersonalRecord> getPersonalRecords(
            User user, List<TrainingRecord> records) {
        
        // 按动作分组，找出每个动作的最佳记录
        Map<String, TrainingRecord> bestRecords = records.stream()
                .collect(Collectors.toMap(
                        TrainingRecord::getExerciseName,
                        r -> r,
                        (r1, r2) -> calculateEstimated1RM(r1) >= calculateEstimated1RM(r2) ? r1 : r2
                ));
        
        return bestRecords.values().stream()
                .map(r -> com.wzl.fitness.dto.response.DashboardOverview.PersonalRecord.builder()
                        .exerciseName(r.getExerciseName())
                        .weight(r.getWeight())
                        .reps(r.getReps())
                        .estimatedOneRepMax(Math.round(calculateEstimated1RM(r) * 10) / 10.0)
                        .achievedDate(r.getTrainingDate())
                        .build())
                .sorted((a, b) -> Double.compare(b.getEstimatedOneRepMax(), a.getEstimatedOneRepMax()))
                .limit(5)
                .collect(Collectors.toList());
    }
    
    private String suggestNextMuscleGroup(List<TrainingRecord> recentRecords) {
        // 统计最近训练的肌群
        Map<String, Long> muscleGroupCount = recentRecords.stream()
                .map(r -> determineBodyPart(r.getExerciseName()))
                .collect(Collectors.groupingBy(s -> s, Collectors.counting()));
        
        // 找出训练最少的肌群
        List<String> allGroups = Arrays.asList("chest", "back", "legs", "shoulders", "arms");
        String leastTrained = allGroups.stream()
                .min(Comparator.comparingLong(g -> muscleGroupCount.getOrDefault(g, 0L)))
                .orElse("legs");
        
        // 转换为中文
        return switch (leastTrained) {
            case "chest" -> "胸部";
            case "back" -> "背部";
            case "legs" -> "腿部";
            case "shoulders" -> "肩部";
            case "arms" -> "手臂";
            default -> "全身";
        };
    }
}