package com.wzl.fitness.controller;

import com.wzl.fitness.annotation.RequireUser;
import com.wzl.fitness.common.ApiResponse;
import com.wzl.fitness.common.BaseController;
import com.wzl.fitness.entity.FitnessData;
import com.wzl.fitness.entity.User;
import com.wzl.fitness.exception.BusinessException;
import com.wzl.fitness.repository.FitnessDataRepository;
import com.wzl.fitness.service.LoadRecoveryService;
import com.wzl.fitness.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/load-recovery")
public class LoadRecoveryController extends BaseController {

    private static final Logger logger = LoggerFactory.getLogger(LoadRecoveryController.class);

    @Autowired
    private LoadRecoveryService loadRecoveryService;

    @Autowired
    private FitnessDataRepository fitnessDataRepository;

    @Autowired
    private UserService userService;

    /**
     * 从Request中获取用户信息
     */
    private User getUser(HttpServletRequest request) {
        Long userId = getUserIdFromRequest(request);
        return userService.getUserById(userId)
                .orElseThrow(() -> new BusinessException("用户不存在"));
    }

    /**
     * 保存训练数据并计算负荷
     */
    @PostMapping("/training-data")
    @RequireUser
    public ResponseEntity<ApiResponse<FitnessData>> saveTrainingData(@RequestBody FitnessData fitnessData, HttpServletRequest request) {
        User user = getUser(request);
        
        fitnessData.setUser(user);
        fitnessData.setTimestamp(LocalDateTime.now());
        
        // 计算训练负荷
        FitnessData calculatedData = loadRecoveryService.calculateTrainingLoad(fitnessData);
        
        // 保存数据
        FitnessData savedData = fitnessDataRepository.save(calculatedData);
        
        return ResponseEntity.ok(ApiResponse.success("训练数据保存成功", savedData));
    }

    /**
     * 评估恢复状态
     */
    @PostMapping("/recovery-assessment")
    @RequireUser
    public ResponseEntity<ApiResponse<Map<String, Object>>> assessRecovery(@RequestBody Map<String, Integer> requestBody, HttpServletRequest request) {
        
        Integer sleepHours = requestBody.get("sleepHours");
        Integer stressLevel = requestBody.get("stressLevel");
        
        User user = getUser(request);
        
        Map<String, Object> result = loadRecoveryService.assessRecoveryStatus(user.getId(), sleepHours, stressLevel);
        
        return ResponseEntity.ok(ApiResponse.success("恢复状态评估成功", result));
    }

    /**
     * 获取训练建议
     */
    @GetMapping("/training-suggestions")
    @RequireUser
    public ResponseEntity<ApiResponse<Map<String, Object>>> getTrainingSuggestions(HttpServletRequest request) {
        User user = getUser(request);
        
        Map<String, Object> suggestions = loadRecoveryService.generateTrainingSuggestions(user.getId());
        
        return ResponseEntity.ok(ApiResponse.success("训练建议获取成功", suggestions));
    }

    /**
     * 获取负荷趋势
     */
    @GetMapping("/load-trend")
    @RequireUser
    public ResponseEntity<ApiResponse<Map<String, Double>>> getLoadTrend(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            HttpServletRequest request) {
        
        // 将LocalDate转换为LocalDateTime，使用一天的开始和结束时间
        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.atTime(23, 59, 59);
        
        User user = getUser(request);
        
        Map<String, Double> trend = loadRecoveryService.getLoadTrend(user.getId(), startDateTime, endDateTime);
        
        return ResponseEntity.ok(ApiResponse.success("负荷趋势获取成功", trend));
    }

    /**
     * 计算1RM并保存记录
     */
    @PostMapping("/one-rep-max/record")
    @RequireUser
    public ResponseEntity<ApiResponse<FitnessData>> saveOneRepMaxRecord(@RequestBody Map<String, Object> body, HttpServletRequest request) {
        User user = getUser(request);
        
        Double weight = Double.valueOf(body.get("weight").toString());
        Integer reps = Integer.valueOf(body.get("reps").toString());
        String model = body.getOrDefault("model", "Epley").toString();
        
        Double oneRepMax = loadRecoveryService.calculateOneRepMax(weight, reps, model);
        
        FitnessData fitnessData = new FitnessData();
        fitnessData.setUser(user);
        fitnessData.setTimestamp(LocalDateTime.now());
        fitnessData.setWeight(weight);
        fitnessData.setReps(reps);
        fitnessData.setOneRepMax(oneRepMax);
        fitnessData.setExerciseType("STRENGTH");
        fitnessData.setExerciseName("RM_CALCULATION");
        
        FitnessData savedData = fitnessDataRepository.save(fitnessData);
        
        return ResponseEntity.ok(ApiResponse.success("1RM记录保存成功", savedData));
    }

    /**
     * 获取我的健身数据
     */
    @GetMapping("/my-data")
    @RequireUser
    public ResponseEntity<ApiResponse<List<FitnessData>>> getMyFitnessData(HttpServletRequest request) {
        User user = getUser(request);
        List<FitnessData> dataList = fitnessDataRepository.findByUserOrderByTimestampDesc(user);
        return ResponseEntity.ok(ApiResponse.success("获取健身数据成功", dataList));
    }

    /**
     * 删除健身数据
     */
    @DeleteMapping("/data/{id}")
    @RequireUser
    public ResponseEntity<ApiResponse<String>> deleteFitnessData(@PathVariable Long id, HttpServletRequest request) {
        User user = getUser(request);
        FitnessData data = fitnessDataRepository.findById(id)
                .orElseThrow(() -> new BusinessException("数据不存在"));
        
        if (!data.getUser().getId().equals(user.getId())) {
            throw new BusinessException("无权操作此数据");
        }
        
        fitnessDataRepository.delete(data);
        return ResponseEntity.ok(ApiResponse.success("删除成功", "删除成功"));
    }

    /**
     * 计算1RM（支持多种模型）
     */
    @GetMapping("/one-rep-max")
    @RequireUser
    public ResponseEntity<ApiResponse<Double>> calculateOneRepMax(
            @RequestParam(required = false) Double weight,
            @RequestParam(required = false) Integer reps,
            @RequestParam(required = false, defaultValue = "Epley") String model,
            HttpServletRequest request) {
        
        // 验证用户身份
        getUserIdFromRequest(request);
        
        if (weight == null || reps == null) {
            return ResponseEntity.ok(ApiResponse.success("参数不足，返回默认值", 0.0));
        }
        
        Double oneRepMax = loadRecoveryService.calculateOneRepMax(weight, reps, model);
        
        return ResponseEntity.ok(ApiResponse.success("1RM计算成功", oneRepMax));
    }
    
    /**
     * 获取所有支持的1RM计算公式模型
     */
    @GetMapping("/one-rep-max/models")
    @RequireUser
    public ResponseEntity<ApiResponse<List<String>>> getSupportedOneRepMaxModels(HttpServletRequest request) {
        
        // 验证用户身份
        getUserIdFromRequest(request);
        
        List<String> models = loadRecoveryService.getSupportedOneRepMaxModels();
        
        return ResponseEntity.ok(ApiResponse.success("获取支持的模型成功", models));
    }
}
