package com.wzl.fitness.service.impl;

import com.wzl.fitness.config.CaffeineCacheConfig;
import com.wzl.fitness.entity.TrainingRecord;
import com.wzl.fitness.entity.User;
import com.wzl.fitness.modules.training.event.TrainingCompletedEvent;
import com.wzl.fitness.repository.TrainingRecordRepository;
import com.wzl.fitness.service.TrainingRecordService;
import com.wzl.fitness.shared.event.EventPublisher;
import com.wzl.fitness.dto.response.TrainingStatsResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 训练记录服务实现
 * 支持软删除和数据恢复功能
 * 使用 Caffeine 缓存提升查询性能
 * 
 * @see Requirements 3.2 - 用户完成训练记录时发布TrainingCompletedEvent事件
 */
@Service
@Transactional
public class TrainingRecordServiceImpl implements TrainingRecordService {
    
    private static final Logger logger = LoggerFactory.getLogger(TrainingRecordServiceImpl.class);
    
    @Autowired
    private TrainingRecordRepository trainingRecordRepository;
    
    @Autowired
    private EventPublisher eventPublisher;
    
    @Override
    @CacheEvict(value = CaffeineCacheConfig.CACHE_TRAINING_RECORDS, allEntries = true)
    public TrainingRecord createTrainingRecord(TrainingRecord record) {
        // 确保新记录未被标记为删除
        record.setDeleted(false);
        logger.debug("创建训练记录，清除缓存");
        TrainingRecord savedRecord = trainingRecordRepository.save(record);
        
        // 发布训练完成事件
        publishTrainingCompletedEvent(savedRecord);
        
        return savedRecord;
    }
    
    /**
     * 发布训练完成事件
     * 
     * @param record 保存的训练记录
     * @see Requirements 3.2 - 用户完成训练记录时发布TrainingCompletedEvent事件
     */
    private void publishTrainingCompletedEvent(TrainingRecord record) {
        if (record == null || record.getUser() == null) {
            logger.warn("无法发布训练完成事件：记录或用户为空");
            return;
        }
        
        try {
            TrainingCompletedEvent event = new TrainingCompletedEvent(
                    record.getUser().getId(),
                    record.getId(),
                    record.getTotalVolume() != null ? record.getTotalVolume() : record.getCalculatedTotalVolume(),
                    record.getExerciseName(),
                    record.getTrainingStress(),
                    record.getDuration()
            );
            
            eventPublisher.publish(event);
            logger.info("训练完成事件已发布: recordId={}, userId={}", record.getId(), record.getUser().getId());
        } catch (Exception e) {
            logger.error("发布训练完成事件失败: recordId={}, error={}", record.getId(), e.getMessage(), e);
            // 不抛出异常，避免影响主业务流程
        }
    }
    
    @Override
    @CacheEvict(value = CaffeineCacheConfig.CACHE_TRAINING_RECORDS, allEntries = true)
    public Optional<TrainingRecord> updateTrainingRecord(Long id, TrainingRecord record) {
        logger.debug("更新训练记录 ID: {}，清除缓存", id);
        return trainingRecordRepository.findById(id)
            .map(existingRecord -> {
                // 更新字段
                existingRecord.setExerciseName(record.getExerciseName());
                existingRecord.setSets(record.getSets());
                existingRecord.setReps(record.getReps());
                existingRecord.setWeight(record.getWeight());
                existingRecord.setTrainingDate(record.getTrainingDate());
                existingRecord.setDuration(record.getDuration());
                existingRecord.setNotes(record.getNotes());
                return trainingRecordRepository.save(existingRecord);
            });
    }
    
    @Override
    @CacheEvict(value = CaffeineCacheConfig.CACHE_TRAINING_RECORDS, allEntries = true)
    public boolean deleteTrainingRecord(Long id) {
        if (trainingRecordRepository.existsById(id)) {
            trainingRecordRepository.deleteById(id);
            logger.debug("删除训练记录 ID: {}，清除缓存", id);
            return true;
        }
        return false;
    }
    
    @Override
    @CacheEvict(value = CaffeineCacheConfig.CACHE_TRAINING_RECORDS, allEntries = true)
    public boolean softDeleteTrainingRecord(Long id, Long userId) {
        logger.info("软删除训练记录，ID: {}, 操作用户: {}", id, userId);
        int updated = trainingRecordRepository.softDelete(id, LocalDateTime.now(), userId);
        if (updated > 0) {
            logger.info("训练记录软删除成功，ID: {}，清除缓存", id);
            return true;
        }
        logger.warn("训练记录软删除失败，ID: {} 不存在", id);
        return false;
    }
    
    @Override
    @CacheEvict(value = CaffeineCacheConfig.CACHE_TRAINING_RECORDS, allEntries = true)
    public boolean restoreTrainingRecord(Long id) {
        logger.info("恢复训练记录，ID: {}", id);
        int updated = trainingRecordRepository.restore(id);
        if (updated > 0) {
            logger.info("训练记录恢复成功，ID: {}，清除缓存", id);
            return true;
        }
        logger.warn("训练记录恢复失败，ID: {} 不存在或未被删除", id);
        return false;
    }
    
