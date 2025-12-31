package com.wzl.fitness.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.time.LocalDateTime;

/**
 * 用户设备关联实体类
 */
@Entity
@Table(name = "user_device", 
       uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "device_id"}))
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserDevice {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "user_id", nullable = false)
    private Long userId;
    
    @Column(name = "device_id", nullable = false)
    private Long deviceId;
    
    @Column(name = "bind_time")
    private LocalDateTime bindTime;
    
    @PrePersist
    protected void onCreate() {
        if (bindTime == null) {
            bindTime = LocalDateTime.now();
        }
    }
}