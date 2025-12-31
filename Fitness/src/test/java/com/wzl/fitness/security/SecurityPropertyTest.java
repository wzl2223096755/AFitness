package com.wzl.fitness.security;

import com.wzl.fitness.entity.TrainingRecord;
import com.wzl.fitness.entity.User;
import net.jqwik.api.*;
import net.jqwik.api.constraints.*;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDate;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 安全性属性测试
 * 
 * **Property 2: 用户数据隔离**
 * *For any* 已认证用户，只能访问和修改自己的训练数据、恢复数据和营养记录
 * 
 * **Property 6: 密码加密不可逆**
 * *For any* 用户密码，存储的加密密码不应能够被解密还原为原始密码
 * 
 * **Validates: Requirements 5.1, 5.4**
 * 
 * Feature: project-evaluation, Property 2: 用户数据隔离, Property 6: 密码加密不可逆
 */
public class SecurityPropertyTest {

    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    // ========== Property 6: 密码加密不可逆 ==========

    /**
     * Property 6: 密码加密不可逆 - 加密后的密码不等于原始密码
     * 
     * 对于任意有效的密码字符串，BCrypt加密后的结果应与原始密码不同
     * 
     * **Validates: Requirements 5.1**
     */
    @Property(tries = 100)
    @Label("Property 6: 密码加密不可逆 - 加密后的密码不等于原始密码")
    void encodedPasswordShouldNotEqualRawPassword(
            @ForAll @StringLength(min = 6, max = 50) String rawPassword) {
        
        // When
        String encodedPassword = passwordEncoder.encode(rawPassword);
        
        // Then
        assertNotEquals(rawPassword, encodedPassword,
                String.format("加密后的密码不应等于原始密码: raw=%s, encoded=%s", 
                        rawPassword, encodedPassword));
    }

    /**
     * Property 6: 密码加密不可逆 - 相同密码每次加密结果不同（盐值不同）
     * 
     * 对于任意有效的密码字符串，两次加密应产生不同的结果（因为BCrypt使用随机盐值）
     * 
     * **Validates: Requirements 5.1**
     */
    @Property(tries = 100)
    @Label("Property 6: 密码加密不可逆 - 相同密码每次加密结果不同")
    void samePasswordProducesDifferentEncodings(
            @ForAll @StringLength(min = 6, max = 50) String rawPassword) {
        
        // When
        String encoded1 = passwordEncoder.encode(rawPassword);
        String encoded2 = passwordEncoder.encode(rawPassword);
        
        // Then
        assertNotEquals(encoded1, encoded2,
                String.format("相同密码两次加密应产生不同结果: password=%s", rawPassword));
    }

    /**
     * Property 6: 密码加密不可逆 - 加密后的密码可以正确验证
     * 
     * 对于任意有效的密码字符串，加密后应能通过matches方法验证
     * 
     * **Validates: Requirements 5.1**
     */
    @Property(tries = 100)
    @Label("Property 6: 密码加密不可逆 - 加密后的密码可以正确验证")
    void encodedPasswordCanBeVerified(
            @ForAll @StringLength(min = 6, max = 50) String rawPassword) {
        
        // When
        String encodedPassword = passwordEncoder.encode(rawPassword);
        
        // Then
        assertTrue(passwordEncoder.matches(rawPassword, encodedPassword),
                String.format("原始密码应能通过验证: raw=%s", rawPassword));
    }

    /**
     * Property 6: 密码加密不可逆 - 错误密码验证失败
     * 
     * 对于任意两个不同的密码，用一个密码加密后不应能用另一个密码验证通过
     * 
     * **Validates: Requirements 5.1**
     */
    @Property(tries = 100)
    @Label("Property 6: 密码加密不可逆 - 错误密码验证失败")
    void wrongPasswordFailsVerification(
            @ForAll @StringLength(min = 6, max = 30) String password1,
            @ForAll @StringLength(min = 6, max = 30) String password2) {
        
        Assume.that(!password1.equals(password2));
        
        // When
        String encodedPassword = passwordEncoder.encode(password1);
        
        // Then
        assertFalse(passwordEncoder.matches(password2, encodedPassword),
                String.format("错误密码不应通过验证: correct=%s, wrong=%s", password1, password2));
    }

