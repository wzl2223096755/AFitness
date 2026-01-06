-- =====================================================
-- AFitness 完整数据初始化脚本
-- 版本: 4.0
-- 创建时间: 2026-01-05
-- 描述: 1个管理员 + 10个用户，每用户50+条数据
-- =====================================================
-- 
-- 测试账户信息 (共11个账户):
-- admin / Test123! (管理员)
-- user01-user10 / Test123! (普通用户)
-- 密码规则: 至少6位，包含大写字母、小写字母和数字
-- =====================================================

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- =====================================================
-- 第一部分: 清理现有数据
-- =====================================================
TRUNCATE TABLE user_achievements;
TRUNCATE TABLE training_advices;
TRUNCATE TABLE nutrition_records;
TRUNCATE TABLE recovery_metrics;
TRUNCATE TABLE recovery_data;
TRUNCATE TABLE cardio_training_data;
TRUNCATE TABLE strength_training_data;
TRUNCATE TABLE exercise_details;
TRUNCATE TABLE training_records;
TRUNCATE TABLE training_plan_exercises;
TRUNCATE TABLE training_plan_days;
TRUNCATE TABLE training_plans;
TRUNCATE TABLE body_records;
TRUNCATE TABLE user_settings;
TRUNCATE TABLE user_nutrition_goals;
TRUNCATE TABLE audit_logs;

DELETE FROM user_table WHERE id > 0;
ALTER TABLE user_table AUTO_INCREMENT = 1;

SET FOREIGN_KEY_CHECKS = 1;

-- =====================================================
-- 第二部分: 插入用户数据 (1管理员 + 10用户)
-- =====================================================

INSERT INTO user_table (id, username, password, email, age, weight, gender, height, experience_level, role, points, created_at) VALUES
(1, 'admin', '$2a$10$.D7h6VTuteCL1IDdMnycguBkvMqMnxE9gpw8yf2wWjjPpHUdXaIJW', 'admin@afitness.com', 35, 75.0, 'MALE', 178, 'ADVANCED', 'ADMIN', 5000, DATE_SUB(NOW(), INTERVAL 365 DAY)),
(2, 'user01', '$2a$10$.D7h6VTuteCL1IDdMnycguBkvMqMnxE9gpw8yf2wWjjPpHUdXaIJW', 'user01@example.com', 28, 82.0, 'MALE', 180, 'ADVANCED', 'USER', 1200, DATE_SUB(NOW(), INTERVAL 180 DAY)),
(3, 'user02', '$2a$10$.D7h6VTuteCL1IDdMnycguBkvMqMnxE9gpw8yf2wWjjPpHUdXaIJW', 'user02@example.com', 25, 58.0, 'FEMALE', 165, 'INTERMEDIATE', 'USER', 800, DATE_SUB(NOW(), INTERVAL 150 DAY)),
(4, 'user03', '$2a$10$.D7h6VTuteCL1IDdMnycguBkvMqMnxE9gpw8yf2wWjjPpHUdXaIJW', 'user03@example.com', 32, 78.0, 'MALE', 175, 'BEGINNER', 'USER', 350, DATE_SUB(NOW(), INTERVAL 90 DAY)),
(5, 'user04', '$2a$10$.D7h6VTuteCL1IDdMnycguBkvMqMnxE9gpw8yf2wWjjPpHUdXaIJW', 'user04@example.com', 30, 62.0, 'FEMALE', 170, 'ADVANCED', 'USER', 1500, DATE_SUB(NOW(), INTERVAL 200 DAY)),
(6, 'user05', '$2a$10$.D7h6VTuteCL1IDdMnycguBkvMqMnxE9gpw8yf2wWjjPpHUdXaIJW', 'user05@example.com', 27, 85.0, 'MALE', 182, 'INTERMEDIATE', 'USER', 650, DATE_SUB(NOW(), INTERVAL 120 DAY)),
(7, 'user06', '$2a$10$.D7h6VTuteCL1IDdMnycguBkvMqMnxE9gpw8yf2wWjjPpHUdXaIJW', 'user06@example.com', 24, 52.0, 'FEMALE', 160, 'BEGINNER', 'USER', 200, DATE_SUB(NOW(), INTERVAL 60 DAY)),
(8, 'user07', '$2a$10$.D7h6VTuteCL1IDdMnycguBkvMqMnxE9gpw8yf2wWjjPpHUdXaIJW', 'user07@example.com', 35, 90.0, 'MALE', 185, 'ADVANCED', 'USER', 2000, DATE_SUB(NOW(), INTERVAL 300 DAY)),
(9, 'user08', '$2a$10$.D7h6VTuteCL1IDdMnycguBkvMqMnxE9gpw8yf2wWjjPpHUdXaIJW', 'user08@example.com', 29, 55.0, 'FEMALE', 163, 'INTERMEDIATE', 'USER', 550, DATE_SUB(NOW(), INTERVAL 100 DAY)),
(10, 'user09', '$2a$10$.D7h6VTuteCL1IDdMnycguBkvMqMnxE9gpw8yf2wWjjPpHUdXaIJW', 'user09@example.com', 22, 70.0, 'MALE', 172, 'BEGINNER', 'USER', 150, DATE_SUB(NOW(), INTERVAL 45 DAY)),
(11, 'user10', '$2a$10$.D7h6VTuteCL1IDdMnycguBkvMqMnxE9gpw8yf2wWjjPpHUdXaIJW', 'user10@example.com', 26, 56.0, 'FEMALE', 162, 'INTERMEDIATE', 'USER', 700, DATE_SUB(NOW(), INTERVAL 130 DAY));

-- =====================================================
-- 第三部分: 用户设置
-- =====================================================

INSERT INTO user_settings (user_id, theme, language, notifications, auto_save) VALUES
(1, 'dark', 'zh-CN', 1, 1),
(2, 'light', 'zh-CN', 1, 1),
(3, 'dark', 'zh-CN', 1, 1),
(4, 'light', 'zh-CN', 0, 1),
(5, 'dark', 'en-US', 1, 1),
(6, 'light', 'zh-CN', 1, 0),
(7, 'dark', 'zh-CN', 0, 1),
(8, 'light', 'en-US', 1, 1),
(9, 'dark', 'zh-CN', 1, 1),
(10, 'light', 'zh-CN', 1, 0),
(11, 'dark', 'zh-CN', 1, 1)
ON DUPLICATE KEY UPDATE theme=VALUES(theme), language=VALUES(language);

-- =====================================================
-- 第四部分: 用户营养目标
-- =====================================================

INSERT INTO user_nutrition_goals (user_id, target_calories, target_protein, target_carbs, target_fat, training_goal, activity_level, use_custom_targets, created_at, updated_at) VALUES
(1, 2500, 150.0, 280.0, 80.0, 'maintenance', 'moderate', 1, NOW(), NOW()),
(2, 3000, 180.0, 350.0, 90.0, 'muscle_gain', 'high', 1, NOW(), NOW()),
(3, 1800, 100.0, 200.0, 60.0, 'weight_loss', 'moderate', 1, NOW(), NOW()),
(4, 2200, 120.0, 250.0, 70.0, 'maintenance', 'moderate', 1, NOW(), NOW()),
(5, 2000, 130.0, 220.0, 65.0, 'muscle_gain', 'high', 1, NOW(), NOW()),
(6, 2800, 160.0, 320.0, 85.0, 'muscle_gain', 'high', 1, NOW(), NOW()),
(7, 1500, 80.0, 170.0, 50.0, 'weight_loss', 'low', 1, NOW(), NOW()),
(8, 3200, 200.0, 380.0, 100.0, 'muscle_gain', 'very_high', 1, NOW(), NOW()),
(9, 1700, 90.0, 190.0, 55.0, 'weight_loss', 'moderate', 1, NOW(), NOW()),
(10, 2000, 100.0, 230.0, 65.0, 'maintenance', 'moderate', 1, NOW(), NOW()),
(11, 1900, 110.0, 210.0, 60.0, 'weight_loss', 'moderate', 1, NOW(), NOW());

-- =====================================================
-- 第五部分: 身体数据记录
-- =====================================================

INSERT INTO body_records (user_id, weight, body_fat, muscle_mass, waist_circumference, hip_circumference, chest_circumference, record_time) VALUES
(2, 80.0, 16.0, 36.0, 82.0, 96.0, 102.0, DATE_SUB(NOW(), INTERVAL 90 DAY)),
(2, 80.5, 15.8, 36.3, 81.5, 96.0, 102.5, DATE_SUB(NOW(), INTERVAL 70 DAY)),
(2, 81.0, 15.5, 36.8, 81.0, 96.5, 103.0, DATE_SUB(NOW(), INTERVAL 50 DAY)),
(2, 81.5, 15.0, 37.5, 80.0, 97.0, 104.0, DATE_SUB(NOW(), INTERVAL 30 DAY)),
(2, 82.0, 14.5, 38.0, 79.0, 97.5, 105.0, DATE_SUB(NOW(), INTERVAL 10 DAY)),
(2, 82.0, 14.2, 38.3, 78.5, 98.0, 105.5, NOW()),
(3, 62.0, 28.0, 22.0, 72.0, 92.0, 88.0, DATE_SUB(NOW(), INTERVAL 90 DAY)),
(3, 61.0, 27.0, 22.5, 71.0, 91.0, 88.0, DATE_SUB(NOW(), INTERVAL 60 DAY)),
(3, 60.0, 26.0, 23.0, 70.0, 90.0, 88.0, DATE_SUB(NOW(), INTERVAL 30 DAY)),
(3, 58.5, 24.5, 23.8, 68.5, 88.5, 88.0, DATE_SUB(NOW(), INTERVAL 10 DAY)),
(3, 58.0, 24.0, 24.0, 68.0, 88.0, 88.0, NOW()),
(4, 80.0, 25.0, 28.0, 90.0, 98.0, 100.0, DATE_SUB(NOW(), INTERVAL 60 DAY)),
(4, 79.0, 24.0, 28.5, 89.0, 97.0, 100.0, DATE_SUB(NOW(), INTERVAL 40 DAY)),
(4, 78.5, 23.5, 28.8, 88.5, 96.5, 100.0, DATE_SUB(NOW(), INTERVAL 20 DAY)),
(4, 78.0, 22.5, 29.2, 87.5, 95.5, 100.0, NOW()),
(5, 64.0, 22.0, 26.0, 68.0, 94.0, 90.0, DATE_SUB(NOW(), INTERVAL 90 DAY)),
(5, 63.0, 21.0, 26.5, 67.0, 94.0, 90.0, DATE_SUB(NOW(), INTERVAL 60 DAY)),
(5, 62.0, 20.0, 27.0, 66.0, 94.0, 90.0, DATE_SUB(NOW(), INTERVAL 30 DAY)),
(5, 62.0, 19.0, 27.5, 65.0, 94.0, 90.0, NOW()),
(6, 86.0, 18.0, 35.0, 85.0, 100.0, 108.0, DATE_SUB(NOW(), INTERVAL 60 DAY)),
(6, 85.5, 17.5, 35.5, 84.5, 100.0, 108.5, DATE_SUB(NOW(), INTERVAL 30 DAY)),
(6, 85.0, 17.0, 36.0, 84.0, 100.0, 109.0, NOW()),
(7, 54.0, 28.0, 18.0, 68.0, 88.0, 82.0, DATE_SUB(NOW(), INTERVAL 40 DAY)),
(7, 53.0, 27.0, 18.5, 67.0, 87.0, 82.0, DATE_SUB(NOW(), INTERVAL 20 DAY)),
(7, 52.0, 26.0, 19.0, 66.0, 86.0, 82.0, NOW()),
(8, 92.0, 15.0, 40.0, 88.0, 102.0, 112.0, DATE_SUB(NOW(), INTERVAL 60 DAY)),
(8, 91.0, 14.5, 40.5, 87.0, 102.0, 113.0, DATE_SUB(NOW(), INTERVAL 30 DAY)),
(8, 90.0, 14.0, 41.0, 86.0, 102.0, 114.0, NOW()),
(9, 57.0, 24.0, 22.0, 70.0, 90.0, 86.0, DATE_SUB(NOW(), INTERVAL 60 DAY)),
(9, 56.0, 23.0, 22.5, 69.0, 89.0, 86.0, DATE_SUB(NOW(), INTERVAL 30 DAY)),
(9, 55.0, 22.0, 23.0, 68.0, 88.0, 86.0, NOW()),
(10, 72.0, 22.0, 28.0, 82.0, 95.0, 98.0, DATE_SUB(NOW(), INTERVAL 30 DAY)),
(10, 71.0, 21.0, 28.5, 81.0, 94.0, 98.0, DATE_SUB(NOW(), INTERVAL 15 DAY)),
(10, 70.0, 20.0, 29.0, 80.0, 93.0, 98.0, NOW()),
(11, 58.0, 26.0, 21.0, 70.0, 90.0, 85.0, DATE_SUB(NOW(), INTERVAL 60 DAY)),
(11, 57.0, 25.0, 21.5, 69.0, 89.0, 85.0, DATE_SUB(NOW(), INTERVAL 30 DAY)),
(11, 56.0, 24.0, 22.0, 68.0, 88.0, 85.0, NOW());


