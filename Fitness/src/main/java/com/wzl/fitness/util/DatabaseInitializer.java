package com.wzl.fitness.util;

// 移除未使用的导入语句
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.FileCopyUtils;

import javax.sql.DataSource;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
// 移除未使用的导入语句 java.sql.Connection
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * 数据库初始化工具类，用于解决数据库内容不匹配的问题
 * 提供初始化和验证数据库内容的功能
 */
@Component
public class DatabaseInitializer {

    private static final Logger log = LoggerFactory.getLogger(DatabaseInitializer.class);

    private final DataSource dataSource;
    private final JdbcTemplate jdbcTemplate;

    public DatabaseInitializer(DataSource dataSource) {
        this.dataSource = dataSource;
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    /**
     * 初始化完整的数据库内容
     * 包括用户、设备、用户设备绑定和健身数据
     */
    @Transactional
    public void initializeFullDatabase() {
        try {
            log.info("开始初始化完整数据库内容...");
            
            // 读取并执行schema.sql（表结构）
            executeScript("schema.sql");
            log.info("成功执行schema.sql，表结构已创建");
            
            // 读取并执行data.sql（示例数据）
            executeScript("data.sql");
            log.info("成功执行data.sql，示例数据已插入");
            
        } catch (Exception e) {
            log.error("初始化数据库内容失败: {}", e.getMessage(), e);
            throw new RuntimeException("数据库初始化失败: " + e.getMessage(), e);
        }
    }

    /**
     * 验证数据库内容完整性
     * @return 验证结果列表，包含每个表的验证状态
     */
    @Transactional(readOnly = true)
    public List<String> validateDatabaseContent() {
        List<String> validationResults = new ArrayList<>();
        
        try (java.sql.Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement()) {
            
            // 检查user_table表
            int userCount = getTableRowCount(statement, "user_table");
            validationResults.add("用户表(user_table): " + (userCount > 0 ? "正常(" + userCount + "条)" : "空表"));
            
            // 检查device表
            int deviceCount = getTableRowCount(statement, "device");
            validationResults.add("设备表(device): " + (deviceCount > 0 ? "正常(" + deviceCount + "条)" : "空表"));
            
            // 检查user_device表
            int userDeviceCount = getTableRowCount(statement, "user_device");
            validationResults.add("用户设备关联表(user_device): " + (userDeviceCount > 0 ? "正常(" + userDeviceCount + "条)" : "空表"));
            
            // 检查fitness_data表
            int fitnessDataCount = getTableRowCount(statement, "fitness_data");
            validationResults.add("健身数据表(fitness_data): " + (fitnessDataCount > 0 ? "正常(" + fitnessDataCount + "条)" : "空表"));
            
        } catch (SQLException e) {
            log.error("验证数据库内容失败: {}", e.getMessage(), e);
            validationResults.add("验证过程出错: " + e.getMessage());
        }
        
        return validationResults;
    }

    /**
     * 仅初始化示例数据（不重建表结构）
     */
    @Transactional
    public void initializeSampleData() {
        try {
            log.info("开始初始化示例数据...");
            
            // 读取并执行data.sql（示例数据）
            executeScript("data.sql");
            log.info("成功执行data.sql，示例数据已插入");
            
        } catch (Exception e) {
            log.error("初始化示例数据失败: {}", e.getMessage(), e);
            throw new RuntimeException("示例数据初始化失败: " + e.getMessage(), e);
        }
    }

    /**
     * 执行SQL脚本文件
     */
    private void executeScript(String scriptPath) throws IOException {
        log.info("执行SQL脚本: {}", scriptPath);
        
        // 读取SQL脚本文件内容
        ClassPathResource resource = new ClassPathResource(scriptPath);
        String sqlScript = FileCopyUtils.copyToString(
                new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8));
        
        // 分割SQL语句并执行
        String[] sqlStatements = sqlScript.split(";\n|;\r\n|;");
        
        for (String sql : sqlStatements) {
            sql = sql.trim();
            // 跳过空行和注释
            if (!sql.isEmpty() && !sql.startsWith("--")) {
                try {
                    jdbcTemplate.execute(sql);
                    log.debug("执行SQL成功: {}", sql.length() > 100 ? sql.substring(0, 100) + "..." : sql);
                } catch (Exception e) {
                    log.warn("执行SQL失败，但继续执行后续语句: {}", e.getMessage());
                    // 记录失败的SQL语句
                    log.debug("失败的SQL: {}", sql);
                }
            }
        }
        
        log.info("SQL脚本 {} 执行完成", scriptPath);
    }

    /**
     * 获取表的行数
     */
    private int getTableRowCount(Statement statement, String tableName) throws SQLException {
        try {
            String sql = "SELECT COUNT(*) FROM " + tableName;
            var resultSet = statement.executeQuery(sql);
            if (resultSet.next()) {
                return resultSet.getInt(1);
            }
        } catch (SQLException e) {
            log.warn("获取表{}行数失败: {}", tableName, e.getMessage());
            // 表可能不存在，返回-1表示错误
            return -1;
        }
        return 0;
    }

}