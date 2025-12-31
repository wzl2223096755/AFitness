package com.wzl.fitness.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDate;

@Entity
@Data
@Table(name = "training_advices")
public class TrainingAdvice {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    private LocalDate adviceDate;
    private String adviceType;
    private String content;
    private Double confidenceScore;
    
    // 显式添加getter方法
    public Long getId() {
        return id;
    }
    
    public User getUser() {
        return user;
    }
    
    public LocalDate getAdviceDate() {
        return adviceDate;
    }
    
    public String getAdviceType() {
        return adviceType;
    }
    
    public String getContent() {
        return content;
    }
    
    public Double getConfidenceScore() {
        return confidenceScore;
    }
    
    // 显式添加setter方法
    public void setUser(User user) {
        this.user = user;
    }
    
    public void setAdviceDate(LocalDate adviceDate) {
        this.adviceDate = adviceDate;
    }
    
    public void setAdviceType(String adviceType) {
        this.adviceType = adviceType;
    }
    
    public void setContent(String content) {
        this.content = content;
    }
    
    public void setConfidenceScore(Double confidenceScore) {
        this.confidenceScore = confidenceScore;
    }
    
    public void setConfidenceScore(double confidenceScore) {
        this.confidenceScore = confidenceScore;
    }
}