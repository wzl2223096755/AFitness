package com.wzl.fitness.dto.response;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 营养记录响应DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class NutritionRecordDTO {
    
    private Long id;
    private Long userId;
    private LocalDate recordDate;
    private String mealType;
    private String foodName;
    private Double calories;
    private Double protein;
    private Double carbs;
    private Double fat;
    private Double fiber;
    private Double sugar;
    private Double sodium;
    private Double amount;
    private String notes;
    private LocalDateTime createdAt;
    
    // Explicit getter and setter methods to fix compilation errors
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public Long getUserId() {
        return userId;
    }
    
    public void setUserId(Long userId) {
        this.userId = userId;
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
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}