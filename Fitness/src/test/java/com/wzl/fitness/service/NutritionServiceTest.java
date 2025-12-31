package com.wzl.fitness.service;

import com.wzl.fitness.dto.request.NutritionRecordRequest;
import com.wzl.fitness.dto.response.NutritionRecordDTO;
import com.wzl.fitness.dto.response.NutritionStatsResponse;
import com.wzl.fitness.entity.NutritionRecord;
import com.wzl.fitness.entity.User;
import com.wzl.fitness.exception.ResourceNotFoundException;
import com.wzl.fitness.repository.NutritionRecordRepository;
import com.wzl.fitness.service.impl.NutritionServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * NutritionService的单元测试类
 * 测试营养记录服务的业务逻辑
 */
@ExtendWith(MockitoExtension.class)
public class NutritionServiceTest {

    @Mock
    private NutritionRecordRepository nutritionRecordRepository;

    @InjectMocks
    private NutritionServiceImpl nutritionService;

    private User testUser;
    private NutritionRecord testRecord;
    private NutritionRecordRequest testRecordDTO;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");
        testUser.setAge(25);
        testUser.setWeight(70.0);
        
        testRecord = new NutritionRecord();
        testRecord.setId(1L);
        testRecord.setUser(testUser);
        testRecord.setRecordDate(LocalDate.now());
        testRecord.setMealType("早餐");
        testRecord.setFoodName("燕麦粥");
        testRecord.setCalories(150.0);
        testRecord.setProtein(5.0);
        testRecord.setCarbs(27.0);
        testRecord.setFat(3.0);
        testRecord.setAmount(100.0);
        
