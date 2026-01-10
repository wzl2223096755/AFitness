package com.wzl.fitness.modules.recovery.api;

import com.wzl.fitness.modules.recovery.dto.RecoveryStatusDTO;
import com.wzl.fitness.modules.recovery.dto.TrainingSuggestionDTO;
import com.wzl.fitness.modules.recovery.service.RecoveryModuleService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 恢复评估模块API实现
 * 
 * 实现RecoveryModuleApi接口，提供恢复评估模块对外服务
 * 
 * @see Requirements 2.1 - 为每个领域模块定义独立的服务接口
 * @see Requirements 2.5 - 模块接口使用DTO进行数据传输
 * @see Requirements 6.2 - 支持通过配置文件启用/禁用模块
 */
@Component
@ConditionalOnProperty(
    prefix = "fitness.modules.recovery",
    name = "enabled",
    havingValue = "true",
    matchIfMissing = true
)
public class RecoveryModuleApiImpl implements RecoveryModuleApi {
    
    private static final Logger log = LoggerFactory.getLogger(RecoveryModuleApiImpl.class);
    
    private final RecoveryModuleService recoveryModuleService;
    
    public RecoveryModuleApiImpl(RecoveryModuleService recoveryModuleService) {
        this.recoveryModuleService = recoveryModuleService;
    }
    
    @Override
    public RecoveryStatusDTO getCurrentStatus(Long userId) {
        log.debug("RecoveryModuleApi: 获取用户 {} 的当前恢复状态", userId);
        return recoveryModuleService.getCurrentStatus(userId);
    }
    
    @Override
    public List<TrainingSuggestionDTO> getSuggestions(Long userId) {
        log.debug("RecoveryModuleApi: 获取用户 {} 的训练建议", userId);
        return recoveryModuleService.getSuggestions(userId);
    }
}
