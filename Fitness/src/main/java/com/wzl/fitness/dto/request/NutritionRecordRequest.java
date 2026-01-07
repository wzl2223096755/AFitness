package com.wzl.fitness.dto.request;

import com.wzl.fitness.validation.NoXss;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Max;
import java.time.LocalDate;

/**
 * 营养记录请求DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class NutritionRecordRequest {
    
    @NotNull(message = "记录日期不能为空")
    private LocalDate recordDate;
    
    @NotBlank(message = "餐次不能为空")
    @NoXss
    private String mealType; // 早餐、午餐、晚餐、加餐
    
    @NotBlank(message = "食物名称不能为空")
    @NoXss
    private String foodName;
    
    @NotNull(message = "卡路里不能为空")
    @Min(value = 0, message = "卡路里不能为负数")
    private Double calories;
    
    @Min(value = 0, message = "蛋白质不能为负数")
    private Double protein;
    
    @Min(value = 0, message = "碳水化合物不能为负数")
    private Double carbs;
    
    @Min(value = 0, message = "脂肪不能为负数")
    private Double fat;
    
    @Min(value = 0, message = "纤维不能为负数")
    private Double fiber;
    
    @Min(value = 0, message = "糖分不能为负数")
    private Double sugar;
    
    @Min(value = 0, message = "钠不能为负数")
    private Double sodium;
    
    @NotNull(message = "份量不能为空")
    @Min(value = 1, message = "份量必须大于0")
    @Max(value = 1000, message = "份量不能超过1000克")
    private Double amount;
    
    @NoXss
    private String notes;
    
    // Explicit getter and setter methods to fix compilation errors
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