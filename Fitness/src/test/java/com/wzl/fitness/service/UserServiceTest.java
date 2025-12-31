package com.wzl.fitness.service;

import com.wzl.fitness.entity.User;
import com.wzl.fitness.repository.UserRepository;
import com.wzl.fitness.service.impl.UserServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * UserService的单元测试类
 * 测试用户服务的业务逻辑
 */
@ExtendWith(MockitoExtension.class)
public class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private CacheService cacheService;

    @InjectMocks
    private UserServiceImpl userService;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");
        testUser.setEmail("test@example.com");
        testUser.setPassword("encodedPassword");
    }

    /**
     * 测试根据用户名查找用户
     */
    @Test
    void testFindByUsername() {
        // Given
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));

        // When
        Optional<User> result = userService.findByUsername("testuser");

        // Then
        assertTrue(result.isPresent());
        assertEquals("testuser", result.get().getUsername());
        verify(userRepository, times(1)).findByUsername("testuser");
    }

    /**
     * 测试根据用户名查找用户 - 用户不存在
     */
    @Test
    void testFindByUsernameNotFound() {
        // Given
        when(userRepository.findByUsername("nonexistent")).thenReturn(Optional.empty());

        // When
        Optional<User> result = userService.findByUsername("nonexistent");

        // Then
        assertFalse(result.isPresent());
        verify(userRepository, times(1)).findByUsername("nonexistent");
    }

    /**
     * 测试创建用户
     */
    @Test
    void testCreateUser() {
        // Given
        User newUser = new User();
        newUser.setUsername("newuser");
        newUser.setEmail("new@example.com");
        newUser.setPassword("plainPassword");

        when(passwordEncoder.encode("plainPassword")).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // When
        User result = userService.createUser(newUser);

        // Then
        assertNotNull(result);
        assertEquals("testuser", result.getUsername());
        verify(passwordEncoder, times(1)).encode("plainPassword");
        verify(userRepository, times(1)).save(any(User.class));
    }

    /**
     * 测试验证用户密码
     */
    @Test
    void testValidatePassword() {
        // Given
        when(passwordEncoder.matches("plainPassword", "encodedPassword")).thenReturn(true);

        // When
        boolean result = userService.validatePassword("plainPassword", "encodedPassword");

        // Then
        assertTrue(result);
        verify(passwordEncoder, times(1)).matches("plainPassword", "encodedPassword");
    }

    /**
     * 测试验证用户密码 - 密码错误
     */
    @Test
    void testValidatePasswordInvalid() {
        // Given
        when(passwordEncoder.matches("wrongPassword", "encodedPassword")).thenReturn(false);

        // When
        boolean result = userService.validatePassword("wrongPassword", "encodedPassword");

        // Then
        assertFalse(result);
        verify(passwordEncoder, times(1)).matches("wrongPassword", "encodedPassword");
    }

    /**
     * 测试检查用户名是否存在
     */
    @Test
    void testExistsByUsername() {
        // Given
        when(userRepository.existsByUsername("testuser")).thenReturn(true);

        // When
        boolean result = userService.existsByUsername("testuser");

        // Then
        assertTrue(result);
        verify(userRepository, times(1)).existsByUsername("testuser");
    }

    /**
     * 测试检查邮箱是否存在
     */
    @Test
    void testExistsByEmail() {
        // Given
        when(userRepository.existsByEmail("test@example.com")).thenReturn(true);

        // When
        boolean result = userService.existsByEmail("test@example.com");

        // Then
        assertTrue(result);
        verify(userRepository, times(1)).existsByEmail("test@example.com");
    }

    /**
     * 测试更新用户信息
     */
    @Test
    void testUpdateUser() {
        // Given
        User updatedUser = new User();
        updatedUser.setEmail("updated@example.com");
        
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // When
        Optional<User> result = userService.updateUser(1L, updatedUser);

        // Then
        assertTrue(result.isPresent());
        verify(userRepository, times(1)).findById(1L);
        verify(userRepository, times(1)).save(any(User.class));
    }

    /**
     * 测试更新用户信息 - 用户不存在
     */
    @Test
    void testUpdateUserNotFound() {
        // Given
        User updatedUser = new User();
        updatedUser.setEmail("updated@example.com");
        
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        // When
        Optional<User> result = userService.updateUser(999L, updatedUser);

        // Then
        assertFalse(result.isPresent());
        verify(userRepository, times(1)).findById(999L);
        verify(userRepository, never()).save(any(User.class));
    }

    /**
     * 测试删除用户
     */
    @Test
    void testDeleteUser() {
        // Given
        when(userRepository.existsById(1L)).thenReturn(true);

        // When
        boolean result = userService.deleteUser(1L);

        // Then
        assertTrue(result);
        verify(userRepository, times(1)).existsById(1L);
        verify(userRepository, times(1)).deleteById(1L);
    }

    /**
     * 测试删除用户 - 用户不存在
     */
    @Test
    void testDeleteUserNotFound() {
        // Given
        when(userRepository.existsById(999L)).thenReturn(false);

        // When
        boolean result = userService.deleteUser(999L);

        // Then
        assertFalse(result);
        verify(userRepository, times(1)).existsById(999L);
        verify(userRepository, never()).deleteById(999L);
    }
}