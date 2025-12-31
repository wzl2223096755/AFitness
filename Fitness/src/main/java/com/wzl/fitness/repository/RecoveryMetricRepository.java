package com.wzl.fitness.repository;

import com.wzl.fitness.entity.RecoveryMetric;
import com.wzl.fitness.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDate;
import java.util.List;

public interface RecoveryMetricRepository extends JpaRepository<RecoveryMetric, Long> {
    List<RecoveryMetric> findByUserId(Long userId);
    RecoveryMetric findFirstByUserIdOrderByRecordDateDesc(Long userId);
    RecoveryMetric findByUserIdAndRecordDate(Long userId, LocalDate recordDate);
    
    // 新增方法
    List<RecoveryMetric> findByUser(User user);
    List<RecoveryMetric> findByUserOrderByRecordDateDesc(User user);
    RecoveryMetric findByUserAndRecordDate(User user, LocalDate recordDate);
    List<RecoveryMetric> findByUserAndRecordDateBetween(User user, LocalDate startDate, LocalDate endDate);
    void deleteByUserAndId(User user, Long id);
}