package com.wzl.fitness.modules.recovery.api;

import com.wzl.fitness.modules.recovery.dto.RecoveryStatusDTO;
import com.wzl.fitness.modules.recovery.dto.TrainingSuggestionDTO;

import java.util.List;

/**
 * 恢复评估模块对外接口
 * 定义恢复评估模块对其他模块暴露的服务契约
 * 
 * @see Requirements 2.1 - 为每个领域模块定义独立的服务接口
 */
public interface RecoveryModuleApi {
    
    /**
     * 获取当前恢复状态
     * 
     * @param userId 用户ID
     * @return 恢复状态DTO
     */
    RecoveryStatusDTO getCurrentStatus(Long userId);
    
    /**
     * 获取训练建议
     * 
     * @param userId 用户ID
     * @return 训练建议DTO列表
     */
    List<TrainingSuggestionDTO> getSuggestions(Long userId);
}