-- =====================================================
-- 第六部分: 训练记录 (每用户15条)
-- =====================================================

INSERT INTO training_records (user_id, exercise_name, sets, reps, weight, training_date, duration, notes, total_volume, training_stress, deleted) VALUES
-- user01 训练记录
(2, 'Barbell Bench Press', 5, 8, 100.0, DATE_SUB(CURDATE(), INTERVAL 1 DAY), 45, 'Great session', 4000.0, 8.5, 0),
(2, 'Barbell Squat', 5, 6, 140.0, DATE_SUB(CURDATE(), INTERVAL 1 DAY), 50, 'PR attempt', 4200.0, 9.0, 0),
(2, 'Deadlift', 4, 5, 160.0, DATE_SUB(CURDATE(), INTERVAL 3 DAY), 40, 'Heavy day', 3200.0, 9.5, 0),
(2, 'Barbell Row', 4, 10, 80.0, DATE_SUB(CURDATE(), INTERVAL 3 DAY), 30, NULL, 3200.0, 7.0, 0),
(2, 'Dumbbell Press', 4, 10, 30.0, DATE_SUB(CURDATE(), INTERVAL 5 DAY), 25, 'Shoulder day', 1200.0, 6.5, 0),
(2, 'Pull-ups', 4, 12, 10.0, DATE_SUB(CURDATE(), INTERVAL 5 DAY), 20, 'Weighted', 480.0, 7.0, 0),
(2, 'Bench Press', 5, 6, 105.0, DATE_SUB(CURDATE(), INTERVAL 7 DAY), 45, 'Weight increase', 3150.0, 8.0, 0),
(2, 'Leg Press', 4, 12, 200.0, DATE_SUB(CURDATE(), INTERVAL 7 DAY), 30, NULL, 9600.0, 7.5, 0),
(2, 'Barbell Curl', 3, 12, 40.0, DATE_SUB(CURDATE(), INTERVAL 9 DAY), 15, 'Arm day', 1440.0, 5.5, 0),
(2, 'Tricep Pushdown', 3, 15, 35.0, DATE_SUB(CURDATE(), INTERVAL 9 DAY), 15, NULL, 1575.0, 5.0, 0),
(2, 'Squat', 5, 5, 145.0, DATE_SUB(CURDATE(), INTERVAL 11 DAY), 50, 'PR success', 3625.0, 9.5, 0),
(2, 'Romanian Deadlift', 4, 10, 100.0, DATE_SUB(CURDATE(), INTERVAL 11 DAY), 30, NULL, 4000.0, 7.0, 0),
(2, 'Dumbbell Fly', 3, 12, 20.0, DATE_SUB(CURDATE(), INTERVAL 13 DAY), 15, NULL, 720.0, 5.0, 0),
(2, 'Lateral Raise', 3, 15, 12.0, DATE_SUB(CURDATE(), INTERVAL 13 DAY), 12, NULL, 540.0, 4.5, 0),
(2, 'Face Pull', 3, 15, 25.0, DATE_SUB(CURDATE(), INTERVAL 15 DAY), 12, 'Shoulder warmup', 1125.0, 4.0, 0),
-- user02 训练记录
(3, 'Smith Machine Squat', 4, 12, 40.0, DATE_SUB(CURDATE(), INTERVAL 1 DAY), 35, 'Leg day', 1920.0, 7.0, 0),
(3, 'Leg Press', 3, 15, 80.0, DATE_SUB(CURDATE(), INTERVAL 1 DAY), 25, NULL, 3600.0, 6.5, 0),
(3, 'Seated Row', 3, 12, 35.0, DATE_SUB(CURDATE(), INTERVAL 2 DAY), 20, 'Back training', 1260.0, 6.0, 0),
(3, 'Lat Pulldown', 3, 12, 40.0, DATE_SUB(CURDATE(), INTERVAL 2 DAY), 20, NULL, 1440.0, 6.0, 0),
(3, 'Dumbbell Press', 3, 12, 8.0, DATE_SUB(CURDATE(), INTERVAL 4 DAY), 20, 'Shoulders', 288.0, 5.5, 0),
(3, 'Lateral Raise', 3, 15, 5.0, DATE_SUB(CURDATE(), INTERVAL 4 DAY), 15, NULL, 225.0, 4.5, 0),
(3, 'Hip Thrust', 3, 15, 30.0, DATE_SUB(CURDATE(), INTERVAL 6 DAY), 20, 'Glute activation', 1350.0, 5.0, 0),
(3, 'Leg Curl', 3, 12, 25.0, DATE_SUB(CURDATE(), INTERVAL 6 DAY), 15, NULL, 900.0, 5.5, 0),
(3, 'Plank', 3, 60, 0.0, DATE_SUB(CURDATE(), INTERVAL 8 DAY), 15, 'Core training', 0.0, 5.0, 0),
(3, 'Crunches', 3, 20, 0.0, DATE_SUB(CURDATE(), INTERVAL 8 DAY), 10, NULL, 0.0, 4.0, 0),
(3, 'Dumbbell Curl', 3, 12, 6.0, DATE_SUB(CURDATE(), INTERVAL 10 DAY), 15, 'Arms', 216.0, 4.5, 0),
(3, 'Tricep Pushdown', 3, 15, 15.0, DATE_SUB(CURDATE(), INTERVAL 10 DAY), 12, NULL, 675.0, 4.5, 0),
(3, 'Squat', 4, 10, 45.0, DATE_SUB(CURDATE(), INTERVAL 12 DAY), 35, 'Good progress', 1800.0, 7.0, 0),
(3, 'Bulgarian Split Squat', 3, 10, 10.0, DATE_SUB(CURDATE(), INTERVAL 12 DAY), 20, NULL, 300.0, 6.0, 0),
(3, 'Push-ups', 3, 12, 0.0, DATE_SUB(CURDATE(), INTERVAL 14 DAY), 15, 'Bodyweight', 0.0, 5.0, 0),
-- user03 训练记录
(4, 'Smith Machine Squat', 3, 10, 40.0, DATE_SUB(CURDATE(), INTERVAL 1 DAY), 30, 'Beginner adaptation', 1200.0, 6.5, 0),
(4, 'Leg Press', 3, 12, 60.0, DATE_SUB(CURDATE(), INTERVAL 1 DAY), 20, NULL, 2160.0, 6.0, 0),
(4, 'Seated Row', 3, 12, 30.0, DATE_SUB(CURDATE(), INTERVAL 3 DAY), 20, NULL, 1080.0, 5.5, 0),
(4, 'Lat Pulldown', 3, 10, 35.0, DATE_SUB(CURDATE(), INTERVAL 3 DAY), 20, NULL, 1050.0, 5.5, 0),
(4, 'Dumbbell Bench Press', 3, 10, 15.0, DATE_SUB(CURDATE(), INTERVAL 5 DAY), 25, 'Chest intro', 450.0, 5.0, 0),
(4, 'Dumbbell Fly', 3, 12, 8.0, DATE_SUB(CURDATE(), INTERVAL 5 DAY), 15, NULL, 288.0, 4.5, 0),
(4, 'Dumbbell Press', 3, 10, 10.0, DATE_SUB(CURDATE(), INTERVAL 7 DAY), 20, NULL, 300.0, 5.0, 0),
(4, 'Lateral Raise', 3, 12, 5.0, DATE_SUB(CURDATE(), INTERVAL 7 DAY), 12, NULL, 180.0, 4.0, 0),
(4, 'Plank', 3, 45, 0.0, DATE_SUB(CURDATE(), INTERVAL 9 DAY), 12, NULL, 0.0, 4.5, 0),
(4, 'Crunches', 3, 15, 0.0, DATE_SUB(CURDATE(), INTERVAL 9 DAY), 10, NULL, 0.0, 4.0, 0),
(4, 'Dumbbell Curl', 3, 12, 8.0, DATE_SUB(CURDATE(), INTERVAL 11 DAY), 15, NULL, 288.0, 4.5, 0),
(4, 'Tricep Pushdown', 3, 12, 15.0, DATE_SUB(CURDATE(), INTERVAL 11 DAY), 12, NULL, 540.0, 4.5, 0),
(4, 'Squat', 3, 8, 50.0, DATE_SUB(CURDATE(), INTERVAL 13 DAY), 30, 'Progressing', 1200.0, 6.0, 0),
(4, 'Leg Curl', 3, 12, 20.0, DATE_SUB(CURDATE(), INTERVAL 13 DAY), 15, NULL, 720.0, 5.0, 0),
(4, 'Push-ups', 3, 10, 0.0, DATE_SUB(CURDATE(), INTERVAL 15 DAY), 12, NULL, 0.0, 4.5, 0);


