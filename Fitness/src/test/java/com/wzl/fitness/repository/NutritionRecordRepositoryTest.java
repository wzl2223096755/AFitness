package com.wzl.fitness.repository;

import com.wzl.fitness.entity.NutritionRecord;
import com.wzl.fitness.entity.Role;
import com.wzl.fitness.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * NutritionRecordRepository的单元测试类
 * 测试营养记录数据访问层
 */
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
public class NutritionRecordRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private NutritionRecordRepository nutritionRecordRepository;

    private User testUser;
    private NutritionRecord testRecord1;
    private NutritionRecord testRecord2;

    @BeforeEach
    void setUp() {
        // 创建测试用户
        testUser = new User();
        testUser.setUsername("testuser");
        testUser.setEmail("test@example.com");
        testUser.setPassword("password123");
        testUser.setRole(Role.USER);
        entityManager.persist(testUser);
        entityManager.flush();

        // 创建第一个营养记录
        testRecord1 = new NutritionRecord();
        testRecord1.setUser(testUser);
        testRecord1.setRecordDate(LocalDate.now());
        testRecord1.setMealType("breakfast");
        testRecord1.setFoodName("燕麦粥");
        testRecord1.setCalories(150.0);
        testRecord1.setProtein(5.0);
        testRecord1.setCarbs(27.0);
        testRecord1.setFat(3.0);
        entityManager.persist(testRecord1);
        entityManager.flush();

        // 创建第二个营养记录
        testRecord2 = new NutritionRecord();
        testRecord2.setUser(testUser);
        testRecord2.setRecordDate(LocalDate.now());
        testRecord2.setMealType("lunch");
        testRecord2.setFoodName("鸡胸肉沙拉");
        testRecord2.setCalories(250.0);
        testRecord2.setProtein(30.0);
        testRecord2.setCarbs(10.0);
        testRecord2.setFat(8.0);
        entityManager.persist(testRecord2);
        entityManager.flush();
    }

    /**
     * 测试根据用户和日期查找营养记录
     */
    @Test
    void testFindByUserAndRecordDateOrderByMealTypeAsc() {
        // When
        List<NutritionRecord> records = nutritionRecordRepository
            .findByUserAndRecordDateOrderByMealTypeAsc(testUser, LocalDate.now());

        // Then
        assertEquals(2, records.size());
        // breakfast comes before lunch alphabetically
        assertEquals("breakfast", records.get(0).getMealType());
        assertEquals("lunch", records.get(1).getMealType());
    }

    /**
     * 测试根据用户和日期查找营养记录 - 无记录
     */
    @Test
    void testFindByUserAndRecordDateOrderByMealTypeAscNoRecords() {
        // When
        List<NutritionRecord> records = nutritionRecordRepository
            .findByUserAndRecordDateOrderByMealTypeAsc(testUser, LocalDate.now().minusDays(1));

        // Then
        assertTrue(records.isEmpty());
    }

    /**
     * 测试根据ID和用户查找营养记录
     */
    @Test
    void testFindByIdAndUser() {
        // When
        Optional<NutritionRecord> record = nutritionRecordRepository
            .findByIdAndUser(testRecord1.getId(), testUser);

        // Then
        assertTrue(record.isPresent());
        assertEquals("燕麦粥", record.get().getFoodName());
    }

    /**
     * 测试根据ID和用户查找营养记录 - 记录不存在
     */
    @Test
    void testFindByIdAndUserNotFound() {
        // 创建另一个用户
        User otherUser = new User();
        otherUser.setUsername("otheruser");
        otherUser.setEmail("other@example.com");
        otherUser.setPassword("password123");
        otherUser.setRole(Role.USER);
        entityManager.persist(otherUser);
        entityManager.flush();

        // When
        Optional<NutritionRecord> record = nutritionRecordRepository
            .findByIdAndUser(testRecord1.getId(), otherUser);

        // Then
        assertFalse(record.isPresent());
    }

    /**
     * 测试分页查找用户的营养记录
     */
    @Test
    void testFindByUserOrderByRecordDateDesc() {
        // 创建昨天的记录
        NutritionRecord yesterdayRecord = new NutritionRecord();
        yesterdayRecord.setUser(testUser);
        yesterdayRecord.setRecordDate(LocalDate.now().minusDays(1));
        yesterdayRecord.setMealType("dinner");
        yesterdayRecord.setFoodName("三文鱼");
        yesterdayRecord.setCalories(300.0);
        entityManager.persist(yesterdayRecord);
        entityManager.flush();

        // When
        Pageable pageable = PageRequest.of(0, 10);
        Page<NutritionRecord> page = nutritionRecordRepository
            .findByUserOrderByRecordDateDesc(testUser, pageable);

        // Then
        assertEquals(3, page.getTotalElements());
        // 记录应该按日期降序排列
        assertEquals(LocalDate.now(), page.getContent().get(0).getRecordDate());
        assertEquals(LocalDate.now(), page.getContent().get(1).getRecordDate());
        assertEquals(LocalDate.now().minusDays(1), page.getContent().get(2).getRecordDate());
    }

    /**
     * 测试获取用户指定日期的营养摄入统计
     */
    @Test
    void testGetNutritionStatsByDate() {
        // When
        Object[] stats = nutritionRecordRepository
            .getNutritionStatsByDate(testUser, LocalDate.now());

        // Then
        assertNotNull(stats);
        // The query returns a single row with 4 columns: totalCalories, totalProtein, totalCarbs, totalFat
        // Check if stats is the row directly or wrapped
        Object[] row;
        if (stats[0] instanceof Object[]) {
            row = (Object[]) stats[0];
        } else {
            row = stats;
        }
        
        // Total calories: 150 + 250 = 400
        assertNotNull(row[0]);
        assertEquals(400.0, ((Number) row[0]).doubleValue(), 0.01);
        // Total protein: 5 + 30 = 35
        assertNotNull(row[1]);
        assertEquals(35.0, ((Number) row[1]).doubleValue(), 0.01);
        // Total carbs: 27 + 10 = 37
        assertNotNull(row[2]);
        assertEquals(37.0, ((Number) row[2]).doubleValue(), 0.01);
        // Total fat: 3 + 8 = 11
        assertNotNull(row[3]);
        assertEquals(11.0, ((Number) row[3]).doubleValue(), 0.01);
    }

    /**
     * 测试获取用户指定日期范围的营养摄入统计
     */
    @Test
    void testGetNutritionStatsByDateRange() {
        // 创建昨天的记录
        NutritionRecord yesterdayRecord = new NutritionRecord();
        yesterdayRecord.setUser(testUser);
        yesterdayRecord.setRecordDate(LocalDate.now().minusDays(1));
        yesterdayRecord.setMealType("dinner");
        yesterdayRecord.setFoodName("三文鱼");
        yesterdayRecord.setCalories(300.0);
        yesterdayRecord.setProtein(25.0);
        yesterdayRecord.setCarbs(5.0);
        yesterdayRecord.setFat(20.0);
        entityManager.persist(yesterdayRecord);
        entityManager.flush();

        // When
        List<Object[]> statsList = nutritionRecordRepository
            .getNutritionStatsByDateRange(
                testUser, 
                LocalDate.now().minusDays(1), 
                LocalDate.now()
            );

        // Then
        assertEquals(2, statsList.size());
        // 结果应该按日期升序排列
        Object[] yesterdayStats = statsList.get(0);
        Object[] todayStats = statsList.get(1);
        
        assertEquals(LocalDate.now().minusDays(1), yesterdayStats[0]);
        assertEquals(300.0, yesterdayStats[1]); // 总卡路里
        assertEquals(25.0, yesterdayStats[2]);  // 总蛋白质
        assertEquals(5.0, yesterdayStats[3]);   // 总碳水化合物
        assertEquals(20.0, yesterdayStats[4]);  // 总脂肪
        
        assertEquals(LocalDate.now(), todayStats[0]);
        assertEquals(400.0, todayStats[1]); // 总卡路里
        assertEquals(35.0, todayStats[2]);  // 总蛋白质
        assertEquals(37.0, todayStats[3]);  // 总碳水化合物
        assertEquals(11.0, todayStats[4]);  // 总脂肪
    }

    /**
     * 测试根据用户和餐食类型查找营养记录
     */
    @Test
    void testFindByUserAndRecordDateAndMealType() {
        // When
        List<NutritionRecord> records = nutritionRecordRepository
            .findByUserAndRecordDateAndMealType(testUser, LocalDate.now(), "breakfast");

        // Then
        assertEquals(1, records.size());
        assertEquals("燕麦粥", records.get(0).getFoodName());
    }

    /**
     * 测试根据用户和日期范围查找营养记录
     */
    @Test
    void testFindByUserAndRecordDateBetweenOrderByRecordDateAscMealTypeAsc() {
        // 创建昨天的记录
        NutritionRecord yesterdayRecord = new NutritionRecord();
        yesterdayRecord.setUser(testUser);
        yesterdayRecord.setRecordDate(LocalDate.now().minusDays(1));
        yesterdayRecord.setMealType("dinner");
        yesterdayRecord.setFoodName("三文鱼");
        yesterdayRecord.setCalories(300.0);
        entityManager.persist(yesterdayRecord);
        entityManager.flush();

        // When
        List<NutritionRecord> records = nutritionRecordRepository
            .findByUserAndRecordDateBetweenOrderByRecordDateAscMealTypeAsc(
                testUser,
                LocalDate.now().minusDays(1),
                LocalDate.now()
            );

        // Then
        assertEquals(3, records.size());
        // 记录应该按日期升序、餐食类型升序排列
        assertEquals(LocalDate.now().minusDays(1), records.get(0).getRecordDate());
        assertEquals("dinner", records.get(0).getMealType());
        assertEquals(LocalDate.now(), records.get(1).getRecordDate());
        assertEquals("breakfast", records.get(1).getMealType());
        assertEquals(LocalDate.now(), records.get(2).getRecordDate());
        assertEquals("lunch", records.get(2).getMealType());
    }
}
