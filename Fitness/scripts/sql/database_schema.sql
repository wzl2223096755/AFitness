-- ========================================
-- Fitness应用数据库建表脚本
-- 版本: 1.0
-- 创建时间: 2025-01-22
-- 描述: 包含所有核心业务表的创建语句
-- ========================================

-- 1. 用户表 (user_table)
CREATE TABLE user_table (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '用户ID',
    username VARCHAR(50) NOT NULL UNIQUE COMMENT '用户名',
    password VARCHAR(255) NOT NULL COMMENT '加密密码',
    email VARCHAR(100) COMMENT '邮箱地址',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    role ENUM('USER', 'ADMIN') NOT NULL DEFAULT 'USER' COMMENT '用户角色',
    
    INDEX idx_username (username),
    INDEX idx_email (email),
    INDEX idx_created_at (created_at)
) COMMENT '用户基本信息表';

-- 2. 训练记录表 (training_records)
CREATE TABLE training_records (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '训练记录ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    exercise_name VARCHAR(100) NOT NULL COMMENT '运动名称',
    sets INT NOT NULL COMMENT '组数',
    reps INT NOT NULL COMMENT '次数',
    weight DOUBLE NOT NULL COMMENT '重量(kg)',
    training_date DATE NOT NULL COMMENT '训练日期',
    duration INT NOT NULL COMMENT '训练时长(分钟)',
    notes VARCHAR(500) COMMENT '备注',
    total_volume DOUBLE COMMENT '总训练量',
    training_stress DOUBLE COMMENT '训练压力',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    
    FOREIGN KEY (user_id) REFERENCES user_table(id) ON DELETE CASCADE,
    INDEX idx_user_id (user_id),
    INDEX idx_training_date (training_date),
    INDEX idx_exercise_name (exercise_name),
    INDEX idx_user_date (user_id, training_date)
) COMMENT '训练记录主表';

-- 3. 动作详情表 (exercise_details)
CREATE TABLE exercise_details (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '动作详情ID',
    record_id BIGINT NOT NULL COMMENT '训练记录ID',
    exercise_name VARCHAR(100) NOT NULL COMMENT '动作名称',
    weight DOUBLE COMMENT '重量(kg)',
    sets INT COMMENT '组数',
    reps INT COMMENT '次数',
    rpe INT COMMENT '感知疲劳指数(1-10)',
    volume DOUBLE COMMENT '训练量',
    exercise_type VARCHAR(50) COMMENT '动作类型',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    
    FOREIGN KEY (record_id) REFERENCES training_records(id) ON DELETE CASCADE,
    INDEX idx_record_id (record_id),
    INDEX idx_exercise_name (exercise_name)
) COMMENT '训练动作详情表';

-- 4. 力量训练数据表 (strength_training_data)
CREATE TABLE strength_training_data (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '力量训练数据ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    timestamp TIMESTAMP NOT NULL COMMENT '记录时间',
    exercise_name VARCHAR(100) NOT NULL COMMENT '动作名称',
    weight DOUBLE NOT NULL COMMENT '重量(kg)',
    sets INT NOT NULL COMMENT '组数',
    reps INT NOT NULL COMMENT '次数',
    exercise_type VARCHAR(50) NOT NULL COMMENT '动作类型',
    one_rep_max DOUBLE COMMENT '最大重量估算',
    training_volume DOUBLE COMMENT '训练量',
    perceived_exertion INT COMMENT '主观疲劳度(1-10)',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    
    FOREIGN KEY (user_id) REFERENCES user_table(id) ON DELETE CASCADE,
    INDEX idx_user_id (user_id),
    INDEX idx_timestamp (timestamp),
    INDEX idx_exercise_type (exercise_type),
    INDEX idx_user_timestamp (user_id, timestamp)
) COMMENT '力量训练数据表';