-- user04-user10 训练记录
INSERT INTO training_records (user_id, exercise_name, sets, reps, weight, training_date, duration, notes, total_volume, training_stress, deleted) VALUES
(5, 'Barbell Squat', 4, 10, 70.0, DATE_SUB(CURDATE(), INTERVAL 1 DAY), 40, 'Leg day', 2800.0, 8.0, 0),
(5, 'Romanian Deadlift', 4, 10, 60.0, DATE_SUB(CURDATE(), INTERVAL 1 DAY), 30, NULL, 2400.0, 7.5, 0),
(5, 'Hip Thrust', 4, 12, 80.0, DATE_SUB(CURDATE(), INTERVAL 3 DAY), 30, 'Glute focus', 3840.0, 7.0, 0),
(5, 'Leg Press', 4, 12, 120.0, DATE_SUB(CURDATE(), INTERVAL 3 DAY), 25, NULL, 5760.0, 7.0, 0),
(5, 'Dumbbell Press', 4, 10, 15.0, DATE_SUB(CURDATE(), INTERVAL 5 DAY), 25, 'Shoulders', 600.0, 6.5, 0),
(5, 'Lateral Raise', 4, 12, 8.0, DATE_SUB(CURDATE(), INTERVAL 5 DAY), 15, NULL, 384.0, 5.5, 0),
(5, 'Seated Row', 4, 12, 45.0, DATE_SUB(CURDATE(), INTERVAL 7 DAY), 25, 'Back', 2160.0, 7.0, 0),
(5, 'Lat Pulldown', 4, 10, 50.0, DATE_SUB(CURDATE(), INTERVAL 7 DAY), 20, NULL, 2000.0, 6.5, 0),
(5, 'Dumbbell Bench Press', 4, 10, 18.0, DATE_SUB(CURDATE(), INTERVAL 9 DAY), 25, 'Chest', 720.0, 6.0, 0),
(5, 'Cable Fly', 3, 15, 20.0, DATE_SUB(CURDATE(), INTERVAL 9 DAY), 15, NULL, 900.0, 5.5, 0),
(5, 'Squat', 4, 8, 75.0, DATE_SUB(CURDATE(), INTERVAL 11 DAY), 40, 'PR attempt', 2400.0, 8.5, 0),
(5, 'Leg Curl', 3, 12, 35.0, DATE_SUB(CURDATE(), INTERVAL 11 DAY), 15, NULL, 1260.0, 6.0, 0),
(5, 'Pull-ups', 3, 8, 0.0, DATE_SUB(CURDATE(), INTERVAL 13 DAY), 15, 'Bodyweight', 0.0, 6.5, 0),
(5, 'Face Pull', 3, 15, 20.0, DATE_SUB(CURDATE(), INTERVAL 13 DAY), 12, NULL, 900.0, 5.0, 0),
(5, 'Plank', 3, 60, 0.0, DATE_SUB(CURDATE(), INTERVAL 15 DAY), 15, 'Core', 0.0, 5.0, 0),
(6, 'Barbell Bench Press', 4, 10, 80.0, DATE_SUB(CURDATE(), INTERVAL 1 DAY), 40, 'Chest day', 3200.0, 7.5, 0),
(6, 'Dumbbell Fly', 3, 12, 18.0, DATE_SUB(CURDATE(), INTERVAL 1 DAY), 15, NULL, 648.0, 5.5, 0),
(6, 'Barbell Squat', 4, 8, 100.0, DATE_SUB(CURDATE(), INTERVAL 3 DAY), 45, 'Legs', 3200.0, 8.0, 0),
(6, 'Leg Press', 4, 12, 150.0, DATE_SUB(CURDATE(), INTERVAL 3 DAY), 25, NULL, 7200.0, 7.0, 0),
(6, 'Barbell Row', 4, 10, 70.0, DATE_SUB(CURDATE(), INTERVAL 5 DAY), 30, 'Back', 2800.0, 7.0, 0),
(6, 'Lat Pulldown', 4, 10, 60.0, DATE_SUB(CURDATE(), INTERVAL 5 DAY), 20, NULL, 2400.0, 6.5, 0),
(6, 'Dumbbell Press', 4, 10, 22.0, DATE_SUB(CURDATE(), INTERVAL 7 DAY), 25, 'Shoulders', 880.0, 6.5, 0),
(6, 'Lateral Raise', 3, 15, 10.0, DATE_SUB(CURDATE(), INTERVAL 7 DAY), 12, NULL, 450.0, 5.0, 0),
(6, 'Barbell Curl', 3, 12, 30.0, DATE_SUB(CURDATE(), INTERVAL 9 DAY), 15, 'Arms', 1080.0, 5.5, 0),
(6, 'Tricep Pushdown', 3, 15, 30.0, DATE_SUB(CURDATE(), INTERVAL 9 DAY), 12, NULL, 1350.0, 5.0, 0),
(6, 'Deadlift', 4, 6, 120.0, DATE_SUB(CURDATE(), INTERVAL 11 DAY), 40, 'Heavy', 2880.0, 8.5, 0),
(6, 'Romanian Deadlift', 3, 10, 80.0, DATE_SUB(CURDATE(), INTERVAL 11 DAY), 20, NULL, 2400.0, 6.5, 0),
(6, 'Bench Press', 4, 8, 85.0, DATE_SUB(CURDATE(), INTERVAL 13 DAY), 40, 'Progressing', 2720.0, 7.5, 0),
(6, 'Dips', 3, 12, 10.0, DATE_SUB(CURDATE(), INTERVAL 13 DAY), 15, NULL, 360.0, 6.0, 0),
(6, 'Plank', 3, 60, 0.0, DATE_SUB(CURDATE(), INTERVAL 15 DAY), 12, NULL, 0.0, 5.0, 0),
(7, 'Smith Machine Squat', 3, 12, 30.0, DATE_SUB(CURDATE(), INTERVAL 1 DAY), 30, 'Beginner', 1080.0, 6.0, 0),
(7, 'Leg Press', 3, 15, 50.0, DATE_SUB(CURDATE(), INTERVAL 1 DAY), 20, NULL, 2250.0, 5.5, 0),
(7, 'Seated Row', 3, 12, 25.0, DATE_SUB(CURDATE(), INTERVAL 3 DAY), 20, NULL, 900.0, 5.5, 0),
(7, 'Lat Pulldown', 3, 12, 30.0, DATE_SUB(CURDATE(), INTERVAL 3 DAY), 18, NULL, 1080.0, 5.5, 0),
(7, 'Dumbbell Press', 3, 12, 5.0, DATE_SUB(CURDATE(), INTERVAL 5 DAY), 18, NULL, 180.0, 4.5, 0),
(7, 'Lateral Raise', 3, 15, 3.0, DATE_SUB(CURDATE(), INTERVAL 5 DAY), 12, NULL, 135.0, 4.0, 0),
(7, 'Hip Thrust', 3, 15, 20.0, DATE_SUB(CURDATE(), INTERVAL 7 DAY), 20, NULL, 900.0, 5.0, 0),
(7, 'Leg Curl', 3, 12, 15.0, DATE_SUB(CURDATE(), INTERVAL 7 DAY), 15, NULL, 540.0, 5.0, 0),
(7, 'Plank', 3, 30, 0.0, DATE_SUB(CURDATE(), INTERVAL 9 DAY), 10, NULL, 0.0, 4.0, 0),
(7, 'Crunches', 3, 15, 0.0, DATE_SUB(CURDATE(), INTERVAL 9 DAY), 8, NULL, 0.0, 3.5, 0),
(7, 'Dumbbell Curl', 3, 12, 4.0, DATE_SUB(CURDATE(), INTERVAL 11 DAY), 12, NULL, 144.0, 4.0, 0),
(7, 'Tricep Pushdown', 3, 15, 10.0, DATE_SUB(CURDATE(), INTERVAL 11 DAY), 10, NULL, 450.0, 4.0, 0),
(7, 'Squat', 3, 10, 35.0, DATE_SUB(CURDATE(), INTERVAL 13 DAY), 25, NULL, 1050.0, 5.5, 0),
(7, 'Leg Extension', 3, 12, 20.0, DATE_SUB(CURDATE(), INTERVAL 13 DAY), 12, NULL, 720.0, 4.5, 0),
(7, 'Push-ups', 3, 8, 0.0, DATE_SUB(CURDATE(), INTERVAL 15 DAY), 10, NULL, 0.0, 4.0, 0);


