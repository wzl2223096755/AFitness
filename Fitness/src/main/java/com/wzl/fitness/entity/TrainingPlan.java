package com.wzl.fitness.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 健身计划实体类
 */
@Entity
@Table(name = "training_plans")
public class TrainingPlan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @JsonIgnore
    private User user;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "start_date")
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    @Column(length = 20)
    private String status = "ACTIVE";

    @Column(name = "is_weekly")
    private Boolean isWeekly = false;

    @Column(length = 20)
    private String goal;

    @Column(length = 20)
    private String level;

    @Column(name = "duration_weeks")
    private Integer durationWeeks;

    @Column(name = "days_per_week")
    private Integer daysPerWeek;

    @Column(name = "duration_per_session")
    private Integer durationPerSession;

    @OneToMany(mappedBy = "plan", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JsonIgnore
    private List<TrainingPlanDay> days = new ArrayList<>();

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    public TrainingPlan() {}
    
    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }
    public LocalDate getEndDate() { return endDate; }
    public void setEndDate(LocalDate endDate) { this.endDate = endDate; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public Boolean getIsWeekly() { return isWeekly; }
    public void setIsWeekly(Boolean isWeekly) { this.isWeekly = isWeekly; }
    public String getGoal() { return goal; }
    public void setGoal(String goal) { this.goal = goal; }
    public String getLevel() { return level; }
    public void setLevel(String level) { this.level = level; }
    public Integer getDurationWeeks() { return durationWeeks; }
    public void setDurationWeeks(Integer durationWeeks) { this.durationWeeks = durationWeeks; }
    public Integer getDaysPerWeek() { return daysPerWeek; }
    public void setDaysPerWeek(Integer daysPerWeek) { this.daysPerWeek = daysPerWeek; }
    public Integer getDurationPerSession() { return durationPerSession; }
    public void setDurationPerSession(Integer durationPerSession) { this.durationPerSession = durationPerSession; }
    public List<TrainingPlanDay> getDays() { return days; }
    public void setDays(List<TrainingPlanDay> days) { this.days = days; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    
    // Builder
    public static TrainingPlanBuilder builder() { return new TrainingPlanBuilder(); }
    
    public static class TrainingPlanBuilder {
        private Long id;
        private User user;
        private String name;
        private String description;
        private LocalDate startDate;
        private LocalDate endDate;
        private String status = "ACTIVE";
        private Boolean isWeekly = false;
        private String goal;
        private String level;
        private Integer durationWeeks;
        private Integer daysPerWeek;
        private Integer durationPerSession;
        private List<TrainingPlanDay> days = new ArrayList<>();
        
        public TrainingPlanBuilder id(Long v) { this.id = v; return this; }
        public TrainingPlanBuilder user(User v) { this.user = v; return this; }
        public TrainingPlanBuilder name(String v) { this.name = v; return this; }
        public TrainingPlanBuilder description(String v) { this.description = v; return this; }
        public TrainingPlanBuilder startDate(LocalDate v) { this.startDate = v; return this; }
        public TrainingPlanBuilder endDate(LocalDate v) { this.endDate = v; return this; }
        public TrainingPlanBuilder status(String v) { this.status = v; return this; }
        public TrainingPlanBuilder isWeekly(Boolean v) { this.isWeekly = v; return this; }
        public TrainingPlanBuilder goal(String v) { this.goal = v; return this; }
        public TrainingPlanBuilder level(String v) { this.level = v; return this; }
        public TrainingPlanBuilder durationWeeks(Integer v) { this.durationWeeks = v; return this; }
        public TrainingPlanBuilder daysPerWeek(Integer v) { this.daysPerWeek = v; return this; }
        public TrainingPlanBuilder durationPerSession(Integer v) { this.durationPerSession = v; return this; }
        public TrainingPlanBuilder days(List<TrainingPlanDay> v) { this.days = v; return this; }
        
        public TrainingPlan build() {
            TrainingPlan p = new TrainingPlan();
            p.id = this.id; p.user = this.user; p.name = this.name; p.description = this.description;
            p.startDate = this.startDate; p.endDate = this.endDate; p.status = this.status;
            p.isWeekly = this.isWeekly; p.goal = this.goal; p.level = this.level;
            p.durationWeeks = this.durationWeeks; p.daysPerWeek = this.daysPerWeek;
            p.durationPerSession = this.durationPerSession; p.days = this.days;
            return p;
        }
    }
}