-- 5. 有氧训练数据表 (cardio_training_data)
CREATE TABLE cardio_training_data (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '有氧训练数据ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    timestamp TIMESTAMP NOT NULL COMMENT '记录时间',
    exercise_type VARCHAR(100) NOT NULL COMMENT '运动类型',
    duration INT NOT NULL COMMENT '运动时长(分钟)',
    distance DOUBLE COMMENT '距离(km)',
    average_heart_rate INT COMMENT '平均心率',
    max_heart_rate INT COMMENT '最大心率',
    calories_burned DOUBLE COMMENT '消耗卡路里',
    average_speed DOUBLE COMMENT '平均速度(km/h)',
    pace DOUBLE COMMENT '配速(分钟/km)',
    perceived_exertion INT COMMENT '主观疲劳度(1-10)',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    
    FOREIGN KEY (user_id) REFERENCES user_table(id) ON DELETE CASCADE,
    INDEX idx_user_id (user_id),
    INDEX idx_timestamp (timestamp),
    INDEX idx_exercise_type (exercise_type)
) COMMENT '有氧训练数据表';

-- 6. 恢复数据表 (recovery_data)
CREATE TABLE recovery_data (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '恢复数据ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    timestamp TIMESTAMP NOT NULL COMMENT '记录时间',
    recovery_score INT NOT NULL COMMENT '恢复评分(1-100)',
    sleep_hours DOUBLE NOT NULL COMMENT '睡眠时长',
    sleep_quality INT NOT NULL COMMENT '睡眠质量(1-10)',
    heart_rate_variability INT NOT NULL COMMENT '心率变异性',
    resting_heart_rate INT NOT NULL COMMENT '静息心率',
    muscle_soreness DOUBLE COMMENT '肌肉酸痛度(1-10)',
    stress_level DOUBLE COMMENT '压力水平(1-10)',
    notes TEXT COMMENT '恢复备注',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    
    FOREIGN KEY (user_id) REFERENCES user_table(id) ON DELETE CASCADE,
    INDEX idx_user_id (user_id),
    INDEX idx_timestamp (timestamp),
    INDEX idx_user_timestamp (user_id, timestamp)
) COMMENT '恢复状态数据表';

-- 7. 恢复指标表 (recovery_metrics)
CREATE TABLE recovery_metrics (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '恢复指标ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    record_date DATE NOT NULL COMMENT '记录日期',
    muscle_soreness INT COMMENT '肌肉酸痛度(1-5)',
    sleep_quality INT COMMENT '睡眠质量(1-5)',
    resting_heart_rate INT COMMENT '静息心率',
    subjective_energy INT COMMENT '主观能量(1-5)',
    
    FOREIGN KEY (user_id) REFERENCES user_table(id) ON DELETE CASCADE,
    INDEX idx_user_id (user_id),
    INDEX idx_record_date (record_date),
    INDEX idx_user_date (user_id, record_date),
    UNIQUE KEY uk_user_date (user_id, record_date)
) COMMENT '每日恢复指标表';

-- 8. 健身数据表 (fitness_data)
CREATE TABLE fitness_data (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '健身数据ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    timestamp TIMESTAMP NOT NULL COMMENT '数据时间戳',
    
    -- 力量训练相关字段
    exercise_name VARCHAR(100) COMMENT '动作名称',
    weight DOUBLE COMMENT '重量(kg)',
    sets INT COMMENT '组数',
    reps INT COMMENT '次数',
    exercise_type VARCHAR(50) COMMENT '动作类型',
    one_rep_max DOUBLE COMMENT '最大重量估算',
    training_volume DOUBLE COMMENT '训练量',
    perceived_exertion INT COMMENT '主观疲劳度(1-10)',
    
    -- 恢复状态相关字段
    recovery_score INT COMMENT '恢复评分(0-100)',
    recovery_status VARCHAR(100) COMMENT '恢复状态描述',
    sleep_hours INT COMMENT '睡眠时长',
    stress_level INT COMMENT '压力水平',
    
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    
    FOREIGN KEY (user_id) REFERENCES user_table(id) ON DELETE CASCADE,
    INDEX idx_user_id (user_id),
    INDEX idx_timestamp (timestamp),
    INDEX idx_user_timestamp (user_id, timestamp)
) COMMENT '综合健身数据表';