-- user07-user10 训练记录
INSERT INTO training_records (user_id, exercise_name, sets, reps, weight, training_date, duration, notes, total_volume, training_stress, deleted) VALUES
(8, 'Barbell Bench Press', 5, 8, 120.0, DATE_SUB(CURDATE(), INTERVAL 1 DAY), 50, 'Heavy chest', 4800.0, 9.0, 0),
(8, 'Incline Dumbbell Press', 4, 10, 40.0, DATE_SUB(CURDATE(), INTERVAL 1 DAY), 25, NULL, 1600.0, 7.0, 0),
(8, 'Barbell Squat', 5, 6, 160.0, DATE_SUB(CURDATE(), INTERVAL 3 DAY), 55, 'Leg day', 4800.0, 9.5, 0),
(8, 'Leg Press', 4, 12, 250.0, DATE_SUB(CURDATE(), INTERVAL 3 DAY), 30, NULL, 12000.0, 8.0, 0),
(8, 'Deadlift', 5, 5, 180.0, DATE_SUB(CURDATE(), INTERVAL 5 DAY), 50, 'PR attempt', 4500.0, 10.0, 0),
(8, 'Barbell Row', 4, 10, 100.0, DATE_SUB(CURDATE(), INTERVAL 5 DAY), 30, NULL, 4000.0, 7.5, 0),
(8, 'Military Press', 4, 8, 70.0, DATE_SUB(CURDATE(), INTERVAL 7 DAY), 35, 'Shoulders', 2240.0, 7.5, 0),
(8, 'Lateral Raise', 4, 12, 15.0, DATE_SUB(CURDATE(), INTERVAL 7 DAY), 15, NULL, 720.0, 5.5, 0),
(8, 'Weighted Pull-ups', 4, 10, 25.0, DATE_SUB(CURDATE(), INTERVAL 9 DAY), 25, 'Back', 1000.0, 8.0, 0),
(8, 'Cable Row', 4, 12, 80.0, DATE_SUB(CURDATE(), INTERVAL 9 DAY), 20, NULL, 3840.0, 7.0, 0),
(8, 'Barbell Curl', 4, 10, 50.0, DATE_SUB(CURDATE(), INTERVAL 11 DAY), 20, 'Arms', 2000.0, 6.5, 0),
(8, 'Skull Crushers', 4, 10, 40.0, DATE_SUB(CURDATE(), INTERVAL 11 DAY), 18, NULL, 1600.0, 6.0, 0),
(8, 'Front Squat', 4, 8, 100.0, DATE_SUB(CURDATE(), INTERVAL 13 DAY), 40, NULL, 3200.0, 8.0, 0),
(8, 'Romanian Deadlift', 4, 10, 120.0, DATE_SUB(CURDATE(), INTERVAL 13 DAY), 25, NULL, 4800.0, 7.5, 0),
(8, 'Dips', 4, 12, 20.0, DATE_SUB(CURDATE(), INTERVAL 15 DAY), 20, 'Weighted', 960.0, 7.0, 0),
(9, 'Smith Machine Squat', 3, 12, 35.0, DATE_SUB(CURDATE(), INTERVAL 1 DAY), 30, 'Leg day', 1260.0, 6.5, 0),
(9, 'Leg Press', 3, 15, 70.0, DATE_SUB(CURDATE(), INTERVAL 1 DAY), 22, NULL, 3150.0, 6.0, 0),
(9, 'Seated Row', 3, 12, 30.0, DATE_SUB(CURDATE(), INTERVAL 3 DAY), 20, 'Back', 1080.0, 5.5, 0),
(9, 'Lat Pulldown', 3, 12, 35.0, DATE_SUB(CURDATE(), INTERVAL 3 DAY), 18, NULL, 1260.0, 5.5, 0),
(9, 'Dumbbell Press', 3, 12, 8.0, DATE_SUB(CURDATE(), INTERVAL 5 DAY), 20, 'Shoulders', 288.0, 5.0, 0),
(9, 'Lateral Raise', 3, 15, 5.0, DATE_SUB(CURDATE(), INTERVAL 5 DAY), 12, NULL, 225.0, 4.5, 0),
(9, 'Hip Thrust', 3, 15, 40.0, DATE_SUB(CURDATE(), INTERVAL 7 DAY), 22, 'Glutes', 1800.0, 5.5, 0),
(9, 'Leg Curl', 3, 12, 25.0, DATE_SUB(CURDATE(), INTERVAL 7 DAY), 15, NULL, 900.0, 5.0, 0),
(9, 'Dumbbell Bench Press', 3, 12, 10.0, DATE_SUB(CURDATE(), INTERVAL 9 DAY), 22, 'Chest', 360.0, 5.0, 0),
(9, 'Cable Fly', 3, 15, 15.0, DATE_SUB(CURDATE(), INTERVAL 9 DAY), 12, NULL, 675.0, 4.5, 0),
(9, 'Plank', 3, 45, 0.0, DATE_SUB(CURDATE(), INTERVAL 11 DAY), 12, 'Core', 0.0, 4.5, 0),
(9, 'Crunches', 3, 20, 0.0, DATE_SUB(CURDATE(), INTERVAL 11 DAY), 10, NULL, 0.0, 4.0, 0),
(9, 'Dumbbell Curl', 3, 12, 6.0, DATE_SUB(CURDATE(), INTERVAL 13 DAY), 15, 'Arms', 216.0, 4.5, 0),
(9, 'Tricep Pushdown', 3, 15, 15.0, DATE_SUB(CURDATE(), INTERVAL 13 DAY), 12, NULL, 675.0, 4.5, 0),
(9, 'Push-ups', 3, 12, 0.0, DATE_SUB(CURDATE(), INTERVAL 15 DAY), 12, NULL, 0.0, 4.5, 0),
(10, 'Smith Machine Squat', 3, 10, 45.0, DATE_SUB(CURDATE(), INTERVAL 1 DAY), 28, 'Beginner', 1350.0, 6.0, 0),
(10, 'Leg Press', 3, 12, 80.0, DATE_SUB(CURDATE(), INTERVAL 1 DAY), 20, NULL, 2880.0, 5.5, 0),
(10, 'Seated Row', 3, 10, 35.0, DATE_SUB(CURDATE(), INTERVAL 3 DAY), 18, 'Back', 1050.0, 5.5, 0),
(10, 'Lat Pulldown', 3, 10, 40.0, DATE_SUB(CURDATE(), INTERVAL 3 DAY), 18, NULL, 1200.0, 5.5, 0),
(10, 'Dumbbell Bench Press', 3, 10, 15.0, DATE_SUB(CURDATE(), INTERVAL 5 DAY), 22, 'Chest', 450.0, 5.0, 0),
(10, 'Dumbbell Fly', 3, 12, 8.0, DATE_SUB(CURDATE(), INTERVAL 5 DAY), 12, NULL, 288.0, 4.5, 0),
(10, 'Dumbbell Press', 3, 10, 10.0, DATE_SUB(CURDATE(), INTERVAL 7 DAY), 18, 'Shoulders', 300.0, 5.0, 0),
(10, 'Lateral Raise', 3, 12, 5.0, DATE_SUB(CURDATE(), INTERVAL 7 DAY), 10, NULL, 180.0, 4.0, 0),
(10, 'Plank', 3, 40, 0.0, DATE_SUB(CURDATE(), INTERVAL 9 DAY), 10, 'Core', 0.0, 4.0, 0),
(10, 'Crunches', 3, 15, 0.0, DATE_SUB(CURDATE(), INTERVAL 9 DAY), 8, NULL, 0.0, 3.5, 0),
(10, 'Dumbbell Curl', 3, 10, 8.0, DATE_SUB(CURDATE(), INTERVAL 11 DAY), 12, 'Arms', 240.0, 4.5, 0),
(10, 'Tricep Pushdown', 3, 12, 15.0, DATE_SUB(CURDATE(), INTERVAL 11 DAY), 10, NULL, 540.0, 4.5, 0),
(10, 'Squat', 3, 8, 55.0, DATE_SUB(CURDATE(), INTERVAL 13 DAY), 28, NULL, 1320.0, 6.0, 0),
(10, 'Leg Curl', 3, 12, 20.0, DATE_SUB(CURDATE(), INTERVAL 13 DAY), 12, NULL, 720.0, 5.0, 0),
(10, 'Push-ups', 3, 10, 0.0, DATE_SUB(CURDATE(), INTERVAL 15 DAY), 10, NULL, 0.0, 4.5, 0),
(11, 'Smith Machine Squat', 3, 12, 40.0, DATE_SUB(CURDATE(), INTERVAL 1 DAY), 30, 'Leg day', 1440.0, 6.5, 0),
(11, 'Leg Press', 3, 15, 80.0, DATE_SUB(CURDATE(), INTERVAL 1 DAY), 22, NULL, 3600.0, 6.0, 0),
(11, 'Seated Row', 3, 12, 35.0, DATE_SUB(CURDATE(), INTERVAL 3 DAY), 20, 'Back', 1260.0, 6.0, 0),
(11, 'Lat Pulldown', 3, 12, 40.0, DATE_SUB(CURDATE(), INTERVAL 3 DAY), 18, NULL, 1440.0, 6.0, 0),
(11, 'Dumbbell Press', 3, 12, 10.0, DATE_SUB(CURDATE(), INTERVAL 5 DAY), 20, 'Shoulders', 360.0, 5.5, 0),
(11, 'Lateral Raise', 3, 15, 6.0, DATE_SUB(CURDATE(), INTERVAL 5 DAY), 12, NULL, 270.0, 4.5, 0),
(11, 'Hip Thrust', 3, 15, 50.0, DATE_SUB(CURDATE(), INTERVAL 7 DAY), 22, 'Glutes', 2250.0, 6.0, 0),
(11, 'Leg Curl', 3, 12, 30.0, DATE_SUB(CURDATE(), INTERVAL 7 DAY), 15, NULL, 1080.0, 5.5, 0),
(11, 'Dumbbell Bench Press', 3, 12, 12.0, DATE_SUB(CURDATE(), INTERVAL 9 DAY), 22, 'Chest', 432.0, 5.5, 0),
(11, 'Cable Fly', 3, 15, 18.0, DATE_SUB(CURDATE(), INTERVAL 9 DAY), 12, NULL, 810.0, 5.0, 0),
(11, 'Plank', 3, 50, 0.0, DATE_SUB(CURDATE(), INTERVAL 11 DAY), 12, 'Core', 0.0, 5.0, 0),
(11, 'Crunches', 3, 20, 0.0, DATE_SUB(CURDATE(), INTERVAL 11 DAY), 10, NULL, 0.0, 4.0, 0),
(11, 'Dumbbell Curl', 3, 12, 7.0, DATE_SUB(CURDATE(), INTERVAL 13 DAY), 15, 'Arms', 252.0, 4.5, 0),
(11, 'Tricep Pushdown', 3, 15, 18.0, DATE_SUB(CURDATE(), INTERVAL 13 DAY), 12, NULL, 810.0, 5.0, 0),
(11, 'Push-ups', 3, 15, 0.0, DATE_SUB(CURDATE(), INTERVAL 15 DAY), 12, NULL, 0.0, 5.0, 0);


-- =====================================================
-- 第七部分: 营养记录 (每用户10条)
-- =====================================================

INSERT INTO nutrition_records (user_id, food_name, calories, protein, carbs, fat, meal_type, record_time) VALUES
-- user01 营养记录
(2, 'Grilled Chicken Breast', 350, 45.0, 5.0, 8.0, 'LUNCH', DATE_SUB(NOW(), INTERVAL 1 DAY)),
(2, 'Brown Rice', 220, 5.0, 45.0, 2.0, 'LUNCH', DATE_SUB(NOW(), INTERVAL 1 DAY)),
(2, 'Protein Shake', 280, 50.0, 8.0, 3.0, 'SNACK', DATE_SUB(NOW(), INTERVAL 1 DAY)),
(2, 'Salmon Fillet', 420, 40.0, 0.0, 28.0, 'DINNER', DATE_SUB(NOW(), INTERVAL 2 DAY)),
(2, 'Oatmeal with Banana', 380, 12.0, 65.0, 8.0, 'BREAKFAST', DATE_SUB(NOW(), INTERVAL 2 DAY)),
(2, 'Greek Yogurt', 150, 15.0, 12.0, 5.0, 'SNACK', DATE_SUB(NOW(), INTERVAL 3 DAY)),
(2, 'Beef Steak', 500, 55.0, 0.0, 30.0, 'DINNER', DATE_SUB(NOW(), INTERVAL 3 DAY)),
(2, 'Eggs and Toast', 320, 20.0, 25.0, 18.0, 'BREAKFAST', DATE_SUB(NOW(), INTERVAL 4 DAY)),
(2, 'Tuna Salad', 280, 35.0, 10.0, 12.0, 'LUNCH', DATE_SUB(NOW(), INTERVAL 4 DAY)),
(2, 'Cottage Cheese', 180, 25.0, 8.0, 5.0, 'SNACK', DATE_SUB(NOW(), INTERVAL 5 DAY)),
-- user02 营养记录
(3, 'Grilled Chicken Salad', 280, 35.0, 15.0, 10.0, 'LUNCH', DATE_SUB(NOW(), INTERVAL 1 DAY)),
(3, 'Quinoa Bowl', 320, 12.0, 55.0, 6.0, 'LUNCH', DATE_SUB(NOW(), INTERVAL 1 DAY)),
(3, 'Fruit Smoothie', 180, 5.0, 40.0, 2.0, 'SNACK', DATE_SUB(NOW(), INTERVAL 2 DAY)),
(3, 'Grilled Fish', 250, 40.0, 0.0, 8.0, 'DINNER', DATE_SUB(NOW(), INTERVAL 2 DAY)),
(3, 'Avocado Toast', 280, 8.0, 25.0, 18.0, 'BREAKFAST', DATE_SUB(NOW(), INTERVAL 3 DAY)),
(3, 'Greek Yogurt Parfait', 220, 18.0, 28.0, 6.0, 'SNACK', DATE_SUB(NOW(), INTERVAL 3 DAY)),
(3, 'Turkey Wrap', 350, 30.0, 35.0, 12.0, 'LUNCH', DATE_SUB(NOW(), INTERVAL 4 DAY)),
(3, 'Vegetable Stir Fry', 200, 8.0, 25.0, 8.0, 'DINNER', DATE_SUB(NOW(), INTERVAL 4 DAY)),
(3, 'Overnight Oats', 300, 10.0, 50.0, 8.0, 'BREAKFAST', DATE_SUB(NOW(), INTERVAL 5 DAY)),
(3, 'Protein Bar', 200, 20.0, 22.0, 8.0, 'SNACK', DATE_SUB(NOW(), INTERVAL 5 DAY)),
-- user03 营养记录
(4, 'Chicken Rice Bowl', 450, 35.0, 50.0, 12.0, 'LUNCH', DATE_SUB(NOW(), INTERVAL 1 DAY)),
(4, 'Banana', 105, 1.0, 27.0, 0.5, 'SNACK', DATE_SUB(NOW(), INTERVAL 1 DAY)),
(4, 'Pasta with Meat Sauce', 550, 25.0, 70.0, 18.0, 'DINNER', DATE_SUB(NOW(), INTERVAL 2 DAY)),
(4, 'Scrambled Eggs', 220, 15.0, 2.0, 16.0, 'BREAKFAST', DATE_SUB(NOW(), INTERVAL 2 DAY)),
(4, 'Sandwich', 380, 18.0, 40.0, 16.0, 'LUNCH', DATE_SUB(NOW(), INTERVAL 3 DAY)),
(4, 'Apple', 95, 0.5, 25.0, 0.3, 'SNACK', DATE_SUB(NOW(), INTERVAL 3 DAY)),
(4, 'Grilled Pork', 400, 40.0, 0.0, 25.0, 'DINNER', DATE_SUB(NOW(), INTERVAL 4 DAY)),
(4, 'Cereal with Milk', 280, 10.0, 45.0, 8.0, 'BREAKFAST', DATE_SUB(NOW(), INTERVAL 4 DAY)),
(4, 'Beef Noodles', 520, 28.0, 60.0, 18.0, 'LUNCH', DATE_SUB(NOW(), INTERVAL 5 DAY)),
(4, 'Orange Juice', 110, 2.0, 26.0, 0.5, 'SNACK', DATE_SUB(NOW(), INTERVAL 5 DAY));


