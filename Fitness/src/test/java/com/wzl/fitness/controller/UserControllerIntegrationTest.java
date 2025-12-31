package com.wzl.fitness.controller;

import com.wzl.fitness.config.TestSecurityConfig;
import com.wzl.fitness.config.TestDisableInterceptorConfig;
import com.wzl.fitness.config.WebConfig;
import com.wzl.fitness.entity.Role;
import com.wzl.fitness.entity.User;
import com.wzl.fitness.repository.UserRepository;
import com.wzl.fitness.service.UserService;
import com.wzl.fitness.service.AuthenticationService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.web.servlet.WebMvcAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.anyString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * UserController的集成测试类
 * 测试用户相关的API接口功能
 */
@SpringBootTest
@AutoConfigureMockMvc
@Import({TestSecurityConfig.class, TestDisableInterceptorConfig.class})
@ActiveProfiles("test")
class UserControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @MockBean
    private AuthenticationService authenticationService;

    /**
     * 测试获取用户列表API
     */
    @Test
    @WithMockUser(roles = {"ADMIN"})
    void testGetUserList() throws Exception {
        // 模拟管理员用户
        User adminUser = new User();
        adminUser.setId(1L);
        adminUser.setUsername("admin");
        adminUser.setRole(Role.ADMIN);
        
        Mockito.when(userService.findByUsername(anyString())).thenReturn(Optional.of(adminUser));
        Mockito.when(userService.getUserById(1L)).thenReturn(Optional.of(adminUser));
        
        MvcResult result = mockMvc.perform(get("/api/v1/users")
                .requestAttr("userId", 1L)
                .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").isArray())
                .andReturn();
        
        // 打印响应内容以便调试
        System.out.println("Response: " + result.getResponse().getContentAsString());
    }

    /**
     * 测试注册用户API
     */
    @Test
    void testRegisterUser() throws Exception {
        String userJson = "{\"username\":\"testuser" + System.currentTimeMillis() + "\",\"password\":\"Password123\",\"email\":\"test" + System.currentTimeMillis() + "@example.com\"}";
        
        // 不需要显式 mock void 方法，Mockito 默认不做任何事
        
        mockMvc.perform(post("/api/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(userJson))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").value("注册成功"));
    }

    /**
     * 测试用户登录API
     */
    @Test
    void testLoginUser() throws Exception {
        // 先注册一个用户
        long timestamp = System.currentTimeMillis();
        String username = "loginuser" + timestamp;
        String password = "Password123";
        String email = "login" + timestamp + "@example.com";
        
        // 模拟登录响应
        com.wzl.fitness.dto.response.LoginResponse loginResponse = new com.wzl.fitness.dto.response.LoginResponse();
        loginResponse.setAccessToken("mock-access-token");
        loginResponse.setRefreshToken("mock-refresh-token");
        loginResponse.setTokenType("Bearer");
        loginResponse.setUserId(1L);
        loginResponse.setUsername(username);
        
        Mockito.when(authenticationService.login(Mockito.any())).thenReturn(loginResponse);
        
        // 然后登录
        String loginJson = String.format("{\"username\":\"%s\",\"password\":\"%s\"}", username, password);
        
        mockMvc.perform(post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(loginJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200)) // 注意：根据 ApiResponse.success 可能是 code 200
                .andExpect(jsonPath("$.data.accessToken").value("mock-access-token"))
                .andExpect(jsonPath("$.data.refreshToken").value("mock-refresh-token"))
                .andExpect(jsonPath("$.data.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.data.userId").value(1))
                .andExpect(jsonPath("$.data.username").value(username))
                .andDo(print());
    }

    /**
     * 测试获取用户详情API
     */
    @Test
    @WithMockUser(roles = {"ADMIN"})
    void testGetUserDetails() throws Exception {
        // 模拟要查询的用户
        User targetUser = new User();
        targetUser.setId(1L);
        targetUser.setUsername("admin");
        targetUser.setRole(Role.ADMIN);
        targetUser.setEmail("admin@example.com");
        
        Mockito.when(userService.getUserById(1L)).thenReturn(Optional.of(targetUser));
        
        // 直接测试用户ID为1的用户
        try {
            MvcResult result = mockMvc.perform(get("/api/v1/users/1")
                    .requestAttr("userId", 1L)
                    .accept(MediaType.APPLICATION_JSON))
                    .andDo(print())
                    .andReturn();
            
            // 打印详细的响应信息
            System.out.println("=== 响应详细信息 ===");
            System.out.println("Status: " + result.getResponse().getStatus());
            System.out.println("Content: " + result.getResponse().getContentAsString());
            System.out.println("Headers: " + result.getResponse().getHeaderNames());
            
            // 检查状态码
            if (result.getResponse().getStatus() == 400) {
                System.out.println("收到400错误，响应内容: " + result.getResponse().getContentAsString());
            }
            
            // 如果状态码不是200，抛出异常显示详细信息
            if (result.getResponse().getStatus() != 200) {
                throw new RuntimeException("测试失败，状态码: " + result.getResponse().getStatus() + 
                    ", 响应内容: " + result.getResponse().getContentAsString());
            }
            
        } catch (Exception e) {
            System.out.println("测试异常: " + e.getMessage());
            throw e;
        }
    }
    
    private Long extractUserIdFromResponse(String response) {
        // 简单的JSON解析来获取用户ID
        // 假设响应格式为 {"code":200,"data":{"id":1,"username":"..."}}
        String idPattern = "\"id\":";
        int idIndex = response.indexOf(idPattern);
        if (idIndex != -1) {
            int startIndex = idIndex + idPattern.length();
            int endIndex = response.indexOf(",", startIndex);
            if (endIndex == -1) {
                endIndex = response.indexOf("}", startIndex);
            }
            String idStr = response.substring(startIndex, endIndex).trim();
            return Long.parseLong(idStr);
        }
        return 1L; // fallback
    }
}
