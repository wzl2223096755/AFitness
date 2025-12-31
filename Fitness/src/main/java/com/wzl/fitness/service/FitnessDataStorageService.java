package com.wzl.fitness.service;

import com.wzl.fitness.common.ResponseCode;
import com.wzl.fitness.entity.FitnessData;
import com.wzl.fitness.entity.User;
import com.wzl.fitness.exception.BusinessException;
import com.wzl.fitness.repository.FitnessDataRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class FitnessDataStorageService {

    private static final Logger log = LoggerFactory.getLogger(FitnessDataStorageService.class);
    
    private final FitnessDataRepository fitnessDataRepository;

    /**
     * 保存健身数据
     */
    @Transactional
    public FitnessData saveFitnessData(FitnessData fitnessData) {
        try {
            FitnessData savedData = fitnessDataRepository.save(fitnessData);
            
            // 安全地获取用户信息，避免空指针异常
            String username = savedData.getUser() != null ? savedData.getUser().getUsername() : "未知用户";
            
            log.info("健身数据保存成功: ID={}, 用户={}, 时间={}", 
                    savedData.getId(), username, savedData.getTimestamp());
            return savedData;
        } catch (Exception e) {
            log.error("保存健身数据失败: {}", e.getMessage(), e);
            throw new BusinessException(ResponseCode.FITNESS_DATA_EXCEPTION.getCode(), "保存健身数据失败: " + e.getMessage());
        }
    }

    /**
     * 批量保存健身数据
     */
    @Transactional
    public List<FitnessData> saveBatchFitnessData(List<FitnessData> fitnessDataList) {
        try {
            List<FitnessData> savedDataList = fitnessDataRepository.saveAll(fitnessDataList);
            log.info("批量保存健身数据成功: 数量={}", savedDataList.size());
            return savedDataList;
        } catch (Exception e) {
            log.error("批量保存健身数据失败: {}", e.getMessage(), e);
            throw new BusinessException(ResponseCode.FITNESS_DATA_EXCEPTION.getCode(), "批量保存健身数据失败: " + e.getMessage());
        }
    }

    /**
     * 根据ID查找健身数据
     */
    public Optional<FitnessData> findById(Long id) {
        return fitnessDataRepository.findById(id);
    }

    /**
     * 根据用户查找健身数据
     */
    public List<FitnessData> findByUser(User user) {
        return fitnessDataRepository.findByUser(user);
    }

    /**
     * 根据用户ID查找健身数据
     * 修复遍历错误：限制时间范围，避免全表扫描
     */
    public List<FitnessData> findByUserId(Long userId) {
        try {
            // 修复遍历错误：限制查询范围为最近30天，避免全表扫描
            LocalDateTime thirtyDaysAgo = LocalDateTime.now().minusDays(30);
            List<FitnessData> result = fitnessDataRepository.findByUserIdAndTimestampBetween(
                    userId, thirtyDaysAgo, LocalDateTime.now());
            
            log.debug("查询用户健身数据: 用户ID={}, 时间范围={}, 数据条数={}", 
                    userId, thirtyDaysAgo, result.size());
            return result;
            
        } catch (Exception e) {
            log.error("查询用户健身数据失败: 用户ID={}, 错误: {}", userId, e.getMessage(), e);
            throw new BusinessException(ResponseCode.FITNESS_DATA_EXCEPTION.getCode(), 
                    "查询用户健身数据失败: " + e.getMessage());
        }
    }

    /**
     * 根据用户ID和时间范围查找健身数据
     */
    public List<FitnessData> findByUserIdAndTimeRange(Long userId, LocalDateTime start, LocalDateTime end) {
        return fitnessDataRepository.findByUserIdAndTimestampBetween(userId, start, end);
    }

    /**
     * 根据设备ID查找健身数据
     * 修复：实现真正的设备ID查询功能
     */
    public List<FitnessData> findByDeviceId(Long deviceId) {
        try {
            log.debug("根据设备ID查找健身数据: 设备ID={}", deviceId);
            
            // 限制查询范围为最近30天，避免全表扫描
            LocalDateTime thirtyDaysAgo = LocalDateTime.now().minusDays(30);
            List<FitnessData> result = fitnessDataRepository.findByDeviceIdAndTimestampBetween(
                    deviceId, thirtyDaysAgo, LocalDateTime.now());
            
            log.debug("根据设备ID查找健身数据完成: 设备ID={}, 时间范围={}, 数据条数={}", 
                    deviceId, thirtyDaysAgo, result.size());
            return result;
            
        } catch (Exception e) {
            log.error("根据设备ID查找健身数据失败: 设备ID={}, 错误: {}", deviceId, e.getMessage(), e);
            throw new BusinessException(ResponseCode.FITNESS_DATA_EXCEPTION.getCode(), 
                    "根据设备ID查找健身数据失败: " + e.getMessage());
        }
    }

    /**
     * 分页查询健身数据
     */
    public Page<FitnessData> findAll(Pageable pageable) {
        return fitnessDataRepository.findAll(pageable);
    }

    /**
     * 获取用户最新的健身数据
     * 修复遍历错误：使用数据库级别的排序，避免内存遍历
     */
    public List<FitnessData> findLatestByUser(Long userId, int limit) {
        try {
            List<FitnessData> result;
            
            // 根据limit值选择最优的查询方法
            switch (limit) {
                case 1:
                    // 使用findTopByUserIdOrderByTimestampDesc，性能最优
                    FitnessData singleData = fitnessDataRepository.findTopByUserIdOrderByTimestampDesc(userId);
                    result = singleData != null ? List.of(singleData) : List.of();
                    break;
                case 10:
                    // 使用专门优化的top10查询
                    result = fitnessDataRepository.findTop10ByUserIdOrderByTimestampDesc(userId);
                    break;
                default:
                    // 使用通用排序查询，然后在应用层限制数量
                    result = fitnessDataRepository.findByUserIdOrderByTimestampDesc(userId);
                    if (result.size() > limit) {
                        result = result.subList(0, limit);
                    }
                    break;
            }
            
            log.debug("获取用户最新健身数据: 用户ID={}, 限制数量={}, 实际返回={}", 
                    userId, limit, result.size());
            return result;
            
        } catch (Exception e) {
            log.error("获取用户最新健身数据失败: 用户ID={}, 限制数量={}, 错误: {}", 
                    userId, limit, e.getMessage(), e);
            throw new BusinessException(ResponseCode.FITNESS_DATA_EXCEPTION.getCode(), 
                    "获取用户最新健身数据失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取用户最新的健身数据（支持自定义限制数量）
     */
    public List<FitnessData> getLatestFitnessDataByUser(Long userId, int limit) {
        return findLatestByUser(userId, limit);
    }
    
    // 保存用户的健身数据 - 兼容性方法
    public FitnessData saveUserFitnessData(FitnessData fitnessData) {
        return this.saveFitnessData(fitnessData);
    }
    
    // 获取用户最新的健身数据 - 兼容性方法
    public List<FitnessData> getUserLatestFitnessData(Long userId) {
        return this.findLatestByUser(userId, 10);
    }

    /**
     * 获取用户今日的健身数据
     */
    public List<FitnessData> findTodayByUser(Long userId) {
        LocalDateTime todayStart = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0);
        LocalDateTime todayEnd = LocalDateTime.now().withHour(23).withMinute(59).withSecond(59);
        return fitnessDataRepository.findByUserIdAndTimestampBetween(userId, todayStart, todayEnd);
    }

    /**
     * 获取用户本周的健身数据
     */
    public List<FitnessData> findThisWeekByUser(Long userId) {
        LocalDateTime weekStart = LocalDateTime.now().with(java.time.DayOfWeek.MONDAY)
                .withHour(0).withMinute(0).withSecond(0);
        LocalDateTime weekEnd = LocalDateTime.now().with(java.time.DayOfWeek.SUNDAY)
                .withHour(23).withMinute(59).withSecond(59);
        return fitnessDataRepository.findByUserIdAndTimestampBetween(userId, weekStart, weekEnd);
    }

    /**
     * 获取用户本月的健身数据
     */
    public List<FitnessData> findThisMonthByUser(Long userId) {
        LocalDateTime monthStart = LocalDateTime.now().withDayOfMonth(1)
                .withHour(0).withMinute(0).withSecond(0);
        LocalDateTime monthEnd = LocalDateTime.now().withDayOfMonth(
                LocalDateTime.now().toLocalDate().lengthOfMonth())
                .withHour(23).withMinute(59).withSecond(59);
        return fitnessDataRepository.findByUserIdAndTimestampBetween(userId, monthStart, monthEnd);
    }

    /**
     * 删除健身数据
     */
    @Transactional
    public void deleteFitnessData(Long id) {
        try {
            fitnessDataRepository.deleteById(id);
            log.info("健身数据删除成功: ID={}", id);
        } catch (Exception e) {
            log.error("删除健身数据失败: ID={}, 错误: {}", id, e.getMessage(), e);
            throw new BusinessException(ResponseCode.FITNESS_DATA_EXCEPTION.getCode(), "删除健身数据失败: " + e.getMessage());
        }
    }

    /**
     * 删除用户的健身数据
     */
    @Transactional
    public void deleteByUser(Long userId) {
        try {
            List<FitnessData> userData = findByUserId(userId);
            fitnessDataRepository.deleteAll(userData);
            log.info("删除用户健身数据成功: 用户ID={}, 数据条数={}", userId, userData.size());
        } catch (Exception e) {
            log.error("删除用户健身数据失败: 用户ID={}, 错误: {}", userId, e.getMessage(), e);
            throw new BusinessException(ResponseCode.FITNESS_DATA_EXCEPTION.getCode(), "删除用户健身数据失败: " + e.getMessage());
        }
    }

    /**
     * 统计用户健身数据
     */
    public FitnessDataStats calculateUserStats(Long userId) {
        List<FitnessData> userData = findByUserId(userId);
        
        if (userData.isEmpty()) {
            return new FitnessDataStats(0, 0.0, 0.0, 0.0, 0, 0);
        }
        
        int totalSessions = userData.size();
        // 由于FitnessData类没有这些字段，暂时使用默认值
        double totalDistance = 0.0;
        double totalCalories = 0.0;
        double avgSpeed = 0.0;
        int avgHeartRate = 0;
        int maxHeartRate = 0;
        
        // 尝试从训练量计算一些统计数据
        double totalVolume = userData.stream()
                .mapToDouble(data -> data.getTrainingVolume() != null ? data.getTrainingVolume() : 0.0)
                .sum();
        log.info("用户训练总量统计: 用户ID={}, 训练总量={}", userId, totalVolume);
        
        return new FitnessDataStats(totalSessions, totalDistance, totalCalories, avgSpeed, avgHeartRate, maxHeartRate);
    }

    /**
     * 健身数据统计信息
     */
    public static class FitnessDataStats {
        private final int totalSessions;
        private final double totalDistance;
        private final double totalCalories;
        private final double avgSpeed;
        private final int avgHeartRate;
        private final int maxHeartRate;

        public FitnessDataStats(int totalSessions, double totalDistance, double totalCalories, 
                               double avgSpeed, int avgHeartRate, int maxHeartRate) {
            this.totalSessions = totalSessions;
            this.totalDistance = totalDistance;
            this.totalCalories = totalCalories;
            this.avgSpeed = avgSpeed;
            this.avgHeartRate = avgHeartRate;
            this.maxHeartRate = maxHeartRate;
        }

        // Getters
        public int getTotalSessions() { return totalSessions; }
        public double getTotalDistance() { return totalDistance; }
        public double getTotalCalories() { return totalCalories; }
        public double getAvgSpeed() { return avgSpeed; }
        public int getAvgHeartRate() { return avgHeartRate; }
        public int getMaxHeartRate() { return maxHeartRate; }
    }
}
