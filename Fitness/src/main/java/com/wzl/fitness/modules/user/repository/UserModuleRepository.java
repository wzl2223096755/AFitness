package com.wzl.fitness.modules.user.repository;

import com.wzl.fitness.entity.User;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import jakarta.persistence.QueryHint;
import java.util.Optional;

/**
 * 用户模块仓储接口
 * 提供用户模块所需的数据访问方法
 * 
 * @see Requirements 1.2 - 领域模块包含独立的repository子包
 */
@Repository
public interface UserModuleRepository extends JpaRepository<User, Long> {
    
    /**
     * 根据用户名查找用户
     */
    @QueryHints(@QueryHint(name = org.hibernate.annotations.QueryHints.CACHEABLE, value = "true"))
    @Cacheable(value = "users", key = "'user:' + #username", unless = "#result == null")
    Optional<User> findByUsername(String username);
    
    /**
     * 检查用户名是否存在
     */
    @QueryHints(@QueryHint(name = org.hibernate.annotations.QueryHints.CACHEABLE, value = "true"))
    Boolean existsByUsername(String username);
    
    /**
     * 检查邮箱是否存在
     */
    @QueryHints(@QueryHint(name = org.hibernate.annotations.QueryHints.CACHEABLE, value = "true"))
    Boolean existsByEmail(String email);
    
    /**
     * 根据ID查找用户（优化版本）
     */
    @Query("SELECT u FROM User u WHERE u.id = :id")
    @QueryHints(@QueryHint(name = org.hibernate.annotations.QueryHints.CACHEABLE, value = "true"))
    @Cacheable(value = "users", key = "#id")
    Optional<User> findByIdOptimized(@Param("id") Long id);
    
    /**
     * 根据邮箱查找用户
     */
    @Query("SELECT u FROM User u WHERE u.email = :email")
    @QueryHints(@QueryHint(name = org.hibernate.annotations.QueryHints.CACHEABLE, value = "true"))
    Optional<User> findByEmail(@Param("email") String email);
}
