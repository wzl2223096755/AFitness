package com.wzl.fitness.repository;

import com.wzl.fitness.entity.TrainingPlan;
import com.wzl.fitness.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface TrainingPlanRepository extends JpaRepository<TrainingPlan, Long> {
    List<TrainingPlan> findByUserOrderByCreatedAtDesc(User user);
    Page<TrainingPlan> findByUser(User user, Pageable pageable);
    List<TrainingPlan> findByUserAndStatus(User user, String status);
    List<TrainingPlan> findByUserAndStartDateBetween(User user, LocalDate startDate, LocalDate endDate);
}
