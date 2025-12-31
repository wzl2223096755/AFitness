package com.wzl.fitness.service;

import com.wzl.fitness.entity.FitnessData;
import com.wzl.fitness.entity.User;
import com.wzl.fitness.repository.FitnessDataRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 健身数据服务实现（已废弃）
 * 提供健身数据的查询、缓存和管理功能
 * 
 * @deprecated 此服务使用已废弃的 {@link FitnessData} 实体。
 * 建议使用以下替代服务：
 * <ul>
 *   <li>力量训练数据：使用 {@link StrengthTrainingService} 和 {@link com.wzl.fitness.service.impl.StrengthTrainingServiceImpl}</li>
 *   <li>恢复状态数据：使用 {@link LoadRecoveryService} 和 {@link com.wzl.fitness.service.impl.LoadRecoveryServiceImpl}</li>
 * </ul>
 * 
 * @author AFitness Team
 * @since 1.0.0
 * @see StrengthTrainingService
 * @see LoadRecoveryService
 */
@Deprecated(since = "1.1.0", forRemoval = true)
@Service
@RequiredArgsConstructor
@SuppressWarnings("deprecation")
public class FitnessDataServiceImpl implements FitnessDataService {

    private static final Logger logger = LoggerFactory.getLogger(FitnessDataServiceImpl.class);
    private final FitnessDataRepository fitnessDataRepository;

    @Override
    @Cacheable(value = "fitnessData", key = "'user_' + #user.id + '_all'", 
               condition = "#user != null && #user.id != null")
    public List<FitnessData> getFitnessDataByUser(User user) {
        logger.debug("查询用户{}的所有健身数据", user.getId());
        // 使用UserId查询替代User对象查询，提高性能
        return fitnessDataRepository.findByUserId(user.getId());
    }

    @Override
    @Cacheable(value = "fitnessDataByDateRange", 
               key = "'user_' + #user.id + '_range_' + #start.toString() + '_' + #end.toString()",
               condition = "#user != null && #user.id != null && #start != null && #end != null")
    public List<FitnessData> getFitnessDataByUserAndDateRange(
            User user, LocalDateTime start, LocalDateTime end) {
        logger.debug("查询用户{}在{}到{}期间的健身数据", user.getId(), start, end);
        if (start.isAfter(end)) {
            logger.warn("开始时间{}晚于结束时间{}，返回空列表", start, end);
            return List.of();
        }
        
        // 使用UserId查询替代User对象查询
        return fitnessDataRepository.findByUserIdAndTimestampBetween(user.getId(), start, end);
    }

    @Override
    @Cacheable(value = "latestFitnessData", key = "'user_' + #userId + '_latest'")
    public List<FitnessData> getLatestFitnessDataByUser(Long userId) {
        logger.debug("查询用户{}的最新健身数据", userId);
        if (userId == null) {
            logger.warn("用户ID为空，返回空列表");
            return List.of();
        }
        
        // 使用findByUserId并手动限制数量，添加排序
        List<FitnessData> allData = fitnessDataRepository.findByUserId(userId);
        return allData.stream()
                .sorted((a, b) -> b.getTimestamp().compareTo(a.getTimestamp())) // 按时间倒序
                .limit(10)
                .collect(Collectors.toList());
    }
    
    /**
     * 保存健身数据并更新缓存
     */
    @CachePut(value = "fitnessData", key = "'user_' + #data.user.id + '_all'")
    @Transactional
    public FitnessData saveFitnessData(FitnessData data) {
        logger.debug("保存健身数据，用户ID: {}", data.getUser().getId());
        try {
            FitnessData saved = fitnessDataRepository.save(data);
            // 清除相关缓存
            evictUserCache(data.getUser().getId());
            return saved;
        } catch (Exception e) {
            logger.error("保存健身数据失败，用户ID: {}, 错误: {}", data.getUser().getId(), e.getMessage());
            throw new RuntimeException("保存健身数据失败", e);
        }
    }
    
    /**
     * 批量保存健身数据
     */
    @Transactional
    public List<FitnessData> saveAllFitnessData(List<FitnessData> dataList) {
        logger.debug("批量保存{}条健身数据", dataList.size());
        try {
            List<FitnessData> saved = fitnessDataRepository.saveAll(dataList);
            // 清除所有相关用户的缓存
            dataList.stream()
                    .map(data -> data.getUser().getId())
                    .distinct()
                    .forEach(this::evictUserCache);
            return saved;
        } catch (Exception e) {
            logger.error("批量保存健身数据失败，数量: {}, 错误: {}", dataList.size(), e.getMessage());
            throw new RuntimeException("批量保存健身数据失败", e);
        }
    }
    
    /**
     * 删除用户的健身数据
     */
    @CacheEvict(value = {"fitnessData", "fitnessDataByDateRange", "latestFitnessData"}, 
                key = "'user_' + #userId + '_*'")
    @Transactional
    public void deleteFitnessDataByUserId(Long userId) {
        logger.debug("删除用户{}的所有健身数据", userId);
        try {
            fitnessDataRepository.deleteAll(fitnessDataRepository.findByUserId(userId));
            logger.info("成功删除用户{}的所有健身数据", userId);
        } catch (Exception e) {
            logger.error("删除用户{}健身数据失败，错误: {}", userId, e.getMessage());
            throw new RuntimeException("删除健身数据失败", e);
        }
    }
    
    /**
     * 清除用户相关的所有缓存
     */
    @CacheEvict(value = {"fitnessData", "fitnessDataByDateRange", "latestFitnessData"}, 
                key = "'user_' + #userId + '_*'")
    public void evictUserCache(Long userId) {
        logger.debug("清除用户{}的所有健身数据缓存", userId);
        // 缓存清理方法
    }
    
    /**
     * 清除所有健身数据缓存
     */
    @CacheEvict(value = {"fitnessData", "fitnessDataByDateRange", "latestFitnessData"}, allEntries = true)
    public void evictAllCache() {
        logger.info("清除所有健身数据缓存");
        // 清除所有缓存
    }
}