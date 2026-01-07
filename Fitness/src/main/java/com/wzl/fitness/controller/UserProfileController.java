package com.wzl.fitness.controller;

import com.wzl.fitness.annotation.Auditable;
import com.wzl.fitness.annotation.RequireUser;
import com.wzl.fitness.common.ApiResponse;
import com.wzl.fitness.common.BaseController;
import com.wzl.fitness.dto.request.BodyRecordRequest;
import com.wzl.fitness.dto.request.ChangePasswordRequest;
import com.wzl.fitness.dto.request.UserProfileRequest;
import com.wzl.fitness.entity.AuditLog.AuditAction;
import com.wzl.fitness.entity.BodyRecord;
import com.wzl.fitness.entity.TrainingRecord;
import com.wzl.fitness.entity.User;
import com.wzl.fitness.entity.UserAchievement;
import com.wzl.fitness.entity.UserSetting;
import com.wzl.fitness.exception.BusinessException;
import com.wzl.fitness.repository.BodyRecordRepository;
import com.wzl.fitness.repository.UserAchievementRepository;
import com.wzl.fitness.repository.UserSettingRepository;
import com.wzl.fitness.service.AuditLogService;
import com.wzl.fitness.service.DashboardService;
import com.wzl.fitness.service.FileService;
import com.wzl.fitness.service.TrainingRecordService;
import com.wzl.fitness.service.UserService;
import com.wzl.fitness.util.JwtUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/user")
@RequiredArgsConstructor
@Tag(name = "用户管理", description = "用户信息管理，包括用户资料、用户设置、身体数据等")
public class UserProfileController extends BaseController {

    private final UserService userService;
    private final JwtUtil jwtUtil;
    private final UserSettingRepository userSettingRepository;
    private final BodyRecordRepository bodyRecordRepository;
    private final UserAchievementRepository userAchievementRepository;
    private final TrainingRecordService trainingRecordService;
    private final DashboardService dashboardService;
    private final FileService fileService;
    private final AuditLogService auditLogService;

    /**
     * 从Request中获取用户信息
     */
    private User getUser(HttpServletRequest request) {
        Long userId = getUserIdFromRequest(request);
        return userService.getUserById(userId)
                .orElseThrow(() -> new BusinessException("用户不存在"));
    }

