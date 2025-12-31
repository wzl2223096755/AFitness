package com.wzl.fitness.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.wzl.fitness.config.TestDisableInterceptorConfig;
import com.wzl.fitness.config.TestSecurityConfig;
import com.wzl.fitness.dto.request.TrainingPlanRequestDTO;
import com.wzl.fitness.entity.TrainingPlan;
import com.wzl.fitness.entity.TrainingPlanDay;
import com.wzl.fitness.entity.TrainingPlanExercise;
import com.wzl.fitness.entity.User;
import com.wzl.fitness.repository.TrainingPlanDayRepository;
import com.wzl.fitness.repository.TrainingPlanExerciseRepository;
import com.wzl.fitness.repository.TrainingPlanRepository;
import com.wzl.fitness.repository.UserRepository;
import com.wzl.fitness.service.TrainingPlanService;
import com.wzl.fitness.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
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
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * TrainingPlanController的集成测试类
 */
@SpringBootTest
@AutoConfigureMockMvc
@Import({TestSecurityConfig.class, TestDisableInterceptorConfig.class})
@ActiveProfiles("test")
class TrainingPlanControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private TrainingPlanService trainingPlanService;

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
     * 测试获取用户所有计划
     */
    @Test
    void testGetMyPlans() throws Exception {
        TrainingPlan plan = new TrainingPlan();
        plan.setId(1L);
        plan.setName("力量训练计划");
        plan.setUser(testUser);

        Mockito.when(trainingPlanService.getPlansByUser(any(User.class)))
                .thenReturn(Collections.singletonList(plan));

        mockMvc.perform(get("/api/v1/training-plans")
                .requestAttr("userId", userId)
                .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data[0].name").value("力量训练计划"));
    }

    /**
     * 测试根据ID获取计划详情
     */
    @Test
    void testGetPlanById() throws Exception {
        TrainingPlan plan = new TrainingPlan();
        plan.setId(1L);
        plan.setName("力量训练计划");
        plan.setUser(testUser);

        Mockito.when(trainingPlanService.getPlanById(1L)).thenReturn(Optional.of(plan));

        mockMvc.perform(get("/api/v1/training-plans/1")
                .requestAttr("userId", userId)
                .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.name").value("力量训练计划"));
    }

    /**
     * 测试创建新计划
     */
    @Test
    void testCreatePlan() throws Exception {
        TrainingPlanRequestDTO requestDTO = new TrainingPlanRequestDTO();
        requestDTO.setName("新训练计划");
        requestDTO.setGoal("增肌");
        requestDTO.setWeeklyPlan(new ArrayList<>());

        TrainingPlan savedPlan = new TrainingPlan();
        savedPlan.setId(1L);
        savedPlan.setName("新训练计划");
        savedPlan.setUser(testUser);

        Mockito.when(trainingPlanService.createPlanFromDto(any(TrainingPlanRequestDTO.class), any(User.class)))
                .thenReturn(savedPlan);

        mockMvc.perform(post("/api/v1/training-plans")
                .requestAttr("userId", userId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDTO)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.name").value("新训练计划"));
    }

    /**
     * 测试创建包含多天训练和多个动作的完整计划
     * Requirements: 1.1, 1.2
     */
    @Test
    @DisplayName("测试创建包含多天训练和多个动作的完整计划")
    void testCreatePlanWithDaysAndExercises() throws Exception {
        // 构建包含完整周计划的请求
        TrainingPlanRequestDTO requestDTO = new TrainingPlanRequestDTO();
        requestDTO.setName("完整力量训练计划");
        requestDTO.setGoal("muscle_gain");
        requestDTO.setLevel("intermediate");
        requestDTO.setDuration(4);
        requestDTO.setDaysPerWeek(3);
        requestDTO.setDurationPerSession(60);

        // 创建周计划
        List<TrainingPlanRequestDTO.WeekDTO> weeklyPlan = new ArrayList<>();
        TrainingPlanRequestDTO.WeekDTO week1 = new TrainingPlanRequestDTO.WeekDTO();
        List<TrainingPlanRequestDTO.DayDTO> days = new ArrayList<>();

        // 第一天 - 胸部训练
        TrainingPlanRequestDTO.DayDTO day1 = new TrainingPlanRequestDTO.DayDTO();
        day1.setHasTraining(true);
        day1.setFocus("胸部");
        List<TrainingPlanRequestDTO.ExerciseDTO> day1Exercises = new ArrayList<>();
        
        TrainingPlanRequestDTO.ExerciseDTO benchPress = new TrainingPlanRequestDTO.ExerciseDTO();
        benchPress.setName("卧推");
        benchPress.setSets(4);
        benchPress.setReps("8-10");
        benchPress.setWeight(60.0);
        benchPress.setRest(90);
        day1Exercises.add(benchPress);

        TrainingPlanRequestDTO.ExerciseDTO inclinePress = new TrainingPlanRequestDTO.ExerciseDTO();
        inclinePress.setName("上斜卧推");
        inclinePress.setSets(3);
        inclinePress.setReps("10-12");
        inclinePress.setWeight(50.0);
        inclinePress.setRest(60);
        day1Exercises.add(inclinePress);

        day1.setExercises(day1Exercises);
        days.add(day1);

        // 第二天 - 休息日
        TrainingPlanRequestDTO.DayDTO day2 = new TrainingPlanRequestDTO.DayDTO();
        day2.setHasTraining(false);
        day2.setFocus("休息");
        days.add(day2);

        // 第三天 - 背部训练
        TrainingPlanRequestDTO.DayDTO day3 = new TrainingPlanRequestDTO.DayDTO();
        day3.setHasTraining(true);
        day3.setFocus("背部");
        List<TrainingPlanRequestDTO.ExerciseDTO> day3Exercises = new ArrayList<>();
        
        TrainingPlanRequestDTO.ExerciseDTO pullUp = new TrainingPlanRequestDTO.ExerciseDTO();
        pullUp.setName("引体向上");
        pullUp.setSets(4);
        pullUp.setReps("6-8");
        pullUp.setRest(120);
        day3Exercises.add(pullUp);

        day3.setExercises(day3Exercises);
        days.add(day3);

        week1.setDays(days);
        weeklyPlan.add(week1);
        requestDTO.setWeeklyPlan(weeklyPlan);

        // 模拟返回的计划（包含days和exercises）
        TrainingPlan savedPlan = new TrainingPlan();
        savedPlan.setId(1L);
        savedPlan.setName("完整力量训练计划");
        savedPlan.setGoal("muscle_gain");
        savedPlan.setUser(testUser);
        savedPlan.setDays(new ArrayList<>());

        // 添加训练日
        TrainingPlanDay planDay1 = TrainingPlanDay.builder()
                .id(1L)
                .plan(savedPlan)
                .dayOfWeek(0)
                .dayName("周一")
                .hasTraining(true)
                .focus("胸部")
                .exercises(new ArrayList<>())
                .build();
        
        planDay1.getExercises().add(TrainingPlanExercise.builder()
                .id(1L)
                .day(planDay1)
                .name("卧推")
                .sets(4)
                .reps("8-10")
                .weight(60.0)
                .restTime(90)
                .build());
        planDay1.getExercises().add(TrainingPlanExercise.builder()
                .id(2L)
                .day(planDay1)
                .name("上斜卧推")
                .sets(3)
                .reps("10-12")
                .weight(50.0)
                .restTime(60)
                .build());
        
        savedPlan.getDays().add(planDay1);

        Mockito.when(trainingPlanService.createPlanFromDto(any(TrainingPlanRequestDTO.class), any(User.class)))
                .thenReturn(savedPlan);

        mockMvc.perform(post("/api/v1/training-plans")
                .requestAttr("userId", userId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDTO)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.name").value("完整力量训练计划"))
                .andExpect(jsonPath("$.data.goal").value("muscle_gain"))
                .andExpect(jsonPath("$.data.days").isArray())
                .andExpect(jsonPath("$.data.days[0].focus").value("胸部"))
                .andExpect(jsonPath("$.data.days[0].exercises").isArray())
                .andExpect(jsonPath("$.data.days[0].exercises[0].name").value("卧推"))
                .andExpect(jsonPath("$.data.days[0].exercises[0].sets").value(4))
                .andExpect(jsonPath("$.data.days[0].exercises[1].name").value("上斜卧推"));
    }

    /**
     * 测试删除计划（验证级联删除）
     * Requirements: 1.5
     */
    @Test
    @DisplayName("测试删除计划时级联删除训练日和动作")
    void testDeletePlanCascade() throws Exception {
        // 创建包含训练日和动作的计划
        TrainingPlan plan = new TrainingPlan();
        plan.setId(1L);
        plan.setName("待删除计划");
        plan.setUser(testUser);
        plan.setDays(new ArrayList<>());

        TrainingPlanDay day = TrainingPlanDay.builder()
                .id(1L)
                .plan(plan)
                .dayOfWeek(0)
                .dayName("周一")
                .exercises(new ArrayList<>())
                .build();
        
        day.getExercises().add(TrainingPlanExercise.builder()
                .id(1L)
                .day(day)
                .name("卧推")
                .sets(4)
                .build());
        
        plan.getDays().add(day);

        Mockito.when(trainingPlanService.getPlanById(1L)).thenReturn(Optional.of(plan));
        Mockito.doNothing().when(trainingPlanService).deletePlan(1L);

        mockMvc.perform(delete("/api/v1/training-plans/1")
                .requestAttr("userId", userId)
                .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("计划删除成功"));

        // 验证deletePlan被调用
        Mockito.verify(trainingPlanService).deletePlan(1L);
    }
}
