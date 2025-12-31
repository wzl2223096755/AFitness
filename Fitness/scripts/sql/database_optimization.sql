-- ========================================
-- Fitness应用数据库优化脚本
-- 版本: 2.0
-- 创建时间: 2025-12-30
-- 描述: 数据库性能优化和结构改进
-- ========================================

-- ==========================================
-- 第一部分：索引优化
-- ==========================================

-- 1.1 用户表索引优化
-- 添加登录查询优化索引
CREATE INDEX IF NOT EXISTS idx_user_username_password ON user_table(username, password);
-- 添加角色查询索引
CREATE INDEX IF NOT EXISTS idx_user_role ON user_table(role);
-- 添加最后登录时间索引（用于活跃用户统计）
CREATE INDEX IF NOT EXISTS idx_user_last_login ON user_table(last_login_at);

-- 1.2 训练记录表索引优化
-- 添加用户+日期范围查询的复合索引
CREATE INDEX IF NOT EXISTS idx_training_user_date_range ON training_records(user_id, training_date DESC);
-- 添加运动类型统计索引
CREATE INDEX IF NOT EXISTS idx_training_exercise_user ON training_records(exercise_name, user_id);
-- 添加软删除过滤索引
CREATE INDEX IF NOT EXISTS idx_training_deleted ON training_records(deleted);
-- 添加用户+删除状态复合索引
CREATE INDEX IF NOT EXISTS idx_training_user_deleted ON training_records(user_id, deleted);

-- 1.3 力量训练数据表索引优化
CREATE INDEX IF NOT EXISTS idx_strength_user_time ON strength_training_data(user_id, timestamp DESC);
CREATE INDEX IF NOT EXISTS idx_strength_exercise_type ON strength_training_data(exercise_type);
CREATE INDEX IF NOT EXISTS idx_strength_user_exercise ON strength_training_data(user_id, exercise_name);

-- 1.4 有氧训练数据表索引优化
CREATE INDEX IF NOT EXISTS idx_cardio_user_time ON cardio_training_data(user_id, timestamp DESC);
CREATE INDEX IF NOT EXISTS idx_cardio_exercise_type ON cardio_training_data(exercise_type);

-- 1.5 恢复数据表索引优化
CREATE INDEX IF NOT EXISTS idx_recovery_user_time ON recovery_data(user_id, timestamp DESC);
CREATE INDEX IF NOT EXISTS idx_recovery_score ON recovery_data(recovery_score);

-- 1.6 营养记录表索引优化
CREATE INDEX IF NOT EXISTS idx_nutrition_user_date ON nutrition_records(user_id, record_date DESC);
CREATE INDEX IF NOT EXISTS idx_nutrition_meal_type ON nutrition_records(meal_type);

-- 1.7 审计日志表索引优化（高频查询）
CREATE INDEX IF NOT EXISTS idx_audit_user_time ON audit_logs(user_id, created_at DESC);
CREATE INDEX IF NOT EXISTS idx_audit_action_result ON audit_logs(action, result);
CREATE INDEX IF NOT EXISTS idx_audit_resource ON audit_logs(resource_type, resource_id);

-- ==========================================
-- 第二部分：表结构优化
-- ==========================================

-- 2.1 为训练记录表添加分区支持（MySQL 8.0+）
-- 注意：此操作需要在生产环境谨慎执行
/*
ALTER TABLE training_records
PARTITION BY RANGE (YEAR(training_date)) (
    PARTITION p2023 VALUES LESS THAN (2024),
    PARTITION p2024 VALUES LESS THAN (2025),
    PARTITION p2025 VALUES LESS THAN (2026),
    PARTITION p_future VALUES LESS THAN MAXVALUE
);
*/

-- 2.2 添加统计汇总表（物化视图替代方案）
CREATE TABLE IF NOT EXISTS user_training_summary (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    summary_date DATE NOT NULL,
    total_sessions INT DEFAULT 0 COMMENT '训练次数',
    total_volume DOUBLE DEFAULT 0 COMMENT '总训练量',
    total_duration INT DEFAULT 0 COMMENT '总时长(分钟)',
    avg_weight DOUBLE DEFAULT 0 COMMENT '平均重量',
    max_weight DOUBLE DEFAULT 0 COMMENT '最大重量',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    UNIQUE KEY uk_user_date (user_id, summary_date),
    INDEX idx_summary_user (user_id),
    INDEX idx_summary_date (summary_date),
    FOREIGN KEY (user_id) REFERENCES user_table(id) ON DELETE CASCADE
) COMMENT '用户训练统计汇总表';

