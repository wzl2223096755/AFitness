package com.wzl.fitness.service;

import com.wzl.fitness.entity.User;
import com.wzl.fitness.entity.Role;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface UserService {
    // 基本CRUD操作
    User createUser(User user);
    Optional<User> getUserById(Long id);
    Optional<User> findByUsername(String username);
    List<User> getAllUsers();
    Optional<User> updateUser(Long id, User userDetails);
    boolean deleteUser(Long id);
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
    boolean validatePassword(String rawPassword, String encodedPassword);
    
    // 分页查询操作
    Page<User> getUsersByRole(Role role, Pageable pageable);
    Page<User> searchUsers(String keyword, Pageable pageable);
    Page<User> getAllUsersPaginated(Pageable pageable);
    
    // 批量操作
    void updateLastLoginTimes(Map<Long, LocalDateTime> userLoginTimes);
    List<User> findUsersByIds(List<Long> userIds);
    Map<Long, User> getUsersByIds(List<Long> userIds);
    
    // 关联查询操作
    Optional<User> findUserWithDevices(Long userId);
    Optional<User> findUserWithTrainingRecords(Long userId);
    
    // 缓存操作
    void evictUserCache(Long userId);
    void evictUserCache(String username);
    void evictAllUserCache();
}