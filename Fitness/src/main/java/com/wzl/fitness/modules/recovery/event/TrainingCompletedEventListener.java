package com.wzl.fitness.modules.recovery.event;

import com.wzl.fitness.modules.recovery.service.RecoveryModuleService;
import com.wzl.fitness.modules.training.event.TrainingCompletedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

/**
 * 训练完成事件监听器
 * 
 * 监听TrainingCompletedEvent事件，在训练完成后自动更新恢复状态
 * 
 * @see Requirements 3.3 - 收到TrainingCompletedEvent时，恢复模块自动更新恢复状态评估
 */
@Component
public class TrainingCompletedEventListener {
    
    private static final Logger log = LoggerFactory.getLogger(TrainingCompletedEventListener.class);
    
    private final RecoveryModuleService recoveryModuleService;
    
    public TrainingCompletedEventListener(RecoveryModuleService recoveryModuleService) {
        this.recoveryModuleService = recoveryModuleService;
    }
    
    /**
     * 处理训练完成事件
     * 
     * 当收到TrainingCompletedEvent时，自动更新用户的恢复状态
     * 使用异步处理以避免阻塞训练记录保存流程
     * 
     * @param event 训练完成事件
     */
    @EventListener
    @Async
    public void handleTrainingCompletedEvent(TrainingCompletedEvent event) {
        log.info("收到训练完成事件: {}", event);
        
        try {
            Long userId = event.getUserId();
            Long trainingRecordId = event.getTrainingRecordId();
            Double totalVolume = event.getTotalVolume();
            String exerciseType = event.getExerciseType();
            
            if (userId == null) {
                log.warn("训练完成事件缺少用户ID，跳过恢复状态更新");
                return;
            }
            
            // 更新恢复状态
            recoveryModuleService.updateRecoveryAfterTraining(
                    userId, 
                    trainingRecordId, 
                    totalVolume, 
                    exerciseType
            );
            
            log.info("用户 {} 的恢复状态已根据训练完成事件更新", userId);
            
        } catch (Exception e) {
            log.error("处理训练完成事件时发生错误: {}", e.getMessage(), e);
            // 不抛出异常，避免影响其他事件监听器
        }
    }
}
