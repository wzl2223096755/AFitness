package com.wzl.fitness.modules.training.service.impl;

import com.wzl.fitness.entity.TrainingRecord;
import com.wzl.fitness.modules.training.dto.TrainingRecordDTO;
import com.wzl.fitness.modules.training.dto.TrainingLoadDTO;
import com.wzl.fitness.modules.training.dto.TrainingStatsDTO;
import com.wzl.fitness.modules.training.repository.TrainingModuleRepository;
import com.wzl.fitness.modules.training.service.TrainingModuleService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 训练模块服务实现
 * 实现训练模块内部的业务逻辑
 * 
 * @see Requirements 1.2 - 领域模块包含独立的service子包
 * @see Requirements 6.2 - 支持通过配置文件启用/禁用模块
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
@ConditionalOnProperty(
    prefix = "fitness.modules.training",
    name = "enabled",
    havingValue = "true",
    matchIfMissing = true
)
public class TrainingModuleServiceImpl implements TrainingModuleService {
    
    private final TrainingModuleRepository trainingModuleRepository;
    
    @Override
    @Cacheable(value = "trainingRecords", key = "'recent:' + #userId + ':' + #limit")
    public List<TrainingRecord> getRecentRecords(Long userId, int limit) {
        log.debug("获取用户 {} 最近 {} 条训练记录", userId, limit);
        return trainingModuleRepository.findRecentByUserId(userId, PageRequest.of(0, limit));
    }
    
    @Override
    @Cacheable(value = "trainingLoad", key = "'load:' + #userId + ':' + #startDate + ':' + #endDate")
    public TrainingLoadDTO calculateLoad(Long userId, LocalDate startDate, LocalDate endDate) {
        log.debug("计算用户 {} 从 {} 到 {} 的训练负荷", userId, startDate, endDate);
        
        // 获取指定日期范围内的训练数据
        List<TrainingRecord> records = trainingModuleRepository.findByUserIdAndDateRange(userId, startDate, endDate);
        Double totalVolume = trainingModuleRepository.sumVolumeByUserIdAndDateRange(userId, startDate, endDate);
        int totalSessions = trainingModuleRepository.countDistinctTrainingDaysByUserIdAndDateRange(userId, startDate, endDate);
        
        // 计算平均训练量
        double averageVolume = totalSessions > 0 ? totalVolume / totalSessions : 0.0;
        
        // 计算急性负荷（最近7天）
        LocalDate acuteStartDate = endDate.minusDays(6);
        Double acuteVolume = trainingModuleRepository.sumVolumeByUserIdAndDateRange(userId, acuteStartDate, endDate);
        double acuteLoad = acuteVolume != null ? acuteVolume / 7.0 : 0.0;
        
        // 计算慢性负荷（最近28天）
        LocalDate chronicStartDate = endDate.minusDays(27);
        Double chronicVolume = trainingModuleRepository.sumVolumeByUserIdAndDateRange(userId, chronicStartDate, endDate);
        double chronicLoad = chronicVolume != null ? chronicVolume / 28.0 : 0.0;
        
        // 计算急慢性负荷比
        double acuteChronicRatio = chronicLoad > 0 ? acuteLoad / chronicLoad : 0.0;
        
        // 确定负荷状态
        String loadStatus = determineLoadStatus(acuteChronicRatio);
        
        return TrainingLoadDTO.builder()
                .userId(userId)
                .startDate(startDate)
                .endDate(endDate)
                .totalVolume(totalVolume != null ? totalVolume : 0.0)
                .averageVolume(averageVolume)
                .totalSessions(totalSessions)
                .acuteLoad(acuteLoad)
                .chronicLoad(chronicLoad)
                .acuteChronicRatio(acuteChronicRatio)
                .loadStatus(loadStatus)
                .build();
    }
    
    @Override
    @Cacheable(value = "trainingStats", key = "'stats:' + #userId")
    public TrainingStatsDTO getStats(Long userId) {
        log.debug("获取用户 {} 的训练统计数据", userId);
        
        // 获取总记录数
        long totalRecords = trainingModuleRepository.countByUserId(userId);
        
        // 获取总训练量
        Double totalVolume = trainingModuleRepository.sumVolumeByUserId(userId);
        
        // 获取总训练时长
        Long totalDuration = trainingModuleRepository.sumDurationByUserId(userId);
        
        // 获取最常训练的动作
        List<String> mostFrequentExercises = trainingModuleRepository.findMostFrequentExerciseByUserId(
                userId, PageRequest.of(0, 1));
        String mostFrequentExercise = mostFrequentExercises.isEmpty() ? null : mostFrequentExercises.get(0);
        
        // 获取最大重量
        Double personalBestWeight = trainingModuleRepository.findMaxWeightByUserId(userId);
        
        // 计算训练次数（按天计算）
        LocalDate thirtyDaysAgo = LocalDate.now().minusDays(30);
        int totalSessions = trainingModuleRepository.countDistinctTrainingDaysByUserIdAndDateRange(
                userId, thirtyDaysAgo, LocalDate.now());
        
        // 计算平均每次训练量
        double averageVolumePerSession = totalSessions > 0 && totalVolume != null 
                ? totalVolume / totalSessions : 0.0;
        
        return TrainingStatsDTO.builder()
                .userId(userId)
                .totalRecords(totalRecords)
                .totalVolume(totalVolume != null ? totalVolume : 0.0)
                .totalDuration(totalDuration != null ? totalDuration : 0L)
                .totalSessions(totalSessions)
                .averageVolumePerSession(averageVolumePerSession)
                .mostFrequentExercise(mostFrequentExercise)
                .personalBestWeight(personalBestWeight != null ? personalBestWeight : 0.0)
                .build();
    }
    
    @Override
    public TrainingRecordDTO convertToDTO(TrainingRecord record) {
        if (record == null) {
            return null;
        }
        
        return TrainingRecordDTO.builder()
                .id(record.getId())
                .userId(record.getUser() != null ? record.getUser().getId() : null)
                .exerciseName(record.getExerciseName())
                .sets(record.getSets())
                .reps(record.getReps())
                .weight(record.getWeight())
                .duration(record.getDuration())
                .totalVolume(record.getTotalVolume() != null ? record.getTotalVolume() : record.getCalculatedTotalVolume())
                .trainingDate(record.getTrainingDate())
                .notes(record.getNotes())
                .createdAt(record.getCreatedAt())
                .build();
    }
    
    @Override
    public List<TrainingRecordDTO> convertToDTOList(List<TrainingRecord> records) {
        if (records == null) {
            return List.of();
        }
        return records.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    /**
     * 根据急慢性负荷比确定负荷状态
     */
    private String determineLoadStatus(double acuteChronicRatio) {
        if (acuteChronicRatio < 0.8) {
            return "UNDERTRAINING";
        } else if (acuteChronicRatio <= 1.3) {
            return "OPTIMAL";
        } else if (acuteChronicRatio <= 1.5) {
            return "HIGH_RISK";
        } else {
            return "VERY_HIGH_RISK";
        }
    }
}
