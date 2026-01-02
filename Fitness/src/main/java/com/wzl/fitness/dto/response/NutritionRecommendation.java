package com.wzl.fitness.dto.response;

/**
 * 营养推荐DTO
 */
public class NutritionRecommendation {
    private Double recommendedCalories;
    private Double recommendedProtein;
    private Double recommendedCarbs;
    private Double recommendedFat;
    private String trainingGoal;
    private Double bodyWeight;
    private String activityLevel;
    private Double proteinPercentage;
    private Double carbsPercentage;
    private Double fatPercentage;
    
    public NutritionRecommendation() {}
    
    public NutritionRecommendation(Double recommendedCalories, Double recommendedProtein, Double recommendedCarbs,
                                   Double recommendedFat, String trainingGoal, Double bodyWeight, String activityLevel,
                                   Double proteinPercentage, Double carbsPercentage, Double fatPercentage) {
        this.recommendedCalories = recommendedCalories;
        this.recommendedProtein = recommendedProtein;
        this.recommendedCarbs = recommendedCarbs;
        this.recommendedFat = recommendedFat;
        this.trainingGoal = trainingGoal;
        this.bodyWeight = bodyWeight;
        this.activityLevel = activityLevel;
        this.proteinPercentage = proteinPercentage;
        this.carbsPercentage = carbsPercentage;
        this.fatPercentage = fatPercentage;
    }
    
    // Getters and Setters
    public Double getRecommendedCalories() { return recommendedCalories; }
    public void setRecommendedCalories(Double recommendedCalories) { this.recommendedCalories = recommendedCalories; }
    public Double getRecommendedProtein() { return recommendedProtein; }
    public void setRecommendedProtein(Double recommendedProtein) { this.recommendedProtein = recommendedProtein; }
    public Double getRecommendedCarbs() { return recommendedCarbs; }
    public void setRecommendedCarbs(Double recommendedCarbs) { this.recommendedCarbs = recommendedCarbs; }
    public Double getRecommendedFat() { return recommendedFat; }
    public void setRecommendedFat(Double recommendedFat) { this.recommendedFat = recommendedFat; }
    public String getTrainingGoal() { return trainingGoal; }
    public void setTrainingGoal(String trainingGoal) { this.trainingGoal = trainingGoal; }
    public Double getBodyWeight() { return bodyWeight; }
    public void setBodyWeight(Double bodyWeight) { this.bodyWeight = bodyWeight; }
    public String getActivityLevel() { return activityLevel; }
    public void setActivityLevel(String activityLevel) { this.activityLevel = activityLevel; }
    public Double getProteinPercentage() { return proteinPercentage; }
    public void setProteinPercentage(Double proteinPercentage) { this.proteinPercentage = proteinPercentage; }
    public Double getCarbsPercentage() { return carbsPercentage; }
    public void setCarbsPercentage(Double carbsPercentage) { this.carbsPercentage = carbsPercentage; }
    public Double getFatPercentage() { return fatPercentage; }
    public void setFatPercentage(Double fatPercentage) { this.fatPercentage = fatPercentage; }
    
    // Builder
    public static NutritionRecommendationBuilder builder() { return new NutritionRecommendationBuilder(); }
    
    public static class NutritionRecommendationBuilder {
        private Double recommendedCalories;
        private Double recommendedProtein;
        private Double recommendedCarbs;
        private Double recommendedFat;
        private String trainingGoal;
        private Double bodyWeight;
        private String activityLevel;
        private Double proteinPercentage;
        private Double carbsPercentage;
        private Double fatPercentage;
        
        public NutritionRecommendationBuilder recommendedCalories(Double v) { this.recommendedCalories = v; return this; }
        public NutritionRecommendationBuilder recommendedProtein(Double v) { this.recommendedProtein = v; return this; }
        public NutritionRecommendationBuilder recommendedCarbs(Double v) { this.recommendedCarbs = v; return this; }
        public NutritionRecommendationBuilder recommendedFat(Double v) { this.recommendedFat = v; return this; }
        public NutritionRecommendationBuilder trainingGoal(String v) { this.trainingGoal = v; return this; }
        public NutritionRecommendationBuilder bodyWeight(Double v) { this.bodyWeight = v; return this; }
        public NutritionRecommendationBuilder activityLevel(String v) { this.activityLevel = v; return this; }
        public NutritionRecommendationBuilder proteinPercentage(Double v) { this.proteinPercentage = v; return this; }
        public NutritionRecommendationBuilder carbsPercentage(Double v) { this.carbsPercentage = v; return this; }
        public NutritionRecommendationBuilder fatPercentage(Double v) { this.fatPercentage = v; return this; }
        
        public NutritionRecommendation build() {
            return new NutritionRecommendation(recommendedCalories, recommendedProtein, recommendedCarbs, recommendedFat,
                    trainingGoal, bodyWeight, activityLevel, proteinPercentage, carbsPercentage, fatPercentage);
        }
    }
}
