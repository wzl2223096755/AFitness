package com.wzl.fitness.modules.user.service;

import com.wzl.fitness.entity.User;
import com.wzl.fitness.modules.user.dto.UserDTO;
import com.wzl.fitness.modules.user.dto.UserBasicInfo;

import java.util.Optional;

/**
 * 用户模块服务接口
 * 定义用户模块内部的业务逻辑方法
 * 
 * @see Requirements 1.2 - 领域模块包含独立的service子包
 */
public interface UserModuleService {
    
    /**
     * 根据ID获取用户实体
     */
    Optional<User> getUserById(Long userId);
    
    /**
     * 根据用户名获取用户实体
     */
    Optional<User> getUserByUsername(String username);
    
    /**
     * 检查用户是否存在
     */
    boolean existsById(Long userId);
    
    /**
     * 将用户实体转换为DTO
     */
    UserDTO convertToDTO(User user);
    
    /**
     * 将用户实体转换为基础信息DTO
     */
    UserBasicInfo convertToBasicInfo(User user);
}