-- user04-user10 营养记录
INSERT INTO nutrition_records (user_id, food_name, calories, protein, carbs, fat, meal_type, record_time) VALUES
(5, 'Grilled Chicken', 320, 42.0, 0.0, 8.0, 'LUNCH', DATE_SUB(NOW(), INTERVAL 1 DAY)),
(5, 'Sweet Potato', 180, 4.0, 40.0, 0.5, 'LUNCH', DATE_SUB(NOW(), INTERVAL 1 DAY)),
(5, 'Protein Shake', 250, 45.0, 10.0, 3.0, 'SNACK', DATE_SUB(NOW(), INTERVAL 2 DAY)),
(5, 'Salmon', 380, 38.0, 0.0, 24.0, 'DINNER', DATE_SUB(NOW(), INTERVAL 2 DAY)),
(5, 'Egg White Omelette', 180, 25.0, 5.0, 6.0, 'BREAKFAST', DATE_SUB(NOW(), INTERVAL 3 DAY)),
(5, 'Greek Yogurt', 140, 18.0, 10.0, 4.0, 'SNACK', DATE_SUB(NOW(), INTERVAL 3 DAY)),
(5, 'Turkey Breast', 280, 45.0, 0.0, 8.0, 'DINNER', DATE_SUB(NOW(), INTERVAL 4 DAY)),
(5, 'Oatmeal', 300, 10.0, 55.0, 6.0, 'BREAKFAST', DATE_SUB(NOW(), INTERVAL 4 DAY)),
(5, 'Tuna Steak', 320, 50.0, 0.0, 12.0, 'LUNCH', DATE_SUB(NOW(), INTERVAL 5 DAY)),
(5, 'Almonds', 160, 6.0, 6.0, 14.0, 'SNACK', DATE_SUB(NOW(), INTERVAL 5 DAY)),
(6, 'Beef Steak', 480, 50.0, 0.0, 30.0, 'DINNER', DATE_SUB(NOW(), INTERVAL 1 DAY)),
(6, 'White Rice', 250, 5.0, 55.0, 1.0, 'DINNER', DATE_SUB(NOW(), INTERVAL 1 DAY)),
(6, 'Protein Shake', 300, 55.0, 12.0, 4.0, 'SNACK', DATE_SUB(NOW(), INTERVAL 2 DAY)),
(6, 'Chicken Breast', 350, 48.0, 0.0, 10.0, 'LUNCH', DATE_SUB(NOW(), INTERVAL 2 DAY)),
(6, 'Eggs Benedict', 450, 22.0, 30.0, 28.0, 'BREAKFAST', DATE_SUB(NOW(), INTERVAL 3 DAY)),
(6, 'Cottage Cheese', 200, 28.0, 8.0, 6.0, 'SNACK', DATE_SUB(NOW(), INTERVAL 3 DAY)),
(6, 'Pork Chop', 420, 45.0, 0.0, 25.0, 'DINNER', DATE_SUB(NOW(), INTERVAL 4 DAY)),
(6, 'Pancakes', 380, 10.0, 60.0, 12.0, 'BREAKFAST', DATE_SUB(NOW(), INTERVAL 4 DAY)),
(6, 'Grilled Fish', 280, 42.0, 0.0, 10.0, 'LUNCH', DATE_SUB(NOW(), INTERVAL 5 DAY)),
(6, 'Banana Shake', 320, 15.0, 50.0, 8.0, 'SNACK', DATE_SUB(NOW(), INTERVAL 5 DAY)),
(7, 'Salad Bowl', 180, 8.0, 20.0, 8.0, 'LUNCH', DATE_SUB(NOW(), INTERVAL 1 DAY)),
(7, 'Grilled Chicken', 200, 35.0, 0.0, 5.0, 'LUNCH', DATE_SUB(NOW(), INTERVAL 1 DAY)),
(7, 'Fruit Cup', 120, 2.0, 30.0, 0.5, 'SNACK', DATE_SUB(NOW(), INTERVAL 2 DAY)),
(7, 'Steamed Fish', 180, 32.0, 0.0, 4.0, 'DINNER', DATE_SUB(NOW(), INTERVAL 2 DAY)),
(7, 'Yogurt', 100, 12.0, 10.0, 2.0, 'BREAKFAST', DATE_SUB(NOW(), INTERVAL 3 DAY)),
(7, 'Vegetable Soup', 80, 4.0, 15.0, 1.0, 'DINNER', DATE_SUB(NOW(), INTERVAL 3 DAY)),
(7, 'Turkey Salad', 220, 28.0, 12.0, 8.0, 'LUNCH', DATE_SUB(NOW(), INTERVAL 4 DAY)),
(7, 'Oatmeal', 200, 7.0, 38.0, 4.0, 'BREAKFAST', DATE_SUB(NOW(), INTERVAL 4 DAY)),
(7, 'Grilled Shrimp', 150, 28.0, 2.0, 3.0, 'DINNER', DATE_SUB(NOW(), INTERVAL 5 DAY)),
(7, 'Apple', 95, 0.5, 25.0, 0.3, 'SNACK', DATE_SUB(NOW(), INTERVAL 5 DAY)),
(8, 'Ribeye Steak', 600, 55.0, 0.0, 42.0, 'DINNER', DATE_SUB(NOW(), INTERVAL 1 DAY)),
(8, 'Mashed Potatoes', 280, 5.0, 50.0, 8.0, 'DINNER', DATE_SUB(NOW(), INTERVAL 1 DAY)),
(8, 'Mass Gainer Shake', 650, 50.0, 90.0, 12.0, 'SNACK', DATE_SUB(NOW(), INTERVAL 2 DAY)),
(8, 'Grilled Chicken', 400, 55.0, 0.0, 12.0, 'LUNCH', DATE_SUB(NOW(), INTERVAL 2 DAY)),
(8, 'Eggs and Bacon', 550, 35.0, 5.0, 42.0, 'BREAKFAST', DATE_SUB(NOW(), INTERVAL 3 DAY)),
(8, 'Protein Bar', 280, 30.0, 25.0, 10.0, 'SNACK', DATE_SUB(NOW(), INTERVAL 3 DAY)),
(8, 'Lamb Chops', 520, 48.0, 0.0, 35.0, 'DINNER', DATE_SUB(NOW(), INTERVAL 4 DAY)),
(8, 'French Toast', 450, 15.0, 55.0, 20.0, 'BREAKFAST', DATE_SUB(NOW(), INTERVAL 4 DAY)),
(8, 'Tuna Steak', 380, 55.0, 0.0, 15.0, 'LUNCH', DATE_SUB(NOW(), INTERVAL 5 DAY)),
(8, 'Peanut Butter', 200, 8.0, 6.0, 16.0, 'SNACK', DATE_SUB(NOW(), INTERVAL 5 DAY)),
(9, 'Chicken Salad', 250, 32.0, 12.0, 10.0, 'LUNCH', DATE_SUB(NOW(), INTERVAL 1 DAY)),
(9, 'Brown Rice', 180, 4.0, 38.0, 2.0, 'LUNCH', DATE_SUB(NOW(), INTERVAL 1 DAY)),
(9, 'Protein Shake', 200, 35.0, 8.0, 3.0, 'SNACK', DATE_SUB(NOW(), INTERVAL 2 DAY)),
(9, 'Grilled Fish', 220, 38.0, 0.0, 6.0, 'DINNER', DATE_SUB(NOW(), INTERVAL 2 DAY)),
(9, 'Egg White Omelette', 150, 22.0, 3.0, 5.0, 'BREAKFAST', DATE_SUB(NOW(), INTERVAL 3 DAY)),
(9, 'Greek Yogurt', 120, 15.0, 8.0, 3.0, 'SNACK', DATE_SUB(NOW(), INTERVAL 3 DAY)),
(9, 'Turkey Breast', 240, 40.0, 0.0, 6.0, 'DINNER', DATE_SUB(NOW(), INTERVAL 4 DAY)),
(9, 'Oatmeal', 250, 8.0, 45.0, 5.0, 'BREAKFAST', DATE_SUB(NOW(), INTERVAL 4 DAY)),
(9, 'Shrimp Stir Fry', 280, 35.0, 15.0, 10.0, 'LUNCH', DATE_SUB(NOW(), INTERVAL 5 DAY)),
(9, 'Almonds', 140, 5.0, 5.0, 12.0, 'SNACK', DATE_SUB(NOW(), INTERVAL 5 DAY)),
(10, 'Chicken Rice', 420, 32.0, 48.0, 12.0, 'LUNCH', DATE_SUB(NOW(), INTERVAL 1 DAY)),
(10, 'Banana', 105, 1.0, 27.0, 0.5, 'SNACK', DATE_SUB(NOW(), INTERVAL 1 DAY)),
(10, 'Beef Noodles', 480, 28.0, 55.0, 18.0, 'DINNER', DATE_SUB(NOW(), INTERVAL 2 DAY)),
(10, 'Toast with Eggs', 300, 18.0, 28.0, 14.0, 'BREAKFAST', DATE_SUB(NOW(), INTERVAL 2 DAY)),
(10, 'Sandwich', 350, 20.0, 38.0, 14.0, 'LUNCH', DATE_SUB(NOW(), INTERVAL 3 DAY)),
(10, 'Orange', 62, 1.0, 15.0, 0.2, 'SNACK', DATE_SUB(NOW(), INTERVAL 3 DAY)),
(10, 'Grilled Pork', 380, 38.0, 0.0, 22.0, 'DINNER', DATE_SUB(NOW(), INTERVAL 4 DAY)),
(10, 'Cereal', 250, 8.0, 42.0, 6.0, 'BREAKFAST', DATE_SUB(NOW(), INTERVAL 4 DAY)),
(10, 'Fish and Chips', 550, 30.0, 50.0, 28.0, 'LUNCH', DATE_SUB(NOW(), INTERVAL 5 DAY)),
(10, 'Milk', 150, 8.0, 12.0, 8.0, 'SNACK', DATE_SUB(NOW(), INTERVAL 5 DAY)),
(11, 'Grilled Chicken Salad', 260, 35.0, 12.0, 10.0, 'LUNCH', DATE_SUB(NOW(), INTERVAL 1 DAY)),
(11, 'Quinoa', 180, 6.0, 32.0, 3.0, 'LUNCH', DATE_SUB(NOW(), INTERVAL 1 DAY)),
(11, 'Protein Shake', 220, 38.0, 10.0, 3.0, 'SNACK', DATE_SUB(NOW(), INTERVAL 2 DAY)),
(11, 'Salmon', 350, 38.0, 0.0, 20.0, 'DINNER', DATE_SUB(NOW(), INTERVAL 2 DAY)),
(11, 'Avocado Toast', 280, 8.0, 25.0, 18.0, 'BREAKFAST', DATE_SUB(NOW(), INTERVAL 3 DAY)),
(11, 'Greek Yogurt', 130, 16.0, 10.0, 4.0, 'SNACK', DATE_SUB(NOW(), INTERVAL 3 DAY)),
(11, 'Turkey Wrap', 320, 28.0, 32.0, 12.0, 'LUNCH', DATE_SUB(NOW(), INTERVAL 4 DAY)),
(11, 'Overnight Oats', 280, 10.0, 48.0, 7.0, 'BREAKFAST', DATE_SUB(NOW(), INTERVAL 4 DAY)),
(11, 'Grilled Fish', 240, 40.0, 0.0, 8.0, 'DINNER', DATE_SUB(NOW(), INTERVAL 5 DAY)),
(11, 'Mixed Nuts', 180, 6.0, 8.0, 16.0, 'SNACK', DATE_SUB(NOW(), INTERVAL 5 DAY));


-- =====================================================
-- 第八部分: 训练计划
-- =====================================================

