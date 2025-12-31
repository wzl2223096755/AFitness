package com.wzl.fitness.repository;

import com.wzl.fitness.entity.BodyRecord;
import com.wzl.fitness.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BodyRecordRepository extends JpaRepository<BodyRecord, Long> {
    List<BodyRecord> findByUserOrderByRecordTimeDesc(User user);
    Page<BodyRecord> findByUserOrderByRecordTimeDesc(User user, Pageable pageable);
}
