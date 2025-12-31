package com.wzl.fitness.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.SQLRestriction;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 训练记录实体
 * 支持软删除功能
 */
@Entity
@Data
@EqualsAndHashCode(callSuper = true)
@Table(name = "training_records")
@ToString(exclude = {"user", "exerciseDetails"})
@SQLRestriction("deleted = false")
public class TrainingRecord extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    // 用户关系
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @JsonIgnore
    private User user;
    
    @NotBlank(message = "运动名称不能为空")
    @Size(max = 100, message = "运动名称不能超过100个字符")
    private String exerciseName;
    
    @NotNull(message = "组数不能为空")
    @Min(value = 1, message = "组数至少为1")
    private Integer sets;
    
    @NotNull(message = "次数不能为空")
    @Min(value = 1, message = "次数至少为1")
    private Integer reps;
    
    @NotNull(message = "重量不能为空")
    @DecimalMin(value = "0.0", message = "重量不能为负数")
    private Double weight;
    
    @NotNull(message = "训练日期不能为空")
    @PastOrPresent(message = "训练日期不能是未来日期")
    private LocalDate trainingDate;
    
    @NotNull(message = "时长不能为空")
    @Min(value = 1, message = "时长至少为1分钟")
    private Integer duration;
    
    @Size(max = 500, message = "备注不能超过500个字符")
    private String notes;
    
    // 计算字段：总训练量 = 组数 × 次数 × 重量（如果持久化字段为空则计算）
    @Transient
    public Double getCalculatedTotalVolume() {
        if (totalVolume != null) {
            return totalVolume;
        }
        return sets != null && reps != null && weight != null ? 
            sets * reps * weight : 0.0;
    }
    
    // 计算字段：消耗卡路里（简化计算）
    @Transient
    public Integer getCaloriesBurned() {
        return duration != null ? (int)(duration * 8.0) : 0; // 假设每分钟消耗8卡路里
    }
    
    // 训练动作详情列表
    @OneToMany(mappedBy = "trainingRecord", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnore
    private List<ExerciseDetail> exerciseDetails;
    
    // 总训练量（持久化字段）
    @Column(name = "total_volume")
    private Double totalVolume;
    
    // 训练压力（持久化字段）
    @Column(name = "training_stress")
    private Double trainingStress;
    
    // 设置训练动作详情
    public void setExerciseDetails(List<ExerciseDetail> exerciseDetails) {
        this.exerciseDetails = exerciseDetails;
    }
    
    // 获取训练动作详情
    public List<ExerciseDetail> getExerciseDetails() {
        return exerciseDetails;
    }
    
    // 获取训练压力
    public Double getTrainingStress() {
        return trainingStress;
    }
    
    // 设置训练压力
    public void setTrainingStress(Double trainingStress) {
        this.trainingStress = trainingStress;
    }
    
    // 设置总训练量
    public void setTotalVolume(Double totalVolume) {
        this.totalVolume = totalVolume;
    }
    
    // 获取总训练量
    public Double getTotalVolume() {
        return totalVolume;
    }
    
    // Explicit getters and setters to ensure compilation
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public User getUser() {
        return user;
    }
    
    public void setUser(User user) {
        this.user = user;
    }
    
    public String getExerciseName() {
        return exerciseName;
    }
    
    public void setExerciseName(String exerciseName) {
        this.exerciseName = exerciseName;
    }
    
    public Integer getSets() {
        return sets;
    }
    
    public void setSets(Integer sets) {
        this.sets = sets;
    }
    
    public Integer getReps() {
        return reps;
    }
    
    public void setReps(Integer reps) {
        this.reps = reps;
    }
    
    public Double getWeight() {
        return weight;
    }
    
    public void setWeight(Double weight) {
        this.weight = weight;
    }
    
    public LocalDate getTrainingDate() {
        return trainingDate;
    }
    
    public void setTrainingDate(LocalDate trainingDate) {
        this.trainingDate = trainingDate;
    }
    
    public Integer getDuration() {
        return duration;
    }
    
    public void setDuration(Integer duration) {
        this.duration = duration;
    }
    
    public String getNotes() {
        return notes;
    }
    
    public void setNotes(String notes) {
        this.notes = notes;
    }
}