INSERT INTO training_plans (user_id, name, description, difficulty, duration_weeks, status, created_at, updated_at) VALUES
(2, 'Push Pull Legs', 'Classic PPL split for muscle building', 'ADVANCED', 12, 'ACTIVE', DATE_SUB(NOW(), INTERVAL 30 DAY), NOW()),
(2, 'Strength Foundation', 'Basic strength program', 'INTERMEDIATE', 8, 'COMPLETED', DATE_SUB(NOW(), INTERVAL 120 DAY), DATE_SUB(NOW(), INTERVAL 60 DAY)),
(3, 'Full Body Beginner', 'Full body workout 3x per week', 'BEGINNER', 8, 'ACTIVE', DATE_SUB(NOW(), INTERVAL 20 DAY), NOW()),
(4, 'Getting Started', 'Introduction to weight training', 'BEGINNER', 6, 'ACTIVE', DATE_SUB(NOW(), INTERVAL 15 DAY), NOW()),
(5, 'Lower Body Focus', 'Emphasis on legs and glutes', 'INTERMEDIATE', 10, 'ACTIVE', DATE_SUB(NOW(), INTERVAL 25 DAY), NOW()),
(6, 'Hypertrophy Program', 'Muscle building focus', 'INTERMEDIATE', 12, 'ACTIVE', DATE_SUB(NOW(), INTERVAL 35 DAY), NOW()),
(7, 'Beginner Basics', 'Learn proper form', 'BEGINNER', 4, 'ACTIVE', DATE_SUB(NOW(), INTERVAL 10 DAY), NOW()),
(8, 'Advanced Powerlifting', 'Competition prep', 'ADVANCED', 16, 'ACTIVE', DATE_SUB(NOW(), INTERVAL 45 DAY), NOW()),
(9, 'Toning Program', 'Fat loss and muscle definition', 'INTERMEDIATE', 8, 'ACTIVE', DATE_SUB(NOW(), INTERVAL 18 DAY), NOW()),
(10, 'Starter Program', 'First gym program', 'BEGINNER', 6, 'ACTIVE', DATE_SUB(NOW(), INTERVAL 12 DAY), NOW()),
(11, 'Body Recomposition', 'Lose fat gain muscle', 'INTERMEDIATE', 10, 'ACTIVE', DATE_SUB(NOW(), INTERVAL 22 DAY), NOW());

-- =====================================================
-- 第九部分: 训练计划天数
-- =====================================================

INSERT INTO training_plan_days (plan_id, day_number, name, description) VALUES
(1, 1, 'Push Day', 'Chest, shoulders, triceps'),
(1, 2, 'Pull Day', 'Back, biceps'),
(1, 3, 'Leg Day', 'Quads, hamstrings, glutes'),
(1, 4, 'Rest', 'Active recovery'),
(1, 5, 'Push Day', 'Chest, shoulders, triceps'),
(1, 6, 'Pull Day', 'Back, biceps'),
(1, 7, 'Leg Day', 'Quads, hamstrings, glutes'),
(2, 1, 'Upper Body', 'Compound upper movements'),
(2, 2, 'Lower Body', 'Compound lower movements'),
(2, 3, 'Rest', 'Recovery'),
(2, 4, 'Upper Body', 'Compound upper movements'),
(2, 5, 'Lower Body', 'Compound lower movements'),
(3, 1, 'Full Body A', 'Squat focus'),
(3, 2, 'Rest', 'Recovery'),
(3, 3, 'Full Body B', 'Deadlift focus'),
(3, 4, 'Rest', 'Recovery'),
(3, 5, 'Full Body C', 'Bench focus'),
(4, 1, 'Introduction', 'Learn basic movements'),
(4, 2, 'Rest', 'Recovery'),
(4, 3, 'Practice', 'Reinforce form'),
(5, 1, 'Quad Focus', 'Squats and leg press'),
(5, 2, 'Glute Focus', 'Hip thrusts and lunges'),
(5, 3, 'Upper Body', 'Maintenance work'),
(5, 4, 'Hamstring Focus', 'Deadlifts and curls'),
(6, 1, 'Chest and Triceps', 'Push movements'),
(6, 2, 'Back and Biceps', 'Pull movements'),
(6, 3, 'Shoulders', 'Deltoid focus'),
(6, 4, 'Legs', 'Lower body'),
(7, 1, 'Full Body', 'Basic movements'),
(7, 2, 'Rest', 'Recovery'),
(7, 3, 'Full Body', 'Basic movements'),
(8, 1, 'Squat Day', 'Heavy squats'),
(8, 2, 'Bench Day', 'Heavy bench'),
(8, 3, 'Deadlift Day', 'Heavy deadlifts'),
(8, 4, 'Accessory Day', 'Weak point training'),
(9, 1, 'Upper Body', 'High rep upper'),
(9, 2, 'Lower Body', 'High rep lower'),
(9, 3, 'Cardio', 'HIIT session'),
(10, 1, 'Full Body', 'Learn movements'),
(10, 2, 'Rest', 'Recovery'),
(10, 3, 'Full Body', 'Practice form'),
(11, 1, 'Upper Body', 'Strength focus'),
(11, 2, 'Lower Body', 'Strength focus'),
(11, 3, 'Upper Body', 'Hypertrophy'),
(11, 4, 'Lower Body', 'Hypertrophy');


-- =====================================================
-- 第十部分: 训练计划动作
-- =====================================================

INSERT INTO training_plan_exercises (day_id, exercise_name, sets, reps, weight, rest_seconds, notes, order_index) VALUES
(1, 'Barbell Bench Press', 4, 8, 100.0, 180, 'Main lift', 1),
(1, 'Incline Dumbbell Press', 3, 10, 35.0, 120, NULL, 2),
(1, 'Dumbbell Shoulder Press', 3, 10, 25.0, 120, NULL, 3),
(1, 'Tricep Pushdown', 3, 12, 30.0, 90, NULL, 4),
(2, 'Barbell Row', 4, 8, 80.0, 180, 'Main lift', 1),
(2, 'Lat Pulldown', 3, 10, 60.0, 120, NULL, 2),
(2, 'Face Pull', 3, 15, 25.0, 90, NULL, 3),
(2, 'Barbell Curl', 3, 10, 35.0, 90, NULL, 4),
(3, 'Barbell Squat', 4, 6, 140.0, 240, 'Main lift', 1),
(3, 'Romanian Deadlift', 3, 10, 100.0, 150, NULL, 2),
(3, 'Leg Press', 3, 12, 180.0, 120, NULL, 3),
(3, 'Leg Curl', 3, 12, 50.0, 90, NULL, 4),
(8, 'Bench Press', 4, 8, 60.0, 150, NULL, 1),
(8, 'Dumbbell Row', 3, 10, 25.0, 120, NULL, 2),
(8, 'Shoulder Press', 3, 10, 20.0, 120, NULL, 3),
(9, 'Squat', 4, 8, 80.0, 180, NULL, 1),
(9, 'Leg Press', 3, 12, 100.0, 120, NULL, 2),
(9, 'Leg Curl', 3, 12, 40.0, 90, NULL, 3),
(13, 'Squat', 3, 10, 40.0, 120, NULL, 1),
(13, 'Bench Press', 3, 10, 30.0, 120, NULL, 2),
(13, 'Row', 3, 10, 30.0, 120, NULL, 3),
(15, 'Deadlift', 3, 8, 50.0, 150, NULL, 1),
(15, 'Shoulder Press', 3, 10, 15.0, 120, NULL, 2),
(15, 'Lat Pulldown', 3, 10, 35.0, 120, NULL, 3),
(17, 'Bench Press', 3, 10, 35.0, 120, NULL, 1),
(17, 'Squat', 3, 10, 45.0, 120, NULL, 2),
(17, 'Row', 3, 10, 35.0, 120, NULL, 3),
(18, 'Squat', 3, 8, 40.0, 120, 'Focus on form', 1),
(18, 'Bench Press', 3, 8, 30.0, 120, 'Focus on form', 2),
(18, 'Row', 3, 8, 25.0, 120, 'Focus on form', 3),
(20, 'Squat', 3, 10, 45.0, 120, NULL, 1),
(20, 'Bench Press', 3, 10, 35.0, 120, NULL, 2),
(20, 'Deadlift', 3, 8, 50.0, 150, NULL, 3),
(21, 'Squat', 4, 10, 65.0, 150, NULL, 1),
(21, 'Leg Press', 3, 12, 100.0, 120, NULL, 2),
(21, 'Leg Extension', 3, 15, 40.0, 90, NULL, 3),
(22, 'Hip Thrust', 4, 12, 70.0, 120, NULL, 1),
(22, 'Walking Lunges', 3, 12, 20.0, 90, NULL, 2),
(22, 'Glute Kickback', 3, 15, 15.0, 60, NULL, 3),
(23, 'Bench Press', 3, 10, 40.0, 120, NULL, 1),
(23, 'Row', 3, 10, 40.0, 120, NULL, 2),
(23, 'Shoulder Press', 3, 10, 20.0, 120, NULL, 3),
(24, 'Romanian Deadlift', 4, 10, 55.0, 150, NULL, 1),
(24, 'Leg Curl', 3, 12, 30.0, 90, NULL, 2),
(24, 'Calf Raise', 3, 15, 40.0, 60, NULL, 3);


-- =====================================================
-- 第十一部分: 恢复数据
-- =====================================================

INSERT INTO recovery_data (user_id, sleep_hours, sleep_quality, muscle_soreness, fatigue_level, stress_level, record_date) VALUES
(2, 7.5, 8, 3, 4, 3, DATE_SUB(CURDATE(), INTERVAL 1 DAY)),
(2, 8.0, 9, 2, 3, 2, DATE_SUB(CURDATE(), INTERVAL 2 DAY)),
(2, 6.5, 6, 5, 6, 4, DATE_SUB(CURDATE(), INTERVAL 3 DAY)),
(2, 7.0, 7, 4, 5, 3, DATE_SUB(CURDATE(), INTERVAL 4 DAY)),
(2, 8.5, 9, 2, 2, 2, DATE_SUB(CURDATE(), INTERVAL 5 DAY)),
(3, 7.0, 7, 4, 4, 4, DATE_SUB(CURDATE(), INTERVAL 1 DAY)),
(3, 7.5, 8, 3, 3, 3, DATE_SUB(CURDATE(), INTERVAL 2 DAY)),
(3, 6.0, 5, 5, 6, 5, DATE_SUB(CURDATE(), INTERVAL 3 DAY)),
(3, 8.0, 8, 2, 3, 3, DATE_SUB(CURDATE(), INTERVAL 4 DAY)),
(3, 7.0, 7, 3, 4, 4, DATE_SUB(CURDATE(), INTERVAL 5 DAY)),
(4, 6.5, 6, 5, 5, 5, DATE_SUB(CURDATE(), INTERVAL 1 DAY)),
(4, 7.0, 7, 4, 4, 4, DATE_SUB(CURDATE(), INTERVAL 2 DAY)),
(4, 7.5, 8, 3, 3, 3, DATE_SUB(CURDATE(), INTERVAL 3 DAY)),
(4, 6.0, 5, 6, 6, 5, DATE_SUB(CURDATE(), INTERVAL 4 DAY)),
(4, 8.0, 8, 2, 2, 3, DATE_SUB(CURDATE(), INTERVAL 5 DAY)),
(5, 8.0, 9, 2, 2, 2, DATE_SUB(CURDATE(), INTERVAL 1 DAY)),
(5, 7.5, 8, 3, 3, 3, DATE_SUB(CURDATE(), INTERVAL 2 DAY)),
(5, 7.0, 7, 4, 4, 3, DATE_SUB(CURDATE(), INTERVAL 3 DAY)),
(5, 8.5, 9, 2, 2, 2, DATE_SUB(CURDATE(), INTERVAL 4 DAY)),
(5, 7.0, 7, 3, 4, 4, DATE_SUB(CURDATE(), INTERVAL 5 DAY)),
(6, 7.0, 7, 4, 5, 4, DATE_SUB(CURDATE(), INTERVAL 1 DAY)),
(6, 7.5, 8, 3, 4, 3, DATE_SUB(CURDATE(), INTERVAL 2 DAY)),
(6, 6.5, 6, 5, 5, 5, DATE_SUB(CURDATE(), INTERVAL 3 DAY)),
(6, 8.0, 8, 2, 3, 3, DATE_SUB(CURDATE(), INTERVAL 4 DAY)),
(6, 7.0, 7, 4, 4, 4, DATE_SUB(CURDATE(), INTERVAL 5 DAY)),
(7, 6.0, 5, 5, 6, 6, DATE_SUB(CURDATE(), INTERVAL 1 DAY)),
(7, 6.5, 6, 4, 5, 5, DATE_SUB(CURDATE(), INTERVAL 2 DAY)),
(7, 7.0, 7, 4, 4, 4, DATE_SUB(CURDATE(), INTERVAL 3 DAY)),
(7, 7.5, 8, 3, 3, 3, DATE_SUB(CURDATE(), INTERVAL 4 DAY)),
(7, 6.0, 5, 5, 6, 5, DATE_SUB(CURDATE(), INTERVAL 5 DAY)),
(8, 8.0, 9, 3, 3, 2, DATE_SUB(CURDATE(), INTERVAL 1 DAY)),
(8, 8.5, 9, 2, 2, 2, DATE_SUB(CURDATE(), INTERVAL 2 DAY)),
(8, 7.5, 8, 4, 4, 3, DATE_SUB(CURDATE(), INTERVAL 3 DAY)),
(8, 8.0, 8, 3, 3, 2, DATE_SUB(CURDATE(), INTERVAL 4 DAY)),
(8, 9.0, 10, 2, 2, 1, DATE_SUB(CURDATE(), INTERVAL 5 DAY)),
(9, 7.0, 7, 4, 4, 4, DATE_SUB(CURDATE(), INTERVAL 1 DAY)),
(9, 7.5, 8, 3, 3, 3, DATE_SUB(CURDATE(), INTERVAL 2 DAY)),
(9, 6.5, 6, 5, 5, 5, DATE_SUB(CURDATE(), INTERVAL 3 DAY)),
(9, 8.0, 8, 2, 3, 3, DATE_SUB(CURDATE(), INTERVAL 4 DAY)),
(9, 7.0, 7, 4, 4, 4, DATE_SUB(CURDATE(), INTERVAL 5 DAY)),
(10, 6.5, 6, 5, 5, 5, DATE_SUB(CURDATE(), INTERVAL 1 DAY)),
(10, 7.0, 7, 4, 4, 4, DATE_SUB(CURDATE(), INTERVAL 2 DAY)),
(10, 7.5, 8, 3, 3, 3, DATE_SUB(CURDATE(), INTERVAL 3 DAY)),
(10, 6.0, 5, 6, 6, 6, DATE_SUB(CURDATE(), INTERVAL 4 DAY)),
(10, 7.0, 7, 4, 4, 4, DATE_SUB(CURDATE(), INTERVAL 5 DAY)),
(11, 7.5, 8, 3, 3, 3, DATE_SUB(CURDATE(), INTERVAL 1 DAY)),
(11, 7.0, 7, 4, 4, 4, DATE_SUB(CURDATE(), INTERVAL 2 DAY)),
(11, 8.0, 8, 2, 3, 3, DATE_SUB(CURDATE(), INTERVAL 3 DAY)),
(11, 6.5, 6, 5, 5, 5, DATE_SUB(CURDATE(), INTERVAL 4 DAY)),
(11, 7.5, 8, 3, 3, 3, DATE_SUB(CURDATE(), INTERVAL 5 DAY));

