package com.wzl.fitness.service.impl;

import com.wzl.fitness.dto.response.NutritionStatsResponse;
import com.wzl.fitness.dto.request.NutritionRecordRequest;
import com.wzl.fitness.dto.response.NutritionRecordDTO;
import com.wzl.fitness.entity.NutritionRecord;
import com.wzl.fitness.entity.User;
import com.wzl.fitness.exception.ResourceNotFoundException;
import com.wzl.fitness.repository.NutritionRecordRepository;
import com.wzl.fitness.service.NutritionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 营养记录服务实现类
 */
@Service
@RequiredArgsConstructor
public class NutritionServiceImpl implements NutritionService {
    
    private static final Logger log = LoggerFactory.getLogger(NutritionServiceImpl.class);
    
    private final NutritionRecordRepository nutritionRecordRepository;
    
    @Override
    @Transactional
    public NutritionRecord addNutritionRecord(User user, NutritionRecordRequest request) {
        log.info("为用户 {} 添加营养记录: {}", user.getId(), request.getFoodName());
        
        if (!validateNutritionRecord(request)) {
            throw new IllegalArgumentException("营养记录数据验证失败");
        }
        
        NutritionRecord record = new NutritionRecord();
        record.setUser(user);
        record.setRecordDate(request.getRecordDate());
        record.setMealType(request.getMealType());
        record.setFoodName(request.getFoodName());
        record.setCalories(request.getCalories());
        record.setProtein(request.getProtein());
        record.setCarbs(request.getCarbs());
        record.setFat(request.getFat());
        record.setFiber(request.getFiber());
        record.setSugar(request.getSugar());
        record.setSodium(request.getSodium());
        record.setAmount(request.getAmount());
        record.setNotes(request.getNotes());
        
        return nutritionRecordRepository.save(record);
    }
    
    @Override
    @Transactional
    public NutritionRecord updateNutritionRecord(Long id, NutritionRecordRequest request) {
        log.info("更新营养记录: {}", id);
        
        if (!validateNutritionRecord(request)) {
            throw new IllegalArgumentException("营养记录数据验证失败");
        }
        
        NutritionRecord record = nutritionRecordRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("营养记录不存在"));
        
        record.setRecordDate(request.getRecordDate());
        record.setMealType(request.getMealType());
        record.setFoodName(request.getFoodName());
        record.setCalories(request.getCalories());
        record.setProtein(request.getProtein());
        record.setCarbs(request.getCarbs());
        record.setFat(request.getFat());
        record.setFiber(request.getFiber());
        record.setSugar(request.getSugar());
        record.setSodium(request.getSodium());
        record.setAmount(request.getAmount());
        record.setNotes(request.getNotes());
        
