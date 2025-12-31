package com.wzl.fitness.repository;

import com.wzl.fitness.entity.User;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.QueryHint;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    
    @QueryHints(@QueryHint(name = org.hibernate.annotations.QueryHints.CACHEABLE, value = "true"))
    @Cacheable(value = "users", key = "'user:' + #username", unless = "#result == null")
    Optional<User> findByUsername(String username);
    
    @QueryHints(@QueryHint(name = org.hibernate.annotations.QueryHints.CACHEABLE, value = "true"))
    Boolean existsByUsername(String username);
    
    @QueryHints(@QueryHint(name = org.hibernate.annotations.QueryHints.CACHEABLE, value = "true"))
    Boolean existsByEmail(String email);
    
    @Query("SELECT u FROM User u WHERE u.id = :id")
    @QueryHints(@QueryHint(name = org.hibernate.annotations.QueryHints.CACHEABLE, value = "true"))
    @Cacheable(value = "users", key = "#id")
    Optional<User> findByIdOptimized(@Param("id") Long id);
    
    @Query("SELECT u FROM User u WHERE u.email = :email")
    @QueryHints(@QueryHint(name = org.hibernate.annotations.QueryHints.CACHEABLE, value = "true"))
    Optional<User> findByEmail(@Param("email") String email);
    
    @Query("SELECT u.id, u.username, u.email, u.role FROM User u WHERE u.id IN :ids")
    List<Object[]> findUserBasicInfoByIds(@Param("ids") List<Long> ids);
    
    @Query("SELECT u FROM User u WHERE u.createdAt BETWEEN :startDate AND :endDate")
    @QueryHints(@QueryHint(name = org.hibernate.annotations.QueryHints.CACHEABLE, value = "true"))
    List<User> findByCreatedAtBetween(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);
    
    @Query("SELECT u FROM User u WHERE u.role = :role")
    @QueryHints(@QueryHint(name = org.hibernate.annotations.QueryHints.CACHEABLE, value = "true"))
    Page<User> findByRole(@Param("role") com.wzl.fitness.entity.Role role, Pageable pageable);
    
    @Query("SELECT COUNT(u) FROM User u WHERE u.role = :role")
    @QueryHints(@QueryHint(name = org.hibernate.annotations.QueryHints.CACHEABLE, value = "true"))
    Long countByRole(@Param("role") com.wzl.fitness.entity.Role role);
    
    @Query("SELECT u FROM User u WHERE u.username LIKE %:keyword% OR u.email LIKE %:keyword%")
    @QueryHints(@QueryHint(name = org.hibernate.annotations.QueryHints.CACHEABLE, value = "true"))
    Page<User> findByUsernameOrEmailContaining(@Param("keyword") String keyword, Pageable pageable);
    
    @Modifying
    @Transactional
    @Query("UPDATE User u SET u.lastLoginAt = :loginTime WHERE u.id = :userId")
    void updateLastLoginAt(@Param("userId") Long userId, @Param("loginTime") LocalDateTime loginTime);
    
    @Query("SELECT u FROM User u WHERE u.id IN :userIds")
    @QueryHints(@QueryHint(name = org.hibernate.annotations.QueryHints.CACHEABLE, value = "true"))
    List<User> findByIdIn(@Param("userIds") List<Long> userIds);
    
    @Query("SELECT DISTINCT u FROM User u LEFT JOIN FETCH u.trainingRecords WHERE u.id = :userId")
    Optional<User> findWithTrainingRecordsById(@Param("userId") Long userId);
    
    @Query("SELECT DISTINCT u FROM User u WHERE u.id = :userId")
    Optional<User> findWithDevicesById(@Param("userId") Long userId);
    
    @Query("SELECT u FROM User u WHERE u.email = :email")
    @QueryHints(@QueryHint(name = org.hibernate.annotations.QueryHints.CACHEABLE, value = "true"))
    boolean existsByEmailAndActive(@Param("email") String email);
    
    @Query("SELECT u FROM User u WHERE u.username = :username")
    @QueryHints(@QueryHint(name = org.hibernate.annotations.QueryHints.CACHEABLE, value = "true"))
    boolean existsByUsernameAndActive(@Param("username") String username);
    
    /**
     * 分页查询用户
     */
    @Query("SELECT u FROM User u WHERE u.username LIKE %:keyword% OR u.email LIKE %:keyword%")
    @QueryHints(@QueryHint(name = org.hibernate.annotations.QueryHints.CACHEABLE, value = "true"))
    Page<User> findByKeyword(@Param("keyword") String keyword, Pageable pageable);
    
    /**
     * 查询指定时间后创建的用户
     */
    @Query("SELECT u FROM User u WHERE u.createdAt > :date")
    @QueryHints(@QueryHint(name = org.hibernate.annotations.QueryHints.CACHEABLE, value = "true"))
    List<User> findUsersCreatedAfter(@Param("date") LocalDateTime date);
    
    /**
     * 查询指定时间后更新的用户
     */
    @Query("SELECT u FROM User u WHERE u.updatedAt > :date")
    @QueryHints(@QueryHint(name = org.hibernate.annotations.QueryHints.CACHEABLE, value = "true"))
    List<User> findUsersUpdatedAfter(@Param("date") LocalDateTime date);
    
    /**
     * 统计用户数量
     */
    @Query("SELECT COUNT(u) FROM User u WHERE u.role = :roleStr")
    Long countByRoleStr(@Param("roleStr") String roleStr);
    
    /**
     * 批量更新用户角色
     */
    @Modifying
    @Transactional
    @Query("UPDATE User u SET u.role = :role WHERE u.id IN :ids")
    int updateUserRoleByIds(@Param("ids") List<Long> ids, @Param("role") String role);
    
    /**
     * 批量删除用户
     */
    @Modifying
    @Transactional
    @Query("DELETE FROM User u WHERE u.id IN :ids")
    int deleteUsersByIds(@Param("ids") List<Long> ids);
    
    /**
     * 查询用户基本信息（不包括敏感信息）
     */
    @Query("SELECT u.id, u.username, u.email, u.role, u.createdAt FROM User u WHERE u.id = :id")
    Object[] findUserPublicInfoById(@Param("id") Long id);
    
    /**
     * 检查用户名和邮箱是否已存在（用于注册验证）
     */
    @Query("SELECT CASE WHEN COUNT(u) > 0 THEN true ELSE false END FROM User u WHERE u.username = :username OR u.email = :email")
    boolean existsByUsernameOrEmail(@Param("username") String username, @Param("email") String email);
    
    /**
     * 获取最近注册的用户
     */
    @Query("SELECT u FROM User u ORDER BY u.createdAt DESC")
    @QueryHints(@QueryHint(name = org.hibernate.annotations.QueryHints.CACHEABLE, value = "true"))
    List<User> findRecentUsers(Pageable pageable);
}
