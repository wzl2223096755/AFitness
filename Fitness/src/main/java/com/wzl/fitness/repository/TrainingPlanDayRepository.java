package com.wzl.fitness.repository;

import com.wzl.fitness.entity.TrainingPlanDay;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TrainingPlanDayRepository extends JpaRepository<TrainingPlanDay, Long> {
    List<TrainingPlanDay> findByPlanIdOrderByDayOfWeekAsc(Long planId);
}