        return nutritionRecordRepository.save(record);
    }
    
    @Override
    @Transactional
    public void deleteNutritionRecord(Long id, User user) {
        log.info("删除营养记录: {}", id);
        
        NutritionRecord record = nutritionRecordRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("营养记录不存在"));
        
        // 验证记录属于当前用户
        if (!record.getUser().getId().equals(user.getId())) {
            throw new IllegalArgumentException("无权删除此营养记录");
        }
        
        nutritionRecordRepository.delete(record);
    }
    
    @Override
    public Optional<NutritionRecord> getNutritionRecord(Long id, User user) {
        return nutritionRecordRepository.findById(id)
                .filter(record -> record.getUser().getId().equals(user.getId()));
    }
    
    @Override
    public List<NutritionRecordDTO> getNutritionRecordsByDate(User user, LocalDate date) {
        List<NutritionRecord> records = nutritionRecordRepository.findByUserAndRecordDateOrderByCreatedAt(user, date);
        return records.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    @Override
    public List<NutritionRecordDTO> getNutritionRecordsByDateRange(User user, LocalDate startDate, LocalDate endDate) {
        List<NutritionRecord> records = nutritionRecordRepository.findByUserAndRecordDateBetweenOrderByRecordDateAsc(user, startDate, endDate);
        return records.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    @Override
    public Page<NutritionRecordDTO> getNutritionRecordsPaginated(User user, Pageable pageable) {
        Page<NutritionRecord> recordPage = nutritionRecordRepository.findByUserOrderByRecordDateDesc(user, pageable);
        return recordPage.map(this::convertToDTO);
    }
    
    @Override
    public NutritionStatsResponse getNutritionStatsByDate(User user, LocalDate date) {
        log.info("获取用户 {} 在 {} 的营养统计", user.getId(), date);
        
        // 获取当日营养记录
        List<NutritionRecordDTO> dailyRecords = getNutritionRecordsByDate(user, date);
        
        // 创建统计对象
        NutritionStatsResponse stats = new NutritionStatsResponse();
        stats.setDate(date);
        stats.setDailyRecords(dailyRecords);
        
        // 计算总量
        double totalCalories = dailyRecords.stream().mapToDouble(NutritionRecordDTO::getCalories).sum();
        double totalProtein = dailyRecords.stream().mapToDouble(r -> r.getProtein() != null ? r.getProtein() : 0).sum();
        double totalCarbs = dailyRecords.stream().mapToDouble(r -> r.getCarbs() != null ? r.getCarbs() : 0).sum();
        double totalFat = dailyRecords.stream().mapToDouble(r -> r.getFat() != null ? r.getFat() : 0).sum();
        double totalFiber = dailyRecords.stream().mapToDouble(r -> r.getFiber() != null ? r.getFiber() : 0).sum();
        double totalSugar = dailyRecords.stream().mapToDouble(r -> r.getSugar() != null ? r.getSugar() : 0).sum();
        double totalSodium = dailyRecords.stream().mapToDouble(r -> r.getSodium() != null ? r.getSodium() : 0).sum();
        
        stats.setTotalCalories(totalCalories);
        stats.setTotalProtein(totalProtein);
        stats.setTotalCarbs(totalCarbs);
        stats.setTotalFat(totalFat);
        stats.setTotalFiber(totalFiber);
        stats.setTotalSugar(totalSugar);
        stats.setTotalSodium(totalSodium);
        
        // 计算营养素占比
        calculateNutritionPercentages(stats);
        
        stats.setNutritionAdvice(generateNutritionAdvice(user, stats));
        
        return stats;
    }
    
    @Override
    public List<NutritionStatsResponse> getNutritionStatsByDateRange(User user, LocalDate startDate, LocalDate endDate) {
        List<NutritionStatsResponse> statsList = new ArrayList<>();
        
        LocalDate currentDate = startDate;
        while (!currentDate.isAfter(endDate)) {
            NutritionStatsResponse stats = getNutritionStatsByDate(user, currentDate);
            statsList.add(stats);
            currentDate = currentDate.plusDays(1);
        }
        
        return statsList;
    }
    
    @Override
    public List<String> getNutritionAdvice(User user, LocalDate date) {
        NutritionStatsResponse stats = getNutritionStatsByDate(user, date);
        return stats.getNutritionAdvice();
    }

    private List<String> generateNutritionAdvice(User user, NutritionStatsResponse stats) {
        List<String> advice = new ArrayList<>();

        if (stats.getTotalCalories() != null) {
            if (stats.getTotalCalories() < 1500) {
                advice.add("今日卡路里摄入偏低，建议适当增加健康食物的摄入");
            } else if (stats.getTotalCalories() > 3000) {
                advice.add("今日卡路里摄入较高，请注意控制分量");
            }
        }

        if (stats.getProteinPercentage() != null && stats.getProteinPercentage() < 15) {
            advice.add("蛋白质摄入比例偏低，建议多吃瘦肉、蛋类、豆制品或补充蛋白粉");
        }

        if (stats.getFatPercentage() != null && stats.getFatPercentage() > 35) {
            advice.add("脂肪摄入比例偏高，建议减少油炸和高油食品，选择健康的单不饱和脂肪");
        }

        if (advice.isEmpty()) {
            advice.add("您的营养摄入非常均衡，请继续保持！");
        }

        return advice;
    }
    
    @Override
    public List<String> getMostFrequentFoods(User user, int limit) {
        Pageable pageable = PageRequest.of(0, limit);
        List<Object[]> results = nutritionRecordRepository.getMostFrequentFoods(user, pageable);
        
        return results.stream()
                .map(result -> (String) result[0])
                .collect(Collectors.toList());
    }
    
    @Override
    public void calculateNutritionPercentages(NutritionStatsResponse stats) {
        double totalCalories = stats.getTotalCalories() != null ? stats.getTotalCalories() : 0;
        if (totalCalories <= 0) {
            stats.setProteinPercentage(0.0);
            stats.setCarbsPercentage(0.0);
            stats.setFatPercentage(0.0);
            return;
        }

        // 每克蛋白质4大卡，碳水4大卡，脂肪9大卡
        double proteinCalories = (stats.getTotalProtein() != null ? stats.getTotalProtein() : 0) * 4;
        double carbsCalories = (stats.getTotalCarbs() != null ? stats.getTotalCarbs() : 0) * 4;
        double fatCalories = (stats.getTotalFat() != null ? stats.getTotalFat() : 0) * 9;

        stats.setProteinPercentage(Math.round(proteinCalories / totalCalories * 1000.0) / 10.0);
        stats.setCarbsPercentage(Math.round(carbsCalories / totalCalories * 1000.0) / 10.0);
        stats.setFatPercentage(Math.round(fatCalories / totalCalories * 1000.0) / 10.0);
    }
    
    @Override
    public boolean validateNutritionRecord(NutritionRecordRequest request) {
        if (request == null) {
            return false;
        }
        
        if (request.getRecordDate() == null || request.getMealType() == null || 
            request.getFoodName() == null || request.getCalories() == null || 
            request.getAmount() == null) {
            return false;
        }
        
        if (request.getCalories() < 0 || request.getAmount() <= 0) {
            return false;
        }
        
        if (request.getProtein() != null && request.getProtein() < 0) {
            return false;
        }
        
        if (request.getCarbs() != null && request.getCarbs() < 0) {
            return false;
        }
        
        if (request.getFat() != null && request.getFat() < 0) {
            return false;
        }
        
        return true;
    }
    
    @Override
    public byte[] exportNutritionData(User user, LocalDate startDate, LocalDate endDate) {
        // 实现营养数据导出功能，返回CSV格式的字节数组
        List<NutritionRecordDTO> records = getNutritionRecordsByDateRange(user, startDate, endDate);
        
        StringBuilder csvBuilder = new StringBuilder();
        csvBuilder.append("日期,餐次,食物名称,份量(g),卡路里,蛋白质(g),碳水化合物(g),脂肪(g),纤维(g),糖分(g),钠(mg),备注\n");
        
        for (NutritionRecordDTO record : records) {
            csvBuilder.append(record.getRecordDate()).append(",")
                    .append(record.getMealType()).append(",")
                    .append(record.getFoodName()).append(",")
                    .append(record.getAmount()).append(",")
                    .append(record.getCalories()).append(",")
                    .append(record.getProtein()).append(",")
                    .append(record.getCarbs()).append(",")
                    .append(record.getFat()).append(",")
                    .append(record.getFiber()).append(",")
                    .append(record.getSugar()).append(",")
                    .append(record.getSodium()).append(",")
                    .append(record.getNotes() != null ? record.getNotes() : "").append("\n");
        }
        
        return csvBuilder.toString().getBytes();
    }
    
    /**
     * 将实体转换为DTO
     */
    private NutritionRecordDTO convertToDTO(NutritionRecord record) {
        NutritionRecordDTO dto = new NutritionRecordDTO();
        dto.setId(record.getId());
        dto.setUserId(record.getUser().getId());
        dto.setRecordDate(record.getRecordDate());
        dto.setMealType(record.getMealType());
        dto.setFoodName(record.getFoodName());
        dto.setCalories(record.getCalories());
        dto.setProtein(record.getProtein());
        dto.setCarbs(record.getCarbs());
        dto.setFat(record.getFat());
        dto.setFiber(record.getFiber());
        dto.setSugar(record.getSugar());
        dto.setSodium(record.getSodium());
        dto.setAmount(record.getAmount());
        dto.setNotes(record.getNotes());
        dto.setCreatedAt(record.getCreatedAt());
        return dto;
    }
}