    /**
     * 获取用户资料
     */
    @GetMapping("/profile")
    @RequireUser
    @Operation(
            summary = "获取用户资料", 
            description = "获取当前登录用户的详细资料信息"
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "获取成功",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "用户资料示例",
                                    value = """
                                            {
                                              "code": 200,
                                              "message": "操作成功",
                                              "data": {
                                                "id": 1,
                                                "username": "fitness_user",
                                                "email": "user@example.com",
                                                "age": 25,
                                                "gender": "male",
                                                "height": 175.0,
                                                "weight": 70.0,
                                                "experienceLevel": "intermediate"
                                              },
                                              "timestamp": "2024-01-01 12:00:00",
                                              "success": true
                                            }
                                            """
                            )
                    )
            )
    })
    public ResponseEntity<ApiResponse<User>> getUserProfile(HttpServletRequest request) {
        try {
            User user = getUser(request);
            return ResponseEntity.ok(ApiResponse.success(user));
        } catch (BusinessException e) {
            return ResponseEntity.badRequest()
                .body(ApiResponse.<User>error(400, e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(ApiResponse.<User>error(500, "获取用户资料失败: " + e.getMessage()));
        }
    }

    /**
     * 更新用户资料
     */
    @PutMapping("/profile")
    @RequireUser
    @Operation(
            summary = "更新用户资料", 
            description = "更新当前登录用户的资料信息"
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "更新成功"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "参数验证失败"
            )
    })
    public ResponseEntity<ApiResponse<User>> updateUserProfile(
            @Valid @RequestBody UserProfileRequest profileRequest,
            BindingResult bindingResult,
            HttpServletRequest request) {
        try {
            if (bindingResult.hasErrors()) {
                String errorMessage = bindingResult.getFieldErrors().get(0).getDefaultMessage();
                return ResponseEntity.badRequest()
                    .body(ApiResponse.<User>error(400, errorMessage));
            }
            
            Long userId = getUserIdFromRequest(request);
            
            // 构建用户更新对象
            User userDetails = User.builder()
                .username(profileRequest.getUsername())
                .email(profileRequest.getEmail())
                .age(profileRequest.getAge())
                .weight(profileRequest.getWeight())
                .gender(profileRequest.getGender())
                .height(profileRequest.getHeight())
                .experienceLevel(profileRequest.getExperienceLevel())
                .build();
            
            User updatedUser = userService.updateUser(userId, userDetails)
                .orElseThrow(() -> new BusinessException("用户更新失败"));
            
            return ResponseEntity.ok(ApiResponse.success(updatedUser));
        } catch (BusinessException e) {
            return ResponseEntity.badRequest()
                .body(ApiResponse.<User>error(400, e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(ApiResponse.<User>error(500, "更新用户资料失败: " + e.getMessage()));
        }
    }

    /**
     * 修改密码
     */
    @PostMapping("/change-password")
    @RequireUser
    @Operation(
            summary = "修改密码", 
            description = "修改当前登录用户的密码，需要提供旧密码验证"
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "密码修改成功"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "旧密码不正确或新密码不符合要求"
            )
    })
    public ResponseEntity<ApiResponse<String>> changePassword(
            @Valid @RequestBody ChangePasswordRequest passwordRequest,
            BindingResult bindingResult,
            HttpServletRequest request) {
        User user = null;
        try {
            if (bindingResult.hasErrors()) {
                String errorMessage = bindingResult.getFieldErrors().get(0).getDefaultMessage();
                return ResponseEntity.badRequest()
                    .body(ApiResponse.<String>error(400, errorMessage));
            }
            
            // 验证新密码和确认密码是否一致
            if (!passwordRequest.getNewPassword().equals(passwordRequest.getConfirmPassword())) {
                return ResponseEntity.badRequest()
                    .body(ApiResponse.<String>error(400, "新密码和确认密码不一致"));
            }
            
            user = getUser(request);
            
            // 验证旧密码
            if (!userService.validatePassword(passwordRequest.getOldPassword(), user.getPassword())) {
                // 记录密码修改失败审计日志
                auditLogService.logPasswordChange(user.getId(), user.getUsername(), false, "旧密码不正确");
                return ResponseEntity.badRequest()
                    .body(ApiResponse.<String>error(400, "旧密码不正确"));
            }
            
            // 更新密码
            User userDetails = User.builder()
                .password(passwordRequest.getNewPassword())
                .build();
            
            userService.updateUser(user.getId(), userDetails);
            
            // 记录密码修改成功审计日志
            auditLogService.logPasswordChange(user.getId(), user.getUsername(), true, null);
            
            return ResponseEntity.ok(ApiResponse.success("密码修改成功"));
        } catch (BusinessException e) {
            if (user != null) {
                auditLogService.logPasswordChange(user.getId(), user.getUsername(), false, e.getMessage());
            }
            return ResponseEntity.badRequest()
                .body(ApiResponse.<String>error(400, e.getMessage()));
        } catch (Exception e) {
            if (user != null) {
                auditLogService.logPasswordChange(user.getId(), user.getUsername(), false, e.getMessage());
            }
            return ResponseEntity.badRequest()
                .body(ApiResponse.<String>error(500, "修改密码失败: " + e.getMessage()));
        }
    }

    /**
     * 获取用户设置
     */
    @GetMapping("/settings")
    @RequireUser
    @Operation(summary = "获取用户设置", description = "获取当前用户的个性化设置")
    public ResponseEntity<ApiResponse<UserSetting>> getUserSettings(HttpServletRequest request) {
        User user = getUser(request);
        UserSetting settings = userSettingRepository.findByUser(user)
                .orElseGet(() -> {
                    UserSetting newSettings = UserSetting.builder().user(user).build();
                    return userSettingRepository.save(newSettings);
                });
        return ResponseEntity.ok(ApiResponse.success(settings));
    }

    /**
     * 更新用户设置
     */
    @PutMapping("/settings")
    @RequireUser
    @Operation(summary = "更新用户设置", description = "更新当前用户的个性化设置")
    public ResponseEntity<ApiResponse<UserSetting>> updateUserSettings(
            @RequestBody UserSetting settingsRequest,
            HttpServletRequest request) {
        User user = getUser(request);
        UserSetting settings = userSettingRepository.findByUser(user)
                .orElseGet(() -> UserSetting.builder().user(user).build());
        
        settings.setTheme(settingsRequest.getTheme());
        settings.setLanguage(settingsRequest.getLanguage());
        settings.setNotifications(settingsRequest.getNotifications());
        settings.setAutoSave(settingsRequest.getAutoSave());
        
        UserSetting savedSettings = userSettingRepository.save(settings);
        return ResponseEntity.ok(ApiResponse.success(savedSettings));
    }

    /**
     * 获取用户统计数据
     */
    @GetMapping("/stats")
    @RequireUser
    @Operation(summary = "获取用户统计数据", description = "获取当前用户的训练统计数据")
    public ResponseEntity<ApiResponse<Object>> getUserStats(HttpServletRequest request) {
        Long userId = getUserIdFromRequest(request);
        return ResponseEntity.ok(ApiResponse.success(dashboardService.getUserStatsOverview(userId)));
    }

    /**
     * 获取用户训练历史
     */
    @GetMapping("/training-history")
    @RequireUser
    @Operation(summary = "获取用户训练历史", description = "获取当前用户的训练历史记录，支持分页")
    public ResponseEntity<ApiResponse<List<TrainingRecord>>> getTrainingHistory(
            @Parameter(description = "页码（从0开始）") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "每页大小") @RequestParam(defaultValue = "10") int size,
            HttpServletRequest request) {
        Long userId = getUserIdFromRequest(request);
        Page<TrainingRecord> history = trainingRecordService.findByUserId(userId, page, size);
        return ResponseEntity.ok(ApiResponse.success(history.getContent()));
    }

    /**
     * 获取用户身体数据记录
     */
    @GetMapping("/body-records")
    @RequireUser
    @Operation(summary = "获取身体数据记录", description = "获取当前用户的身体数据记录列表")
    public ResponseEntity<ApiResponse<List<BodyRecord>>> getBodyRecords(HttpServletRequest request) {
        Long userId = getUserIdFromRequest(request);
        User user = userService.getUserById(userId).orElseThrow(() -> new BusinessException("用户不存在"));
        List<BodyRecord> records = bodyRecordRepository.findByUserOrderByRecordTimeDesc(user);
        return ResponseEntity.ok(ApiResponse.success(records));
    }

    /**
     * 添加身体数据记录
     */
    @PostMapping("/body-records")
    @RequireUser
    @Operation(summary = "添加身体数据记录", description = "添加一条新的身体数据记录")
    public ResponseEntity<ApiResponse<BodyRecord>> addBodyRecord(
            @RequestBody BodyRecordRequest recordRequest,
            HttpServletRequest request) {
        Long userId = getUserIdFromRequest(request);
        User user = userService.getUserById(userId).orElseThrow(() -> new BusinessException("用户不存在"));
        
        BodyRecord record = BodyRecord.builder()
                .user(user)
                .weight(recordRequest.getWeight())
                .bodyFat(recordRequest.getBodyFat())
                .muscleMass(recordRequest.getMuscleMass())
                .waistCircumference(recordRequest.getWaistCircumference())
                .hipCircumference(recordRequest.getHipCircumference())
                .chestCircumference(recordRequest.getChestCircumference())
                .recordTime(recordRequest.getRecordTime())
                .build();
        
        BodyRecord savedRecord = bodyRecordRepository.save(record);
        return ResponseEntity.ok(ApiResponse.success(savedRecord));
    }

    /**
     * 更新身体数据记录
     */
    @PutMapping("/body-records/{id}")
    @RequireUser
    @Operation(summary = "更新身体数据记录", description = "更新指定的身体数据记录")
    public ResponseEntity<ApiResponse<BodyRecord>> updateBodyRecord(
            @Parameter(description = "记录ID") @PathVariable Long id,
            @RequestBody BodyRecordRequest recordRequest,
            HttpServletRequest request) {
        Long userId = getUserIdFromRequest(request);
        BodyRecord record = bodyRecordRepository.findById(id)
                .orElseThrow(() -> new BusinessException("记录不存在"));
        
        if (!record.getUser().getId().equals(userId)) {
            throw new BusinessException("无权修改此记录");
        }
        
        record.setWeight(recordRequest.getWeight());
        record.setBodyFat(recordRequest.getBodyFat());
        record.setMuscleMass(recordRequest.getMuscleMass());
        record.setWaistCircumference(recordRequest.getWaistCircumference());
        record.setHipCircumference(recordRequest.getHipCircumference());
        record.setChestCircumference(recordRequest.getChestCircumference());
        if (recordRequest.getRecordTime() != null) {
            record.setRecordTime(recordRequest.getRecordTime());
        }
        
        BodyRecord updatedRecord = bodyRecordRepository.save(record);
        return ResponseEntity.ok(ApiResponse.success(updatedRecord));
    }

    /**
     * 删除身体数据记录
     */
    @DeleteMapping("/body-records/{id}")
    @RequireUser
    @Operation(summary = "删除身体数据记录", description = "删除指定的身体数据记录")
    public ResponseEntity<ApiResponse<Void>> deleteBodyRecord(
            @Parameter(description = "记录ID") @PathVariable Long id,
            HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        BodyRecord record = bodyRecordRepository.findById(id)
                .orElseThrow(() -> new BusinessException("记录不存在"));
        
        if (!record.getUser().getId().equals(userId)) {
            throw new BusinessException("无权删除此记录");
        }
        
        bodyRecordRepository.delete(record);
        
        // 记录数据删除审计日志
        User user = record.getUser();
        auditLogService.logDataDelete(userId, user.getUsername(), "身体数据记录", id);
        
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    /**
     * 上传头像
     */
    @PostMapping("/avatar")
    @RequireUser
    @Operation(summary = "上传头像", description = "上传用户头像图片")
    public ResponseEntity<ApiResponse<String>> uploadAvatar(
            @RequestParam("file") MultipartFile file,
            HttpServletRequest request) {
        try {
            Long userId = (Long) request.getAttribute("userId");
            User user = userService.getUserById(userId).orElseThrow(() -> new BusinessException("用户不存在"));
            
            String avatarUrl = fileService.saveFile(file, "avatars");
            user.setAvatar(avatarUrl);
            userService.updateUser(userId, user);
            
            return ResponseEntity.ok(ApiResponse.success(avatarUrl));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(ApiResponse.<String>error(500, "头像上传失败: " + e.getMessage()));
        }
    }

    /**
     * 获取用户成就
     */
    @GetMapping("/achievements")
    @RequireUser
    @Operation(summary = "获取用户成就", description = "获取当前用户已解锁的成就列表")
    public ResponseEntity<ApiResponse<List<UserAchievement>>> getAchievements(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        User user = userService.getUserById(userId).orElseThrow(() -> new BusinessException("用户不存在"));
        List<UserAchievement> achievements = userAchievementRepository.findByUserOrderByUnlockTimeDesc(user);
        return ResponseEntity.ok(ApiResponse.success(achievements));
    }

    /**
     * 导出用户数据
     */
    @GetMapping("/export")
    @RequireUser
    @Auditable(action = AuditAction.DATA_EXPORT, resourceType = "用户数据")
    @Operation(summary = "导出用户数据", description = "导出当前用户的所有数据，包括资料、训练记录、身体数据等")
    public ResponseEntity<ApiResponse<Map<String, Object>>> exportUserData(HttpServletRequest request) {
        try {
            User user = getUser(request);
            Long userId = user.getId();
            
            // 收集用户所有数据
            Map<String, Object> exportData = new java.util.HashMap<>();
            
            // 用户基本信息（脱敏处理）
            Map<String, Object> userInfo = new java.util.HashMap<>();
            userInfo.put("username", user.getUsername());
            userInfo.put("email", user.getEmail());
            userInfo.put("age", user.getAge());
            userInfo.put("gender", user.getGender());
            userInfo.put("height", user.getHeight());
            userInfo.put("weight", user.getWeight());
            userInfo.put("experienceLevel", user.getExperienceLevel());
            userInfo.put("createdAt", user.getCreatedAt());
            exportData.put("userInfo", userInfo);
            
            // 用户设置
            Optional<UserSetting> settings = userSettingRepository.findByUser(user);
            settings.ifPresent(s -> {
                Map<String, Object> settingsMap = new java.util.HashMap<>();
                settingsMap.put("theme", s.getTheme());
                settingsMap.put("language", s.getLanguage());
                settingsMap.put("notifications", s.getNotifications());
                settingsMap.put("autoSave", s.getAutoSave());
                exportData.put("settings", settingsMap);
            });
            
            // 身体数据记录
            List<BodyRecord> bodyRecords = bodyRecordRepository.findByUserOrderByRecordTimeDesc(user);
            exportData.put("bodyRecords", bodyRecords);
            
            // 训练历史
            Page<TrainingRecord> trainingHistory = trainingRecordService.findByUserId(userId, 0, 1000);
            exportData.put("trainingHistory", trainingHistory.getContent());
            
            // 用户成就
            List<UserAchievement> achievements = userAchievementRepository.findByUserOrderByUnlockTimeDesc(user);
            exportData.put("achievements", achievements);
            
            // 用户统计
            exportData.put("stats", dashboardService.getUserStatsOverview(userId));
            
            // 导出时间戳
            exportData.put("exportedAt", java.time.LocalDateTime.now().toString());
            
            return ResponseEntity.ok(ApiResponse.success("数据导出成功", exportData));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(ApiResponse.<Map<String, Object>>error(500, "数据导出失败: " + e.getMessage()));
        }
    }

    /**
     * 重置用户数据（删除所有训练数据，保留账户）
     */
    @PostMapping("/reset-data")
    @RequireUser
    @Auditable(action = AuditAction.DATA_DELETE, resourceType = "用户训练数据")
    @Operation(summary = "重置用户数据", description = "删除当前用户的所有训练数据，保留账户信息")
    public ResponseEntity<ApiResponse<String>> resetUserData(
            @RequestBody Map<String, String> requestBody,
            HttpServletRequest request) {
        try {
            String password = requestBody.get("password");
            if (password == null || password.isEmpty()) {
                return ResponseEntity.badRequest()
                    .body(ApiResponse.<String>error(400, "请输入密码确认操作"));
            }
            
            User user = getUser(request);
            
            // 验证密码
            if (!userService.validatePassword(password, user.getPassword())) {
                return ResponseEntity.badRequest()
                    .body(ApiResponse.<String>error(400, "密码不正确"));
            }
            
            Long userId = user.getId();
            
            // 删除身体数据记录
            List<BodyRecord> bodyRecords = bodyRecordRepository.findByUserOrderByRecordTimeDesc(user);
            bodyRecordRepository.deleteAll(bodyRecords);
            
            // 删除训练记录
            trainingRecordService.deleteAllByUserId(userId);
            
            // 删除用户成就
            List<UserAchievement> achievements = userAchievementRepository.findByUserOrderByUnlockTimeDesc(user);
            userAchievementRepository.deleteAll(achievements);
            
            // 记录审计日志
            auditLogService.logDataDelete(userId, user.getUsername(), "所有训练数据", null);
            
            return ResponseEntity.ok(ApiResponse.success("数据重置成功，您的账户信息已保留"));
        } catch (BusinessException e) {
            return ResponseEntity.badRequest()
                .body(ApiResponse.<String>error(400, e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(ApiResponse.<String>error(500, "数据重置失败: " + e.getMessage()));
        }
    }

    /**
     * 删除用户账户
     */
    @PostMapping("/delete-account")
    @RequireUser
    @Auditable(action = AuditAction.DATA_DELETE, resourceType = "用户账户")
    @Operation(summary = "删除用户账户", description = "永久删除当前用户的账户及所有相关数据")
    public ResponseEntity<ApiResponse<String>> deleteAccount(
            @RequestBody Map<String, String> requestBody,
            HttpServletRequest request) {
        try {
            String password = requestBody.get("password");
            if (password == null || password.isEmpty()) {
                return ResponseEntity.badRequest()
                    .body(ApiResponse.<String>error(400, "请输入密码确认操作"));
            }
            
            User user = getUser(request);
            
            // 验证密码
            if (!userService.validatePassword(password, user.getPassword())) {
                return ResponseEntity.badRequest()
                    .body(ApiResponse.<String>error(400, "密码不正确"));
            }
            
            Long userId = user.getId();
            String username = user.getUsername();
            
            // 删除用户所有关联数据
            // 1. 删除身体数据记录
            List<BodyRecord> bodyRecords = bodyRecordRepository.findByUserOrderByRecordTimeDesc(user);
            bodyRecordRepository.deleteAll(bodyRecords);
            
            // 2. 删除训练记录
            trainingRecordService.deleteAllByUserId(userId);
            
            // 3. 删除用户成就
            List<UserAchievement> achievements = userAchievementRepository.findByUserOrderByUnlockTimeDesc(user);
            userAchievementRepository.deleteAll(achievements);
            
            // 4. 删除用户设置
            userSettingRepository.findByUser(user).ifPresent(userSettingRepository::delete);
            
            // 5. 删除用户账户
            userService.deleteUser(userId);
            
            // 记录审计日志
            auditLogService.logDataDelete(userId, username, "用户账户", userId);
            
            return ResponseEntity.ok(ApiResponse.success("账户已删除"));
        } catch (BusinessException e) {
            return ResponseEntity.badRequest()
                .body(ApiResponse.<String>error(400, e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(ApiResponse.<String>error(500, "账户删除失败: " + e.getMessage()));
        }
    }
}
