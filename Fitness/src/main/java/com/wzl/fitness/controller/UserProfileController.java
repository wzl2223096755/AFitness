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
    public ResponseEntity<ApiResponse<Object>> getUserStats(HttpServletRequest request) {
        Long userId = getUserIdFromRequest(request);
        return ResponseEntity.ok(ApiResponse.success(dashboardService.getUserStatsOverview(userId)));
    }

    /**
     * 获取用户训练历史
     */
    @GetMapping("/training-history")
    @RequireUser
    public ResponseEntity<ApiResponse<List<TrainingRecord>>> getTrainingHistory(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
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
    public ResponseEntity<ApiResponse<BodyRecord>> updateBodyRecord(
            @PathVariable Long id,
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
    public ResponseEntity<ApiResponse<Void>> deleteBodyRecord(
            @PathVariable Long id,
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
    public ResponseEntity<ApiResponse<List<UserAchievement>>> getAchievements(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        User user = userService.getUserById(userId).orElseThrow(() -> new BusinessException("用户不存在"));
        List<UserAchievement> achievements = userAchievementRepository.findByUserOrderByUnlockTimeDesc(user);
        return ResponseEntity.ok(ApiResponse.success(achievements));
    }
}