-- =====================================================
-- 第十二部分: 恢复指标
-- =====================================================

INSERT INTO recovery_metrics (user_id, hrv, resting_heart_rate, recovery_score, readiness_score, record_date) VALUES
(2, 65.0, 58, 85.0, 88.0, DATE_SUB(CURDATE(), INTERVAL 1 DAY)),
(2, 70.0, 55, 90.0, 92.0, DATE_SUB(CURDATE(), INTERVAL 2 DAY)),
(2, 55.0, 62, 70.0, 72.0, DATE_SUB(CURDATE(), INTERVAL 3 DAY)),
(2, 60.0, 60, 78.0, 80.0, DATE_SUB(CURDATE(), INTERVAL 4 DAY)),
(2, 72.0, 54, 92.0, 94.0, DATE_SUB(CURDATE(), INTERVAL 5 DAY)),
(3, 58.0, 65, 75.0, 78.0, DATE_SUB(CURDATE(), INTERVAL 1 DAY)),
(3, 62.0, 62, 80.0, 82.0, DATE_SUB(CURDATE(), INTERVAL 2 DAY)),
(3, 50.0, 70, 65.0, 68.0, DATE_SUB(CURDATE(), INTERVAL 3 DAY)),
(3, 65.0, 60, 82.0, 85.0, DATE_SUB(CURDATE(), INTERVAL 4 DAY)),
(3, 55.0, 66, 72.0, 75.0, DATE_SUB(CURDATE(), INTERVAL 5 DAY)),
(4, 52.0, 68, 68.0, 70.0, DATE_SUB(CURDATE(), INTERVAL 1 DAY)),
(4, 55.0, 66, 72.0, 74.0, DATE_SUB(CURDATE(), INTERVAL 2 DAY)),
(4, 60.0, 63, 78.0, 80.0, DATE_SUB(CURDATE(), INTERVAL 3 DAY)),
(4, 48.0, 72, 62.0, 65.0, DATE_SUB(CURDATE(), INTERVAL 4 DAY)),
(4, 62.0, 62, 80.0, 82.0, DATE_SUB(CURDATE(), INTERVAL 5 DAY)),
(5, 68.0, 56, 88.0, 90.0, DATE_SUB(CURDATE(), INTERVAL 1 DAY)),
(5, 65.0, 58, 85.0, 87.0, DATE_SUB(CURDATE(), INTERVAL 2 DAY)),
(5, 60.0, 60, 78.0, 80.0, DATE_SUB(CURDATE(), INTERVAL 3 DAY)),
(5, 70.0, 55, 90.0, 92.0, DATE_SUB(CURDATE(), INTERVAL 4 DAY)),
(5, 58.0, 62, 75.0, 78.0, DATE_SUB(CURDATE(), INTERVAL 5 DAY)),
(6, 55.0, 64, 72.0, 75.0, DATE_SUB(CURDATE(), INTERVAL 1 DAY)),
(6, 60.0, 62, 78.0, 80.0, DATE_SUB(CURDATE(), INTERVAL 2 DAY)),
(6, 50.0, 68, 65.0, 68.0, DATE_SUB(CURDATE(), INTERVAL 3 DAY)),
(6, 62.0, 60, 80.0, 82.0, DATE_SUB(CURDATE(), INTERVAL 4 DAY)),
(6, 55.0, 65, 72.0, 75.0, DATE_SUB(CURDATE(), INTERVAL 5 DAY)),
(7, 45.0, 72, 58.0, 60.0, DATE_SUB(CURDATE(), INTERVAL 1 DAY)),
(7, 48.0, 70, 62.0, 65.0, DATE_SUB(CURDATE(), INTERVAL 2 DAY)),
(7, 52.0, 68, 68.0, 70.0, DATE_SUB(CURDATE(), INTERVAL 3 DAY)),
(7, 55.0, 66, 72.0, 74.0, DATE_SUB(CURDATE(), INTERVAL 4 DAY)),
(7, 45.0, 72, 58.0, 60.0, DATE_SUB(CURDATE(), INTERVAL 5 DAY)),
(8, 75.0, 52, 92.0, 94.0, DATE_SUB(CURDATE(), INTERVAL 1 DAY)),
(8, 78.0, 50, 95.0, 96.0, DATE_SUB(CURDATE(), INTERVAL 2 DAY)),
(8, 68.0, 56, 85.0, 88.0, DATE_SUB(CURDATE(), INTERVAL 3 DAY)),
(8, 72.0, 54, 90.0, 92.0, DATE_SUB(CURDATE(), INTERVAL 4 DAY)),
(8, 80.0, 48, 98.0, 98.0, DATE_SUB(CURDATE(), INTERVAL 5 DAY)),
(9, 55.0, 64, 72.0, 75.0, DATE_SUB(CURDATE(), INTERVAL 1 DAY)),
(9, 60.0, 62, 78.0, 80.0, DATE_SUB(CURDATE(), INTERVAL 2 DAY)),
(9, 50.0, 68, 65.0, 68.0, DATE_SUB(CURDATE(), INTERVAL 3 DAY)),
(9, 62.0, 60, 80.0, 82.0, DATE_SUB(CURDATE(), INTERVAL 4 DAY)),
(9, 55.0, 65, 72.0, 75.0, DATE_SUB(CURDATE(), INTERVAL 5 DAY)),
(10, 48.0, 70, 62.0, 65.0, DATE_SUB(CURDATE(), INTERVAL 1 DAY)),
(10, 52.0, 68, 68.0, 70.0, DATE_SUB(CURDATE(), INTERVAL 2 DAY)),
(10, 55.0, 66, 72.0, 74.0, DATE_SUB(CURDATE(), INTERVAL 3 DAY)),
(10, 45.0, 72, 58.0, 60.0, DATE_SUB(CURDATE(), INTERVAL 4 DAY)),
(10, 50.0, 68, 65.0, 68.0, DATE_SUB(CURDATE(), INTERVAL 5 DAY)),
(11, 60.0, 62, 78.0, 80.0, DATE_SUB(CURDATE(), INTERVAL 1 DAY)),
(11, 55.0, 65, 72.0, 75.0, DATE_SUB(CURDATE(), INTERVAL 2 DAY)),
(11, 62.0, 60, 80.0, 82.0, DATE_SUB(CURDATE(), INTERVAL 3 DAY)),
(11, 50.0, 68, 65.0, 68.0, DATE_SUB(CURDATE(), INTERVAL 4 DAY)),
(11, 58.0, 63, 75.0, 78.0, DATE_SUB(CURDATE(), INTERVAL 5 DAY));


-- =====================================================
-- 第十三部分: 用户成就
-- =====================================================

