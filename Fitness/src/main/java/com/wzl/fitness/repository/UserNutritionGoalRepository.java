package com.wzl.fitness.repository;

import com.wzl.fitness.entity.User;
import com.wzl.fitness.entity.UserNutritionGoal;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * 用户营养目标Repository
 */
@Repository
public interface UserNutritionGoalRepository extends JpaRepository<UserNutritionGoal, Long> {
    
    /**
     * 根据用户查找营养目标
     */
    Optional<UserNutritionGoal> findByUser(User user);
    
    /**
     * 根据用户ID查找营养目标
     */
    Optional<UserNutritionGoal> findByUserId(Long userId);
    
    /**
     * 检查用户是否已设置营养目标
     */
    boolean existsByUser(User user);
    
    /**
     * 删除用户的营养目标
     */
    void deleteByUser(User user);
}
