package com.wzl.fitness.repository;

import com.wzl.fitness.entity.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.*;

/**
 * UserRepository的单元测试类
 * 测试用户数据访问层的功能
 */
@DataJpaTest
@ActiveProfiles("test")
public class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    /**
     * 测试保存用户功能
     */
    @Test
    void testSaveUser() {
        // 创建测试用户
        User user = new User();
        user.setUsername("testuser");
        user.setPassword("password123");
        user.setEmail("test@example.com");

        // 保存用户
        User savedUser = userRepository.save(user);

        // 验证保存结果
        assertNotNull(savedUser.getId());
        assertEquals("testuser", savedUser.getUsername());
        assertEquals("password123", savedUser.getPassword());
        assertEquals("test@example.com", savedUser.getEmail());
    }

    /**
     * 测试通过用户名查找用户功能
     */
    @Test
    void testFindByUsername() {
        // 创建并保存测试用户
        User user = new User();
        user.setUsername("finduser");
        user.setPassword("password123");
        user.setEmail("find@example.com");
        userRepository.save(user);

        // 通过用户名查找用户
        User foundUser = userRepository.findByUsername("finduser")
                .orElse(null);

        // 验证查找结果
        assertNotNull(foundUser);
        assertEquals("finduser", foundUser.getUsername());
    }

    /**
     * 测试通过不存在的用户名查找用户功能
     */
    @Test
    void testFindByNonExistentUsername() {
        // 通过不存在的用户名查找用户
        User foundUser = userRepository.findByUsername("nonexistent")
                .orElse(null);

        // 验证查找结果为空
        assertNull(foundUser);
    }
}