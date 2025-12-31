package com.wzl.fitness.service.impl;

import com.wzl.fitness.entity.TrainingRecord;
import com.wzl.fitness.entity.User;
import com.wzl.fitness.repository.TrainingRecordRepository;
import com.wzl.fitness.service.TrainingRecordService;
import com.wzl.fitness.dto.response.TrainingStatsResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
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
 */
@Service
@Transactional
public class TrainingRecordServiceImpl implements TrainingRecordService {
    
    private static final Logger logger = LoggerFactory.getLogger(TrainingRecordServiceImpl.class);
    
    @Autowired
    private TrainingRecordRepository trainingRecordRepository;
    
    @Override
    public TrainingRecord createTrainingRecord(TrainingRecord record) {
        // 确保新记录未被标记为删除
        record.setDeleted(false);
        return trainingRecordRepository.save(record);
    }
    
    @Override
    public Optional<TrainingRecord> updateTrainingRecord(Long id, TrainingRecord record) {
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
    public boolean deleteTrainingRecord(Long id) {
        if (trainingRecordRepository.existsById(id)) {
            trainingRecordRepository.deleteById(id);
            return true;
        }
        return false;
    }
    
    @Override
    public boolean softDeleteTrainingRecord(Long id, Long userId) {
        logger.info("软删除训练记录，ID: {}, 操作用户: {}", id, userId);
        int updated = trainingRecordRepository.softDelete(id, LocalDateTime.now(), userId);
        if (updated > 0) {
            logger.info("训练记录软删除成功，ID: {}", id);
            return true;
        }
        logger.warn("训练记录软删除失败，ID: {} 不存在", id);
        return false;
    }
    
    @Override
    public boolean restoreTrainingRecord(Long id) {
        logger.info("恢复训练记录，ID: {}", id);
        int updated = trainingRecordRepository.restore(id);
        if (updated > 0) {
            logger.info("训练记录恢复成功，ID: {}", id);
            return true;
        }
        logger.warn("训练记录恢复失败，ID: {} 不存在或未被删除", id);
        return false;
    }
    
    @Override
    public int restoreAllByUserId(Long userId) {
        logger.info("批量恢复用户训练记录，用户ID: {}", userId);
        int restored = trainingRecordRepository.restoreAllByUserId(userId);
        logger.info("批量恢复完成，用户ID: {}, 恢复记录数: {}", userId, restored);
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
    public Optional<TrainingRecord> findById(Long id) {
        return trainingRecordRepository.findById(id);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<TrainingRecord> findByUserId(Long userId) {
        return trainingRecordRepository.findByUserId(userId);
    }
    
    @Override
    @Transactional(readOnly = true)
    public Page<TrainingRecord> findByUserId(Long userId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return trainingRecordRepository.findByUserIdOrderByTrainingDateDesc(userId, pageable);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<TrainingRecord> findByUserIdAndDateRange(Long userId, LocalDate startDate, LocalDate endDate) {
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
    public TrainingStatsResponse getTrainingStats(Long userId, LocalDate startDate, LocalDate endDate) {
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
    public List<TrainingRecord> getRecentTrainingRecords(Long userId) {
        return trainingRecordRepository.findTop10ByUserIdOrderByTrainingDateDesc(userId);
    }
    
    @Override
    @Transactional(readOnly = true)
    public long countByUserId(Long userId) {
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
    public void deleteByUserAndId(User user, Long id) {
        trainingRecordRepository.deleteByUserAndId(user, id);
    }
    
    @Override
    public void deleteAllByUserId(Long userId) {
        logger.info("删除用户所有训练记录，用户ID: {}", userId);
        trainingRecordRepository.deleteAllByUserId(userId);
        logger.info("用户训练记录删除完成，用户ID: {}", userId);
    }
}