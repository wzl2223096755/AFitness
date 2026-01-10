package com.wzl.fitness.modules.user;

import com.wzl.fitness.entity.Role;
import com.wzl.fitness.entity.User;
import com.wzl.fitness.modules.user.api.UserModuleApi;
import com.wzl.fitness.modules.user.api.UserModuleApiImpl;
import com.wzl.fitness.modules.user.dto.UserBasicInfo;
import com.wzl.fitness.modules.user.dto.UserDTO;
import com.wzl.fitness.modules.user.service.UserModuleService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * 用户模块API单元测试
 * 
 * 测试UserModuleApi接口实现的正确性
 * 
 * **Validates: Requirements 2.1, 2.5, 8.1**
 * 
 * Feature: modular-architecture, Task 3.3
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("用户模块API测试")
class UserModuleApiTest {

    @Mock
    private UserModuleService userModuleService;
    
    private UserModuleApi userModuleApi;
    
    private User testUser;
    private UserDTO testUserDTO;
    private UserBasicInfo testBasicInfo;
    
    @BeforeEach
    void setUp() {
        userModuleApi = new UserModuleApiImpl(userModuleService);
        
        // 创建测试用户（使用Builder）
        testUser = User.builder()
                .id(1L)
                .username("testuser")
                .email("test@example.com")
                .role(Role.USER)
                .build();
        
        // 创建测试DTO
        testUserDTO = UserDTO.builder()
                .id(1L)
                .username("testuser")
                .nickname("Test User")
                .email("test@example.com")
                .role("USER")
                .enabled(true)
                .createdAt(LocalDateTime.now())
                .build();
        
        // 创建测试基础信息
        testBasicInfo = UserBasicInfo.builder()
                .id(1L)
                .username("testuser")
                .nickname("Test User")
                .avatar(null)
                .build();
    }
    
    @Test
    @DisplayName("getUserById - 用户存在时返回UserDTO")
    void getUserById_WhenUserExists_ReturnsUserDTO() {
        // Given
        when(userModuleService.getUserById(1L)).thenReturn(Optional.of(testUser));
        when(userModuleService.convertToDTO(testUser)).thenReturn(testUserDTO);
        
        // When
        UserDTO result = userModuleApi.getUserById(1L);
        
        // Then
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("testuser", result.getUsername());
        verify(userModuleService).getUserById(1L);
        verify(userModuleService).convertToDTO(testUser);
    }
    
    @Test
    @DisplayName("getUserById - 用户不存在时返回null")
    void getUserById_WhenUserNotExists_ReturnsNull() {
        // Given
        when(userModuleService.getUserById(999L)).thenReturn(Optional.empty());
        
        // When
        UserDTO result = userModuleApi.getUserById(999L);
        
        // Then
        assertNull(result);
        verify(userModuleService).getUserById(999L);
        verify(userModuleService, never()).convertToDTO(any());
    }
    
    @Test
    @DisplayName("getUserByUsername - 用户存在时返回UserDTO")
    void getUserByUsername_WhenUserExists_ReturnsUserDTO() {
        // Given
        when(userModuleService.getUserByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(userModuleService.convertToDTO(testUser)).thenReturn(testUserDTO);
        
        // When
        UserDTO result = userModuleApi.getUserByUsername("testuser");
        
        // Then
        assertNotNull(result);
        assertEquals("testuser", result.getUsername());
        verify(userModuleService).getUserByUsername("testuser");
    }
    
    @Test
    @DisplayName("getUserByUsername - 用户不存在时返回null")
    void getUserByUsername_WhenUserNotExists_ReturnsNull() {
        // Given
        when(userModuleService.getUserByUsername("nonexistent")).thenReturn(Optional.empty());
        
        // When
        UserDTO result = userModuleApi.getUserByUsername("nonexistent");
        
        // Then
        assertNull(result);
    }
    
    @Test
    @DisplayName("existsById - 用户存在时返回true")
    void existsById_WhenUserExists_ReturnsTrue() {
        // Given
        when(userModuleService.existsById(1L)).thenReturn(true);
        
        // When
        boolean result = userModuleApi.existsById(1L);
        
        // Then
        assertTrue(result);
        verify(userModuleService).existsById(1L);
    }
    
    @Test
    @DisplayName("existsById - 用户不存在时返回false")
    void existsById_WhenUserNotExists_ReturnsFalse() {
        // Given
        when(userModuleService.existsById(999L)).thenReturn(false);
        
        // When
        boolean result = userModuleApi.existsById(999L);
        
        // Then
        assertFalse(result);
    }
    
    @Test
    @DisplayName("getBasicInfo - 用户存在时返回UserBasicInfo")
    void getBasicInfo_WhenUserExists_ReturnsBasicInfo() {
        // Given
        when(userModuleService.getUserById(1L)).thenReturn(Optional.of(testUser));
        when(userModuleService.convertToBasicInfo(testUser)).thenReturn(testBasicInfo);
        
        // When
        UserBasicInfo result = userModuleApi.getBasicInfo(1L);
        
        // Then
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("testuser", result.getUsername());
        assertEquals("Test User", result.getNickname());
        verify(userModuleService).convertToBasicInfo(testUser);
    }
    
    @Test
    @DisplayName("getBasicInfo - 用户不存在时返回null")
    void getBasicInfo_WhenUserNotExists_ReturnsNull() {
        // Given
        when(userModuleService.getUserById(999L)).thenReturn(Optional.empty());
        
        // When
        UserBasicInfo result = userModuleApi.getBasicInfo(999L);
        
        // Then
        assertNull(result);
        verify(userModuleService, never()).convertToBasicInfo(any());
    }
    
    @Test
    @DisplayName("API返回的是DTO而非Entity")
    void apiReturnsDTO_NotEntity() {
        // Given
        when(userModuleService.getUserById(1L)).thenReturn(Optional.of(testUser));
        when(userModuleService.convertToDTO(testUser)).thenReturn(testUserDTO);
        
        // When
        UserDTO result = userModuleApi.getUserById(1L);
        
        // Then
        assertNotNull(result);
        // 验证返回类型是DTO
        assertTrue(result instanceof UserDTO);
        // 验证DTO包含必要字段
        assertNotNull(result.getId());
        assertNotNull(result.getUsername());
    }
}
