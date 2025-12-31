-- AFitness Database Schema (MySQL 8.0)

-- 1. 用户表
CREATE TABLE IF NOT EXISTS `user_table` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `username` VARCHAR(50) NOT NULL UNIQUE COMMENT '用户名',
    `password` VARCHAR(255) NOT NULL COMMENT '加密后的密码',
    `email` VARCHAR(100) COMMENT '邮箱',
    `age` INT DEFAULT 25 COMMENT '年龄',
    `weight` DOUBLE DEFAULT 70.0 COMMENT '体重(kg)',
    `gender` VARCHAR(10) COMMENT '性别',
    `height` INT COMMENT '身高(cm)',
    `experience_level` VARCHAR(20) COMMENT '经验等级',
    `avatar` VARCHAR(255) COMMENT '头像URL',
    `points` INT DEFAULT 0 COMMENT '积分',
    `role` VARCHAR(20) NOT NULL DEFAULT 'USER' COMMENT '角色: USER, ADMIN',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `last_login_at` DATETIME
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户信息表';

-- 2. 训练计划表
CREATE TABLE IF NOT EXISTS `training_plans` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `user_id` BIGINT NOT NULL,
    `name` VARCHAR(100) NOT NULL COMMENT '计划名称',
    `description` TEXT COMMENT '计划描述',
    `start_date` DATE,
    `end_date` DATE,
    `status` VARCHAR(20) DEFAULT 'ACTIVE' COMMENT '状态: ACTIVE, COMPLETED, PAUSED',
    `is_weekly` BOOLEAN DEFAULT FALSE,
    `goal` VARCHAR(50) COMMENT '目标: 减脂, 增肌等',
    `level` VARCHAR(20) COMMENT '难度等级',
    `duration_weeks` INT,
    `days_per_week` INT,
    `duration_per_session` INT,
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT `fk_plan_user` FOREIGN KEY (`user_id`) REFERENCES `user_table` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='训练计划表';

-- 3. 计划天数表
CREATE TABLE IF NOT EXISTS `training_plan_days` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `plan_id` BIGINT NOT NULL,
    `day_number` INT NOT NULL COMMENT '第几天(1-7)',
    `name` VARCHAR(50) COMMENT '训练日名称(如: 胸部训练)',
    CONSTRAINT `fk_day_plan` FOREIGN KEY (`plan_id`) REFERENCES `training_plans` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='计划训练日表';

-- 4. 计划动作表
CREATE TABLE IF NOT EXISTS `training_plan_exercises` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `day_id` BIGINT NOT NULL,
    `exercise_name` VARCHAR(100) NOT NULL COMMENT '动作名称',
    `sets` INT COMMENT '组数',
    `reps` INT COMMENT '次数',
    `weight` DOUBLE COMMENT '重量',
    `order_index` INT COMMENT '执行顺序',
    `is_completed` BOOLEAN DEFAULT FALSE COMMENT '是否已完成',
    CONSTRAINT `fk_exercise_day` FOREIGN KEY (`day_id`) REFERENCES `training_plan_days` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='计划包含的动作表';

-- 5. 计算记录表 (1RM, 训练容量, 热量)
CREATE TABLE IF NOT EXISTS `calculation_records` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `user_id` BIGINT NOT NULL,
    `calculation_type` VARCHAR(20) NOT NULL COMMENT '类型: 1RM, VOLUME, CALORIES',
    `input_params` JSON NOT NULL COMMENT '输入参数JSON',
    `result_value` DOUBLE NOT NULL COMMENT '计算结果',
    `model_used` VARCHAR(50) COMMENT '使用的公式模型',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT `fk_calc_user` FOREIGN KEY (`user_id`) REFERENCES `user_table` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='算法计算历史记录表';

-- 6. 训练记录表 (实际完成)
CREATE TABLE IF NOT EXISTS `training_records` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `user_id` BIGINT NOT NULL,
    `exercise_name` VARCHAR(100) NOT NULL,
    `sets` INT,
    `reps` INT,
    `weight` DOUBLE,
    `total_volume` DOUBLE,
    `training_stress` DOUBLE,
    `duration` INT COMMENT '持续时间(分钟)',
    `training_date` DATE,
    `notes` TEXT,
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT `fk_record_user` FOREIGN KEY (`user_id`) REFERENCES `user_table` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='训练执行记录表';

-- ER图说明:
-- [User] 1 --- N [TrainingPlan] : 一个用户可以有多个训练计划
-- [TrainingPlan] 1 --- N [TrainingPlanDay] : 一个计划包含多个训练日
-- [TrainingPlanDay] 1 --- N [TrainingPlanExercise] : 一个训练日包含多个动作
-- [User] 1 --- N [CalculationRecord] : 一个用户可以有多次计算历史
-- [User] 1 --- N [TrainingRecord] : 一个用户可以记录多次实际训练