-- 9. 训练建议表 (training_advices)
CREATE TABLE training_advices (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '训练建议ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    advice_date DATE NOT NULL COMMENT '建议日期',
    advice_type VARCHAR(50) COMMENT '建议类型',
    content TEXT COMMENT '建议内容',
    confidence_score DOUBLE COMMENT '置信度评分',
    
    FOREIGN KEY (user_id) REFERENCES user_table(id) ON DELETE CASCADE,
    INDEX idx_user_id (user_id),
    INDEX idx_advice_date (advice_date),
    INDEX idx_advice_type (advice_type),
    INDEX idx_user_date (user_id, advice_date)
) COMMENT '智能训练建议表';

-- ========================================
-- 插入初始数据
-- ========================================

-- 插入默认管理员用户
INSERT INTO user_table (username, password, email, role) VALUES 
('admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVEFDa', 'admin@fitness.com', 'ADMIN');

-- 插入测试用户
INSERT INTO user_table (username, password, email, role) VALUES 
('testuser', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVEFDa', 'test@fitness.com', 'USER'),
('demo', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVEFDa', 'demo@fitness.com', 'USER');

-- ========================================
-- 创建视图 (可选)
-- ========================================

-- 用户训练统计视图
CREATE VIEW user_training_stats AS
SELECT 
    u.id as user_id,
    u.username,
    COUNT(tr.id) as total_training_sessions,
    SUM(tr.duration) as total_training_minutes,
    AVG(tr.weight) as avg_weight,
    MAX(tr.training_date) as last_training_date
FROM user_table u
LEFT JOIN training_records tr ON u.id = tr.user_id
GROUP BY u.id, u.username;

-- 用户恢复统计视图
CREATE VIEW user_recovery_stats AS
SELECT 
    u.id as user_id,
    u.username,
    AVG(rd.recovery_score) as avg_recovery_score,
    AVG(rd.sleep_hours) as avg_sleep_hours,
    AVG(rd.sleep_quality) as avg_sleep_quality,
    COUNT(rd.id) as total_recovery_records
FROM user_table u
LEFT JOIN recovery_data rd ON u.id = rd.user_id
GROUP BY u.id, u.username;

-- ========================================
-- 创建存储过程 (可选)
-- ========================================

-- 计算用户训练总量的存储过程
DELIMITER //
CREATE PROCEDURE CalculateUserTrainingVolume(IN user_id_param BIGINT)
BEGIN
    SELECT 
        SUM(weight * sets * reps) as total_volume,
        COUNT(*) as total_exercises,
        AVG(weight) as avg_weight,
        MAX(weight) as max_weight
    FROM training_records 
    WHERE user_id = user_id_param;
END //
DELIMITER ;

-- 获取用户最近7天恢复数据的存储过程
DELIMITER //
CREATE PROCEDURE GetUserRecentRecovery(IN user_id_param BIGINT)
BEGIN
    SELECT 
        DATE(timestamp) as recovery_date,
        recovery_score,
        sleep_hours,
        sleep_quality,
        muscle_soreness,
        stress_level
    FROM recovery_data 
    WHERE user_id = user_id_param 
    AND timestamp >= DATE_SUB(CURDATE(), INTERVAL 7 DAY)
    ORDER BY timestamp DESC;
END //
DELIMITER ;

-- ========================================
-- 数据库性能优化设置
-- ========================================

-- 设置字符集
ALTER DATABASE CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- 设置InnoDB引擎参数 (MySQL 8.0+)
-- SET GLOBAL innodb_file_per_table = ON;
-- SET GLOBAL innodb_flush_log_at_trx_commit = 2;
-- SET GLOBAL innodb_buffer_pool_size = 1073741824; -- 1GB

-- ========================================
-- 备份和恢复脚本模板
-- ========================================

/*
-- 备份数据库
mysqldump -u username -p fitness_db > fitness_backup_$(date +%Y%m%d_%H%M%S).sql

-- 恢复数据库
mysql -u username -p fitness_db < fitness_backup_20250122_120000.sql

-- 定期清理旧数据 (保留1年)
DELETE FROM training_records 
WHERE training_date < DATE_SUB(CURDATE(), INTERVAL 1 YEAR);

DELETE FROM recovery_data 
WHERE timestamp < DATE_SUB(NOW(), INTERVAL 1 YEAR);
*/