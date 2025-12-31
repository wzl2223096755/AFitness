package com.wzl.fitness.service.impl;

import com.wzl.fitness.common.ResponseCode;
import com.wzl.fitness.entity.User;
import com.wzl.fitness.entity.Role;
import com.wzl.fitness.exception.BusinessException;
import com.wzl.fitness.exception.UserNotFoundException;
import com.wzl.fitness.repository.UserRepository;
import com.wzl.fitness.service.UserService;
import com.wzl.fitness.service.CacheService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {

    private static final Logger log = LoggerFactory.getLogger(UserServiceImpl.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder; // 添加密码编码器
    private final CacheService cacheService; // 添加缓存服务

    // 使用反射设置字段值
    private void setField(User user, String fieldName, Object value) {
        try {
            Field field = User.class.getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(user, value);
        } catch (Exception e) {
            log.error("Failed to set field {} with value {}", fieldName, value, e);
            throw new RuntimeException("Failed to set field " + fieldName, e);
        }
    }

    // 使用反射获取字段值
    private Object getField(User user, String fieldName) {
        try {
            Field field = User.class.getDeclaredField(fieldName);
            field.setAccessible(true);
            return field.get(user);
        } catch (Exception e) {
            log.error("Failed to get field {} from user", fieldName, e);
            throw new RuntimeException("Failed to get field " + fieldName, e);
        }
    }

    @Override
    @Transactional
    @CacheEvict(value = "users", allEntries = true)
    public User createUser(User user) {
        // 检查用户名是否已存在
        if (existsByUsername(user.getUsername())) {
            throw new BusinessException(ResponseCode.USERNAME_EXISTS);
        }
        
        // 检查邮箱是否已存在
        String email = (String) getField(user, "email");
        if (email != null && !email.isEmpty()) {
            if (existsByEmail(email)) {
                throw new BusinessException(ResponseCode.EMAIL_EXISTS);
            }
        }
        
        // 加密用户密码
        String password = (String) getField(user, "password");
        if (password != null && !password.isEmpty()) {
            setField(user, "password", passwordEncoder.encode(password));
        } else {
            throw new BusinessException(ResponseCode.PARAM_ERROR.getCode(), "密码不能为空");
        }
        
        User savedUser = userRepository.save(user);
        
        // 缓存新创建的用户
        cacheService.put("users", "user:" + savedUser.getId(), savedUser, 30, TimeUnit.MINUTES);
        cacheService.put("users", "user:" + savedUser.getUsername(), savedUser, 30, TimeUnit.MINUTES);
        
        return savedUser;
    }

    @Override
    @Cacheable(value = "users", key = "'id:' + #id")
    public Optional<User> getUserById(Long id) {
        return userRepository.findById(id);
    }

    @Override
    @Cacheable(value = "users", key = "'username:' + #username")
    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    @Override
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    @Override
    @Transactional
    @CacheEvict(value = "users", allEntries = true)
    public Optional<User> updateUser(Long id, User userDetails) {
        User user = userRepository.findById(id)
                .orElse(null);
        
        if (user == null) {
            return Optional.empty();
        }
        
        // 更新用户信息
        String newUsername = (String) getField(userDetails, "username");
        if (newUsername != null && !newUsername.equals(user.getUsername())) {
            if (existsByUsername(newUsername)) {
                throw new BusinessException(ResponseCode.USERNAME_EXISTS);
            }
            setField(user, "username", newUsername);
        }
        
        String newEmail = (String) getField(userDetails, "email");
        String oldEmail = (String) getField(user, "email");
        if (newEmail != null && !newEmail.equals(oldEmail)) {
            if (existsByEmail(newEmail)) {
                throw new BusinessException(ResponseCode.EMAIL_EXISTS);
            }
            setField(user, "email", newEmail);
        }
        
        // 更新密码（如果提供了新密码）
        String newPassword = (String) getField(userDetails, "password");
        if (newPassword != null && !newPassword.isEmpty()) {
            setField(user, "password", passwordEncoder.encode(newPassword));
        }
        
        // 更新其他字段
        Integer age = (Integer) getField(userDetails, "age");
        if (age != null) {
            setField(user, "age", age);
        }
        
        Double weight = (Double) getField(userDetails, "weight");
        if (weight != null) {
            setField(user, "weight", weight);
        }
        
        String gender = (String) getField(userDetails, "gender");
        if (gender != null) {
            setField(user, "gender", gender);
        }
        
        Integer height = (Integer) getField(userDetails, "height");
        if (height != null) {
            setField(user, "height", height);
        }
        
        String experienceLevel = (String) getField(userDetails, "experienceLevel");
        if (experienceLevel != null) {
            setField(user, "experienceLevel", experienceLevel);
        }
        
        User savedUser = userRepository.save(user);
        
        // 更新缓存
        cacheService.put("users", "user:" + savedUser.getId(), savedUser, 30, TimeUnit.MINUTES);
        cacheService.put("users", "user:" + savedUser.getUsername(), savedUser, 30, TimeUnit.MINUTES);
        
        return Optional.of(savedUser);
    }

    @Override
    @Transactional
    @CacheEvict(value = "users", allEntries = true)
    public boolean deleteUser(Long id) {
        if (!userRepository.existsById(id)) {
            return false;
        }
        userRepository.deleteById(id);
        
        // 清除相关缓存
        cacheService.evictByPattern("users", "*");
        
        return true;
    }

    @Override
    public boolean validatePassword(String rawPassword, String encodedPassword) {
        return passwordEncoder.matches(rawPassword, encodedPassword);
    }

    @Override
    public boolean existsByUsername(String username) {
        return userRepository.existsByUsername(username);
    }

    @Override
    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }

    // ========== 分页查询操作实现 ==========
    
    @Override
    @Cacheable(value = "users", key = "'role:' + #role.name() + ':page:' + #pageable.pageNumber + ':size:' + #pageable.pageSize")
    public Page<User> getUsersByRole(Role role, Pageable pageable) {
        log.debug("获取角色为 {} 的用户，页码: {}, 页大小: {}", role.name(), pageable.getPageNumber(), pageable.getPageSize());
        return userRepository.findByRole(role, pageable);
    }

    @Override
    @Cacheable(value = "users", key = "'search:' + #keyword + ':page:' + #pageable.pageNumber + ':size:' + #pageable.pageSize")
    public Page<User> searchUsers(String keyword, Pageable pageable) {
        log.debug("搜索用户，关键词: {}, 页码: {}, 页大小: {}", keyword, pageable.getPageNumber(), pageable.getPageSize());
        return userRepository.findByUsernameOrEmailContaining(keyword, pageable);
    }

    @Override
    @Cacheable(value = "users", key = "'all:page:' + #pageable.pageNumber + ':size:' + #pageable.pageSize")
    public Page<User> getAllUsersPaginated(Pageable pageable) {
        log.debug("获取所有用户，页码: {}, 页大小: {}", pageable.getPageNumber(), pageable.getPageSize());
        return userRepository.findAll(pageable);
    }

    // ========== 批量操作实现 ==========
    
    @Override
    @Transactional
    public void updateLastLoginTimes(Map<Long, LocalDateTime> userLoginTimes) {
        log.debug("批量更新用户最后登录时间，用户数量: {}", userLoginTimes.size());
        userLoginTimes.forEach(userRepository::updateLastLoginAt);
        
        // 清除相关缓存
        cacheService.evictAll("users", userLoginTimes.keySet());
        log.debug("已清除用户缓存，数量: {}", userLoginTimes.size());
    }

    @Override
    @Cacheable(value = "users", key = "'batch:' + #userIds.hashCode()")
    public List<User> findUsersByIds(List<Long> userIds) {
        log.debug("批量查找用户，ID数量: {}", userIds.size());
        return userRepository.findAllById(userIds);
    }

    @Override
    public Map<Long, User> getUsersByIds(List<Long> userIds) {
        log.debug("获取用户映射，ID数量: {}", userIds.size());
        
        // 先尝试从缓存批量获取
        Map<Long, User> cachedUsers = cacheService.getAll("users", userIds, User.class);
        
        // 找出缓存中没有的用户ID
        List<Long> uncachedIds = userIds.stream()
                .filter(id -> !cachedUsers.containsKey(id))
                .collect(Collectors.toList());
        
        if (!uncachedIds.isEmpty()) {
            // 从数据库查询未缓存的用户
            List<User> uncachedUsers = userRepository.findAllById(uncachedIds);
            
            // 将查询结果放入缓存
            Map<Long, User> newEntries = uncachedUsers.stream()
                    .collect(Collectors.toMap(User::getId, user -> user));
            
            cacheService.putAll("users", newEntries);
            
            // 合并结果
            cachedUsers.putAll(newEntries);
        }
        
        return cachedUsers;
    }

    // ========== 关联查询操作实现 ==========
    
    @Override
    @Cacheable(value = "users", key = "'withDevices:' + #userId")
    public Optional<User> findUserWithDevices(Long userId) {
        log.debug("获取用户及其设备信息，用户ID: {}", userId);
        return userRepository.findWithDevicesById(userId);
    }

    @Override
    @Cacheable(value = "users", key = "'withRecords:' + #userId")
    public Optional<User> findUserWithTrainingRecords(Long userId) {
        log.debug("获取用户及其训练记录，用户ID: {}", userId);
        return userRepository.findWithTrainingRecordsById(userId);
    }

    // ========== 缓存操作实现 ==========
    
    @Override
    @CacheEvict(value = "users", key = "'user:' + #userId")
    public void evictUserCache(Long userId) {
        log.debug("清除用户缓存，用户ID: {}", userId);
        // 同时清除相关的分页缓存
        cacheService.evictByPattern("users", "*");
    }

    @Override
    @CacheEvict(value = "users", key = "'user:' + #username")
    public void evictUserCache(String username) {
        log.debug("清除用户缓存，用户名: {}", username);
        // 同时清除相关的分页缓存
        cacheService.evictByPattern("users", "*");
    }

    @Override
    @CacheEvict(value = "users", allEntries = true)
    public void evictAllUserCache() {
        log.debug("清除所有用户缓存");
    }
}