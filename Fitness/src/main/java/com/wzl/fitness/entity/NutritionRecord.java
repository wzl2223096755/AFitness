package com.wzl.fitness.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.hibernate.annotations.SQLRestriction;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 营养记录实体类
 * 用于记录用户的每日饮食和营养摄入情况
 * 支持软删除功能
 */
@Entity
@Table(name = "nutrition_records")
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@SQLRestriction("deleted = false")
public class NutritionRecord extends BaseEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    @Column(name = "record_date", nullable = false)
    private LocalDate recordDate;
    
    @Column(name = "meal_type", nullable = false, length = 20)
    private String mealType; // 早餐、午餐、晚餐、加餐
    
    @Column(name = "food_name", nullable = false, length = 100)
    private String foodName;
    
    @Column(name = "calories", nullable = false)
    private Double calories;
    
    @Column(name = "protein")
    private Double protein; // 蛋白质(g)
    
    @Column(name = "carbs")
    private Double carbs; // 碳水化合物(g)
    
    @Column(name = "fat")
    private Double fat; // 脂肪(g)
    
    @Column(name = "fiber")
    private Double fiber; // 纤维(g)
    
    @Column(name = "sugar")
    private Double sugar; // 糖分(g)
    
    @Column(name = "sodium")
    private Double sodium; // 钠(mg)
    
    @Column(name = "amount")
    private Double amount; // 份量(g)
    
    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;
    
    // Explicit getter and setter methods to fix compilation errors
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
    
    public LocalDate getRecordDate() {
        return recordDate;
    }
    
    public void setRecordDate(LocalDate recordDate) {
        this.recordDate = recordDate;
    }
    
    public String getMealType() {
        return mealType;
    }
    
    public void setMealType(String mealType) {
        this.mealType = mealType;
    }
    
    public String getFoodName() {
        return foodName;
    }
    
    public void setFoodName(String foodName) {
        this.foodName = foodName;
    }
    
    public Double getCalories() {
        return calories;
    }
    
    public void setCalories(Double calories) {
        this.calories = calories;
    }
    
    public Double getProtein() {
        return protein;
    }
    
    public void setProtein(Double protein) {
        this.protein = protein;
    }
    
    public Double getCarbs() {
        return carbs;
    }
    
    public void setCarbs(Double carbs) {
        this.carbs = carbs;
    }
    
    public Double getFat() {
        return fat;
    }
    
    public void setFat(Double fat) {
        this.fat = fat;
    }
    
    public Double getFiber() {
        return fiber;
    }
    
    public void setFiber(Double fiber) {
        this.fiber = fiber;
    }
    
    public Double getSugar() {
        return sugar;
    }
    
    public void setSugar(Double sugar) {
        this.sugar = sugar;
    }
    
    public Double getSodium() {
        return sodium;
    }
    
    public void setSodium(Double sodium) {
        this.sodium = sodium;
    }
    
    public Double getAmount() {
        return amount;
    }
    
    public void setAmount(Double amount) {
        this.amount = amount;
    }
    
    public String getNotes() {
        return notes;
    }
    
    public void setNotes(String notes) {
        this.notes = notes;
    }
}