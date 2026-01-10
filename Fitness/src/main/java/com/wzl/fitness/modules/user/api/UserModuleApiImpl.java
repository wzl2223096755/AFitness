package com.wzl.fitness.modules.user.api;

import com.wzl.fitness.entity.User;
import com.wzl.fitness.modules.user.dto.UserDTO;
import com.wzl.fitness.modules.user.dto.UserBasicInfo;
import com.wzl.fitness.modules.user.service.UserModuleService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * 用户模块API实现
 * 实现用户模块对外暴露的服务契约
 * 
 * @see Requirements 2.1 - 为每个领域模块定义独立的服务接口
 * @see Requirements 2.5 - 模块接口使用DTO进行数据传输
 * @see Requirements 6.2 - 支持通过配置文件启用/禁用模块
 */
@Component
@RequiredArgsConstructor
@Slf4j
@ConditionalOnProperty(
    prefix = "fitness.modules.user",
    name = "enabled",
    havingValue = "true",
    matchIfMissing = true
)
public class UserModuleApiImpl implements UserModuleApi {
    
    private final UserModuleService userModuleService;
    
    @Override
    public UserDTO getUserById(Long userId) {
        log.debug("UserModuleApi.getUserById: 获取用户信息，用户ID: {}", userId);
        return userModuleService.getUserById(userId)
                .map(userModuleService::convertToDTO)
                .orElse(null);
    }
    
    @Override
    public UserDTO getUserByUsername(String username) {
        log.debug("UserModuleApi.getUserByUsername: 获取用户信息，用户名: {}", username);
        return userModuleService.getUserByUsername(username)
                .map(userModuleService::convertToDTO)
                .orElse(null);
    }
    
    @Override
    public boolean existsById(Long userId) {
        log.debug("UserModuleApi.existsById: 检查用户是否存在，用户ID: {}", userId);
        return userModuleService.existsById(userId);
    }
    
    @Override
    public UserBasicInfo getBasicInfo(Long userId) {
        log.debug("UserModuleApi.getBasicInfo: 获取用户基础信息，用户ID: {}", userId);
        return userModuleService.getUserById(userId)
                .map(userModuleService::convertToBasicInfo)
                .orElse(null);
    }
}