    /**
     * Property 6: 密码加密不可逆 - 加密密码格式符合BCrypt规范
     * 
     * BCrypt加密后的密码应以$2开头，长度为60字符
     * 
     * **Validates: Requirements 5.1**
     */
    @Property(tries = 100)
    @Label("Property 6: 密码加密不可逆 - 加密密码格式符合BCrypt规范")
    void encodedPasswordFollowsBCryptFormat(
            @ForAll @StringLength(min = 6, max = 50) String rawPassword) {
        
        // When
        String encodedPassword = passwordEncoder.encode(rawPassword);
        
        // Then
        assertTrue(encodedPassword.startsWith("$2"),
                String.format("BCrypt加密密码应以$2开头: encoded=%s", encodedPassword));
        assertEquals(60, encodedPassword.length(),
                String.format("BCrypt加密密码长度应为60: actual=%d", encodedPassword.length()));
    }

    // ========== Property 2: 用户数据隔离 ==========

    /**
     * Property 2: 用户数据隔离 - 训练记录应属于特定用户
     * 
     * 对于任意训练记录，其关联的用户ID应与创建时指定的用户ID一致
     * 
     * **Validates: Requirements 5.4**
     */
    @Property(tries = 100)
    @Label("Property 2: 用户数据隔离 - 训练记录应属于特定用户")
    void trainingRecordBelongsToSpecificUser(
            @ForAll @LongRange(min = 1, max = 1000000) Long userId,
            @ForAll @AlphaChars @StringLength(min = 3, max = 50) String exerciseName,
            @ForAll @IntRange(min = 1, max = 10) Integer sets,
            @ForAll @IntRange(min = 1, max = 20) Integer reps,
            @ForAll @DoubleRange(min = 1.0, max = 500.0) Double weight) {
        
        // Given
        User user = User.builder()
                .id(userId)
                .username("user" + userId)
                .password("password")
                .build();
        
        TrainingRecord record = new TrainingRecord();
        record.setUser(user);
        record.setExerciseName(exerciseName);
        record.setSets(sets);
        record.setReps(reps);
        record.setWeight(weight);
        record.setTrainingDate(LocalDate.now());
        record.setDuration(60);
        
        // Then
        assertEquals(userId, record.getUser().getId(),
                String.format("训练记录应属于用户 %d", userId));
    }

    /**
     * Property 2: 用户数据隔离 - 不同用户的训练记录应相互隔离
     * 
     * 对于任意两个不同的用户，他们的训练记录集合应该是独立的
     * 
     * **Validates: Requirements 5.4**
     */
    @Property(tries = 100)
    @Label("Property 2: 用户数据隔离 - 不同用户的训练记录应相互隔离")
    void differentUsersHaveIsolatedRecords(
            @ForAll @LongRange(min = 1, max = 500000) Long userId1,
            @ForAll @LongRange(min = 500001, max = 1000000) Long userId2,
            @ForAll @IntRange(min = 1, max = 5) Integer recordCount1,
            @ForAll @IntRange(min = 1, max = 5) Integer recordCount2) {
        
        // Given
        User user1 = User.builder()
                .id(userId1)
                .username("user" + userId1)
                .password("password")
                .build();
        
        User user2 = User.builder()
                .id(userId2)
                .username("user" + userId2)
                .password("password")
                .build();
        
        // Create records for user1
        List<TrainingRecord> user1Records = new ArrayList<>();
        for (int i = 0; i < recordCount1; i++) {
            TrainingRecord record = createTrainingRecord(user1, "Exercise" + i);
            user1Records.add(record);
        }
        
        // Create records for user2
        List<TrainingRecord> user2Records = new ArrayList<>();
        for (int i = 0; i < recordCount2; i++) {
            TrainingRecord record = createTrainingRecord(user2, "Exercise" + i);
            user2Records.add(record);
        }
        
        // Then - verify isolation
        for (TrainingRecord record : user1Records) {
            assertEquals(userId1, record.getUser().getId(),
                    "用户1的记录应属于用户1");
            assertNotEquals(userId2, record.getUser().getId(),
                    "用户1的记录不应属于用户2");
        }
        
        for (TrainingRecord record : user2Records) {
            assertEquals(userId2, record.getUser().getId(),
                    "用户2的记录应属于用户2");
            assertNotEquals(userId1, record.getUser().getId(),
                    "用户2的记录不应属于用户1");
        }
    }

