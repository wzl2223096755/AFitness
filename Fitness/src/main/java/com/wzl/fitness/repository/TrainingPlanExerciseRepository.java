package com.wzl.fitness.repository;

import com.wzl.fitness.entity.TrainingPlanExercise;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TrainingPlanExerciseRepository extends JpaRepository<TrainingPlanExercise, Long> {
    List<TrainingPlanExercise> findByDayIdOrderByOrderIndexAsc(Long dayId);
}
