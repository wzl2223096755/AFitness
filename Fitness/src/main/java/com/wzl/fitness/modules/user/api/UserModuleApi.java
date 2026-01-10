package com.wzl.fitness.modules.user.api;

import com.wzl.fitness.modules.user.dto.UserDTO;
import com.wzl.fitness.modules.user.dto.UserBasicInfo;

/**
 * 用户模块对外接口
 * 定义用户模块对其他模块暴露的服务契约
 * 
 * @see Requirements 2.1 - 为每个领域模块定义独立的服务接口
 */
public interface UserModuleApi {
    
    /**
     * 根据ID获取用户信息
     * 
     * @param userId 用户ID
     * @return 用户DTO，如果用户不存在返回null
     */
    UserDTO getUserById(Long userId);
    
    /**
     * 根据用户名获取用户信息
     * 
     * @param username 用户名
     * @return 用户DTO，如果用户不存在返回null
     */
    UserDTO getUserByUsername(String username);
    
    /**
     * 检查用户是否存在
     * 
     * @param userId 用户ID
     * @return 如果用户存在返回true，否则返回false
     */
    boolean existsById(Long userId);
    
    /**
     * 获取用户基础信息（用于其他模块引用）
     * 返回精简的用户信息，避免暴露敏感数据
     * 
     * @param userId 用户ID
     * @return 用户基础信息DTO，如果用户不存在返回null
     */
    UserBasicInfo getBasicInfo(Long userId);
}
