package com.wzl.fitness.service.impl;

import com.wzl.fitness.entity.CardioTrainingData;
import com.wzl.fitness.entity.User;
import com.wzl.fitness.repository.CardioTrainingDataRepository;
import com.wzl.fitness.repository.UserRepository;
import com.wzl.fitness.service.CardioTrainingService;
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
 * 有氧训练数据服务实现
 */
@Service
@RequiredArgsConstructor
@Transactional
public class CardioTrainingServiceImpl implements CardioTrainingService {

    private static final Logger logger = LoggerFactory.getLogger(CardioTrainingServiceImpl.class);

    private final CardioTrainingDataRepository cardioTrainingDataRepository;
    private final UserRepository userRepository;

    @Override
    public CardioTrainingData createCardioTrainingData(Long userId, CardioTrainingData data) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("用户不存在"));
        
        data.setUser(user);
        if (data.getTimestamp() == null) {
            data.setTimestamp(LocalDateTime.now());
        }
        
        // 计算配速 (分钟/千米)
        if (data.getDuration() != null && data.getDistance() != null && data.getDistance() > 0) {
            data.setPace(data.getDuration().doubleValue() / data.getDistance());
            // 计算平均速度 (km/h)
            data.setAverageSpeed(data.getDistance() / (data.getDuration().doubleValue() / 60.0));
        }
        
        CardioTrainingData savedData = cardioTrainingDataRepository.save(data);
        logger.info("创建有氧训练数据成功，用户ID: {}, 数据ID: {}", userId, savedData.getId());
        return savedData;
    }

    @Override
    public CardioTrainingData updateCardioTrainingData(Long id, CardioTrainingData data) {
        CardioTrainingData existingData = getCardioTrainingDataById(id);
        
        existingData.setExerciseType(data.getExerciseType());
        existingData.setDuration(data.getDuration());
        existingData.setDistance(data.getDistance());
        existingData.setAverageHeartRate(data.getAverageHeartRate());
        existingData.setMaxHeartRate(data.getMaxHeartRate());
        existingData.setCaloriesBurned(data.getCaloriesBurned());
        existingData.setPerceivedExertion(data.getPerceivedExertion());
        existingData.setTimestamp(data.getTimestamp());
        
        // 重新计算配速和平均速度
        if (existingData.getDuration() != null && existingData.getDistance() != null && existingData.getDistance() > 0) {
            existingData.setPace(existingData.getDuration().doubleValue() / existingData.getDistance());
            existingData.setAverageSpeed(existingData.getDistance() / (existingData.getDuration().doubleValue() / 60.0));
        }
        
        CardioTrainingData updatedData = cardioTrainingDataRepository.save(existingData);
        logger.info("更新有氧训练数据成功，数据ID: {}", id);
        return updatedData;
    }

    @Override
    public void deleteCardioTrainingData(Long id) {
        if (!cardioTrainingDataRepository.existsById(id)) {
            throw new RuntimeException("有氧训练数据不存在");
        }
        cardioTrainingDataRepository.deleteById(id);
        logger.info("删除有氧训练数据成功，数据ID: {}", id);
    }

    @Override
    @Transactional(readOnly = true)
    public CardioTrainingData getCardioTrainingDataById(Long id) {
        return cardioTrainingDataRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("有氧训练数据不存在"));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<CardioTrainingData> getUserCardioTrainingData(Long userId, Pageable pageable) {
        return cardioTrainingDataRepository.findByUserId(userId, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CardioTrainingData> getCardioTrainingDataByDateRange(Long userId, LocalDateTime start, LocalDateTime end) {
        return cardioTrainingDataRepository.findByUserIdAndTimestampBetween(userId, start, end);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CardioTrainingData> getCardioTrainingDataByExerciseType(Long userId, String exerciseType) {
        return cardioTrainingDataRepository.findByUserIdAndExerciseType(userId, exerciseType);
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, Object> getCardioStats(Long userId, LocalDateTime start, LocalDateTime end) {
        Map<String, Object> stats = new HashMap<>();
        
        Integer totalDuration = cardioTrainingDataRepository.calculateTotalDurationByUserAndDateRange(userId, start, end);
        Double totalCalories = cardioTrainingDataRepository.calculateTotalCaloriesByUserAndDateRange(userId, start, end);
        
        stats.put("totalDuration", totalDuration != null ? totalDuration : 0);
        stats.put("totalCalories", totalCalories != null ? totalCalories : 0.0);
        
        return stats;
    }
}
