package com.wzl.fitness.modules.training.api;

import com.wzl.fitness.modules.training.dto.TrainingRecordDTO;
import com.wzl.fitness.modules.training.dto.TrainingLoadDTO;
import com.wzl.fitness.modules.training.dto.TrainingStatsDTO;
import com.wzl.fitness.modules.training.service.TrainingModuleService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

/**
 * 训练模块API实现
 * 实现训练模块对外暴露的服务契约
 * 
 * @see Requirements 2.1 - 为每个领域模块定义独立的服务接口
 * @see Requirements 2.5 - 模块接口使用DTO进行数据传输
 * @see Requirements 6.2 - 支持通过配置文件启用/禁用模块
 */
@Component
@RequiredArgsConstructor
@Slf4j
@ConditionalOnProperty(
    prefix = "fitness.modules.training",
    name = "enabled",
    havingValue = "true",
    matchIfMissing = true
)
public class TrainingModuleApiImpl implements TrainingModuleApi {
    
    private final TrainingModuleService trainingModuleService;
    
    @Override
    public List<TrainingRecordDTO> getRecentRecords(Long userId, int limit) {
        log.debug("TrainingModuleApi.getRecentRecords: 获取用户 {} 最近 {} 条训练记录", userId, limit);
        return trainingModuleService.convertToDTOList(
                trainingModuleService.getRecentRecords(userId, limit));
    }
    
    @Override
    public TrainingLoadDTO calculateLoad(Long userId, LocalDate startDate, LocalDate endDate) {
        log.debug("TrainingModuleApi.calculateLoad: 计算用户 {} 从 {} 到 {} 的训练负荷", userId, startDate, endDate);
        return trainingModuleService.calculateLoad(userId, startDate, endDate);
    }
    
    @Override
    public TrainingStatsDTO getStats(Long userId) {
        log.debug("TrainingModuleApi.getStats: 获取用户 {} 的训练统计数据", userId);
        return trainingModuleService.getStats(userId);
    }
}
