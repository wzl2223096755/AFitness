package com.wzl.fitness.modules.training.api;

import com.wzl.fitness.modules.training.dto.TrainingRecordDTO;
import com.wzl.fitness.modules.training.dto.TrainingLoadDTO;
import com.wzl.fitness.modules.training.dto.TrainingStatsDTO;

import java.time.LocalDate;
import java.util.List;

/**
 * 训练模块对外接口
 * 定义训练模块对其他模块暴露的服务契约
 * 
 * @see Requirements 2.1 - 为每个领域模块定义独立的服务接口
 */
public interface TrainingModuleApi {
    
    /**
     * 获取用户最近的训练记录
     * 
     * @param userId 用户ID
     * @param limit 返回记录数量限制
     * @return 训练记录DTO列表
     */
    List<TrainingRecordDTO> getRecentRecords(Long userId, int limit);
    
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
}
