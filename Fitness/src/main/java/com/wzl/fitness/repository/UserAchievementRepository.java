package com.wzl.fitness.repository;

import com.wzl.fitness.entity.User;
import com.wzl.fitness.entity.UserAchievement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserAchievementRepository extends JpaRepository<UserAchievement, Long> {
    List<UserAchievement> findByUserOrderByUnlockTimeDesc(User user);
    List<UserAchievement> findByUserAndUnlockedTrueOrderByUnlockTimeDesc(User user);
}
