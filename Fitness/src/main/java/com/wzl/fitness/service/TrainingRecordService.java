package com.wzl.fitness.service;

import com.wzl.fitness.entity.TrainingRecord;
import com.wzl.fitness.entity.User;
import com.wzl.fitness.dto.response.TrainingStatsResponse;
import org.springframework.data.domain.Page;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * 训练记录服务接口
 * 支持软删除和数据恢复功能
 */
public interface TrainingRecordService {
    
    /**
     * 创建训练记录
     */
    TrainingRecord createTrainingRecord(TrainingRecord record);
    
    /**
     * 更新训练记录
     */
    Optional<TrainingRecord> updateTrainingRecord(Long id, TrainingRecord record);
    
    /**
     * 删除训练记录（软删除）
     */
    boolean deleteTrainingRecord(Long id);
    
    /**
     * 软删除训练记录
     * 
     * @param id 记录ID
     * @param userId 执行删除操作的用户ID
     * @return 是否删除成功
     */
    boolean softDeleteTrainingRecord(Long id, Long userId);
    
    /**
     * 恢复已删除的训练记录
     * 
     * @param id 记录ID
     * @return 是否恢复成功
     */
    boolean restoreTrainingRecord(Long id);
    
    /**
     * 批量恢复用户的所有已删除记录
     * 
     * @param userId 用户ID
     * @return 恢复的记录数
     */
    int restoreAllByUserId(Long userId);
    
    /**
     * 获取用户已删除的训练记录
     * 
     * @param userId 用户ID
     * @return 已删除的记录列表
     */
    List<TrainingRecord> findDeletedByUserId(Long userId);
    
    /**
     * 统计用户已删除的记录数
     * 
     * @param userId 用户ID
     * @return 已删除的记录数
     */
    long countDeletedByUserId(Long userId);
    
    /**
     * 根据ID获取训练记录
     */
    Optional<TrainingRecord> findById(Long id);
    
    /**
     * 根据用户获取训练记录
     */
    List<TrainingRecord> findByUserId(Long userId);
    
    /**
     * 根据用户获取训练记录（分页）
     */
    Page<TrainingRecord> findByUserId(Long userId, int page, int size);
    
    /**
     * 根据用户和日期范围获取训练记录
     */
    List<TrainingRecord> findByUserIdAndDateRange(Long userId, LocalDate startDate, LocalDate endDate);
    
    /**
     * 获取用户训练统计
     */
    TrainingStatsResponse getTrainingStats(Long userId);
    
    /**
     * 获取用户训练统计（带日期范围）
     */
    TrainingStatsResponse getTrainingStats(Long userId, LocalDate startDate, LocalDate endDate);
    
    /**
     * 获取用户最近的训练记录
     */
    List<TrainingRecord> getRecentTrainingRecords(Long userId);
    
    /**
     * 统计用户的训练记录数量
     */
    long countByUserId(Long userId);
    
    /**
     * 根据用户获取训练记录
     */
    List<TrainingRecord> findByUser(User user);
    
    /**
     * 根据用户和日期获取训练记录
     */
    Optional<TrainingRecord> findByUserAndDate(User user, LocalDate date);
    
    /**
     * 删除用户的训练记录
     */
    void deleteByUserAndId(User user, Long id);
    
    /**
     * 删除用户的所有训练记录
     * 
     * @param userId 用户ID
     */
    void deleteAllByUserId(Long userId);
}