-- 2.3 添加用户恢复汇总表
CREATE TABLE IF NOT EXISTS user_recovery_summary (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    summary_date DATE NOT NULL,
    avg_recovery_score DOUBLE DEFAULT 0 COMMENT '平均恢复评分',
    avg_sleep_hours DOUBLE DEFAULT 0 COMMENT '平均睡眠时长',
    avg_sleep_quality DOUBLE DEFAULT 0 COMMENT '平均睡眠质量',
    record_count INT DEFAULT 0 COMMENT '记录数量',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    UNIQUE KEY uk_user_date (user_id, summary_date),
    INDEX idx_recovery_summary_user (user_id),
    INDEX idx_recovery_summary_date (summary_date),
    FOREIGN KEY (user_id) REFERENCES user_table(id) ON DELETE CASCADE
) COMMENT '用户恢复统计汇总表';

-- ==========================================
-- 第三部分：查询优化视图
-- ==========================================

-- 3.1 用户最近训练视图
CREATE OR REPLACE VIEW v_user_recent_training AS
SELECT 
    tr.user_id,
    tr.id as record_id,
    tr.exercise_name,
    tr.weight,
    tr.sets,
    tr.reps,
    tr.training_date,
    tr.duration,
    tr.total_volume,
    tr.created_at
FROM training_records tr
WHERE tr.deleted = false
  AND tr.training_date >= DATE_SUB(CURDATE(), INTERVAL 30 DAY)
ORDER BY tr.training_date DESC;

-- 3.2 用户训练统计视图
CREATE OR REPLACE VIEW v_user_training_stats AS
SELECT 
    u.id as user_id,
    u.username,
    COUNT(tr.id) as total_sessions,
    COALESCE(SUM(tr.duration), 0) as total_minutes,
    COALESCE(AVG(tr.weight), 0) as avg_weight,
    COALESCE(MAX(tr.weight), 0) as max_weight,
    COALESCE(SUM(tr.total_volume), 0) as total_volume,
    MAX(tr.training_date) as last_training_date
FROM user_table u
LEFT JOIN training_records tr ON u.id = tr.user_id AND tr.deleted = false
GROUP BY u.id, u.username;

-- 3.3 用户恢复趋势视图
CREATE OR REPLACE VIEW v_user_recovery_trend AS
SELECT 
    user_id,
    DATE(timestamp) as record_date,
    AVG(recovery_score) as avg_recovery_score,
    AVG(sleep_hours) as avg_sleep_hours,
    AVG(sleep_quality) as avg_sleep_quality,
    COUNT(*) as record_count
FROM recovery_data
WHERE timestamp >= DATE_SUB(NOW(), INTERVAL 30 DAY)
GROUP BY user_id, DATE(timestamp)
ORDER BY record_date DESC;

-- ==========================================
-- 第四部分：存储过程优化
-- ==========================================

-- 4.1 更新用户训练汇总的存储过程
DELIMITER //
CREATE PROCEDURE IF NOT EXISTS sp_update_training_summary(IN p_user_id BIGINT, IN p_date DATE)
BEGIN
    INSERT INTO user_training_summary (user_id, summary_date, total_sessions, total_volume, total_duration, avg_weight, max_weight)
    SELECT 
        p_user_id,
        p_date,
        COUNT(*),
        COALESCE(SUM(total_volume), 0),
        COALESCE(SUM(duration), 0),
        COALESCE(AVG(weight), 0),
        COALESCE(MAX(weight), 0)
    FROM training_records
    WHERE user_id = p_user_id 
      AND training_date = p_date
      AND deleted = false
    ON DUPLICATE KEY UPDATE
        total_sessions = VALUES(total_sessions),
        total_volume = VALUES(total_volume),
        total_duration = VALUES(total_duration),
        avg_weight = VALUES(avg_weight),
        max_weight = VALUES(max_weight),
        updated_at = CURRENT_TIMESTAMP;
END //
DELIMITER ;

-- 4.2 批量更新训练汇总的存储过程
DELIMITER //
CREATE PROCEDURE IF NOT EXISTS sp_batch_update_training_summary(IN p_days INT)
BEGIN
    DECLARE done INT DEFAULT FALSE;
    DECLARE v_user_id BIGINT;
    DECLARE v_date DATE;
    
    DECLARE cur CURSOR FOR 
        SELECT DISTINCT user_id, training_date 
        FROM training_records 
        WHERE training_date >= DATE_SUB(CURDATE(), INTERVAL p_days DAY)
          AND deleted = false;
    
    DECLARE CONTINUE HANDLER FOR NOT FOUND SET done = TRUE;
    
    OPEN cur;
    
    read_loop: LOOP
        FETCH cur INTO v_user_id, v_date;
        IF done THEN
            LEAVE read_loop;
        END IF;
        
        CALL sp_update_training_summary(v_user_id, v_date);
    END LOOP;
    
    CLOSE cur;