INSERT INTO user_achievements (user_id, achievement_type, achievement_name, description, achieved_at, points_earned) VALUES
(2, 'TRAINING', 'First Workout', 'Completed first training session', DATE_SUB(NOW(), INTERVAL 180 DAY), 50),
(2, 'TRAINING', 'Week Warrior', 'Trained 7 days in a row', DATE_SUB(NOW(), INTERVAL 150 DAY), 100),
(2, 'STRENGTH', 'Bench Press 100kg', 'Benched 100kg for the first time', DATE_SUB(NOW(), INTERVAL 90 DAY), 200),
(2, 'STRENGTH', 'Squat 140kg', 'Squatted 140kg for the first time', DATE_SUB(NOW(), INTERVAL 60 DAY), 250),
(2, 'CONSISTENCY', 'Month Master', 'Trained consistently for 30 days', DATE_SUB(NOW(), INTERVAL 120 DAY), 150),
(3, 'TRAINING', 'First Workout', 'Completed first training session', DATE_SUB(NOW(), INTERVAL 150 DAY), 50),
(3, 'WEIGHT_LOSS', 'First 2kg Lost', 'Lost first 2kg of body weight', DATE_SUB(NOW(), INTERVAL 100 DAY), 100),
(3, 'CONSISTENCY', 'Two Week Streak', 'Trained for 14 consecutive days', DATE_SUB(NOW(), INTERVAL 80 DAY), 75),
(3, 'NUTRITION', 'Calorie Tracker', 'Logged meals for 7 days straight', DATE_SUB(NOW(), INTERVAL 60 DAY), 50),
(4, 'TRAINING', 'First Workout', 'Completed first training session', DATE_SUB(NOW(), INTERVAL 90 DAY), 50),
(4, 'TRAINING', 'Form Master', 'Completed form tutorial', DATE_SUB(NOW(), INTERVAL 85 DAY), 30),
(4, 'CONSISTENCY', 'Week Starter', 'Trained 3 times in first week', DATE_SUB(NOW(), INTERVAL 80 DAY), 40),
(5, 'TRAINING', 'First Workout', 'Completed first training session', DATE_SUB(NOW(), INTERVAL 200 DAY), 50),
(5, 'STRENGTH', 'Squat 70kg', 'Squatted 70kg for the first time', DATE_SUB(NOW(), INTERVAL 150 DAY), 150),
(5, 'CONSISTENCY', 'Month Master', 'Trained consistently for 30 days', DATE_SUB(NOW(), INTERVAL 160 DAY), 150),
(5, 'TRAINING', 'Leg Day Legend', 'Completed 50 leg workouts', DATE_SUB(NOW(), INTERVAL 100 DAY), 200),
(6, 'TRAINING', 'First Workout', 'Completed first training session', DATE_SUB(NOW(), INTERVAL 120 DAY), 50),
(6, 'STRENGTH', 'Bench Press 80kg', 'Benched 80kg for the first time', DATE_SUB(NOW(), INTERVAL 80 DAY), 150),
(6, 'CONSISTENCY', 'Two Week Streak', 'Trained for 14 consecutive days', DATE_SUB(NOW(), INTERVAL 90 DAY), 75),
(7, 'TRAINING', 'First Workout', 'Completed first training session', DATE_SUB(NOW(), INTERVAL 60 DAY), 50),
(7, 'TRAINING', 'Beginner Badge', 'Completed beginner program', DATE_SUB(NOW(), INTERVAL 40 DAY), 30),
(8, 'TRAINING', 'First Workout', 'Completed first training session', DATE_SUB(NOW(), INTERVAL 300 DAY), 50),
(8, 'STRENGTH', 'Bench Press 120kg', 'Benched 120kg for the first time', DATE_SUB(NOW(), INTERVAL 200 DAY), 300),
(8, 'STRENGTH', 'Squat 160kg', 'Squatted 160kg for the first time', DATE_SUB(NOW(), INTERVAL 150 DAY), 350),
(8, 'STRENGTH', 'Deadlift 180kg', 'Deadlifted 180kg for the first time', DATE_SUB(NOW(), INTERVAL 100 DAY), 400),
(8, 'CONSISTENCY', 'Year Warrior', 'Trained consistently for 365 days', DATE_SUB(NOW(), INTERVAL 50 DAY), 500),
(9, 'TRAINING', 'First Workout', 'Completed first training session', DATE_SUB(NOW(), INTERVAL 100 DAY), 50),
(9, 'WEIGHT_LOSS', 'First 2kg Lost', 'Lost first 2kg of body weight', DATE_SUB(NOW(), INTERVAL 70 DAY), 100),
(9, 'CONSISTENCY', 'Two Week Streak', 'Trained for 14 consecutive days', DATE_SUB(NOW(), INTERVAL 60 DAY), 75),
(10, 'TRAINING', 'First Workout', 'Completed first training session', DATE_SUB(NOW(), INTERVAL 45 DAY), 50),
(10, 'TRAINING', 'Form Master', 'Completed form tutorial', DATE_SUB(NOW(), INTERVAL 40 DAY), 30),
(11, 'TRAINING', 'First Workout', 'Completed first training session', DATE_SUB(NOW(), INTERVAL 130 DAY), 50),
(11, 'WEIGHT_LOSS', 'First 2kg Lost', 'Lost first 2kg of body weight', DATE_SUB(NOW(), INTERVAL 90 DAY), 100),
(11, 'CONSISTENCY', 'Month Master', 'Trained consistently for 30 days', DATE_SUB(NOW(), INTERVAL 80 DAY), 150);

-- =====================================================
-- 第十四部分: 训练建议
-- =====================================================

INSERT INTO training_advices (user_id, advice_type, title, content, priority, is_read, created_at) VALUES
(2, 'RECOVERY', 'Rest Day Recommended', 'Based on your recent training volume, consider taking a rest day tomorrow.', 'HIGH', 0, DATE_SUB(NOW(), INTERVAL 1 DAY)),
(2, 'NUTRITION', 'Increase Protein', 'Your protein intake is below target. Consider adding a protein shake post-workout.', 'MEDIUM', 1, DATE_SUB(NOW(), INTERVAL 3 DAY)),
(2, 'TRAINING', 'Progressive Overload', 'Time to increase bench press weight by 2.5kg based on your recent performance.', 'MEDIUM', 0, DATE_SUB(NOW(), INTERVAL 5 DAY)),
(3, 'TRAINING', 'Form Check', 'Consider recording your squat form to ensure proper technique.', 'MEDIUM', 0, DATE_SUB(NOW(), INTERVAL 1 DAY)),
(3, 'NUTRITION', 'Calorie Deficit', 'You are on track with your calorie deficit. Keep it up!', 'LOW', 1, DATE_SUB(NOW(), INTERVAL 2 DAY)),
(3, 'RECOVERY', 'Sleep Quality', 'Your sleep quality has been low. Try to improve sleep hygiene.', 'HIGH', 0, DATE_SUB(NOW(), INTERVAL 4 DAY)),
(4, 'TRAINING', 'Beginner Tips', 'Focus on learning proper form before increasing weights.', 'HIGH', 0, DATE_SUB(NOW(), INTERVAL 1 DAY)),
(4, 'NUTRITION', 'Meal Timing', 'Try to eat a balanced meal 2-3 hours before training.', 'LOW', 1, DATE_SUB(NOW(), INTERVAL 3 DAY)),
(5, 'TRAINING', 'Leg Day Progress', 'Great progress on your leg workouts! Consider adding hip thrusts.', 'MEDIUM', 0, DATE_SUB(NOW(), INTERVAL 1 DAY)),
(5, 'RECOVERY', 'Stretching', 'Add 10 minutes of stretching after leg workouts for better recovery.', 'LOW', 1, DATE_SUB(NOW(), INTERVAL 2 DAY)),
(6, 'TRAINING', 'Upper Body Balance', 'Add more pulling exercises to balance your push-heavy routine.', 'MEDIUM', 0, DATE_SUB(NOW(), INTERVAL 1 DAY)),
(6, 'NUTRITION', 'Pre-Workout Meal', 'Consider eating complex carbs 2 hours before training.', 'LOW', 1, DATE_SUB(NOW(), INTERVAL 3 DAY)),
(7, 'TRAINING', 'Consistency', 'Try to maintain at least 3 workouts per week for best results.', 'HIGH', 0, DATE_SUB(NOW(), INTERVAL 1 DAY)),
(7, 'RECOVERY', 'Sleep More', 'Aim for 7-8 hours of sleep for optimal recovery.', 'HIGH', 0, DATE_SUB(NOW(), INTERVAL 2 DAY)),
(8, 'TRAINING', 'Deload Week', 'Consider a deload week to prevent overtraining.', 'HIGH', 0, DATE_SUB(NOW(), INTERVAL 1 DAY)),
(8, 'NUTRITION', 'Competition Prep', 'Start adjusting macros for your upcoming competition.', 'HIGH', 0, DATE_SUB(NOW(), INTERVAL 3 DAY)),
(9, 'TRAINING', 'Cardio Addition', 'Add 2-3 HIIT sessions per week for faster fat loss.', 'MEDIUM', 0, DATE_SUB(NOW(), INTERVAL 1 DAY)),
(9, 'NUTRITION', 'Protein Target', 'Increase protein to preserve muscle during weight loss.', 'HIGH', 1, DATE_SUB(NOW(), INTERVAL 2 DAY)),
(10, 'TRAINING', 'Form Focus', 'Spend extra time on squat form before adding weight.', 'HIGH', 0, DATE_SUB(NOW(), INTERVAL 1 DAY)),
(10, 'RECOVERY', 'Rest Days', 'Take at least 2 rest days per week as a beginner.', 'MEDIUM', 1, DATE_SUB(NOW(), INTERVAL 3 DAY)),
(11, 'TRAINING', 'Progressive Overload', 'Time to increase weights on your main lifts.', 'MEDIUM', 0, DATE_SUB(NOW(), INTERVAL 1 DAY)),
(11, 'NUTRITION', 'Meal Prep', 'Consider meal prepping to stay consistent with nutrition.', 'LOW', 1, DATE_SUB(NOW(), INTERVAL 2 DAY));


-- =====================================================
-- 第十五部分: 审计日志 (管理员操作记录)
-- =====================================================

INSERT INTO audit_logs (user_id, action, entity_type, entity_id, old_value, new_value, ip_address, user_agent, created_at) VALUES
(1, 'LOGIN', 'USER', 1, NULL, NULL, '192.168.1.100', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64)', DATE_SUB(NOW(), INTERVAL 1 DAY)),
(1, 'VIEW', 'USER_LIST', NULL, NULL, NULL, '192.168.1.100', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64)', DATE_SUB(NOW(), INTERVAL 1 DAY)),
(1, 'UPDATE', 'USER', 3, '{"role":"USER"}', '{"role":"USER"}', '192.168.1.100', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64)', DATE_SUB(NOW(), INTERVAL 2 DAY)),
(1, 'VIEW', 'SYSTEM_STATS', NULL, NULL, NULL, '192.168.1.100', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64)', DATE_SUB(NOW(), INTERVAL 2 DAY)),
(1, 'LOGIN', 'USER', 1, NULL, NULL, '192.168.1.100', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64)', DATE_SUB(NOW(), INTERVAL 3 DAY)),
(1, 'VIEW', 'AUDIT_LOGS', NULL, NULL, NULL, '192.168.1.100', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64)', DATE_SUB(NOW(), INTERVAL 3 DAY)),
(1, 'UPDATE', 'SYSTEM_SETTINGS', NULL, '{"maintenance":false}', '{"maintenance":false}', '192.168.1.100', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64)', DATE_SUB(NOW(), INTERVAL 4 DAY)),
(1, 'LOGIN', 'USER', 1, NULL, NULL, '192.168.1.100', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64)', DATE_SUB(NOW(), INTERVAL 5 DAY)),
(1, 'VIEW', 'USER', 2, NULL, NULL, '192.168.1.100', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64)', DATE_SUB(NOW(), INTERVAL 5 DAY)),
(1, 'VIEW', 'USER', 5, NULL, NULL, '192.168.1.100', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64)', DATE_SUB(NOW(), INTERVAL 6 DAY)),
(2, 'LOGIN', 'USER', 2, NULL, NULL, '192.168.1.101', 'Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7)', DATE_SUB(NOW(), INTERVAL 1 DAY)),
(2, 'UPDATE', 'PROFILE', 2, '{"weight":81.5}', '{"weight":82.0}', '192.168.1.101', 'Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7)', DATE_SUB(NOW(), INTERVAL 1 DAY)),
(3, 'LOGIN', 'USER', 3, NULL, NULL, '192.168.1.102', 'Mozilla/5.0 (iPhone; CPU iPhone OS 15_0)', DATE_SUB(NOW(), INTERVAL 1 DAY)),
(4, 'LOGIN', 'USER', 4, NULL, NULL, '192.168.1.103', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64)', DATE_SUB(NOW(), INTERVAL 2 DAY)),
(5, 'LOGIN', 'USER', 5, NULL, NULL, '192.168.1.104', 'Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7)', DATE_SUB(NOW(), INTERVAL 1 DAY));

-- =====================================================
-- 完成
-- =====================================================

SET FOREIGN_KEY_CHECKS = 1;

-- 验证数据
SELECT 'Users' as table_name, COUNT(*) as count FROM user_table
UNION ALL SELECT 'User Settings', COUNT(*) FROM user_settings
UNION ALL SELECT 'Nutrition Goals', COUNT(*) FROM user_nutrition_goals
UNION ALL SELECT 'Body Records', COUNT(*) FROM body_records
UNION ALL SELECT 'Training Records', COUNT(*) FROM training_records
UNION ALL SELECT 'Nutrition Records', COUNT(*) FROM nutrition_records
UNION ALL SELECT 'Training Plans', COUNT(*) FROM training_plans
UNION ALL SELECT 'Plan Days', COUNT(*) FROM training_plan_days
UNION ALL SELECT 'Plan Exercises', COUNT(*) FROM training_plan_exercises
UNION ALL SELECT 'Recovery Data', COUNT(*) FROM recovery_data
UNION ALL SELECT 'Recovery Metrics', COUNT(*) FROM recovery_metrics
UNION ALL SELECT 'Achievements', COUNT(*) FROM user_achievements
UNION ALL SELECT 'Training Advices', COUNT(*) FROM training_advices
UNION ALL SELECT 'Audit Logs', COUNT(*) FROM audit_logs;
