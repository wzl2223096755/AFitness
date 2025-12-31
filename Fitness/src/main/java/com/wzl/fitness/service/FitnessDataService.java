package com.wzl.fitness.service;

import com.wzl.fitness.entity.FitnessData;
import com.wzl.fitness.entity.User;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 健身数据服务接口（已废弃）
 * 
 * @deprecated 此服务使用已废弃的 {@link FitnessData} 实体。
 * 建议使用以下替代服务：
 * <ul>
 *   <li>力量训练数据：使用 {@link StrengthTrainingService}</li>
 *   <li>恢复状态数据：使用 {@link LoadRecoveryService}</li>
 * </ul>
 * 
 * @see StrengthTrainingService
 * @see LoadRecoveryService
 */
@Deprecated(since = "1.1.0", forRemoval = true)
public interface FitnessDataService {
    List<FitnessData> getFitnessDataByUser(User user);
    List<FitnessData> getFitnessDataByUserAndDateRange(User user, LocalDateTime start, LocalDateTime end);
    List<FitnessData> getLatestFitnessDataByUser(Long userId);

}