END //
DELIMITER ;

-- 4.3 清理过期审计日志的存储过程
DELIMITER //
CREATE PROCEDURE IF NOT EXISTS sp_cleanup_audit_logs(IN p_retention_days INT)
BEGIN
    DELETE FROM audit_logs 
    WHERE created_at < DATE_SUB(NOW(), INTERVAL p_retention_days DAY);
    
    SELECT ROW_COUNT() as deleted_count;
END //
DELIMITER ;

-- 4.4 获取用户训练进度的存储过程
DELIMITER //
CREATE PROCEDURE IF NOT EXISTS sp_get_user_progress(IN p_user_id BIGINT, IN p_exercise_name VARCHAR(100))
BEGIN
    SELECT 
        training_date,
        MAX(weight) as max_weight,
        SUM(sets * reps * weight) as total_volume,
        AVG(weight) as avg_weight
    FROM training_records
    WHERE user_id = p_user_id 
      AND exercise_name = p_exercise_name
      AND deleted = false
    GROUP BY training_date
    ORDER BY training_date DESC
    LIMIT 30;
END //
DELIMITER ;

-- ==========================================
-- 第五部分：触发器优化
-- ==========================================

-- 5.1 训练记录插入后自动更新汇总
DELIMITER //
CREATE TRIGGER IF NOT EXISTS tr_training_after_insert
AFTER INSERT ON training_records
FOR EACH ROW
BEGIN
    IF NEW.deleted = false THEN
        CALL sp_update_training_summary(NEW.user_id, NEW.training_date);
    END IF;
END //
DELIMITER ;

-- 5.2 训练记录更新后自动更新汇总
DELIMITER //
CREATE TRIGGER IF NOT EXISTS tr_training_after_update
AFTER UPDATE ON training_records
FOR EACH ROW
BEGIN
    -- 更新旧日期的汇总
    IF OLD.training_date != NEW.training_date OR OLD.deleted != NEW.deleted THEN
        CALL sp_update_training_summary(OLD.user_id, OLD.training_date);
    END IF;
    -- 更新新日期的汇总
    IF NEW.deleted = false THEN
        CALL sp_update_training_summary(NEW.user_id, NEW.training_date);
    END IF;
END //
DELIMITER ;

-- ==========================================
-- 第六部分：性能监控查询
-- ==========================================

-- 6.1 查看表大小和索引使用情况（MySQL）
/*
SELECT 
    table_name,
    table_rows,
    ROUND(data_length / 1024 / 1024, 2) as data_size_mb,
    ROUND(index_length / 1024 / 1024, 2) as index_size_mb
FROM information_schema.tables
WHERE table_schema = DATABASE()
ORDER BY data_length DESC;
*/

-- 6.2 查看未使用的索引（MySQL 8.0+）
/*
SELECT 
    object_schema,
    object_name,
    index_name
FROM performance_schema.table_io_waits_summary_by_index_usage
WHERE index_name IS NOT NULL
  AND count_star = 0
  AND object_schema = DATABASE()
ORDER BY object_name;
*/

-- ==========================================
-- 第七部分：数据清理和维护
-- ==========================================

-- 7.1 清理孤儿数据
DELETE FROM exercise_details 
WHERE record_id NOT IN (SELECT id FROM training_records);

-- 7.2 更新统计信息（MySQL）
-- ANALYZE TABLE user_table, training_records, strength_training_data, recovery_data;

-- 7.3 优化表碎片（MySQL）
-- OPTIMIZE TABLE training_records, strength_training_data, recovery_data;

-- ==========================================
-- 执行说明
-- ==========================================
/*
使用说明：
1. 在开发环境（H2）中，部分MySQL特有语法可能不支持
2. 生产环境执行前请先备份数据库
3. 建议在低峰期执行索引创建操作
4. 触发器和存储过程需要根据实际需求启用

执行顺序：
1. 先执行索引优化部分
2. 再执行表结构优化部分
3. 最后执行存储过程和触发器部分

性能监控：
- 定期执行 ANALYZE TABLE 更新统计信息
- 定期执行 OPTIMIZE TABLE 整理表碎片
- 监控慢查询日志，针对性优化
*/