        testRecordDTO = new NutritionRecordRequest();
        testRecordDTO.setRecordDate(LocalDate.now());
        testRecordDTO.setMealType("早餐");
        testRecordDTO.setFoodName("燕麦粥");
        testRecordDTO.setCalories(150.0);
        testRecordDTO.setProtein(5.0);
        testRecordDTO.setCarbs(27.0);
        testRecordDTO.setFat(3.0);
        testRecordDTO.setAmount(100.0);
    }

    /**
     * 测试获取指定日期的营养记录
     */
    @Test
    void testGetNutritionRecordsByDate() {
        // Given
        LocalDate date = LocalDate.now();
        List<NutritionRecord> records = Arrays.asList(testRecord);
        when(nutritionRecordRepository.findByUserAndRecordDateOrderByCreatedAt(testUser, date))
            .thenReturn(records);

        // When
        List<NutritionRecordDTO> result = nutritionService.getNutritionRecordsByDate(testUser, date);

        // Then
        assertEquals(1, result.size());
        assertEquals("燕麦粥", result.get(0).getFoodName());
        assertEquals(150.0, result.get(0).getCalories());
        verify(nutritionRecordRepository, times(1))
            .findByUserAndRecordDateOrderByCreatedAt(testUser, date);
    }

    /**
     * 测试添加营养记录
     */
    @Test
    void testAddNutritionRecord() {
        // Given
        NutritionRecord savedRecord = new NutritionRecord();
        savedRecord.setId(2L);
        savedRecord.setUser(testUser);
        savedRecord.setRecordDate(LocalDate.now());
        savedRecord.setMealType("早餐");
        savedRecord.setFoodName("燕麦粥");
        savedRecord.setCalories(150.0);
        savedRecord.setProtein(5.0);
        savedRecord.setCarbs(27.0);
        savedRecord.setFat(3.0);
        savedRecord.setAmount(100.0);
        
        when(nutritionRecordRepository.save(any(NutritionRecord.class))).thenReturn(savedRecord);

        // When
        NutritionRecord result = nutritionService.addNutritionRecord(testUser, testRecordDTO);

        // Then
        assertNotNull(result);
        assertEquals("燕麦粥", result.getFoodName());
        assertEquals(150.0, result.getCalories());
        verify(nutritionRecordRepository, times(1)).save(any(NutritionRecord.class));
    }

    /**
     * 测试添加营养记录 - 验证失败
     */
    @Test
    void testAddNutritionRecordValidationFailed() {
        // Given
        NutritionRecordRequest invalidRequest = new NutritionRecordRequest();
        // Missing required fields

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> {
            nutritionService.addNutritionRecord(testUser, invalidRequest);
        });
        
        verify(nutritionRecordRepository, never()).save(any(NutritionRecord.class));
    }

    /**
     * 测试更新营养记录
     */
    @Test
    void testUpdateNutritionRecord() {
        // Given
        NutritionRecordRequest updateDTO = new NutritionRecordRequest();
        updateDTO.setRecordDate(LocalDate.now());
        updateDTO.setMealType("晚餐");
        updateDTO.setFoodName("三文鱼");
        updateDTO.setCalories(300.0);
        updateDTO.setProtein(25.0);
        updateDTO.setCarbs(5.0);
        updateDTO.setFat(20.0);
        updateDTO.setAmount(150.0);
        
        NutritionRecord updatedRecord = new NutritionRecord();
        updatedRecord.setId(1L);
        updatedRecord.setUser(testUser);
        updatedRecord.setRecordDate(LocalDate.now());
        updatedRecord.setMealType("晚餐");
        updatedRecord.setFoodName("三文鱼");
        updatedRecord.setCalories(300.0);
        updatedRecord.setProtein(25.0);
        updatedRecord.setCarbs(5.0);
        updatedRecord.setFat(20.0);
        updatedRecord.setAmount(150.0);
        
        when(nutritionRecordRepository.findById(1L))
            .thenReturn(Optional.of(testRecord));
        when(nutritionRecordRepository.save(any(NutritionRecord.class)))
            .thenReturn(updatedRecord);

        // When
        NutritionRecord result = nutritionService.updateNutritionRecord(1L, updateDTO);

        // Then
        assertNotNull(result);
        assertEquals("三文鱼", result.getFoodName());
        assertEquals(300.0, result.getCalories());
        verify(nutritionRecordRepository, times(1)).findById(1L);
        verify(nutritionRecordRepository, times(1)).save(any(NutritionRecord.class));
    }

    /**
     * 测试更新营养记录 - 记录不存在
     */
    @Test
    void testUpdateNutritionRecordNotFound() {
        // Given
        NutritionRecordRequest updateDTO = new NutritionRecordRequest();
        updateDTO.setRecordDate(LocalDate.now());
        updateDTO.setMealType("晚餐");
        updateDTO.setFoodName("三文鱼");
        updateDTO.setCalories(300.0);
        updateDTO.setAmount(150.0);
        
        when(nutritionRecordRepository.findById(999L))
            .thenReturn(Optional.empty());

        // When & Then
        assertThrows(ResourceNotFoundException.class, () -> {
            nutritionService.updateNutritionRecord(999L, updateDTO);
        });
        
        verify(nutritionRecordRepository, times(1)).findById(999L);
        verify(nutritionRecordRepository, never()).save(any(NutritionRecord.class));
    }

    /**
     * 测试删除营养记录
     */
    @Test
    void testDeleteNutritionRecord() {
        // Given
        when(nutritionRecordRepository.findById(1L))
            .thenReturn(Optional.of(testRecord));
        doNothing().when(nutritionRecordRepository).delete(testRecord);

        // When
        nutritionService.deleteNutritionRecord(1L, testUser);

        // Then
        verify(nutritionRecordRepository, times(1)).findById(1L);
        verify(nutritionRecordRepository, times(1)).delete(testRecord);
    }

    /**
     * 测试删除营养记录 - 记录不存在
     */
    @Test
    void testDeleteNutritionRecordNotFound() {
        // Given
        when(nutritionRecordRepository.findById(999L))
            .thenReturn(Optional.empty());

        // When & Then
        assertThrows(ResourceNotFoundException.class, () -> {
            nutritionService.deleteNutritionRecord(999L, testUser);
        });
        
        verify(nutritionRecordRepository, times(1)).findById(999L);
        verify(nutritionRecordRepository, never()).delete(any(NutritionRecord.class));
    }

    /**
     * 测试获取指定日期的营养统计
     */
    @Test
    void testGetNutritionStatsByDate() {
        // Given
        LocalDate date = LocalDate.now();
        List<NutritionRecord> records = Arrays.asList(testRecord);
        
        when(nutritionRecordRepository.findByUserAndRecordDateOrderByCreatedAt(testUser, date))
            .thenReturn(records);

        // When
        NutritionStatsResponse result = nutritionService.getNutritionStatsByDate(testUser, date);

        // Then
        assertNotNull(result);
        assertEquals(date, result.getDate());
        assertEquals(150.0, result.getTotalCalories());
        assertEquals(5.0, result.getTotalProtein());
        assertEquals(27.0, result.getTotalCarbs());
        assertEquals(3.0, result.getTotalFat());
        verify(nutritionRecordRepository, times(1))
            .findByUserAndRecordDateOrderByCreatedAt(testUser, date);
    }

    /**
     * 测试获取日期范围的营养统计
     */
    @Test
    void testGetNutritionStatsByDateRange() {
        // Given
        LocalDate startDate = LocalDate.now().minusDays(1);
        LocalDate endDate = LocalDate.now();
        
        NutritionRecord record1 = new NutritionRecord();
        record1.setId(1L);
        record1.setUser(testUser);
        record1.setRecordDate(startDate);
        record1.setMealType("早餐");
        record1.setFoodName("燕麦粥");
        record1.setCalories(150.0);
        record1.setProtein(5.0);
        record1.setCarbs(27.0);
        record1.setFat(3.0);
        record1.setAmount(100.0);
        
        NutritionRecord record2 = new NutritionRecord();
        record2.setId(2L);
        record2.setUser(testUser);
        record2.setRecordDate(endDate);
        record2.setMealType("午餐");
        record2.setFoodName("鸡胸肉");
        record2.setCalories(200.0);
        record2.setProtein(30.0);
        record2.setCarbs(5.0);
        record2.setFat(8.0);
        record2.setAmount(150.0);
        
        when(nutritionRecordRepository.findByUserAndRecordDateOrderByCreatedAt(testUser, startDate))
            .thenReturn(Arrays.asList(record1));
        when(nutritionRecordRepository.findByUserAndRecordDateOrderByCreatedAt(testUser, endDate))
            .thenReturn(Arrays.asList(record2));

        // When
        List<NutritionStatsResponse> result = nutritionService.getNutritionStatsByDateRange(testUser, startDate, endDate);

        // Then
        assertEquals(2, result.size());
        assertEquals(startDate, result.get(0).getDate());
        assertEquals(150.0, result.get(0).getTotalCalories());
        assertEquals(endDate, result.get(1).getDate());
        assertEquals(200.0, result.get(1).getTotalCalories());
    }

    /**
     * 测试获取营养建议
     */
    @Test
    void testGetNutritionAdvice() {
        // Given
        LocalDate date = LocalDate.now();
        List<NutritionRecord> records = Arrays.asList(testRecord);
        
        when(nutritionRecordRepository.findByUserAndRecordDateOrderByCreatedAt(testUser, date))
            .thenReturn(records);

        // When
        var result = nutritionService.getNutritionAdvice(testUser, date);

        // Then
        assertNotNull(result);
        verify(nutritionRecordRepository, times(1))
            .findByUserAndRecordDateOrderByCreatedAt(testUser, date);
    }

    /**
     * 测试分页获取营养记录
     */
    @Test
    void testGetNutritionRecordsPaginated() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);
        Page<NutritionRecord> recordPage = new PageImpl<>(Arrays.asList(testRecord));
        
        when(nutritionRecordRepository.findByUserOrderByRecordDateDesc(testUser, pageable))
            .thenReturn(recordPage);

        // When
        Page<NutritionRecordDTO> result = nutritionService.getNutritionRecordsPaginated(testUser, pageable);

        // Then
        assertEquals(1, result.getContent().size());
        assertEquals("燕麦粥", result.getContent().get(0).getFoodName());
        verify(nutritionRecordRepository, times(1))
            .findByUserOrderByRecordDateDesc(testUser, pageable);
    }
}
