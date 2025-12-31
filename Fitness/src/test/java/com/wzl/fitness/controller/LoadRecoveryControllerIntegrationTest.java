package com.wzl.fitness.controller;

import com.wzl.fitness.config.TestDisableInterceptorConfig;
import com.wzl.fitness.config.TestSecurityConfig;
import com.wzl.fitness.entity.User;
import com.wzl.fitness.service.LoadRecoveryService;
import com.wzl.fitness.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * LoadRecoveryController的集成测试类
 */
@SpringBootTest
@AutoConfigureMockMvc
@Import({TestSecurityConfig.class, TestDisableInterceptorConfig.class})
@ActiveProfiles("test")
class LoadRecoveryControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private LoadRecoveryService loadRecoveryService;

    @MockBean
    private UserService userService;

    private User testUser;
    private final Long userId = 1L;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(userId);
        testUser.setUsername("testuser");

        Mockito.when(userService.getUserById(userId)).thenReturn(Optional.of(testUser));
    }

    /**
     * 测试1RM计算API
     */
    @Test
    void testCalculateOneRepMax() throws Exception {
        Double weight = 100.0;
        Integer reps = 5;
        String model = "Epley";
        Double expectedRM = 116.67;

        Mockito.when(loadRecoveryService.calculateOneRepMax(eq(weight), eq(reps), eq(model)))
                .thenReturn(expectedRM);

        mockMvc.perform(get("/api/v1/load-recovery/one-rep-max")
                .param("weight", weight.toString())
                .param("reps", reps.toString())
                .param("model", model)
                .requestAttr("userId", userId)
                .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").value(expectedRM));
    }

    /**
     * 测试获取支持的1RM模型API
     */
    @Test
    void testGetSupportedModels() throws Exception {
        List<String> models = Arrays.asList("Epley", "Brzycki", "Lombardi", "OConner", "Mayhew");

        Mockito.when(loadRecoveryService.getSupportedOneRepMaxModels()).thenReturn(models);

        mockMvc.perform(get("/api/v1/load-recovery/one-rep-max/models")
                .requestAttr("userId", userId)
                .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data[0]").value("Epley"));
    }
}
