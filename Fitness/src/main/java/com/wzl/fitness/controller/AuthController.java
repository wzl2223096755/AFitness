package com.wzl.fitness.controller;

import com.wzl.fitness.common.ApiResponse;
import com.wzl.fitness.dto.request.LoginRequest;
import com.wzl.fitness.dto.request.RegisterRequest;
import com.wzl.fitness.dto.request.RefreshTokenRequest;
import com.wzl.fitness.dto.response.LoginResponse;
import com.wzl.fitness.dto.response.RefreshTokenResponse;
import com.wzl.fitness.entity.User;
import com.wzl.fitness.service.AuditLogService;
import com.wzl.fitness.service.AuthenticationService;
import com.wzl.fitness.service.JwtRefreshService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * 认证控制器
 */
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "认证管理", description = "用户认证相关接口")
public class AuthController {

    // 显式添加Logger实例
    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    private final AuthenticationService authenticationService;
    private final JwtRefreshService jwtRefreshService;
    private final AuditLogService auditLogService;

    /**
     * 用户登录
     */
    @PostMapping("/login")
    @Operation(summary = "用户登录", description = "用户使用用户名和密码登录")
    public ResponseEntity<ApiResponse<LoginResponse>> login(@Valid @RequestBody LoginRequest request) {
        logger.debug("AuthController.login: 接收到登录请求，用户名: {}", request.getUsername());
        try {
            LoginResponse response = authenticationService.login(request);
            logger.debug("AuthController.login: 登录成功");
            return ResponseEntity.ok(ApiResponse.success(response));
        } catch (Exception e) {
            logger.error("AuthController.login: 登录失败", e);
            throw e;
        }
    }

    /**
     * 用户注册
     */
    @PostMapping("/register")
    @Operation(summary = "用户注册", description = "新用户注册")
    public ResponseEntity<ApiResponse<String>> register(@Valid @RequestBody RegisterRequest request) {
        authenticationService.register(request);
        return ResponseEntity.ok(ApiResponse.success("注册成功"));
    }

    /**
     * 刷新token
     */
    @PostMapping("/refresh")
    @Operation(summary = "刷新访问令牌", description = "使用刷新令牌获取新的访问令牌")
    public ResponseEntity<ApiResponse<RefreshTokenResponse>> refreshToken(@Valid @RequestBody RefreshTokenRequest request) {
        String newAccessToken = jwtRefreshService.refreshToken(request.getRefreshToken());
        RefreshTokenResponse response = RefreshTokenResponse.builder()
                .accessToken(newAccessToken)
                .tokenType("Bearer")
                .expiresIn(86400L)
                .build();
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * 用户登出
     */
    @PostMapping("/logout")
    @Operation(summary = "用户登出", description = "用户登出并使token失效")
    public ResponseEntity<ApiResponse<String>> logout(@RequestHeader("Authorization") String authorization) {
        String token = authorization.substring(7); // 移除 "Bearer " 前缀
        String username = jwtRefreshService.getUsernameFromToken(token);
        
        // 使token失效
        jwtRefreshService.blacklistToken(token);
        
        // 尝试获取用户信息用于审计日志（可能失败，不影响登出）
        try {
            User user = authenticationService.getUserByUsername(username);
            if (user != null) {
                auditLogService.logLogout(user.getId(), user.getUsername());
            } else {
                auditLogService.logLogout(null, username);
            }
        } catch (Exception e) {
            // 即使获取用户失败，也记录登出日志
            auditLogService.logLogout(null, username);
        }
        
        return ResponseEntity.ok(ApiResponse.success("登出成功"));
    }

    /**
     * 验证token并获取用户信息
     */
    @GetMapping("/me")
    @Operation(summary = "获取当前用户信息", description = "验证token并返回当前登录用户的信息")
    public ResponseEntity<ApiResponse<User>> getCurrentUser(@RequestHeader("Authorization") String authorization) {
        String token = authorization.substring(7);
        String username = jwtRefreshService.getUsernameFromToken(token);
        User user = authenticationService.getUserByUsername(username);
        return ResponseEntity.ok(ApiResponse.success(user));
    }

    /**
     * 检查用户名是否存在
     */
    @GetMapping("/check-username")
    @Operation(summary = "检查用户名是否存在", description = "检查指定的用户名是否已被占用")
    public ResponseEntity<ApiResponse<Boolean>> checkUsername(@RequestParam String username) {
        boolean exists = authenticationService.checkUsernameExists(username);
        return ResponseEntity.ok(ApiResponse.success(exists));
    }

    /**
     * 检查邮箱是否存在
     */
    @GetMapping("/check-email")
    @Operation(summary = "检查邮箱是否存在", description = "检查指定的邮箱是否已被占用")
    public ResponseEntity<ApiResponse<Boolean>> checkEmail(@RequestParam String email) {
        boolean exists = authenticationService.checkEmailExists(email);
        return ResponseEntity.ok(ApiResponse.success(exists));
    }
}
