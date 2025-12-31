-- ========================================
-- H2数据库建表脚本 (开发/测试环境)
-- 版本: 1.0
-- 适配: H2 Database Engine
-- ========================================

-- 1. 用户表 (user_table)
CREATE TABLE user_table (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    email VARCHAR(100),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    role VARCHAR(10) NOT NULL DEFAULT 'USER' CHECK (role IN ('USER', 'ADMIN')),
    age INT DEFAULT 25,
    weight DOUBLE DEFAULT 70.0,
    gender VARCHAR(10),
    height INT,
    experience_level VARCHAR(20),
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 2. 训练记录表 (training_records)
CREATE TABLE training_records (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    exercise_name VARCHAR(100) NOT NULL,
    sets INT NOT NULL,
    reps INT NOT NULL,
    weight DOUBLE NOT NULL,
    training_date DATE NOT NULL,
    duration INT NOT NULL,
    notes VARCHAR(500),
    total_volume DOUBLE,
    training_stress DOUBLE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted BOOLEAN DEFAULT FALSE NOT NULL,
    deleted_at TIMESTAMP NULL,
    deleted_by BIGINT NULL,
    
    FOREIGN KEY (user_id) REFERENCES user_table(id) ON DELETE CASCADE
);

-- 3. 动作详情表 (exercise_details)
CREATE TABLE exercise_details (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    record_id BIGINT NOT NULL,
    exercise_name VARCHAR(100) NOT NULL,
    weight DOUBLE,
    sets INT,
    reps INT,
    rpe INT CHECK (rpe >= 1 AND rpe <= 10),
    volume DOUBLE,
    exercise_type VARCHAR(50),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    FOREIGN KEY (record_id) REFERENCES training_records(id) ON DELETE CASCADE
);

-- 4. 力量训练数据表 (strength_training_data)
CREATE TABLE strength_training_data (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    timestamp TIMESTAMP NOT NULL,
    exercise_name VARCHAR(100) NOT NULL,
    weight DOUBLE NOT NULL,
    sets INT NOT NULL,
    reps INT NOT NULL,
    exercise_type VARCHAR(50) NOT NULL,
    one_rep_max DOUBLE,
    training_volume DOUBLE,
    perceived_exertion INT CHECK (perceived_exertion >= 1 AND perceived_exertion <= 10),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    FOREIGN KEY (user_id) REFERENCES user_table(id) ON DELETE CASCADE
);

-- 5. 有氧训练数据表 (cardio_training_data)
CREATE TABLE cardio_training_data (
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
    perceived_exertion INT CHECK (perceived_exertion >= 1 AND perceived_exertion <= 10),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    FOREIGN KEY (user_id) REFERENCES user_table(id) ON DELETE CASCADE
);

-- 6. 恢复数据表 (recovery_data)
CREATE TABLE recovery_data (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    timestamp TIMESTAMP NOT NULL,
    recovery_score INT NOT NULL CHECK (recovery_score >= 1 AND recovery_score <= 100),
    sleep_hours DOUBLE NOT NULL,
    sleep_quality INT NOT NULL CHECK (sleep_quality >= 1 AND sleep_quality <= 10),
    heart_rate_variability INT NOT NULL,
    resting_heart_rate INT NOT NULL,
    muscle_soreness DOUBLE CHECK (muscle_soreness >= 1 AND muscle_soreness <= 10),
    stress_level DOUBLE CHECK (stress_level >= 1 AND stress_level <= 10),
    notes TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    FOREIGN KEY (user_id) REFERENCES user_table(id) ON DELETE CASCADE
);

-- 7. 恢复指标表 (recovery_metrics)
CREATE TABLE recovery_metrics (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    record_date DATE NOT NULL,
    muscle_soreness INT CHECK (muscle_soreness >= 1 AND muscle_soreness <= 5),
    sleep_quality INT CHECK (sleep_quality >= 1 AND sleep_quality <= 5),
    resting_heart_rate INT,
    subjective_energy INT CHECK (subjective_energy >= 1 AND subjective_energy <= 5),
    
    FOREIGN KEY (user_id) REFERENCES user_table(id) ON DELETE CASCADE,
    UNIQUE (user_id, record_date)
);

-- 8. 健身数据表 (fitness_data)
CREATE TABLE fitness_data (
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
    training_volume DOUBLE,
    perceived_exertion INT CHECK (perceived_exertion >= 1 AND perceived_exertion <= 10),
    
    -- 恢复状态相关字段
    recovery_score INT CHECK (recovery_score >= 0 AND recovery_score <= 100),
    recovery_status VARCHAR(100),
    sleep_hours INT,
    stress_level INT,
    
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    FOREIGN KEY (user_id) REFERENCES user_table(id) ON DELETE CASCADE
);

-- 9. 训练建议表 (training_advices)
CREATE TABLE training_advices (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    advice_date DATE NOT NULL,
    advice_type VARCHAR(50),
    content TEXT,
    confidence_score DOUBLE,
    
    FOREIGN KEY (user_id) REFERENCES user_table(id) ON DELETE CASCADE
);

-- 10. 健身计划表 (training_plans)
CREATE TABLE training_plans (
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
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    FOREIGN KEY (user_id) REFERENCES user_table(id) ON DELETE CASCADE
);

-- 11. 计划每日安排表 (training_plan_days)
CREATE TABLE training_plan_days (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    plan_id BIGINT NOT NULL,
    week_number INT,
    day_of_week INT, -- 0-6 (周一到周日)
    day_name VARCHAR(20),
    has_training BOOLEAN,
    focus VARCHAR(50),
    notes TEXT,
    
    FOREIGN KEY (plan_id) REFERENCES training_plans(id) ON DELETE CASCADE
);

-- 12. 计划动作详情表 (training_plan_exercises)
CREATE TABLE training_plan_exercises (
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
    
    FOREIGN KEY (day_id) REFERENCES training_plan_days(id) ON DELETE CASCADE
);

-- 13. 营养记录表 (nutrition_records)
CREATE TABLE nutrition_records (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    record_date DATE NOT NULL,
    meal_type VARCHAR(20) NOT NULL,
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
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted BOOLEAN DEFAULT FALSE NOT NULL,
    deleted_at TIMESTAMP NULL,
    deleted_by BIGINT NULL,
    
    FOREIGN KEY (user_id) REFERENCES user_table(id) ON DELETE CASCADE
);

-- 14. 审计日志表 (audit_logs)
CREATE TABLE audit_logs (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT,
    username VARCHAR(50),
    action VARCHAR(50) NOT NULL,
    description VARCHAR(500),
    resource_type VARCHAR(50),
    resource_id BIGINT,
    result VARCHAR(20) NOT NULL,
    ip_address VARCHAR(50),
    user_agent VARCHAR(500),
    request_path VARCHAR(200),
    request_method VARCHAR(10),
    error_message VARCHAR(1000),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- ========================================
-- 创建索引 (H2语法)
-- ========================================

-- 用户表索引
CREATE INDEX idx_user_username ON user_table(username);
CREATE INDEX idx_user_email ON user_table(email);
CREATE INDEX idx_user_created_at ON user_table(created_at);

-- 训练记录表索引
CREATE INDEX idx_training_user_id ON training_records(user_id);
CREATE INDEX idx_training_date ON training_records(training_date);
CREATE INDEX idx_training_exercise ON training_records(exercise_name);
CREATE INDEX idx_training_user_date ON training_records(user_id, training_date);
CREATE INDEX idx_training_deleted ON training_records(deleted);
CREATE INDEX idx_training_deleted_at ON training_records(deleted_at);

-- 动作详情表索引
CREATE INDEX idx_detail_record_id ON exercise_details(record_id);
CREATE INDEX idx_detail_exercise ON exercise_details(exercise_name);

-- 力量训练数据表索引
CREATE INDEX idx_strength_user_id ON strength_training_data(user_id);
CREATE INDEX idx_strength_timestamp ON strength_training_data(timestamp);
CREATE INDEX idx_strength_exercise_type ON strength_training_data(exercise_type);
CREATE INDEX idx_strength_user_timestamp ON strength_training_data(user_id, timestamp);

-- 有氧训练数据表索引
CREATE INDEX idx_cardio_user_id ON cardio_training_data(user_id);
CREATE INDEX idx_cardio_timestamp ON cardio_training_data(timestamp);
CREATE INDEX idx_cardio_exercise_type ON cardio_training_data(exercise_type);

-- 恢复数据表索引
CREATE INDEX idx_recovery_user_id ON recovery_data(user_id);
CREATE INDEX idx_recovery_timestamp ON recovery_data(timestamp);
CREATE INDEX idx_recovery_user_timestamp ON recovery_data(user_id, timestamp);

-- 恢复指标表索引
CREATE INDEX idx_metrics_user_id ON recovery_metrics(user_id);
CREATE INDEX idx_metrics_record_date ON recovery_metrics(record_date);
CREATE INDEX idx_metrics_user_date ON recovery_metrics(user_id, record_date);

-- 健身数据表索引
CREATE INDEX idx_fitness_user_id ON fitness_data(user_id);
CREATE INDEX idx_fitness_timestamp ON fitness_data(timestamp);
CREATE INDEX idx_fitness_user_timestamp ON fitness_data(user_id, timestamp);

-- 训练建议表索引
CREATE INDEX idx_advice_user_id ON training_advices(user_id);
CREATE INDEX idx_advice_date ON training_advices(advice_date);
CREATE INDEX idx_advice_type ON training_advices(advice_type);
CREATE INDEX idx_advice_user_date ON training_advices(user_id, advice_date);

-- 健身计划表索引
CREATE INDEX idx_plan_user_id ON training_plans(user_id);
CREATE INDEX idx_plan_status ON training_plans(status);
CREATE INDEX idx_plan_created_at ON training_plans(created_at);

-- 计划每日安排表索引
CREATE INDEX idx_plan_day_plan_id ON training_plan_days(plan_id);

-- 计划动作详情表索引
CREATE INDEX idx_plan_ex_day_id ON training_plan_exercises(day_id);

-- 营养记录表索引
CREATE INDEX idx_nutrition_user_id ON nutrition_records(user_id);
CREATE INDEX idx_nutrition_record_date ON nutrition_records(record_date);
CREATE INDEX idx_nutrition_user_date ON nutrition_records(user_id, record_date);
CREATE INDEX idx_nutrition_deleted ON nutrition_records(deleted);
CREATE INDEX idx_nutrition_deleted_at ON nutrition_records(deleted_at);

-- 审计日志表索引
CREATE INDEX idx_audit_user_id ON audit_logs(user_id);
CREATE INDEX idx_audit_action ON audit_logs(action);
CREATE INDEX idx_audit_created_at ON audit_logs(created_at);
CREATE INDEX idx_audit_user_action ON audit_logs(user_id, action);

-- ========================================
-- 插入初始数据
-- ========================================

-- 插入默认管理员用户 (密码: admin123)
INSERT INTO user_table (username, password, email, role) VALUES 
('admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVEFDa', 'admin@fitness.com', 'ADMIN');

-- 插入测试用户 (密码: test123)
INSERT INTO user_table (username, password, email, role) VALUES 
('testuser', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVEFDa', 'test@fitness.com', 'USER'),
('demo', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVEFDa', 'demo@fitness.com', 'USER');

-- ========================================
-- 创建视图 (H2语法)
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
-- 创建序列 (H2语法)
-- ========================================

-- 如果需要自定义序列，可以创建
-- CREATE SEQUENCE hibernate_sequence START WITH 1 INCREMENT BY 1;

-- ========================================
-- H2数据库特定设置
-- ========================================

-- 设置数据库模式
SET MODE MySQL;

-- 设置时区
SET TIME ZONE 'Asia/Shanghai';

-- 启用外键约束
SET REFERENTIAL_INTEGRITY TRUE;

-- ========================================
-- 数据清理脚本 (测试用)
-- ========================================

/*
-- 清理所有表数据 (保留表结构)
TRUNCATE TABLE training_plan_exercises;
TRUNCATE TABLE training_plan_days;
TRUNCATE TABLE training_plans;
TRUNCATE TABLE training_advices;
TRUNCATE TABLE fitness_data;
TRUNCATE TABLE recovery_metrics;
TRUNCATE TABLE recovery_data;
TRUNCATE TABLE cardio_training_data;
TRUNCATE TABLE strength_training_data;
TRUNCATE TABLE exercise_details;
TRUNCATE TABLE training_records;
TRUNCATE TABLE user_table;

-- 重置自增序列
ALTER TABLE user_table ALTER COLUMN id RESTART WITH 1;
ALTER TABLE training_records ALTER COLUMN id RESTART WITH 1;
ALTER TABLE exercise_details ALTER COLUMN id RESTART WITH 1;
ALTER TABLE strength_training_data ALTER COLUMN id RESTART WITH 1;
ALTER TABLE cardio_training_data ALTER COLUMN id RESTART WITH 1;
ALTER TABLE recovery_data ALTER COLUMN id RESTART WITH 1;
ALTER TABLE recovery_metrics ALTER COLUMN id RESTART WITH 1;
ALTER TABLE fitness_data ALTER COLUMN id RESTART WITH 1;
ALTER TABLE training_advices ALTER COLUMN id RESTART WITH 1;
ALTER TABLE training_plans ALTER COLUMN id RESTART WITH 1;
ALTER TABLE training_plan_days ALTER COLUMN id RESTART WITH 1;
ALTER TABLE training_plan_exercises ALTER COLUMN id RESTART WITH 1;
*/