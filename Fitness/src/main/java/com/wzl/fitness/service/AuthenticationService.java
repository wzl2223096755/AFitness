package com.wzl.fitness.service;

import com.wzl.fitness.dto.request.LoginRequest;
import com.wzl.fitness.dto.request.RegisterRequest;
import com.wzl.fitness.dto.response.LoginResponse;
import com.wzl.fitness.entity.Role;
import com.wzl.fitness.entity.User;
import com.wzl.fitness.exception.BusinessException;
import com.wzl.fitness.repository.UserRepository;
import com.wzl.fitness.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

/**
 * 认证服务
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AuthenticationService {

    // 显式添加Logger实例
    private static final Logger logger = LoggerFactory.getLogger(AuthenticationService.class);

    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;
    private final JwtRefreshService jwtRefreshService;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final CacheService cacheService;
    @Lazy
    private final AuditLogService auditLogService;

    /**
     * 用户登录
     */
    @Transactional
    public LoginResponse login(LoginRequest request) {
        try {
            logger.debug("尝试登录用户: {}", request.getUsername());
            
            // 先从缓存获取用户信息
            String cacheKey = "user:" + request.getUsername();
            User user = cacheService.get("users", cacheKey, User.class);
            
            if (user == null) {
                user = userRepository.findByUsername(request.getUsername())
                    .orElseThrow(() -> new BusinessException("用户不存在"));
                
                // 缓存用户信息30分钟
                cacheService.put("users", cacheKey, user, 30, TimeUnit.MINUTES);
                logger.debug("用户信息已缓存: {}", request.getUsername());
            }
            
            logger.debug("找到用户: {}", user.getUsername());
            
            // 认证用户
            Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
            );

            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            
            // 生成访问令牌
            String accessToken = jwtTokenProvider.generateToken(userDetails, user.getId());
            
            // 生成并保存刷新令牌
            String refreshToken = jwtRefreshService.generateAndSaveRefreshToken(userDetails, user.getId());

            // 更新最后登录时间
            userRepository.updateLastLoginAt(user.getId(), LocalDateTime.now());
            
            // 清除用户相关缓存，确保数据一致性
            cacheService.evict("users", "user:" + user.getId());
            cacheService.evictByPattern("users", "*");

            // 缓存登录响应
            LoginResponse response = LoginResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresIn(86400L) // 24小时
                .userId(user.getId())
                .username(user.getUsername())
                .role(user.getRole().name())
                .build();
            
            // 缓存登录响应15分钟
            cacheService.put("loginResponses", "login:" + user.getId(), response, 15, TimeUnit.MINUTES);

            // 记录登录成功审计日志
            auditLogService.logLoginSuccess(user.getId(), user.getUsername());

            return response;

        } catch (AuthenticationException e) {
            logger.warn("用户登录失败: {}, 错误: {}", request.getUsername(), e.getMessage());
            // 记录登录失败审计日志
            auditLogService.logLoginFailure(request.getUsername(), e.getMessage());
            throw new BusinessException("用户名或密码错误");
        }
    }

    /**
     * 用户注册
     */
    @Transactional
    @CacheEvict(value = "users", allEntries = true)
    public void register(RegisterRequest request) {
        // 检查用户名是否已存在
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new BusinessException("用户名已存在");
        }

        // 检查邮箱是否已存在
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BusinessException("邮箱已被注册");
        }

        // 创建新用户
        String rawPassword = request.getPassword();
        String encodedPassword = passwordEncoder.encode(rawPassword);
        
        logger.debug("注册用户 - 原始密码: {}, 编码后密码: {}", rawPassword, encodedPassword);
        logger.debug("密码编码器类型: {}", passwordEncoder.getClass().getSimpleName());

        User user = User.builder()
            .username(request.getUsername())
            .email(request.getEmail())
            .password(encodedPassword)
            .role(Role.USER)
            .build();

        User savedUser = userRepository.save(user);
        logger.info("新用户注册成功: {} ({}), ID: {}", request.getUsername(), request.getEmail(), savedUser.getId());
        
        // 记录注册审计日志
        auditLogService.logRegister(savedUser.getId(), savedUser.getUsername());
        
        // 清除相关缓存
        cacheService.evict("users", "user:" + request.getUsername());
    }

    /**
     * 检查用户名是否存在
     */
    public boolean checkUsernameExists(String username) {
        return userRepository.existsByUsername(username);
    }

    /**
     * 检查邮箱是否存在
     */
    public boolean checkEmailExists(String email) {
        return userRepository.existsByEmail(email);
    }

    /**
     * 根据ID获取用户
     */
    public User getUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException("用户不存在"));
    }

    /**
     * 根据用户名获取用户
     */
    public User getUserByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new BusinessException("用户不存在"));
    }

    /**
     * 检查用户名是否存在
     */
    @Cacheable(value = "usernameCheck", key = "#username")
    public boolean isUsernameExists(String username) {
        return userRepository.existsByUsername(username);
    }

    /**
     * 检查邮箱是否存在
     */
    @Cacheable(value = "emailCheck", key = "#email")
    public boolean isEmailExists(String email) {
        return userRepository.existsByEmail(email);
    }
}