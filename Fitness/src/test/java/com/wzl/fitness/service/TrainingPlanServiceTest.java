package com.wzl.fitness.service;

import com.wzl.fitness.dto.request.TrainingPlanRequestDTO;
import com.wzl.fitness.entity.TrainingPlan;
import com.wzl.fitness.entity.TrainingPlanDay;
import com.wzl.fitness.entity.TrainingPlanExercise;
import com.wzl.fitness.entity.User;
import com.wzl.fitness.exception.BusinessException;
import com.wzl.fitness.repository.TrainingPlanDayRepository;
import com.wzl.fitness.repository.TrainingPlanExerciseRepository;
import com.wzl.fitness.repository.TrainingPlanRepository;
import com.wzl.fitness.service.impl.TrainingPlanServiceImpl;
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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * TrainingPlanService 单元测试类
 * 测试训练计划服务的业务逻辑
 */
@ExtendWith(MockitoExtension.class)
public class TrainingPlanServiceTest {

    @Mock
    private TrainingPlanRepository trainingPlanRepository;

    @Mock
    private TrainingPlanDayRepository trainingPlanDayRepository;

    @Mock
    private TrainingPlanExerciseRepository trainingPlanExerciseRepository;

    @InjectMocks
    private TrainingPlanServiceImpl trainingPlanService;

    private User testUser;
    private TrainingPlan testPlan;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");

