package com.wzl.fitness.service.impl;

import com.wzl.fitness.entity.StrengthTrainingData;
import com.wzl.fitness.entity.User;
import com.wzl.fitness.repository.StrengthTrainingDataRepository;
import com.wzl.fitness.repository.UserRepository;
import com.wzl.fitness.service.StrengthTrainingService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 力量训练数据服务实现
 */
@Service
@RequiredArgsConstructor
@Transactional
public class StrengthTrainingServiceImpl implements StrengthTrainingService {

    private static final Logger logger = LoggerFactory.getLogger(StrengthTrainingServiceImpl.class);

    private final StrengthTrainingDataRepository strengthTrainingDataRepository;
    private final UserRepository userRepository;

    @Override
    public StrengthTrainingData createStrengthTrainingData(Long userId, StrengthTrainingData data) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("用户不存在"));
        
        data.setUser(user);
        data.setTimestamp(LocalDateTime.now());
        
        // 计算训练量
        if (data.getWeight() != null && data.getSets() != null && data.getReps() != null) {
            data.setTrainingVolume(data.getWeight() * data.getSets() * data.getReps());
        }
        
        // 估算最大重量（使用3RM公式：重量 × (1 + 次数 × 0.033)）
        if (data.getWeight() != null && data.getReps() != null && data.getReps() <= 10) {
            data.setOneRepMax(data.getWeight() * (1 + data.getReps() * 0.033));
        }
        
        StrengthTrainingData savedData = strengthTrainingDataRepository.save(data);
        logger.info("创建力量训练数据成功，用户ID: {}, 数据ID: {}", userId, savedData.getId());
        return savedData;
    }

    @Override
    public StrengthTrainingData updateStrengthTrainingData(Long id, StrengthTrainingData data) {
        StrengthTrainingData existingData = getStrengthTrainingDataById(id);
        
        // 更新字段
        existingData.setExerciseName(data.getExerciseName());
        existingData.setWeight(data.getWeight());
        existingData.setSets(data.getSets());
        existingData.setReps(data.getReps());
        existingData.setExerciseType(data.getExerciseType());
        existingData.setPerceivedExertion(data.getPerceivedExertion());
        
        // 重新计算训练量和最大重量
        if (existingData.getWeight() != null && existingData.getSets() != null && existingData.getReps() != null) {
            existingData.setTrainingVolume(existingData.getWeight() * existingData.getSets() * existingData.getReps());
        }
        
        if (existingData.getWeight() != null && existingData.getReps() != null && existingData.getReps() <= 10) {
            existingData.setOneRepMax(existingData.getWeight() * (1 + existingData.getReps() * 0.033));
        }
        
        StrengthTrainingData updatedData = strengthTrainingDataRepository.save(existingData);
        logger.info("更新力量训练数据成功，数据ID: {}", id);
        return updatedData;
    }

    @Override
    public void deleteStrengthTrainingData(Long id) {
        if (!strengthTrainingDataRepository.existsById(id)) {
            throw new RuntimeException("力量训练数据不存在");
        }
        strengthTrainingDataRepository.deleteById(id);
        logger.info("删除力量训练数据成功，数据ID: {}", id);
    }

    @Override
    @Transactional(readOnly = true)
    public StrengthTrainingData getStrengthTrainingDataById(Long id) {
        return strengthTrainingDataRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("力量训练数据不存在"));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<StrengthTrainingData> getUserStrengthTrainingData(Long userId, Pageable pageable) {
        return strengthTrainingDataRepository.findByUserId(userId, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public List<StrengthTrainingData> getStrengthTrainingDataByDateRange(Long userId, LocalDateTime start, LocalDateTime end) {
        return strengthTrainingDataRepository.findByUserIdAndTimestampBetween(userId, start, end);
    }

    @Override
    @Transactional(readOnly = true)
    public List<StrengthTrainingData> getStrengthTrainingDataByExercise(Long userId, String exerciseName) {
        return strengthTrainingDataRepository.findByUserIdAndExerciseType(userId, exerciseName);
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, Object> getMaxWeightStats(Long userId) {
        Map<String, Object> stats = new HashMap<>();
        
        // 获取所有动作类型
        List<String> exerciseTypes = strengthTrainingDataRepository.findByUserId(userId)
                .stream()
                .map(StrengthTrainingData::getExerciseType)
                .distinct()
                .toList();
        
        Map<String, Double> maxWeights = new HashMap<>();
        for (String exerciseType : exerciseTypes) {
            Double maxWeight = strengthTrainingDataRepository.findMaxWeightByUserAndExercise(userId, exerciseType);
            if (maxWeight != null) {
                maxWeights.put(exerciseType, maxWeight);
            }
        }
        
        stats.put("maxWeights", maxWeights);
        stats.put("totalExercises", exerciseTypes.size());
        return stats;
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, Object> getTrainingVolumeStats(Long userId, LocalDateTime start, LocalDateTime end) {
        Map<String, Object> stats = new HashMap<>();
        
        Double totalVolume = strengthTrainingDataRepository.calculateTotalVolumeByUserAndDateRange(userId, start, end);
        List<StrengthTrainingData> trainingData = strengthTrainingDataRepository.findByUserIdAndTimestampBetween(userId, start, end);
        
        stats.put("totalVolume", totalVolume != null ? totalVolume : 0.0);
        stats.put("trainingSessions", trainingData.size());
        stats.put("averageVolume", trainingData.size() > 0 ? (totalVolume != null ? totalVolume / trainingData.size() : 0.0) : 0.0);
        
        return stats;
    }
}