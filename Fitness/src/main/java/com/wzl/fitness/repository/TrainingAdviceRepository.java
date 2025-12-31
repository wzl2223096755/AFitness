package com.wzl.fitness.repository;

import com.wzl.fitness.entity.TrainingAdvice;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface TrainingAdviceRepository extends JpaRepository<TrainingAdvice, Long> {
    List<TrainingAdvice> findByUserIdOrderByAdviceDateDesc(Long userId);
}