        testPlan = TrainingPlan.builder()
                .id(1L)
                .user(testUser)
                .name("增肌计划")
                .goal("增肌")
                .level("中级")
                .durationWeeks(8)
                .daysPerWeek(4)
                .status("ACTIVE")
                .startDate(LocalDate.now())
                .endDate(LocalDate.now().plusWeeks(8))
                .isWeekly(true)
                .build();
        testPlan.setDays(new ArrayList<>());
    }

    /**
     * 测试创建训练计划
     */
    @Test
    void testCreatePlan() {
        when(trainingPlanRepository.save(any(TrainingPlan.class))).thenReturn(testPlan);

        TrainingPlan result = trainingPlanService.createPlan(testPlan);

        assertNotNull(result);
        assertEquals("增肌计划", result.getName());
        verify(trainingPlanRepository, times(1)).save(any(TrainingPlan.class));
    }

    /**
     * 测试根据ID获取训练计划
     */
    @Test
    void testGetPlanById() {
        when(trainingPlanRepository.findById(1L)).thenReturn(Optional.of(testPlan));

        Optional<TrainingPlan> result = trainingPlanService.getPlanById(1L);

        assertTrue(result.isPresent());
        assertEquals("增肌计划", result.get().getName());
        verify(trainingPlanRepository, times(1)).findById(1L);
    }

    /**
     * 测试根据ID获取训练计划 - 计划不存在
     */
    @Test
    void testGetPlanByIdNotFound() {
        when(trainingPlanRepository.findById(999L)).thenReturn(Optional.empty());

        Optional<TrainingPlan> result = trainingPlanService.getPlanById(999L);

        assertFalse(result.isPresent());
        verify(trainingPlanRepository, times(1)).findById(999L);
    }

    /**
     * 测试获取用户的所有训练计划
     */
    @Test
    void testGetPlansByUser() {
        List<TrainingPlan> plans = Arrays.asList(testPlan);
        when(trainingPlanRepository.findByUserOrderByCreatedAtDesc(testUser)).thenReturn(plans);

        List<TrainingPlan> result = trainingPlanService.getPlansByUser(testUser);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("增肌计划", result.get(0).getName());
        verify(trainingPlanRepository, times(1)).findByUserOrderByCreatedAtDesc(testUser);
    }

    /**
     * 测试分页获取用户的训练计划
     */
    @Test
    void testGetPlansByUserPaged() {
        List<TrainingPlan> plans = Arrays.asList(testPlan);
        Pageable pageable = PageRequest.of(0, 10);
        Page<TrainingPlan> page = new PageImpl<>(plans, pageable, plans.size());
        
        when(trainingPlanRepository.findByUser(testUser, pageable)).thenReturn(page);

        Page<TrainingPlan> result = trainingPlanService.getPlansByUser(testUser, pageable);

        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        verify(trainingPlanRepository, times(1)).findByUser(testUser, pageable);
    }

    /**
     * 测试根据状态获取训练计划
     */
    @Test
    void testGetPlansByStatus() {
        List<TrainingPlan> plans = Arrays.asList(testPlan);
        when(trainingPlanRepository.findByUserAndStatus(testUser, "ACTIVE")).thenReturn(plans);

        List<TrainingPlan> result = trainingPlanService.getPlansByStatus(testUser, "ACTIVE");

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("ACTIVE", result.get(0).getStatus());
        verify(trainingPlanRepository, times(1)).findByUserAndStatus(testUser, "ACTIVE");
    }

    /**
     * 测试删除训练计划
     */
    @Test
    void testDeletePlan() {
        doNothing().when(trainingPlanRepository).deleteById(1L);

        trainingPlanService.deletePlan(1L);

        verify(trainingPlanRepository, times(1)).deleteById(1L);
    }

    /**
     * 测试更新训练计划
     */
    @Test
    void testUpdatePlan() {
        TrainingPlan updatedPlan = TrainingPlan.builder()
                .name("更新后的计划")
                .description("新描述")
                .status("PAUSED")
                .startDate(LocalDate.now())
                .endDate(LocalDate.now().plusWeeks(12))
                .isWeekly(true)
                .build();
        updatedPlan.setDays(new ArrayList<>());

        when(trainingPlanRepository.findById(1L)).thenReturn(Optional.of(testPlan));
        when(trainingPlanRepository.save(any(TrainingPlan.class))).thenReturn(testPlan);

        TrainingPlan result = trainingPlanService.updatePlan(1L, updatedPlan);

        assertNotNull(result);
        verify(trainingPlanRepository, times(1)).findById(1L);
        verify(trainingPlanRepository, times(1)).save(any(TrainingPlan.class));
    }

    /**
     * 测试更新训练计划 - 计划不存在
     */
    @Test
    void testUpdatePlanNotFound() {
        TrainingPlan updatedPlan = TrainingPlan.builder().name("更新后的计划").build();
        when(trainingPlanRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(BusinessException.class, () -> {
            trainingPlanService.updatePlan(999L, updatedPlan);
        });

        verify(trainingPlanRepository, times(1)).findById(999L);
        verify(trainingPlanRepository, never()).save(any(TrainingPlan.class));
    }

    /**
     * 测试激活训练计划
     */
    @Test
    void testActivatePlan() {
        TrainingPlan anotherPlan = TrainingPlan.builder()
                .id(2L)
                .user(testUser)
                .name("另一个计划")
                .status("ACTIVE")
                .build();
        
        when(trainingPlanRepository.findById(1L)).thenReturn(Optional.of(testPlan));
        when(trainingPlanRepository.findByUserAndStatus(testUser, "ACTIVE"))
                .thenReturn(Arrays.asList(anotherPlan));
        when(trainingPlanRepository.save(any(TrainingPlan.class))).thenReturn(testPlan);

        TrainingPlan result = trainingPlanService.activatePlan(1L, testUser);

        assertNotNull(result);
        verify(trainingPlanRepository, times(1)).findById(1L);
        verify(trainingPlanRepository, times(1)).findByUserAndStatus(testUser, "ACTIVE");
    }

    /**
     * 测试激活训练计划 - 无权操作
     */
    @Test
    void testActivatePlanUnauthorized() {
        User anotherUser = new User();
        anotherUser.setId(2L);
        
        when(trainingPlanRepository.findById(1L)).thenReturn(Optional.of(testPlan));

        assertThrows(BusinessException.class, () -> {
            trainingPlanService.activatePlan(1L, anotherUser);
        });
    }

    /**
     * 测试暂停训练计划
     */
    @Test
    void testPausePlan() {
        when(trainingPlanRepository.findById(1L)).thenReturn(Optional.of(testPlan));
        when(trainingPlanRepository.save(any(TrainingPlan.class))).thenReturn(testPlan);

        TrainingPlan result = trainingPlanService.pausePlan(1L, testUser);

        assertNotNull(result);
        verify(trainingPlanRepository, times(1)).findById(1L);
        verify(trainingPlanRepository, times(1)).save(any(TrainingPlan.class));
    }

    /**
     * 测试完成训练计划
     */
    @Test
    void testCompletePlan() {
        when(trainingPlanRepository.findById(1L)).thenReturn(Optional.of(testPlan));
        when(trainingPlanRepository.save(any(TrainingPlan.class))).thenReturn(testPlan);

        TrainingPlan result = trainingPlanService.completePlan(1L, testUser);

        assertNotNull(result);
        verify(trainingPlanRepository, times(1)).findById(1L);
        verify(trainingPlanRepository, times(1)).save(any(TrainingPlan.class));
    }

    /**
     * 测试获取激活的训练计划
     */
    @Test
    void testGetActivePlan() {
        when(trainingPlanRepository.findByUserAndStatus(testUser, "ACTIVE"))
                .thenReturn(Arrays.asList(testPlan));

        Optional<TrainingPlan> result = trainingPlanService.getActivePlan(testUser);

        assertTrue(result.isPresent());
        assertEquals("增肌计划", result.get().getName());
    }

    /**
     * 测试获取激活的训练计划 - 无激活计划
     */
    @Test
    void testGetActivePlanEmpty() {
        when(trainingPlanRepository.findByUserAndStatus(testUser, "ACTIVE"))
                .thenReturn(new ArrayList<>());

        Optional<TrainingPlan> result = trainingPlanService.getActivePlan(testUser);

        assertFalse(result.isPresent());
    }

    /**
     * 测试切换动作完成状态
     */
    @Test
    void testToggleExerciseCompletion() {
        TrainingPlanExercise exercise = TrainingPlanExercise.builder()
                .id(1L)
                .name("卧推")
                .completed(false)
                .build();
        
        when(trainingPlanExerciseRepository.findById(1L)).thenReturn(Optional.of(exercise));
        when(trainingPlanExerciseRepository.save(any(TrainingPlanExercise.class))).thenReturn(exercise);

        trainingPlanService.toggleExerciseCompletion(1L);

        verify(trainingPlanExerciseRepository, times(1)).findById(1L);
        verify(trainingPlanExerciseRepository, times(1)).save(any(TrainingPlanExercise.class));
    }

    /**
     * 测试切换动作完成状态 - 动作不存在
     */
    @Test
    void testToggleExerciseCompletionNotFound() {
        when(trainingPlanExerciseRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(BusinessException.class, () -> {
            trainingPlanService.toggleExerciseCompletion(999L);
        });
    }

    /**
     * 测试根据日期范围获取训练计划
     */
    @Test
    void testGetPlansByDateRange() {
        LocalDate startDate = LocalDate.now().minusDays(30);
        LocalDate endDate = LocalDate.now();
        List<TrainingPlan> plans = Arrays.asList(testPlan);
        
        when(trainingPlanRepository.findByUserAndStartDateBetween(testUser, startDate, endDate))
                .thenReturn(plans);

        List<TrainingPlan> result = trainingPlanService.getPlansByDateRange(testUser, startDate, endDate);

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(trainingPlanRepository, times(1)).findByUserAndStartDateBetween(testUser, startDate, endDate);
    }

    /**
     * 测试保存周计划
     */
    @Test
    void testSaveWeeklyPlan() {
        when(trainingPlanRepository.save(any(TrainingPlan.class))).thenReturn(testPlan);

        TrainingPlan result = trainingPlanService.saveWeeklyPlan(testUser, testPlan);

        assertNotNull(result);
        verify(trainingPlanRepository, times(1)).save(any(TrainingPlan.class));
    }

    /**
     * 测试复制训练计划
     */
    @Test
    void testCopyPlan() {
        when(trainingPlanRepository.findById(1L)).thenReturn(Optional.of(testPlan));
        when(trainingPlanRepository.save(any(TrainingPlan.class))).thenReturn(testPlan);

        TrainingPlan result = trainingPlanService.copyPlan(1L, testUser, "复制的计划");

        assertNotNull(result);
        verify(trainingPlanRepository, times(1)).findById(1L);
        verify(trainingPlanRepository, times(1)).save(any(TrainingPlan.class));
    }

    /**
     * 测试复制训练计划 - 计划不存在
     */
    @Test
    void testCopyPlanNotFound() {
        when(trainingPlanRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(BusinessException.class, () -> {
            trainingPlanService.copyPlan(999L, testUser, "复制的计划");
        });
    }
}
