package com.wzl.fitness.modules.user.service.impl;

import com.wzl.fitness.entity.User;
import com.wzl.fitness.modules.user.dto.UserDTO;
import com.wzl.fitness.modules.user.dto.UserBasicInfo;
import com.wzl.fitness.modules.user.repository.UserModuleRepository;
import com.wzl.fitness.modules.user.service.UserModuleService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * 用户模块服务实现
 * 实现用户模块内部的业务逻辑
 * 
 * @see Requirements 1.2 - 领域模块包含独立的service子包
 * @see Requirements 6.2 - 支持通过配置文件启用/禁用模块
 */
@Service
@RequiredArgsConstructor
@Slf4j
@ConditionalOnProperty(
    prefix = "fitness.modules.user",
    name = "enabled",
    havingValue = "true",
    matchIfMissing = true
)
public class UserModuleServiceImpl implements UserModuleService {
    
    private final UserModuleRepository userModuleRepository;
    
    @Override
    @Cacheable(value = "users", key = "'id:' + #userId")
    public Optional<User> getUserById(Long userId) {
        log.debug("获取用户信息，用户ID: {}", userId);
        return userModuleRepository.findById(userId);
    }
    
    @Override
    @Cacheable(value = "users", key = "'username:' + #username")
    public Optional<User> getUserByUsername(String username) {
        log.debug("获取用户信息，用户名: {}", username);
        return userModuleRepository.findByUsername(username);
    }
    
    @Override
    public boolean existsById(Long userId) {
        return userModuleRepository.existsById(userId);
    }
    
    @Override
    public UserDTO convertToDTO(User user) {
        if (user == null) {
            return null;
        }
        
        return UserDTO.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .avatar(user.getAvatar())
                .gender(user.getGender())
                .age(user.getAge())
                .height(user.getHeight() != null ? user.getHeight().doubleValue() : null)
                .weight(user.getWeight())
                .role(user.getRole() != null ? user.getRole().name() : null)
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }
    
    @Override
    public UserBasicInfo convertToBasicInfo(User user) {
        if (user == null) {
            return null;
        }
        
        return UserBasicInfo.builder()
                .id(user.getId())
                .username(user.getUsername())
                .nickname(user.getUsername()) // 使用用户名作为昵称
                .avatar(user.getAvatar())
                .build();
    }
}
