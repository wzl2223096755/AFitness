package com.wzl.fitness.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * 用户身体数据记录实体类
 */
@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "body_records")
public class BodyRecord {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @JsonIgnore
    private User user;

    @Column(nullable = false)
    private Double weight; // 体重 (kg)

    @Column
    private Double bodyFat; // 体脂率 (%)

    @Column
    private Double muscleMass; // 肌肉量 (kg)

    @Column
    private Double waistCircumference; // 腰围 (cm)

    @Column
    private Double hipCircumference; // 臀围 (cm)

    @Column
    private Double chestCircumference; // 胸围 (cm)

    @Column(name = "record_time")
    private LocalDateTime recordTime;

    @Column(name = "created_at")
    @CreationTimestamp
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        if (recordTime == null) {
            recordTime = LocalDateTime.now();
        }
    }
}
