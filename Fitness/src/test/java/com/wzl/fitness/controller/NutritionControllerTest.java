package com.wzl.fitness.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.wzl.fitness.dto.request.NutritionRecordRequest;
import com.wzl.fitness.dto.response.NutritionRecordDTO;
import com.wzl.fitness.dto.response.NutritionStatsResponse;
import com.wzl.fitness.entity.NutritionRecord;
import com.wzl.fitness.entity.User;
import com.wzl.fitness.service.AuditLogService;
import com.wzl.fitness.service.NutritionService;
import com.wzl.fitness.util.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.hamcrest.Matchers.*;

/**
 * NutritionController的单元测试类
 * 测试营养记录控制器的API端点
 */
@ExtendWith(MockitoExtension.class)
public class NutritionControllerTest {

    @Mock
    private NutritionService nutritionService;

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private com.wzl.fitness.service.UserService userService;

    @Mock
    private AuditLogService auditLogService;

    @InjectMocks
    private NutritionController nutritionController;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;
    private User testUser;
    private NutritionRecordRequest testRecordRequest;
    private NutritionRecord testRecord;
    private NutritionRecordDTO testRecordDTO;
    private NutritionStatsResponse testStatsDTO;

    @BeforeEach
    void setUp() {
        // 设置MockMvc
        mockMvc = MockMvcBuilders.standaloneSetup(nutritionController).build();

        // 初始化ObjectMapper
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());

        // 创建测试数据
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");

        testRecordRequest = new NutritionRecordRequest();
        testRecordRequest.setFoodName("燕麦粥");
        testRecordRequest.setCalories(150.0);
        testRecordRequest.setProtein(5.0);
        testRecordRequest.setCarbs(27.0);
        testRecordRequest.setFat(3.0);
        testRecordRequest.setMealType("早餐");
        testRecordRequest.setRecordDate(LocalDate.now());
        testRecordRequest.setAmount(100.0);

        testRecord = new NutritionRecord();
        testRecord.setId(1L);
        testRecord.setUser(testUser);
        testRecord.setFoodName("燕麦粥");
        testRecord.setCalories(150.0);
        testRecord.setProtein(5.0);
        testRecord.setCarbs(27.0);
        testRecord.setFat(3.0);
        testRecord.setMealType("早餐");
        testRecord.setRecordDate(LocalDate.now());

        testRecordDTO = new NutritionRecordDTO();
        testRecordDTO.setId(1L);
        testRecordDTO.setFoodName("燕麦粥");
        testRecordDTO.setCalories(150.0);
        testRecordDTO.setProtein(5.0);
        testRecordDTO.setCarbs(27.0);
        testRecordDTO.setFat(3.0);
        testRecordDTO.setMealType("早餐");
        testRecordDTO.setRecordDate(LocalDate.now());

        testStatsDTO = new NutritionStatsResponse();
        testStatsDTO.setDate(LocalDate.now());
        testStatsDTO.setTotalCalories(2000.0);
        testStatsDTO.setTotalProtein(80.0);
        testStatsDTO.setTotalCarbs(250.0);
        testStatsDTO.setTotalFat(65.0);