    @Override
    @CacheEvict(value = CaffeineCacheConfig.CACHE_TRAINING_RECORDS, allEntries = true)
    public int restoreAllByUserId(Long userId) {
        logger.info("批量恢复用户训练记录，用户ID: {}", userId);
        int restored = trainingRecordRepository.restoreAllByUserId(userId);
        logger.info("批量恢复完成，用户ID: {}, 恢复记录数: {}，清除缓存", userId, restored);
        return restored;
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<TrainingRecord> findDeletedByUserId(Long userId) {
        return trainingRecordRepository.findDeletedByUserId(userId);
    }
    
    @Override
    @Transactional(readOnly = true)
    public long countDeletedByUserId(Long userId) {
        return trainingRecordRepository.countDeletedByUserId(userId);
    }
    
    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = CaffeineCacheConfig.CACHE_TRAINING_RECORDS, key = "'record:' + #id")
    public Optional<TrainingRecord> findById(Long id) {
        logger.debug("从数据库查询训练记录 ID: {}", id);
        return trainingRecordRepository.findById(id);
    }
    
    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = CaffeineCacheConfig.CACHE_TRAINING_RECORDS, key = "'user:' + #userId")
    public List<TrainingRecord> findByUserId(Long userId) {
        logger.debug("从数据库查询用户 {} 的训练记录", userId);
        return trainingRecordRepository.findByUserId(userId);
    }
    
    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = CaffeineCacheConfig.CACHE_TRAINING_RECORDS, key = "'user:' + #userId + ':page:' + #page + ':size:' + #size")
    public Page<TrainingRecord> findByUserId(Long userId, int page, int size) {
        logger.debug("从数据库分页查询用户 {} 的训练记录，页码: {}, 大小: {}", userId, page, size);
        Pageable pageable = PageRequest.of(page, size);
        return trainingRecordRepository.findByUserIdOrderByTrainingDateDesc(userId, pageable);
    }
    
    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = CaffeineCacheConfig.CACHE_TRAINING_RECORDS, 
               key = "'user:' + #userId + ':range:' + #startDate.toString() + ':' + #endDate.toString()")
    public List<TrainingRecord> findByUserIdAndDateRange(Long userId, LocalDate startDate, LocalDate endDate) {
        logger.debug("从数据库查询用户 {} 在 {} 到 {} 的训练记录", userId, startDate, endDate);
        return trainingRecordRepository.findByUserIdAndTrainingDateBetweenOrderByTrainingDateDesc(userId, startDate, endDate);
    }
    
    @Override
    public TrainingStatsResponse getTrainingStats(Long userId) {
        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusDays(30);
        return getTrainingStats(userId, startDate, endDate);
    }
    
    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = CaffeineCacheConfig.CACHE_TRAINING_RECORDS, 
               key = "'stats:' + #userId + ':' + #startDate.toString() + ':' + #endDate.toString()")
    public TrainingStatsResponse getTrainingStats(Long userId, LocalDate startDate, LocalDate endDate) {
        logger.debug("从数据库计算用户 {} 在 {} 到 {} 的训练统计", userId, startDate, endDate);
        
        // 获取指定时间范围内的训练记录总数
        Long totalRecords = trainingRecordRepository.countByUserIdAndTrainingDateBetween(userId, startDate, endDate);
        
        // 获取指定时间范围内的总体积
        Double totalVolume = trainingRecordRepository.sumVolumeByUserIdAndDateRange(userId, startDate, endDate);
        
        // 获取指定时间范围内的总时长
        Long totalDuration = trainingRecordRepository.sumDurationByUserIdAndDateRange(userId, startDate, endDate);
        
        return new TrainingStatsResponse(
            totalRecords != null ? totalRecords : 0L,
            totalVolume != null ? totalVolume : 0.0,
            totalDuration != null ? totalDuration : 0L
        );
    }
    
    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = CaffeineCacheConfig.CACHE_TRAINING_RECORDS, key = "'recent:' + #userId")
    public List<TrainingRecord> getRecentTrainingRecords(Long userId) {
        logger.debug("从数据库查询用户 {} 的最近训练记录", userId);
        return trainingRecordRepository.findTop10ByUserIdOrderByTrainingDateDesc(userId);
    }
    
    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = CaffeineCacheConfig.CACHE_TRAINING_RECORDS, key = "'count:' + #userId")
    public long countByUserId(Long userId) {
        logger.debug("从数据库统计用户 {} 的训练记录数量", userId);
        return trainingRecordRepository.countByUserId(userId);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<TrainingRecord> findByUser(User user) {
        return trainingRecordRepository.findByUser(user);
    }
    
    @Override
    @Transactional(readOnly = true)
    public Optional<TrainingRecord> findByUserAndDate(User user, LocalDate date) {
        List<TrainingRecord> records = trainingRecordRepository.findByUserAndTrainingDate(user, date);
        return records.isEmpty() ? Optional.empty() : Optional.of(records.get(0));
    }
    
    @Override
    @CacheEvict(value = CaffeineCacheConfig.CACHE_TRAINING_RECORDS, allEntries = true)
    public void deleteByUserAndId(User user, Long id) {
        logger.debug("删除用户 {} 的训练记录 ID: {}，清除缓存", user.getId(), id);
        trainingRecordRepository.deleteByUserAndId(user, id);
    }
    
    @Override
    @CacheEvict(value = CaffeineCacheConfig.CACHE_TRAINING_RECORDS, allEntries = true)
    public void deleteAllByUserId(Long userId) {
        logger.info("删除用户所有训练记录，用户ID: {}，清除缓存", userId);
        trainingRecordRepository.deleteAllByUserId(userId);
        logger.info("用户训练记录删除完成，用户ID: {}", userId);
    }
}