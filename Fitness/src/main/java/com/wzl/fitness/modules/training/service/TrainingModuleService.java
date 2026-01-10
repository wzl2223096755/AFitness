package com.wzl.fitness.modules.training.service;

import com.wzl.fitness.entity.TrainingRecord;
import com.wzl.fitness.modules.training.dto.TrainingRecordDTO;
import com.wzl.fitness.modules.training.dto.TrainingLoadDTO;
import com.wzl.fitness.modules.training.dto.TrainingStatsDTO;

import java.time.LocalDate;
import java.util.List;

/**
 * 训练模块服务接口
 * 定义训练模块内部的业务逻辑方法
 * 
 * @see Requirements 1.2 - 领域模块包含独立的service子包
 */
public interface TrainingModuleService {
    
    /**
     * 获取用户最近的训练记录
     * 
     * @param userId 用户ID
     * @param limit 返回记录数量限制
     * @return 训练记录列表
     */
    List<TrainingRecord> getRecentRecords(Long userId, int limit);
    
    /**
     * 计算用户训练负荷
     * 
     * @param userId 用户ID
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 训练负荷DTO
     */
    TrainingLoadDTO calculateLoad(Long userId, LocalDate startDate, LocalDate endDate);
    
    /**
     * 获取训练统计数据
     * 
     * @param userId 用户ID
     * @return 训练统计DTO
     */
    TrainingStatsDTO getStats(Long userId);
    
    /**
     * 将训练记录实体转换为DTO
     * 
     * @param record 训练记录实体
     * @return 训练记录DTO
     */
    TrainingRecordDTO convertToDTO(TrainingRecord record);
    
    /**
     * 批量将训练记录实体转换为DTO
     * 
     * @param records 训练记录实体列表
     * @return 训练记录DTO列表
     */
    List<TrainingRecordDTO> convertToDTOList(List<TrainingRecord> records);
}
