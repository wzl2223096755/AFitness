package com.wzl.fitness.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.wzl.fitness.dto.request.LoginRequest;
import com.wzl.fitness.dto.request.RegisterRequest;
import com.wzl.fitness.dto.response.LoginResponse;
import com.wzl.fitness.service.AuditLogService;
import com.wzl.fitness.service.AuthenticationService;
import com.wzl.fitness.service.JwtRefreshService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * AuthController集成测试
 * 
 * 测试认证相关API端点：登录、注册、令牌刷新
 * 
 * Requirements: 8.2, 8.3
 */
@ExtendWith(MockitoExtension.class)
public class AuthControllerIntegrationTest {

    @Mock
    private AuthenticationService authenticationService;

    @Mock
    private JwtRefreshService jwtRefreshService;

    @Mock
    private AuditLogService auditLogService;

    @InjectMocks
    private AuthController authController;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(authController).build();
        objectMapper = new ObjectMapper();
    }

    @Nested
    @DisplayName("登录API测试")
    class LoginTests {

        @Test
        @DisplayName("有效凭证登录成功")
        void testLoginWithValidCredentials() throws Exception {
            // Given
            LoginRequest request = new LoginRequest();
            request.setUsername("testuser");
            request.setPassword("password123");

            LoginResponse response = LoginResponse.builder()
                    .accessToken("test-access-token")
                    .refreshToken("test-refresh-token")
                    .tokenType("Bearer")
                    .expiresIn(86400L)
                    .build();

            when(authenticationService.login(any(LoginRequest.class))).thenReturn(response);

            // When & Then
            mockMvc.perform(post("/api/v1/auth/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data.accessToken").value("test-access-token"))
                    .andExpect(jsonPath("$.data.refreshToken").value("test-refresh-token"))
                    .andExpect(jsonPath("$.data.tokenType").value("Bearer"));

            verify(authenticationService, times(1)).login(any(LoginRequest.class));
        }

        @Test
        @DisplayName("无效凭证登录失败")
        void testLoginWithInvalidCredentials() throws Exception {
            // Given
            LoginRequest request = new LoginRequest();
            request.setUsername("testuser");
            request.setPassword("wrongpassword");

            when(authenticationService.login(any(LoginRequest.class)))
                    .thenThrow(new RuntimeException("用户名或密码错误"));

            // When & Then - 异常会被抛出，导致500错误
            try {
                mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                        .andExpect(status().is5xxServerError());
            } catch (Exception e) {
                // 预期会抛出异常
                assertTrue(e.getCause().getMessage().contains("用户名或密码错误"));
            }

            verify(authenticationService, times(1)).login(any(LoginRequest.class));
        }
    }

    @Nested
    @DisplayName("注册API测试")
    class RegisterTests {

        @Test
        @DisplayName("有效信息注册成功")
        void testRegisterWithValidInfo() throws Exception {
            // Given - 密码需要包含大写、小写和数字
            RegisterRequest request = new RegisterRequest();
            request.setUsername("newuser");
            request.setPassword("Password123");  // 符合密码规则
            request.setEmail("newuser@example.com");

            doNothing().when(authenticationService).register(any(RegisterRequest.class));

            // When & Then
            mockMvc.perform(post("/api/v1/auth/register")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data").value("注册成功"));

            verify(authenticationService, times(1)).register(any(RegisterRequest.class));
        }

        @Test
        @DisplayName("用户名已存在注册失败")
        void testRegisterWithExistingUsername() throws Exception {
            // Given - 密码需要包含大写、小写和数字
            RegisterRequest request = new RegisterRequest();
            request.setUsername("existinguser");
            request.setPassword("Password123");  // 符合密码规则
            request.setEmail("new@example.com");

            doThrow(new RuntimeException("用户名已存在"))
                    .when(authenticationService).register(any(RegisterRequest.class));

            // When & Then - 异常会被抛出
            try {
                mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                        .andExpect(status().is5xxServerError());
            } catch (Exception e) {
                // 预期会抛出异常
                assertTrue(e.getCause().getMessage().contains("用户名已存在"));
            }

            verify(authenticationService, times(1)).register(any(RegisterRequest.class));
        }
    }

    @Nested
    @DisplayName("用户名检查API测试")
    class CheckUsernameTests {

        @Test
        @DisplayName("检查用户名存在")
        void testCheckUsernameExists() throws Exception {
            // Given
            when(authenticationService.checkUsernameExists("existinguser")).thenReturn(true);

            // When & Then
            mockMvc.perform(get("/api/v1/auth/check-username")
                    .param("username", "existinguser"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data").value(true));

            verify(authenticationService, times(1)).checkUsernameExists("existinguser");
        }

        @Test
        @DisplayName("检查用户名不存在")
        void testCheckUsernameNotExists() throws Exception {
            // Given
            when(authenticationService.checkUsernameExists("newuser")).thenReturn(false);

            // When & Then
            mockMvc.perform(get("/api/v1/auth/check-username")
                    .param("username", "newuser"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data").value(false));

            verify(authenticationService, times(1)).checkUsernameExists("newuser");
        }
    }

    @Nested
    @DisplayName("邮箱检查API测试")
    class CheckEmailTests {

        @Test
        @DisplayName("检查邮箱存在")
        void testCheckEmailExists() throws Exception {
            // Given
            when(authenticationService.checkEmailExists("existing@example.com")).thenReturn(true);

            // When & Then
            mockMvc.perform(get("/api/v1/auth/check-email")
                    .param("email", "existing@example.com"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data").value(true));

            verify(authenticationService, times(1)).checkEmailExists("existing@example.com");
        }

        @Test
        @DisplayName("检查邮箱不存在")
        void testCheckEmailNotExists() throws Exception {
            // Given
            when(authenticationService.checkEmailExists("new@example.com")).thenReturn(false);

            // When & Then
            mockMvc.perform(get("/api/v1/auth/check-email")
                    .param("email", "new@example.com"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data").value(false));

            verify(authenticationService, times(1)).checkEmailExists("new@example.com");
        }
    }

    @Nested
    @DisplayName("登出API测试")
    class LogoutTests {

        @Test
        @DisplayName("登出成功")
        void testLogoutSuccess() throws Exception {
            // Given
            String token = "test-token";
            String username = "testuser";
            when(jwtRefreshService.getUsernameFromToken(token)).thenReturn(username);
            doNothing().when(jwtRefreshService).blacklistToken(token);
            doNothing().when(auditLogService).logLogout(any(), anyString());

            // When & Then
            mockMvc.perform(post("/api/v1/auth/logout")
                    .header("Authorization", "Bearer " + token))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data").value("登出成功"));

            verify(jwtRefreshService, times(1)).blacklistToken(token);
        }
    }
}
