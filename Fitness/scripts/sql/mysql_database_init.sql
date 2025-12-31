-- ========================================
-- MySQL数据库初始化脚本
-- 版本: 1.0
-- 适配: MySQL 8.0+
-- ========================================

-- 创建数据库
CREATE DATABASE IF NOT EXISTS fitness_db 
CHARACTER SET utf8mb4 
COLLATE utf8mb4_unicode_ci;

USE fitness_db;

-- 1. 用户表
CREATE TABLE IF NOT EXISTS user_table (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    email VARCHAR(100),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    role VARCHAR(10) NOT NULL DEFAULT 'USER',
    age INT DEFAULT 25,
    weight DOUBLE DEFAULT 70.0,
    gender VARCHAR(10),
    height INT,
    experience_level VARCHAR(20),
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    INDEX idx_username (username),
    INDEX idx_email (email),
    INDEX idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 2. 训练记录表
CREATE TABLE IF NOT EXISTS training_records (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    exercise_name VARCHAR(100) NOT NULL,
    sets INT NOT NULL,
    reps INT NOT NULL,
    weight DOUBLE NOT NULL,
    training_date DATE NOT NULL,
    duration INT NOT NULL,
    notes VARCHAR(500),
    total_volume DOUBLE GENERATED ALWAYS AS (weight * sets * reps) STORED,
    training_stress DOUBLE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    FOREIGN KEY (user_id) REFERENCES user_table(id) ON DELETE CASCADE,
    INDEX idx_user_id (user_id),
    INDEX idx_training_date (training_date),
    INDEX idx_exercise_name (exercise_name),
    INDEX idx_user_date (user_id, training_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 3. 动作详情表
CREATE TABLE IF NOT EXISTS exercise_details (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    record_id BIGINT NOT NULL,
    exercise_name VARCHAR(100) NOT NULL,
    weight DOUBLE,
    sets INT,
    reps INT,
    rpe INT COMMENT '主观疲劳度 1-10',
    volume DOUBLE GENERATED ALWAYS AS (weight * sets * reps) STORED,
    exercise_type VARCHAR(50),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    FOREIGN KEY (record_id) REFERENCES training_records(id) ON DELETE CASCADE,
    INDEX idx_record_id (record_id),
    INDEX idx_exercise_name (exercise_name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 4. 力量训练数据表
CREATE TABLE IF NOT EXISTS strength_training_data (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    timestamp TIMESTAMP NOT NULL,
    exercise_name VARCHAR(100) NOT NULL,
    weight DOUBLE NOT NULL,
    sets INT NOT NULL,
    reps INT NOT NULL,
    exercise_type VARCHAR(50) NOT NULL,
    one_rep_max DOUBLE,
    training_volume DOUBLE GENERATED ALWAYS AS (weight * sets * reps) STORED,
    perceived_exertion INT COMMENT '主观疲劳度 1-10',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    FOREIGN KEY (user_id) REFERENCES user_table(id) ON DELETE CASCADE,
    INDEX idx_user_id (user_id),
    INDEX idx_timestamp (timestamp),
    INDEX idx_exercise_type (exercise_type),
    INDEX idx_user_timestamp (user_id, timestamp)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 5. 有氧训练数据表
CREATE TABLE IF NOT EXISTS cardio_training_data (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    timestamp TIMESTAMP NOT NULL,
    exercise_type VARCHAR(100) NOT NULL,
    duration INT NOT NULL,
    distance DOUBLE,
    average_heart_rate INT,
    max_heart_rate INT,
    calories_burned DOUBLE,
    average_speed DOUBLE,
    pace DOUBLE,
    perceived_exertion INT COMMENT '主观疲劳度 1-10',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    FOREIGN KEY (user_id) REFERENCES user_table(id) ON DELETE CASCADE,
    INDEX idx_user_id (user_id),
    INDEX idx_timestamp (timestamp),
    INDEX idx_exercise_type (exercise_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 6. 恢复数据表
CREATE TABLE IF NOT EXISTS recovery_data (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    timestamp TIMESTAMP NOT NULL,
    recovery_score INT NOT NULL COMMENT '恢复评分 1-100',
    sleep_hours DOUBLE NOT NULL,
    sleep_quality INT NOT NULL COMMENT '睡眠质量 1-10',
    heart_rate_variability INT NOT NULL,
    resting_heart_rate INT NOT NULL,
    muscle_soreness DOUBLE COMMENT '肌肉酸痛度 1-10',
    stress_level DOUBLE COMMENT '压力水平 1-10',
    notes TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    FOREIGN KEY (user_id) REFERENCES user_table(id) ON DELETE CASCADE,
    INDEX idx_user_id (user_id),
    INDEX idx_timestamp (timestamp),
    INDEX idx_user_timestamp (user_id, timestamp)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 7. 恢复指标表
CREATE TABLE IF NOT EXISTS recovery_metrics (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    record_date DATE NOT NULL,
    muscle_soreness INT COMMENT '肌肉酸痛度 1-5',
    sleep_quality INT COMMENT '睡眠质量 1-5',
    resting_heart_rate INT,
    subjective_energy INT COMMENT '主观能量 1-5',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    FOREIGN KEY (user_id) REFERENCES user_table(id) ON DELETE CASCADE,
    UNIQUE KEY uk_user_date (user_id, record_date),
    INDEX idx_user_id (user_id),
    INDEX idx_record_date (record_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 8. 健身数据综合表
CREATE TABLE IF NOT EXISTS fitness_data (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    timestamp TIMESTAMP NOT NULL,
    
    -- 力量训练相关字段
    exercise_name VARCHAR(100),
    weight DOUBLE,
    sets INT,
    reps INT,
    exercise_type VARCHAR(50),
    one_rep_max DOUBLE,
    training_volume DOUBLE GENERATED ALWAYS AS (weight * sets * reps) STORED,
    perceived_exertion INT COMMENT '主观疲劳度 1-10',
    
    -- 恢复状态相关字段
    recovery_score INT COMMENT '恢复评分 0-100',
    recovery_status VARCHAR(100),
    sleep_hours INT,
    stress_level INT,
    
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    FOREIGN KEY (user_id) REFERENCES user_table(id) ON DELETE CASCADE,
    INDEX idx_user_id (user_id),
    INDEX idx_timestamp (timestamp),
    INDEX idx_exercise_type (exercise_type),
    INDEX idx_user_timestamp (user_id, timestamp)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 9. 训练建议表
CREATE TABLE IF NOT EXISTS training_advices (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    advice_date DATE NOT NULL,
    advice_type VARCHAR(50),
    content TEXT,
    confidence_score DOUBLE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    FOREIGN KEY (user_id) REFERENCES user_table(id) ON DELETE CASCADE,
    INDEX idx_user_id (user_id),
    INDEX idx_advice_date (advice_date),
    INDEX idx_advice_type (advice_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 10. 训练计划表
CREATE TABLE IF NOT EXISTS training_plans (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    name VARCHAR(100) NOT NULL,
    description TEXT,
    start_date DATE,
    end_date DATE,
    status VARCHAR(20) DEFAULT 'ACTIVE',
    is_weekly BOOLEAN DEFAULT FALSE,
    goal VARCHAR(20),
    level VARCHAR(20),
    duration_weeks INT,
    days_per_week INT,
    duration_per_session INT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    FOREIGN KEY (user_id) REFERENCES user_table(id) ON DELETE CASCADE,
    INDEX idx_user_id (user_id),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 11. 计划每日安排表
CREATE TABLE IF NOT EXISTS training_plan_days (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    plan_id BIGINT NOT NULL,
    week_number INT,
    day_of_week INT, -- 0-6 (周一到周日)
    day_name VARCHAR(20),
    has_training BOOLEAN,
    focus VARCHAR(50),
    notes TEXT,
    
    FOREIGN KEY (plan_id) REFERENCES training_plans(id) ON DELETE CASCADE,
    INDEX idx_plan_id (plan_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 12. 计划动作详情表
CREATE TABLE IF NOT EXISTS training_plan_exercises (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    day_id BIGINT NOT NULL,
    name VARCHAR(100) NOT NULL,
    sets INT,
    reps VARCHAR(50),
    weight DOUBLE,
    duration_minutes DOUBLE,
    intensity INT,
    target_muscles VARCHAR(200),
    rest_time INT, -- 秒
    notes TEXT,
    order_index INT,
    completed BOOLEAN DEFAULT FALSE,
    
    FOREIGN KEY (day_id) REFERENCES training_plan_days(id) ON DELETE CASCADE,
    INDEX idx_day_id (day_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 13. 营养记录表
CREATE TABLE IF NOT EXISTS nutrition_records (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    record_date DATE NOT NULL,
    meal_type VARCHAR(20) NOT NULL COMMENT '餐次：早餐、午餐、晚餐、加餐',
    food_name VARCHAR(100) NOT NULL,
    calories DOUBLE NOT NULL,
    protein DOUBLE,
    carbs DOUBLE,
    fat DOUBLE,
    fiber DOUBLE,
    sugar DOUBLE,
    sodium DOUBLE,
    amount DOUBLE,
    notes TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    FOREIGN KEY (user_id) REFERENCES user_table(id) ON DELETE CASCADE,
    INDEX idx_user_id (user_id),
    INDEX idx_record_date (record_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 13. 身体测量数据表
CREATE TABLE IF NOT EXISTS body_measurements (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    measurement_date DATE NOT NULL,
    weight DOUBLE COMMENT '体重(kg)',
    body_fat_percentage DOUBLE COMMENT '体脂率(%)',
    muscle_mass DOUBLE COMMENT '肌肉量(kg)',
    bmi DOUBLE GENERATED ALWAYS AS (weight / POWER(height/100, 2)) STORED,
    height DOUBLE COMMENT '身高(cm)',
    chest_circumference DOUBLE COMMENT '胸围(cm)',
    waist_circumference DOUBLE COMMENT '腰围(cm)',
    hip_circumference DOUBLE COMMENT '臀围(cm)',
    arm_circumference DOUBLE COMMENT '臂围(cm)',
    thigh_circumference DOUBLE COMMENT '大腿围(cm)',
    notes TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    FOREIGN KEY (user_id) REFERENCES user_table(id) ON DELETE CASCADE,
    INDEX idx_user_id (user_id),
    INDEX idx_measurement_date (measurement_date),
    UNIQUE KEY uk_user_date (user_id, measurement_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 插入默认管理员用户
INSERT IGNORE INTO user_table (id, username, password, email, role) 
VALUES (1, 'admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVEFDa', 'admin@fitness.com', 'ADMIN');

-- 插入示例训练计划
INSERT IGNORE INTO training_plans (id, user_id, name, description, goal, level, duration_weeks, days_per_week, status, is_weekly) 
VALUES 
(1, 1, '新手增肌计划', '适合健身新手的全身增肌训练计划', '增肌', '初级', 8, 3, 'ACTIVE', TRUE),
(2, 1, '中级减脂计划', '适合有一定基础的减脂训练计划', '减脂', '中级', 12, 4, 'INACTIVE', TRUE);

-- 插入示例计划每日安排
INSERT IGNORE INTO training_plan_days (id, plan_id, day_of_week, day_name, has_training, focus)
VALUES
(1, 1, 1, '周一', TRUE, '全身训练 A'),
(2, 1, 2, '周二', FALSE, '休息'),
(3, 1, 3, '周三', TRUE, '全身训练 B'),
(4, 1, 4, '周四', FALSE, '休息'),
(5, 1, 5, '周五', TRUE, '全身训练 C'),
(6, 1, 6, '周六', FALSE, '休息'),
(7, 1, 0, '周日', FALSE, '休息');

-- 插入示例计划动作
INSERT IGNORE INTO training_plan_exercises (day_id, name, sets, reps, weight, target_muscles, rest_time, order_index) 
VALUES 
(1, '深蹲', 3, '8-12', 60.0, '腿部', 90, 1),
(1, '卧推', 3, '8-12', 50.0, '胸部', 90, 2),
(1, '杠铃划船', 3, '8-12', 40.0, '背部', 90, 3),
(3, '硬拉', 3, '5-8', 80.0, '全身', 120, 1),
(3, '引体向上', 3, '6-10', 0.0, '背部', 90, 2),
(3, '推举', 3, '8-12', 30.0, '肩部', 90, 3),
(5, '腿举', 3, '12-15', 120.0, '腿部', 90, 1),
(5, '双杠臂屈伸', 3, '8-12', 0.0, '肱三头肌', 60, 2),
(5, '哑铃弯举', 3, '12-15', 10.0, '肱二头肌', 60, 3);

COMMIT;