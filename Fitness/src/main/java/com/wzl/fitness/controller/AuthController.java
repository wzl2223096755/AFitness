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
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
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
 * 
 * 提供用户认证相关功能，包括：
 * - 用户登录/登出
 * - 用户注册
 * - 令牌刷新
 * - 用户名/邮箱检查
 */
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "认证管理", description = "用户认证相关接口，包括注册、登录、令牌刷新等")
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
    @Operation(
            summary = "用户登录", 
            description = "用户使用用户名和密码登录，成功后返回JWT访问令牌和刷新令牌"
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "登录成功",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "登录成功示例",
                                    value = """
                                            {
                                              "code": 200,
                                              "message": "操作成功",
                                              "data": {
                                                "accessToken": "eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJhZG1pbiIsImlhdCI6MTcwNDEyMzQ1NiwiZXhwIjoxNzA0MjA5ODU2fQ...",
                                                "refreshToken": "eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJhZG1pbiIsImlhdCI6MTcwNDEyMzQ1NiwiZXhwIjoxNzA0NzI4MjU2fQ...",
                                                "tokenType": "Bearer",
                                                "expiresIn": 86400,
                                                "userId": 1,
                                                "username": "admin",
                                                "role": "ROLE_ADMIN"
                                              },
                                              "timestamp": "2024-01-01 12:00:00",
                                              "success": true
                                            }
                                            """
                            )
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "登录失败 - 用户名或密码错误",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "登录失败示例",
                                    value = """
                                            {
                                              "code": 1005,
                                              "message": "用户名或密码错误",
                                              "data": null,
                                              "timestamp": "2024-01-01 12:00:00",
                                              "success": false
                                            }
                                            """
                            )
                    )
            )
    })
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "登录请求参数",
            required = true,
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = LoginRequest.class),
                    examples = @ExampleObject(
                            name = "登录请求示例",
                            value = """
                                    {
                                      "username": "admin",
                                      "password": "123456"
                                    }
                                    """
                    )
            )
    )
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
    @Operation(
            summary = "用户注册", 
            description = "新用户注册，需要提供用户名、密码和邮箱"
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "注册成功",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "注册成功示例",
                                    value = """
                                            {
                                              "code": 200,
                                              "message": "注册成功",
                                              "data": "注册成功",
                                              "timestamp": "2024-01-01 12:00:00",
                                              "success": true
                                            }
                                            """
                            )
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "注册失败 - 用户名或邮箱已存在",
                    content = @Content(
                            mediaType = "application/json",
                            examples = {
                                    @ExampleObject(
                                            name = "用户名已存在",
                                            value = """
                                                    {
                                                      "code": 1002,
                                                      "message": "用户名已存在",
                                                      "data": null,
                                                      "timestamp": "2024-01-01 12:00:00",
                                                      "success": false
                                                    }
                                                    """
                                    ),
                                    @ExampleObject(
                                            name = "邮箱已存在",
                                            value = """
                                                    {
                                                      "code": 1003,
                                                      "message": "邮箱已存在",
                                                      "data": null,
                                                      "timestamp": "2024-01-01 12:00:00",
                                                      "success": false
                                                    }
                                                    """
                                    )
                            }
                    )
            )
    })
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "注册请求参数",
            required = true,
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = RegisterRequest.class),
                    examples = @ExampleObject(
                            name = "注册请求示例",
                            value = """
                                    {
                                      "username": "fitness_user",
                                      "password": "Password123",
                                      "email": "user@example.com"
                                    }
                                    """
                    )
            )
    )
    public ResponseEntity<ApiResponse<String>> register(@Valid @RequestBody RegisterRequest request) {
        authenticationService.register(request);
        return ResponseEntity.ok(ApiResponse.success("注册成功"));
    }

    /**
     * 刷新token
     */
    @PostMapping("/refresh")
    @Operation(
            summary = "刷新访问令牌", 
            description = "使用刷新令牌获取新的访问令牌，当访问令牌过期时调用此接口"
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "刷新成功",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "刷新成功示例",
                                    value = """
                                            {
                                              "code": 200,
                                              "message": "操作成功",
                                              "data": {
                                                "accessToken": "eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJhZG1pbiIsImlhdCI6MTcwNDEyMzQ1NiwiZXhwIjoxNzA0MjA5ODU2fQ...",
                                                "tokenType": "Bearer",
                                                "expiresIn": 86400
                                              },
                                              "timestamp": "2024-01-01 12:00:00",
                                              "success": true
                                            }
                                            """
                            )
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = "刷新令牌无效或已过期",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "刷新失败示例",
                                    value = """
                                            {
                                              "code": 401,
                                              "message": "刷新令牌无效或已过期",
                                              "data": null,
                                              "timestamp": "2024-01-01 12:00:00",
                                              "success": false
                                            }
                                            """
                            )
                    )
            )
    })
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "刷新令牌请求",
            required = true,
            content = @Content(
                    mediaType = "application/json",
                    examples = @ExampleObject(
                            name = "刷新令牌请求示例",
                            value = """
                                    {
                                      "refreshToken": "eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJhZG1pbiIsImlhdCI6MTcwNDEyMzQ1NiwiZXhwIjoxNzA0NzI4MjU2fQ..."
                                    }
                                    """
                    )
            )
    )
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
    @Operation(
            summary = "用户登出", 
            description = "用户登出并使当前token失效，需要在请求头中携带Authorization"
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "登出成功",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "登出成功示例",
                                    value = """
                                            {
                                              "code": 200,
                                              "message": "登出成功",
                                              "data": "登出成功",
                                              "timestamp": "2024-01-01 12:00:00",
                                              "success": true
                                            }
                                            """
                            )
                    )
            )
    })
    public ResponseEntity<ApiResponse<String>> logout(
            @Parameter(description = "Bearer Token", required = true, example = "Bearer eyJhbGciOiJIUzUxMiJ9...")
            @RequestHeader("Authorization") String authorization) {
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
    @Operation(
            summary = "获取当前用户信息", 
            description = "验证token并返回当前登录用户的详细信息"
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "获取成功",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "获取用户信息成功示例",
                                    value = """
                                            {
                                              "code": 200,
                                              "message": "操作成功",
                                              "data": {
                                                "id": 1,
                                                "username": "admin",
                                                "email": "admin@example.com",
                                                "role": "ROLE_ADMIN",
                                                "createdAt": "2024-01-01T00:00:00",
                                                "updatedAt": "2024-01-01T12:00:00"
                                              },
                                              "timestamp": "2024-01-01 12:00:00",
                                              "success": true
                                            }
                                            """
                            )
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = "Token无效或已过期"
            )
    })
    public ResponseEntity<ApiResponse<User>> getCurrentUser(
            @Parameter(description = "Bearer Token", required = true, example = "Bearer eyJhbGciOiJIUzUxMiJ9...")
            @RequestHeader("Authorization") String authorization) {
        String token = authorization.substring(7);
        String username = jwtRefreshService.getUsernameFromToken(token);
        User user = authenticationService.getUserByUsername(username);
        return ResponseEntity.ok(ApiResponse.success(user));
    }

    /**
     * 检查用户名是否存在
     */
    @GetMapping("/check-username")
    @Operation(
            summary = "检查用户名是否存在", 
            description = "检查指定的用户名是否已被占用，用于注册前的验证"
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "检查完成",
                    content = @Content(
                            mediaType = "application/json",
                            examples = {
                                    @ExampleObject(
                                            name = "用户名已存在",
                                            value = """
                                                    {
                                                      "code": 200,
                                                      "message": "操作成功",
                                                      "data": true,
                                                      "timestamp": "2024-01-01 12:00:00",
                                                      "success": true
                                                    }
                                                    """
                                    ),
                                    @ExampleObject(
                                            name = "用户名可用",
                                            value = """
                                                    {
                                                      "code": 200,
                                                      "message": "操作成功",
                                                      "data": false,
                                                      "timestamp": "2024-01-01 12:00:00",
                                                      "success": true
                                                    }
                                                    """
                                    )
                            }
                    )
            )
    })
    public ResponseEntity<ApiResponse<Boolean>> checkUsername(
            @Parameter(description = "要检查的用户名", required = true, example = "fitness_user")
            @RequestParam String username) {
        boolean exists = authenticationService.checkUsernameExists(username);
        return ResponseEntity.ok(ApiResponse.success(exists));
    }

    /**
     * 检查邮箱是否存在
     */
    @GetMapping("/check-email")
    @Operation(
            summary = "检查邮箱是否存在", 
            description = "检查指定的邮箱是否已被占用，用于注册前的验证"
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "检查完成",
                    content = @Content(
                            mediaType = "application/json",
                            examples = {
                                    @ExampleObject(
                                            name = "邮箱已存在",
                                            value = """
                                                    {
                                                      "code": 200,
                                                      "message": "操作成功",
                                                      "data": true,
                                                      "timestamp": "2024-01-01 12:00:00",
                                                      "success": true
                                                    }
                                                    """
                                    ),
                                    @ExampleObject(
                                            name = "邮箱可用",
                                            value = """
                                                    {
                                                      "code": 200,
                                                      "message": "操作成功",
                                                      "data": false,
                                                      "timestamp": "2024-01-01 12:00:00",
                                                      "success": true
                                                    }
                                                    """
                                    )
                            }
                    )
            )
    })
    public ResponseEntity<ApiResponse<Boolean>> checkEmail(
            @Parameter(description = "要检查的邮箱", required = true, example = "user@example.com")
            @RequestParam String email) {
        boolean exists = authenticationService.checkEmailExists(email);
        return ResponseEntity.ok(ApiResponse.success(exists));
    }
}
