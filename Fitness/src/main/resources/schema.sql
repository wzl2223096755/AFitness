-- =====================================================
-- AFitness 数据库结构 (MySQL 8.0 优化版)
-- =====================================================

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- -----------------------------------------------------
-- 1. 用户表
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `user_table` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `username` VARCHAR(50) NOT NULL COMMENT '用户名',
    `password` VARCHAR(255) NOT NULL COMMENT '加密密码',
    `email` VARCHAR(100) COMMENT '邮箱',
    `age` INT DEFAULT 25 COMMENT '年龄',
    `weight` DOUBLE DEFAULT 70.0 COMMENT '体重(kg)',
    `gender` VARCHAR(10) COMMENT '性别',
    `height` INT COMMENT '身高(cm)',
    `experience_level` VARCHAR(20) COMMENT '经验等级',
    `avatar` VARCHAR(255) COMMENT '头像URL',
    `points` INT DEFAULT 0 COMMENT '积分',
    `role` VARCHAR(20) NOT NULL DEFAULT 'USER' COMMENT '角色',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `last_login_at` DATETIME,
    UNIQUE KEY `uk_username` (`username`),
    KEY `idx_user_role` (`role`),
    KEY `idx_user_email` (`email`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户表';

-- -----------------------------------------------------
-- 2. 用户设置表
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `user_settings` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `theme` VARCHAR(20) DEFAULT 'light' COMMENT '主题',
    `language` VARCHAR(10) DEFAULT 'zh-CN' COMMENT '语言',
    `notifications` TINYINT(1) DEFAULT 1 COMMENT '通知开关',
    `auto_save` TINYINT(1) DEFAULT 1 COMMENT '自动保存',
    UNIQUE KEY `uk_user_setting` (`user_id`),
    CONSTRAINT `fk_setting_user` FOREIGN KEY (`user_id`) REFERENCES `user_table` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户设置表';

-- -----------------------------------------------------
-- 3. 身体数据记录表
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `body_records` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `user_id` BIGINT NOT NULL,
    `weight` DOUBLE NOT NULL COMMENT '体重(kg)',
    `body_fat` DOUBLE COMMENT '体脂率(%)',
    `muscle_mass` DOUBLE COMMENT '肌肉量(kg)',
    `waist_circumference` DOUBLE COMMENT '腰围(cm)',
    `hip_circumference` DOUBLE COMMENT '臀围(cm)',
    `chest_circumference` DOUBLE COMMENT '胸围(cm)',
    `record_time` DATETIME COMMENT '记录时间',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
    KEY `idx_body_user` (`user_id`),
    KEY `idx_body_time` (`user_id`, `record_time`),
    CONSTRAINT `fk_body_user` FOREIGN KEY (`user_id`) REFERENCES `user_table` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='身体数据记录表';

-- -----------------------------------------------------
-- 4. 训练计划表
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `training_plans` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `user_id` BIGINT NOT NULL,
    `name` VARCHAR(100) NOT NULL COMMENT '计划名称',
    `description` TEXT COMMENT '计划描述',
    `start_date` DATE,
    `end_date` DATE,
    `status` VARCHAR(20) DEFAULT 'ACTIVE' COMMENT 'ACTIVE/COMPLETED/PAUSED',
    `is_weekly` TINYINT(1) DEFAULT 0,
    `goal` VARCHAR(50) COMMENT '目标',
    `level` VARCHAR(20) COMMENT '难度',
    `duration_weeks` INT,
    `days_per_week` INT,
    `duration_per_session` INT COMMENT '每次时长(分钟)',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    KEY `idx_plan_user` (`user_id`),
    KEY `idx_plan_status` (`user_id`, `status`),
    CONSTRAINT `fk_plan_user` FOREIGN KEY (`user_id`) REFERENCES `user_table` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='训练计划表';


-- -----------------------------------------------------
-- 5. 训练计划天数表
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `training_plan_days` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `plan_id` BIGINT NOT NULL,
    `day_of_week` INT COMMENT '星期几(0-6)',
    `week_number` INT COMMENT '第几周',
    `day_name` VARCHAR(20) COMMENT '训练日名称',
    `has_training` TINYINT(1) DEFAULT 1,
    `focus` VARCHAR(50) COMMENT '训练重点',
    `notes` TEXT,
    KEY `idx_day_plan` (`plan_id`),
    CONSTRAINT `fk_day_plan` FOREIGN KEY (`plan_id`) REFERENCES `training_plans` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='训练计划天数表';

-- -----------------------------------------------------
-- 6. 训练计划动作表
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `training_plan_exercises` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `day_id` BIGINT NOT NULL,
    `name` VARCHAR(100) NOT NULL COMMENT '动作名称',
    `sets` INT COMMENT '组数',
    `reps` VARCHAR(50) COMMENT '次数',
    `weight` DOUBLE COMMENT '重量(kg)',
    `duration_minutes` DOUBLE COMMENT '时长(分钟)',
    `intensity` INT COMMENT '强度(1-10)',
    `target_muscles` VARCHAR(200) COMMENT '目标肌群',
    `rest_time` INT COMMENT '休息时间(秒)',
    `notes` TEXT,
    `order_index` INT COMMENT '顺序',
    `completed` TINYINT(1) DEFAULT 0,
    KEY `idx_exercise_day` (`day_id`),
    CONSTRAINT `fk_exercise_day` FOREIGN KEY (`day_id`) REFERENCES `training_plan_days` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='训练计划动作表';

-- -----------------------------------------------------
-- 7. 训练记录表 (支持软删除)
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `training_records` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `user_id` BIGINT NOT NULL,
    `exercise_name` VARCHAR(100) NOT NULL COMMENT '运动名称',
    `sets` INT NOT NULL COMMENT '组数',
    `reps` INT NOT NULL COMMENT '次数',
    `weight` DOUBLE NOT NULL COMMENT '重量(kg)',
    `training_date` DATE NOT NULL COMMENT '训练日期',
    `duration` INT NOT NULL COMMENT '时长(分钟)',
    `notes` VARCHAR(500) COMMENT '备注',
    `total_volume` DOUBLE COMMENT '总训练量',
    `training_stress` DOUBLE COMMENT '训练压力',
    `deleted` TINYINT(1) DEFAULT 0 COMMENT '软删除标记',
    `deleted_at` DATETIME COMMENT '删除时间',
    `deleted_by` BIGINT COMMENT '删除人',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    KEY `idx_record_user` (`user_id`),
    KEY `idx_record_date` (`user_id`, `training_date`),
    KEY `idx_record_deleted` (`deleted`),
    CONSTRAINT `fk_record_user` FOREIGN KEY (`user_id`) REFERENCES `user_table` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='训练记录表';

-- -----------------------------------------------------
-- 8. 训练动作详情表
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `exercise_details` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `record_id` BIGINT NOT NULL,
    `exercise_name` VARCHAR(100) NOT NULL,
    `weight` DOUBLE,
    `sets` INT,
    `reps` INT,
    `rpe` INT COMMENT 'RPE(1-10)',
    `volume` DOUBLE COMMENT '训练量',
    `exercise_type` VARCHAR(50),
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
    KEY `idx_detail_record` (`record_id`),
    CONSTRAINT `fk_detail_record` FOREIGN KEY (`record_id`) REFERENCES `training_records` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='训练动作详情表';

-- -----------------------------------------------------
-- 9. 力量训练数据表
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `strength_training_data` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `user_id` BIGINT NOT NULL,
    `timestamp` DATETIME NOT NULL,
    `exercise_name` VARCHAR(100) NOT NULL,
    `weight` DOUBLE NOT NULL,
    `sets` INT NOT NULL,
    `reps` INT NOT NULL,
    `exercise_type` VARCHAR(50) NOT NULL COMMENT '动作类型',
    `one_rep_max` DOUBLE COMMENT '1RM估算',
    `training_volume` DOUBLE COMMENT '训练量',
    `perceived_exertion` INT COMMENT 'RPE(1-10)',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
    KEY `idx_strength_user` (`user_id`),
    KEY `idx_strength_time` (`user_id`, `timestamp`),
    KEY `idx_strength_type` (`user_id`, `exercise_type`),
    CONSTRAINT `fk_strength_user` FOREIGN KEY (`user_id`) REFERENCES `user_table` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='力量训练数据表';

-- -----------------------------------------------------
-- 10. 有氧训练数据表
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `cardio_training_data` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `user_id` BIGINT NOT NULL,
    `timestamp` DATETIME NOT NULL,
    `exercise_type` VARCHAR(100) NOT NULL COMMENT '运动类型',
    `duration` INT NOT NULL COMMENT '时长(分钟)',
    `distance` DOUBLE COMMENT '距离(km)',
    `average_heart_rate` INT COMMENT '平均心率',
    `max_heart_rate` INT COMMENT '最大心率',
    `calories_burned` DOUBLE COMMENT '消耗卡路里',
    `average_speed` DOUBLE COMMENT '平均速度(km/h)',
    `pace` DOUBLE COMMENT '配速(min/km)',
    `perceived_exertion` INT COMMENT 'RPE(1-10)',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
    KEY `idx_cardio_user` (`user_id`),
    KEY `idx_cardio_time` (`user_id`, `timestamp`),
    KEY `idx_cardio_type` (`user_id`, `exercise_type`),
    CONSTRAINT `fk_cardio_user` FOREIGN KEY (`user_id`) REFERENCES `user_table` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='有氧训练数据表';


-- -----------------------------------------------------
-- 11. 恢复数据表
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `recovery_data` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `user_id` BIGINT NOT NULL,
    `timestamp` DATETIME NOT NULL,
    `recovery_score` INT NOT NULL COMMENT '恢复评分(1-100)',
    `sleep_hours` DOUBLE NOT NULL COMMENT '睡眠时长',
    `sleep_quality` INT NOT NULL COMMENT '睡眠质量(1-10)',
    `heart_rate_variability` INT NOT NULL COMMENT 'HRV',
    `resting_heart_rate` INT NOT NULL COMMENT '静息心率',
    `muscle_soreness` DOUBLE COMMENT '肌肉酸痛(1-10)',
    `stress_level` DOUBLE COMMENT '压力水平(1-10)',
    `notes` VARCHAR(500),
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
    KEY `idx_recovery_user` (`user_id`),
    KEY `idx_recovery_time` (`user_id`, `timestamp`),
    KEY `idx_recovery_score` (`user_id`, `recovery_score`),
    CONSTRAINT `fk_recovery_user` FOREIGN KEY (`user_id`) REFERENCES `user_table` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='恢复数据表';

-- -----------------------------------------------------
-- 12. 恢复指标表 (日记形式)
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `recovery_metrics` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `user_id` BIGINT NOT NULL,
    `record_date` DATE NOT NULL,
    `muscle_soreness` INT COMMENT '肌肉酸痛(1-5)',
    `sleep_quality` INT COMMENT '睡眠质量(1-5)',
    `resting_heart_rate` INT COMMENT '静息心率',
    `subjective_energy` INT COMMENT '主观精力(1-5)',
    KEY `idx_metric_user` (`user_id`),
    KEY `idx_metric_date` (`user_id`, `record_date`),
    UNIQUE KEY `uk_metric_user_date` (`user_id`, `record_date`),
    CONSTRAINT `fk_metric_user` FOREIGN KEY (`user_id`) REFERENCES `user_table` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='恢复指标表';

-- -----------------------------------------------------
-- 13. 营养记录表 (支持软删除)
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `nutrition_records` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `user_id` BIGINT NOT NULL,
    `record_date` DATE NOT NULL,
    `meal_type` VARCHAR(20) NOT NULL COMMENT '餐次类型',
    `food_name` VARCHAR(100) NOT NULL COMMENT '食物名称',
    `calories` DOUBLE NOT NULL COMMENT '卡路里',
    `protein` DOUBLE COMMENT '蛋白质(g)',
    `carbs` DOUBLE COMMENT '碳水(g)',
    `fat` DOUBLE COMMENT '脂肪(g)',
    `fiber` DOUBLE COMMENT '纤维(g)',
    `sugar` DOUBLE COMMENT '糖分(g)',
    `sodium` DOUBLE COMMENT '钠(mg)',
    `amount` DOUBLE COMMENT '份量(g)',
    `notes` TEXT,
    `deleted` TINYINT(1) DEFAULT 0,
    `deleted_at` DATETIME,
    `deleted_by` BIGINT,
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    KEY `idx_nutrition_user` (`user_id`),
    KEY `idx_nutrition_date` (`user_id`, `record_date`),
    KEY `idx_nutrition_meal` (`user_id`, `record_date`, `meal_type`),
    CONSTRAINT `fk_nutrition_user` FOREIGN KEY (`user_id`) REFERENCES `user_table` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='营养记录表';

-- -----------------------------------------------------
-- 14. 训练建议表
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `training_advices` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `user_id` BIGINT NOT NULL,
    `advice_date` DATE NOT NULL,
    `advice_type` VARCHAR(50) COMMENT '建议类型',
    `content` TEXT NOT NULL COMMENT '建议内容',
    `confidence_score` DOUBLE COMMENT '置信度',
    KEY `idx_advice_user` (`user_id`),
    KEY `idx_advice_date` (`user_id`, `advice_date`),
    CONSTRAINT `fk_advice_user` FOREIGN KEY (`user_id`) REFERENCES `user_table` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='训练建议表';

-- -----------------------------------------------------
-- 15. 用户成就表
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `user_achievements` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `user_id` BIGINT NOT NULL,
    `name` VARCHAR(100) NOT NULL COMMENT '成就名称',
    `description` VARCHAR(255) COMMENT '成就描述',
    `icon` VARCHAR(100) COMMENT '图标',
    `unlocked` TINYINT(1) DEFAULT 0 COMMENT '是否解锁',
    `unlock_time` DATETIME COMMENT '解锁时间',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
    KEY `idx_achievement_user` (`user_id`),
    KEY `idx_achievement_unlocked` (`user_id`, `unlocked`),
    CONSTRAINT `fk_achievement_user` FOREIGN KEY (`user_id`) REFERENCES `user_table` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户成就表';

-- -----------------------------------------------------
-- 16. 用户营养目标表
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `user_nutrition_goals` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `user_id` BIGINT NOT NULL,
    `training_goal` VARCHAR(50) DEFAULT 'maintenance' COMMENT '训练目标: fat_loss, muscle_gain, maintenance',
    `activity_level` VARCHAR(20) DEFAULT 'moderate' COMMENT '活动水平: sedentary, light, moderate, active, very_active',
    `target_calories` DOUBLE COMMENT '目标卡路里',
    `target_protein` DOUBLE COMMENT '目标蛋白质(g)',
    `target_carbs` DOUBLE COMMENT '目标碳水(g)',
    `target_fat` DOUBLE COMMENT '目标脂肪(g)',
    `use_custom_targets` TINYINT(1) DEFAULT 0 COMMENT '是否使用自定义目标',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY `uk_nutrition_goal_user` (`user_id`),
    CONSTRAINT `fk_nutrition_goal_user` FOREIGN KEY (`user_id`) REFERENCES `user_table` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户营养目标表';

-- -----------------------------------------------------
-- 17. 审计日志表
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `audit_logs` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `user_id` BIGINT,
    `username` VARCHAR(50),
    `action` VARCHAR(50) NOT NULL COMMENT '操作类型',
    `description` VARCHAR(500),
    `resource_type` VARCHAR(50),
    `resource_id` BIGINT,
    `result` VARCHAR(20) NOT NULL COMMENT '操作结果',
    `ip_address` VARCHAR(50),
    `user_agent` VARCHAR(500),
    `request_path` VARCHAR(200),
    `request_method` VARCHAR(10),
    `error_message` VARCHAR(1000),
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
    KEY `idx_audit_user` (`user_id`),
    KEY `idx_audit_action` (`action`),
    KEY `idx_audit_time` (`created_at`),
    KEY `idx_audit_user_action` (`user_id`, `action`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='审计日志表';

SET FOREIGN_KEY_CHECKS = 1;