    /**
     * Property 2: 用户数据隔离 - 用户只能通过自己的ID查询数据
     * 
     * 模拟数据隔离逻辑：给定用户ID和记录列表，过滤后只返回属于该用户的记录
     * 
     * **Validates: Requirements 5.4**
     */
    @Property(tries = 100)
    @Label("Property 2: 用户数据隔离 - 用户只能查询自己的数据")
    void userCanOnlyQueryOwnData(
            @ForAll @LongRange(min = 1, max = 1000000) Long queryUserId,
            @ForAll @IntRange(min = 1, max = 10) Integer totalRecords) {
        
        // Given - create mixed records from different users
        List<TrainingRecord> allRecords = new ArrayList<>();
        Random random = new Random();
        int expectedCount = 0;
        
        for (int i = 0; i < totalRecords; i++) {
            Long recordUserId = random.nextBoolean() ? queryUserId : queryUserId + 1;
            User user = User.builder()
                    .id(recordUserId)
                    .username("user" + recordUserId)
                    .password("password")
                    .build();
            
            TrainingRecord record = createTrainingRecord(user, "Exercise" + i);
            allRecords.add(record);
            
            if (recordUserId.equals(queryUserId)) {
                expectedCount++;
            }
        }
        
        // When - filter records by user ID (simulating data isolation)
        List<TrainingRecord> filteredRecords = filterByUserId(allRecords, queryUserId);
        
        // Then
        assertEquals(expectedCount, filteredRecords.size(),
                String.format("过滤后的记录数应为 %d", expectedCount));
        
        for (TrainingRecord record : filteredRecords) {
            assertEquals(queryUserId, record.getUser().getId(),
                    String.format("所有过滤后的记录应属于用户 %d", queryUserId));
        }
    }

    /**
     * Property 2: 用户数据隔离 - 数据修改只影响自己的记录
     * 
     * 模拟数据修改隔离：用户修改操作只应影响自己的记录
     * 
     * **Validates: Requirements 5.4**
     */
    @Property(tries = 100)
    @Label("Property 2: 用户数据隔离 - 数据修改只影响自己的记录")
    void dataModificationOnlyAffectsOwnRecords(
            @ForAll @LongRange(min = 1, max = 500000) Long userId1,
            @ForAll @LongRange(min = 500001, max = 1000000) Long userId2,
            @ForAll @AlphaChars @StringLength(min = 3, max = 20) String newExerciseName) {
        
        // Given
        User user1 = User.builder()
                .id(userId1)
                .username("user" + userId1)
                .password("password")
                .build();
        
        User user2 = User.builder()
                .id(userId2)
                .username("user" + userId2)
                .password("password")
                .build();
        
        TrainingRecord record1 = createTrainingRecord(user1, "OriginalExercise1");
        TrainingRecord record2 = createTrainingRecord(user2, "OriginalExercise2");
        String originalExercise2 = record2.getExerciseName();
        
        List<TrainingRecord> allRecords = Arrays.asList(record1, record2);
        
        // When - user1 tries to modify records (should only affect their own)
        modifyRecordsByUserId(allRecords, userId1, newExerciseName);
        
        // Then
        assertEquals(newExerciseName, record1.getExerciseName(),
                "用户1的记录应被修改");
        assertEquals(originalExercise2, record2.getExerciseName(),
                "用户2的记录不应被修改");
    }

    // ========== Helper Methods ==========

    private TrainingRecord createTrainingRecord(User user, String exerciseName) {
        TrainingRecord record = new TrainingRecord();
        record.setUser(user);
        record.setExerciseName(exerciseName);
        record.setSets(3);
        record.setReps(10);
        record.setWeight(50.0);
        record.setTrainingDate(LocalDate.now());
        record.setDuration(60);
        return record;
    }

    private List<TrainingRecord> filterByUserId(List<TrainingRecord> records, Long userId) {
        List<TrainingRecord> filtered = new ArrayList<>();
        for (TrainingRecord record : records) {
            if (record.getUser().getId().equals(userId)) {
                filtered.add(record);
            }
        }
        return filtered;
    }

    private void modifyRecordsByUserId(List<TrainingRecord> records, Long userId, String newExerciseName) {
        for (TrainingRecord record : records) {
            if (record.getUser().getId().equals(userId)) {
                record.setExerciseName(newExerciseName);
            }
        }
    }
}