        // 模拟UserService行为
        lenient().when(userService.getUserById(1L)).thenReturn(java.util.Optional.of(testUser));
    }

    @Test
    void testGetNutritionRecordsByDate() throws Exception {
        // Given
        List<NutritionRecordDTO> records = Arrays.asList(testRecordDTO);
        when(nutritionService.getNutritionRecordsByDate(any(User.class), any(LocalDate.class)))
            .thenReturn(records);

        // When & Then
        mockMvc.perform(get("/api/v1/nutrition/records/{date}", LocalDate.now())
                .requestAttr("userId", 1L)
                .header("Authorization", "Bearer token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data[0].foodName").value("燕麦粥"))
                .andExpect(jsonPath("$.data[0].calories").value(150.0));
        
        verify(nutritionService, times(1))
            .getNutritionRecordsByDate(any(User.class), eq(LocalDate.now()));
    }

    @Test
    void testAddNutritionRecord() throws Exception {
        // Given
        when(nutritionService.addNutritionRecord(any(User.class), any(NutritionRecordRequest.class)))
            .thenReturn(testRecord);

        // When & Then
        mockMvc.perform(post("/api/v1/nutrition/records")
                .requestAttr("userId", 1L)
                .header("Authorization", "Bearer token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testRecordRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.foodName").value("燕麦粥"))
                .andExpect(jsonPath("$.data.calories").value(150.0));
        
        verify(nutritionService, times(1))
            .addNutritionRecord(any(User.class), any(NutritionRecordRequest.class));
    }

    @Test
    void testUpdateNutritionRecord() throws Exception {
        // Given
        Long recordId = 1L;
        when(nutritionService.updateNutritionRecord(eq(recordId), any(NutritionRecordRequest.class)))
            .thenReturn(testRecord);

        // When & Then
        mockMvc.perform(put("/api/v1/nutrition/records/{id}", recordId)
                .requestAttr("userId", 1L)
                .header("Authorization", "Bearer token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testRecordRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.foodName").value("燕麦粥"))
                .andExpect(jsonPath("$.data.calories").value(150.0));
        
        verify(nutritionService, times(1))
            .updateNutritionRecord(eq(recordId), any(NutritionRecordRequest.class));
    }

    @Test
    void testDeleteNutritionRecord() throws Exception {
        // Given
        Long recordId = 1L;
        doNothing().when(nutritionService).deleteNutritionRecord(eq(recordId), any(User.class));

        // When & Then
        mockMvc.perform(delete("/api/v1/nutrition/records/{id}", recordId)
                .requestAttr("userId", 1L)
                .header("Authorization", "Bearer token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
        
        verify(nutritionService, times(1))
            .deleteNutritionRecord(eq(recordId), any(User.class));
    }

    @Test
    void testGetNutritionStatsByDate() throws Exception {
        // Given
        when(nutritionService.getNutritionStatsByDate(any(User.class), any(LocalDate.class)))
            .thenReturn(testStatsDTO);

        // When & Then
        mockMvc.perform(get("/api/v1/nutrition/stats/{date}", LocalDate.now())
                .requestAttr("userId", 1L)
                .header("Authorization", "Bearer token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.totalCalories").value(2000.0))
                .andExpect(jsonPath("$.data.totalProtein").value(80.0))
                .andExpect(jsonPath("$.data.totalCarbs").value(250.0))
                .andExpect(jsonPath("$.data.totalFat").value(65.0));
        
        verify(nutritionService, times(1))
            .getNutritionStatsByDate(any(User.class), eq(LocalDate.now()));
    }

    @Test
    void testGetNutritionStatsByDateRange() throws Exception {
        // Given
        List<NutritionStatsResponse> statsList = Arrays.asList(testStatsDTO);
        when(nutritionService.getNutritionStatsByDateRange(any(User.class), any(LocalDate.class), any(LocalDate.class)))
            .thenReturn(statsList);

        // When & Then
        mockMvc.perform(get("/api/v1/nutrition/stats/range")
                .requestAttr("userId", 1L)
                .header("Authorization", "Bearer token")
                .param("startDate", LocalDate.now().toString())
                .param("endDate", LocalDate.now().toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data[0].totalCalories").value(2000.0))
                .andExpect(jsonPath("$.data[0].totalProtein").value(80.0))
                .andExpect(jsonPath("$.data[0].totalCarbs").value(250.0))
                .andExpect(jsonPath("$.data[0].totalFat").value(65.0));
        
        verify(nutritionService, times(1))
            .getNutritionStatsByDateRange(any(User.class), eq(LocalDate.now()), eq(LocalDate.now()));
    }

    @Test
    void testGetNutritionAdvice() throws Exception {
        // Given
        List<String> adviceList = Arrays.asList("保持均衡饮食", "增加蔬菜摄入");
        when(nutritionService.getNutritionAdvice(any(User.class), any(LocalDate.class)))
            .thenReturn(adviceList);

        // When & Then
        mockMvc.perform(get("/api/v1/nutrition/advice")
                .requestAttr("userId", 1L)
                .header("Authorization", "Bearer token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data[0]").value("保持均衡饮食"))
                .andExpect(jsonPath("$.data[1]").value("增加蔬菜摄入"));
        
        verify(nutritionService, times(1))
            .getNutritionAdvice(any(User.class), any(LocalDate.class));
    }
}