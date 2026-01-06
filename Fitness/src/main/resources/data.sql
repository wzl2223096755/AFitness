-- =====================================================
-- AFitness 测试数据 (MySQL)
-- =====================================================
-- 测试账户:
-- 管理员: admin / Admin123! (由系统自动设置)
-- 普通用户: testuser, fitnessfan, gymmaster, healthpro / password
-- =====================================================

SET NAMES utf8mb4;

-- -----------------------------------------------------
-- 用户数据 (admin已由系统创建，这里添加普通用户)
-- -----------------------------------------------------
INSERT IGNORE INTO `user_table` (`id`, `username`, `password`, `email`, `age`, `weight`, `gender`, `height`, `experience_level`, `role`, `points`) VALUES
(2, 'testuser', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZRGdjGj/n3.rsQ0fBZVIPAe/zIhne', 'testuser@example.com', 25, 70.5, 'MALE', 175, 'INTERMEDIATE', 'USER', 150),
(3, 'fitnessfan', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZRGdjGj/n3.rsQ0fBZVIPAe/zIhne', 'fitnessfan@example.com', 28, 65.0, 'FEMALE', 165, 'BEGINNER', 'USER', 80),
(4, 'gymmaster', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZRGdjGj/n3.rsQ0fBZVIPAe/zIhne', 'gymmaster@example.com', 32, 80.0, 'MALE', 180, 'ADVANCED', 'USER', 320),
(5, 'healthpro', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZRGdjGj/n3.rsQ0fBZVIPAe/zIhne', 'healthpro@example.com', 22, 55.0, 'FEMALE', 160, 'BEGINNER', 'USER', 45);

-- -----------------------------------------------------
-- 用户设置
-- -----------------------------------------------------
INSERT IGNORE INTO `user_settings` (`user_id`, `theme`, `language`, `notifications`, `auto_save`) VALUES
(1, 'dark', 'zh-CN', 1, 1),
(2, 'light', 'zh-CN', 1, 1),
(3, 'dark', 'zh-CN', 0, 1),
(4, 'light', 'en-US', 1, 0),
(5, 'dark', 'zh-CN', 1, 1);

-- -----------------------------------------------------
-- 身体数据记录 (20条)
-- -----------------------------------------------------
INSERT IGNORE INTO `body_records` (`user_id`, `weight`, `body_fat`, `muscle_mass`, `waist_circumference`, `hip_circumference`, `chest_circumference`, `record_time`) VALUES
(1, 72.5, 18.5, 32.0, 82.0, 95.0, 98.0, DATE_SUB(NOW(), INTERVAL 30 DAY)),
(1, 72.0, 18.2, 32.2, 81.5, 94.5, 98.5, DATE_SUB(NOW(), INTERVAL 25 DAY)),
(1, 71.5, 17.8, 32.5, 81.0, 94.0, 99.0, DATE_SUB(NOW(), INTERVAL 20 DAY)),
(1, 71.0, 17.5, 32.8, 80.5, 93.5, 99.5, DATE_SUB(NOW(), INTERVAL 15 DAY)),
(1, 70.5, 17.2, 33.0, 80.0, 93.0, 100.0, DATE_SUB(NOW(), INTERVAL 10 DAY)),
(2, 70.5, 16.0, 30.5, 78.0, 92.0, 95.0, DATE_SUB(NOW(), INTERVAL 28 DAY)),
(2, 70.2, 15.8, 30.8, 77.5, 91.5, 95.5, DATE_SUB(NOW(), INTERVAL 21 DAY)),
(2, 69.8, 15.5, 31.0, 77.0, 91.0, 96.0, DATE_SUB(NOW(), INTERVAL 14 DAY)),
(2, 69.5, 15.2, 31.2, 76.5, 90.5, 96.5, DATE_SUB(NOW(), INTERVAL 7 DAY)),
(3, 65.0, 22.0, 25.0, 70.0, 88.0, 85.0, DATE_SUB(NOW(), INTERVAL 20 DAY)),
(3, 64.5, 21.5, 25.3, 69.5, 87.5, 85.5, DATE_SUB(NOW(), INTERVAL 10 DAY)),
(4, 80.0, 15.0, 38.0, 85.0, 98.0, 105.0, DATE_SUB(NOW(), INTERVAL 25 DAY)),
(4, 79.5, 14.8, 38.2, 84.5, 97.5, 105.5, DATE_SUB(NOW(), INTERVAL 18 DAY)),
(4, 79.0, 14.5, 38.5, 84.0, 97.0, 106.0, DATE_SUB(NOW(), INTERVAL 11 DAY)),
(4, 78.5, 14.2, 38.8, 83.5, 96.5, 106.5, DATE_SUB(NOW(), INTERVAL 4 DAY)),
(5, 55.0, 25.0, 20.0, 65.0, 85.0, 80.0, DATE_SUB(NOW(), INTERVAL 15 DAY)),
(5, 54.8, 24.5, 20.2, 64.5, 84.5, 80.5, DATE_SUB(NOW(), INTERVAL 8 DAY)),
(5, 54.5, 24.0, 20.5, 64.0, 84.0, 81.0, DATE_SUB(NOW(), INTERVAL 1 DAY)),
(1, 70.0, 17.0, 33.2, 79.5, 92.5, 100.5, DATE_SUB(NOW(), INTERVAL 5 DAY)),
(1, 69.8, 16.8, 33.5, 79.0, 92.0, 101.0, NOW());

-- -----------------------------------------------------
-- 训练计划 (5条)
-- -----------------------------------------------------
INSERT IGNORE INTO `training_plans` (`id`, `user_id`, `name`, `description`, `start_date`, `end_date`, `status`, `goal`, `level`, `duration_weeks`, `days_per_week`, `duration_per_session`) VALUES
(1, 1, '增肌计划A', '针对上半身的增肌训练计划', CURDATE(), DATE_ADD(CURDATE(), INTERVAL 56 DAY), 'ACTIVE', '增肌', '中级', 8, 4, 60),
(2, 1, '减脂计划', '高强度间歇训练减脂', DATE_SUB(CURDATE(), INTERVAL 28 DAY), DATE_ADD(CURDATE(), INTERVAL 28 DAY), 'ACTIVE', '减脂', '中级', 8, 5, 45),
(3, 2, '新手入门', '适合健身新手的基础训练', CURDATE(), DATE_ADD(CURDATE(), INTERVAL 28 DAY), 'ACTIVE', '健身', '初级', 4, 3, 40),
(4, 4, '力量提升', '专注于提升最大力量', DATE_SUB(CURDATE(), INTERVAL 14 DAY), DATE_ADD(CURDATE(), INTERVAL 42 DAY), 'ACTIVE', '力量', '高级', 8, 4, 75),
(5, 3, '塑形计划', '全身塑形训练', CURDATE(), DATE_ADD(CURDATE(), INTERVAL 42 DAY), 'ACTIVE', '塑形', '初级', 6, 4, 50);

-- -----------------------------------------------------
-- 训练计划天数
-- -----------------------------------------------------
INSERT IGNORE INTO `training_plan_days` (`id`, `plan_id`, `day_of_week`, `day_name`, `has_training`, `focus`) VALUES
(1, 1, 1, '周一', 1, '胸部+三头'),
(2, 1, 3, '周三', 1, '背部+二头'),
(3, 1, 5, '周五', 1, '肩部'),
(4, 1, 6, '周六', 1, '腿部'),
(5, 2, 1, '周一', 1, 'HIIT'),
(6, 2, 2, '周二', 1, '有氧'),
(7, 2, 3, '周三', 1, 'HIIT'),
(8, 2, 4, '周四', 1, '力量'),
(9, 2, 5, '周五', 1, 'HIIT'),
(10, 3, 1, '周一', 1, '全身'),
(11, 3, 3, '周三', 1, '全身'),
(12, 3, 5, '周五', 1, '全身');

-- -----------------------------------------------------
-- 训练计划动作
-- -----------------------------------------------------
INSERT IGNORE INTO `training_plan_exercises` (`day_id`, `name`, `sets`, `reps`, `weight`, `target_muscles`, `rest_time`, `order_index`) VALUES
(1, '杠铃卧推', 4, '8-10', 60.0, '胸大肌', 90, 1),
(1, '哑铃飞鸟', 3, '12', 12.0, '胸大肌', 60, 2),
(1, '绳索下压', 3, '15', 20.0, '三头肌', 60, 3),
(2, '引体向上', 4, '8-10', 0.0, '背阔肌', 90, 1),
(2, '杠铃划船', 4, '10', 50.0, '背阔肌', 90, 2),
(2, '哑铃弯举', 3, '12', 10.0, '二头肌', 60, 3),
(3, '哑铃推举', 4, '10', 15.0, '三角肌', 90, 1),
(3, '侧平举', 3, '15', 8.0, '三角肌中束', 60, 2),
(4, '深蹲', 4, '8', 80.0, '股四头肌', 120, 1),
(4, '腿举', 3, '12', 100.0, '股四头肌', 90, 2);

-- -----------------------------------------------------
-- 训练记录 (20条)
-- -----------------------------------------------------
INSERT IGNORE INTO `training_records` (`user_id`, `exercise_name`, `sets`, `reps`, `weight`, `training_date`, `duration`, `notes`, `total_volume`, `training_stress`) VALUES
(1, '杠铃卧推', 4, 10, 60.0, DATE_SUB(CURDATE(), INTERVAL 1 DAY), 45, '状态不错', 2400.0, 7.5),
(1, '哑铃飞鸟', 3, 12, 12.0, DATE_SUB(CURDATE(), INTERVAL 1 DAY), 15, NULL, 432.0, 5.0),
(1, '引体向上', 4, 8, 0.0, DATE_SUB(CURDATE(), INTERVAL 3 DAY), 20, '体重训练', 0.0, 6.0),
(1, '杠铃划船', 4, 10, 50.0, DATE_SUB(CURDATE(), INTERVAL 3 DAY), 25, NULL, 2000.0, 7.0),
(1, '深蹲', 4, 8, 80.0, DATE_SUB(CURDATE(), INTERVAL 5 DAY), 30, '腿部训练日', 2560.0, 8.5),
(2, '哑铃推举', 3, 12, 10.0, DATE_SUB(CURDATE(), INTERVAL 2 DAY), 20, '肩部训练', 360.0, 5.5),
(2, '俯卧撑', 3, 15, 0.0, DATE_SUB(CURDATE(), INTERVAL 2 DAY), 10, '自重训练', 0.0, 4.0),
(2, '平板支撑', 3, 60, 0.0, DATE_SUB(CURDATE(), INTERVAL 4 DAY), 15, '核心训练', 0.0, 5.0),
(3, '史密斯深蹲', 3, 10, 30.0, DATE_SUB(CURDATE(), INTERVAL 1 DAY), 25, '新手友好', 900.0, 6.0),
(3, '坐姿划船', 3, 12, 25.0, DATE_SUB(CURDATE(), INTERVAL 1 DAY), 20, NULL, 900.0, 5.0),
(4, '硬拉', 5, 5, 120.0, DATE_SUB(CURDATE(), INTERVAL 2 DAY), 40, '力量训练', 3000.0, 9.0),
(4, '杠铃卧推', 5, 5, 100.0, DATE_SUB(CURDATE(), INTERVAL 4 DAY), 35, '大重量日', 2500.0, 8.5),
(4, '深蹲', 5, 5, 140.0, DATE_SUB(CURDATE(), INTERVAL 6 DAY), 45, 'PR尝试', 3500.0, 9.5),
(5, '哑铃弯举', 3, 12, 5.0, DATE_SUB(CURDATE(), INTERVAL 1 DAY), 15, '手臂训练', 180.0, 4.0),
(5, '腿举', 3, 15, 40.0, DATE_SUB(CURDATE(), INTERVAL 3 DAY), 20, NULL, 1800.0, 5.5),
(1, '哑铃推举', 4, 10, 15.0, DATE_SUB(CURDATE(), INTERVAL 7 DAY), 25, '肩部日', 600.0, 6.5),
(1, '绳索下压', 3, 15, 25.0, DATE_SUB(CURDATE(), INTERVAL 7 DAY), 15, '三头肌', 1125.0, 5.0),
(2, '跑步机', 1, 30, 0.0, DATE_SUB(CURDATE(), INTERVAL 6 DAY), 30, '有氧训练', 0.0, 6.0),
(4, '杠铃划船', 4, 8, 80.0, DATE_SUB(CURDATE(), INTERVAL 8 DAY), 30, '背部训练', 2560.0, 7.5),
(3, '哑铃侧平举', 3, 15, 4.0, DATE_SUB(CURDATE(), INTERVAL 5 DAY), 12, '肩部塑形', 180.0, 4.0);


-- -----------------------------------------------------
-- 力量训练数据 (20条)
-- -----------------------------------------------------
INSERT IGNORE INTO `strength_training_data` (`user_id`, `timestamp`, `exercise_name`, `weight`, `sets`, `reps`, `exercise_type`, `one_rep_max`, `training_volume`, `perceived_exertion`) VALUES
(1, DATE_SUB(NOW(), INTERVAL 1 DAY), '杠铃卧推', 60.0, 4, 10, 'COMPOUND', 80.0, 2400.0, 7),
(1, DATE_SUB(NOW(), INTERVAL 3 DAY), '杠铃划船', 50.0, 4, 10, 'COMPOUND', 66.7, 2000.0, 7),
(1, DATE_SUB(NOW(), INTERVAL 5 DAY), '深蹲', 80.0, 4, 8, 'COMPOUND', 98.5, 2560.0, 8),
(1, DATE_SUB(NOW(), INTERVAL 7 DAY), '硬拉', 90.0, 4, 6, 'COMPOUND', 104.0, 2160.0, 8),
(2, DATE_SUB(NOW(), INTERVAL 2 DAY), '哑铃推举', 10.0, 3, 12, 'COMPOUND', 14.3, 360.0, 6),
(2, DATE_SUB(NOW(), INTERVAL 4 DAY), '哑铃弯举', 8.0, 3, 12, 'ISOLATION', 11.4, 288.0, 5),
(4, DATE_SUB(NOW(), INTERVAL 2 DAY), '硬拉', 120.0, 5, 5, 'COMPOUND', 135.0, 3000.0, 9),
(4, DATE_SUB(NOW(), INTERVAL 4 DAY), '杠铃卧推', 100.0, 5, 5, 'COMPOUND', 112.5, 2500.0, 8),
(4, DATE_SUB(NOW(), INTERVAL 6 DAY), '深蹲', 140.0, 5, 5, 'COMPOUND', 157.5, 3500.0, 9),
(4, DATE_SUB(NOW(), INTERVAL 8 DAY), '杠铃划船', 80.0, 4, 8, 'COMPOUND', 98.5, 2560.0, 7),
(1, DATE_SUB(NOW(), INTERVAL 9 DAY), '杠铃卧推', 57.5, 4, 10, 'COMPOUND', 76.7, 2300.0, 7),
(1, DATE_SUB(NOW(), INTERVAL 11 DAY), '深蹲', 77.5, 4, 8, 'COMPOUND', 95.4, 2480.0, 8),
(3, DATE_SUB(NOW(), INTERVAL 1 DAY), '史密斯深蹲', 30.0, 3, 10, 'COMPOUND', 40.0, 900.0, 6),
(3, DATE_SUB(NOW(), INTERVAL 3 DAY), '坐姿划船', 25.0, 3, 12, 'COMPOUND', 35.7, 900.0, 5),
(5, DATE_SUB(NOW(), INTERVAL 1 DAY), '哑铃弯举', 5.0, 3, 12, 'ISOLATION', 7.1, 180.0, 4),
(5, DATE_SUB(NOW(), INTERVAL 3 DAY), '腿举', 40.0, 3, 15, 'COMPOUND', 60.0, 1800.0, 5),
(2, DATE_SUB(NOW(), INTERVAL 8 DAY), '杠铃卧推', 40.0, 3, 10, 'COMPOUND', 53.3, 1200.0, 6),
(1, DATE_SUB(NOW(), INTERVAL 13 DAY), '哑铃推举', 15.0, 4, 10, 'COMPOUND', 20.0, 600.0, 6),
(4, DATE_SUB(NOW(), INTERVAL 10 DAY), '引体向上', 10.0, 4, 8, 'COMPOUND', 12.3, 320.0, 7),
(3, DATE_SUB(NOW(), INTERVAL 5 DAY), '哑铃侧平举', 4.0, 3, 15, 'ISOLATION', 6.0, 180.0, 4);

-- -----------------------------------------------------
-- 有氧训练数据 (20条)
-- -----------------------------------------------------
INSERT IGNORE INTO `cardio_training_data` (`user_id`, `timestamp`, `exercise_type`, `duration`, `distance`, `average_heart_rate`, `max_heart_rate`, `calories_burned`, `average_speed`, `pace`, `perceived_exertion`) VALUES
(1, DATE_SUB(NOW(), INTERVAL 2 DAY), '跑步', 30, 5.0, 145, 165, 350, 10.0, 6.0, 7),
(1, DATE_SUB(NOW(), INTERVAL 4 DAY), '骑行', 45, 15.0, 130, 150, 400, 20.0, NULL, 6),
(1, DATE_SUB(NOW(), INTERVAL 6 DAY), '跑步', 25, 4.0, 150, 170, 300, 9.6, 6.25, 7),
(2, DATE_SUB(NOW(), INTERVAL 1 DAY), '跑步', 20, 3.0, 155, 175, 250, 9.0, 6.67, 6),
(2, DATE_SUB(NOW(), INTERVAL 3 DAY), '椭圆机', 30, NULL, 140, 160, 280, NULL, NULL, 5),
(2, DATE_SUB(NOW(), INTERVAL 5 DAY), '游泳', 40, 1.5, 135, 155, 350, 2.25, NULL, 6),
(3, DATE_SUB(NOW(), INTERVAL 2 DAY), '快走', 40, 4.0, 120, 140, 200, 6.0, 10.0, 4),
(3, DATE_SUB(NOW(), INTERVAL 4 DAY), '跑步', 15, 2.0, 160, 180, 180, 8.0, 7.5, 7),
(4, DATE_SUB(NOW(), INTERVAL 3 DAY), '划船机', 20, 4.0, 150, 170, 250, 12.0, NULL, 7),
(4, DATE_SUB(NOW(), INTERVAL 7 DAY), '跑步', 35, 6.5, 140, 160, 420, 11.1, 5.4, 6),
(5, DATE_SUB(NOW(), INTERVAL 1 DAY), '快走', 30, 3.0, 115, 135, 150, 6.0, 10.0, 3),
(5, DATE_SUB(NOW(), INTERVAL 4 DAY), '骑行', 25, 8.0, 125, 145, 180, 19.2, NULL, 4),
(1, DATE_SUB(NOW(), INTERVAL 8 DAY), 'HIIT', 20, NULL, 165, 185, 300, NULL, NULL, 9),
(1, DATE_SUB(NOW(), INTERVAL 10 DAY), '跑步', 40, 7.0, 142, 162, 450, 10.5, 5.7, 7),
(2, DATE_SUB(NOW(), INTERVAL 7 DAY), '跳绳', 15, NULL, 160, 180, 200, NULL, NULL, 8),
(4, DATE_SUB(NOW(), INTERVAL 11 DAY), '骑行', 60, 25.0, 135, 155, 550, 25.0, NULL, 6),
(3, DATE_SUB(NOW(), INTERVAL 6 DAY), '椭圆机', 25, NULL, 130, 150, 180, NULL, NULL, 5),
(5, DATE_SUB(NOW(), INTERVAL 6 DAY), '游泳', 30, 0.8, 130, 150, 200, 1.6, NULL, 5),
(1, DATE_SUB(NOW(), INTERVAL 12 DAY), '划船机', 25, 5.5, 148, 168, 320, 13.2, NULL, 7),
(2, DATE_SUB(NOW(), INTERVAL 9 DAY), '跑步', 25, 4.0, 152, 172, 280, 9.6, 6.25, 7);

-- -----------------------------------------------------
-- 恢复数据 (20条)
-- -----------------------------------------------------
INSERT IGNORE INTO `recovery_data` (`user_id`, `timestamp`, `recovery_score`, `sleep_hours`, `sleep_quality`, `heart_rate_variability`, `resting_heart_rate`, `muscle_soreness`, `stress_level`, `notes`) VALUES
(1, DATE_SUB(NOW(), INTERVAL 1 DAY), 85, 7.5, 8, 65, 58, 3.0, 4.0, '状态良好'),
(1, DATE_SUB(NOW(), INTERVAL 2 DAY), 72, 6.5, 6, 55, 62, 5.0, 6.0, '训练后肌肉酸痛'),
(1, DATE_SUB(NOW(), INTERVAL 3 DAY), 78, 7.0, 7, 60, 60, 4.0, 5.0, NULL),
(1, DATE_SUB(NOW(), INTERVAL 4 DAY), 90, 8.0, 9, 70, 55, 2.0, 3.0, '休息日恢复好'),
(2, DATE_SUB(NOW(), INTERVAL 1 DAY), 80, 7.0, 7, 58, 65, 3.5, 4.5, NULL),
(2, DATE_SUB(NOW(), INTERVAL 2 DAY), 75, 6.0, 6, 52, 68, 4.5, 5.5, '睡眠不足'),
(2, DATE_SUB(NOW(), INTERVAL 3 DAY), 82, 7.5, 8, 62, 63, 3.0, 4.0, NULL),
(3, DATE_SUB(NOW(), INTERVAL 1 DAY), 70, 6.5, 6, 48, 72, 5.0, 6.0, '新手适应期'),
(3, DATE_SUB(NOW(), INTERVAL 2 DAY), 68, 6.0, 5, 45, 75, 6.0, 7.0, '肌肉酸痛明显'),
(3, DATE_SUB(NOW(), INTERVAL 3 DAY), 75, 7.0, 7, 50, 70, 4.0, 5.0, NULL),
(4, DATE_SUB(NOW(), INTERVAL 1 DAY), 88, 8.0, 8, 72, 52, 4.0, 3.0, '恢复能力强'),
(4, DATE_SUB(NOW(), INTERVAL 2 DAY), 82, 7.5, 7, 68, 54, 5.0, 4.0, '大重量训练后'),
(4, DATE_SUB(NOW(), INTERVAL 3 DAY), 92, 8.5, 9, 75, 50, 2.0, 2.0, '状态极佳'),
(5, DATE_SUB(NOW(), INTERVAL 1 DAY), 65, 6.0, 5, 42, 78, 5.5, 7.0, '需要更多休息'),
(5, DATE_SUB(NOW(), INTERVAL 2 DAY), 72, 7.0, 6, 48, 74, 4.5, 6.0, NULL),
(1, DATE_SUB(NOW(), INTERVAL 5 DAY), 83, 7.5, 8, 63, 57, 3.5, 4.5, NULL),
(2, DATE_SUB(NOW(), INTERVAL 4 DAY), 78, 7.0, 7, 55, 66, 4.0, 5.0, NULL),
(4, DATE_SUB(NOW(), INTERVAL 4 DAY), 85, 7.5, 8, 70, 53, 3.5, 3.5, NULL),
(1, DATE_SUB(NOW(), INTERVAL 6 DAY), 76, 6.5, 6, 58, 61, 4.5, 5.5, '工作压力大'),
(3, DATE_SUB(NOW(), INTERVAL 4 DAY), 73, 7.0, 6, 47, 71, 4.5, 5.5, NULL);

-- -----------------------------------------------------
-- 恢复指标 (20条)
-- -----------------------------------------------------
INSERT IGNORE INTO `recovery_metrics` (`user_id`, `record_date`, `muscle_soreness`, `sleep_quality`, `resting_heart_rate`, `subjective_energy`) VALUES
(1, DATE_SUB(CURDATE(), INTERVAL 1 DAY), 2, 4, 58, 4),
(1, DATE_SUB(CURDATE(), INTERVAL 2 DAY), 3, 3, 62, 3),
(1, DATE_SUB(CURDATE(), INTERVAL 3 DAY), 3, 4, 60, 4),
(1, DATE_SUB(CURDATE(), INTERVAL 4 DAY), 1, 5, 55, 5),
(2, DATE_SUB(CURDATE(), INTERVAL 1 DAY), 2, 4, 65, 4),
(2, DATE_SUB(CURDATE(), INTERVAL 2 DAY), 3, 3, 68, 3),
(2, DATE_SUB(CURDATE(), INTERVAL 3 DAY), 2, 4, 63, 4),
(3, DATE_SUB(CURDATE(), INTERVAL 1 DAY), 4, 3, 72, 3),
(3, DATE_SUB(CURDATE(), INTERVAL 2 DAY), 4, 3, 75, 2),
(3, DATE_SUB(CURDATE(), INTERVAL 3 DAY), 3, 4, 70, 3),
(4, DATE_SUB(CURDATE(), INTERVAL 1 DAY), 3, 4, 52, 4),
(4, DATE_SUB(CURDATE(), INTERVAL 2 DAY), 4, 4, 54, 4),
(4, DATE_SUB(CURDATE(), INTERVAL 3 DAY), 1, 5, 50, 5),
(5, DATE_SUB(CURDATE(), INTERVAL 1 DAY), 4, 3, 78, 2),
(5, DATE_SUB(CURDATE(), INTERVAL 2 DAY), 3, 3, 74, 3),
(1, DATE_SUB(CURDATE(), INTERVAL 5 DAY), 2, 4, 57, 4),
(2, DATE_SUB(CURDATE(), INTERVAL 4 DAY), 3, 4, 66, 3),
(4, DATE_SUB(CURDATE(), INTERVAL 4 DAY), 2, 4, 53, 4),
(1, DATE_SUB(CURDATE(), INTERVAL 6 DAY), 3, 3, 61, 3),
(3, DATE_SUB(CURDATE(), INTERVAL 4 DAY), 3, 3, 71, 3);

-- -----------------------------------------------------
-- 营养记录 (20条)
-- -----------------------------------------------------
INSERT IGNORE INTO `nutrition_records` (`user_id`, `record_date`, `meal_type`, `food_name`, `calories`, `protein`, `carbs`, `fat`, `fiber`, `amount`) VALUES
(1, CURDATE(), 'BREAKFAST', '鸡蛋', 150, 12.0, 1.0, 10.0, 0.0, 100),
(1, CURDATE(), 'BREAKFAST', '全麦面包', 180, 8.0, 30.0, 3.0, 4.0, 80),
(1, CURDATE(), 'LUNCH', '鸡胸肉', 250, 45.0, 0.0, 6.0, 0.0, 200),
(1, CURDATE(), 'LUNCH', '糙米饭', 220, 5.0, 45.0, 2.0, 3.0, 150),
(1, CURDATE(), 'DINNER', '三文鱼', 300, 35.0, 0.0, 18.0, 0.0, 180),
(2, CURDATE(), 'BREAKFAST', '燕麦粥', 200, 7.0, 35.0, 4.0, 5.0, 200),
(2, CURDATE(), 'LUNCH', '牛肉', 280, 40.0, 0.0, 12.0, 0.0, 180),
(2, CURDATE(), 'SNACK', '蛋白粉', 120, 24.0, 3.0, 1.0, 0.0, 30),
(3, CURDATE(), 'BREAKFAST', '酸奶', 150, 10.0, 15.0, 5.0, 0.0, 200),
(3, CURDATE(), 'LUNCH', '沙拉', 180, 5.0, 20.0, 8.0, 6.0, 250),
(4, CURDATE(), 'BREAKFAST', '鸡蛋', 225, 18.0, 1.5, 15.0, 0.0, 150),
(4, CURDATE(), 'LUNCH', '鸡胸肉', 375, 67.5, 0.0, 9.0, 0.0, 300),
(4, CURDATE(), 'DINNER', '牛排', 450, 50.0, 0.0, 28.0, 0.0, 250),
(4, CURDATE(), 'SNACK', '坚果', 200, 6.0, 8.0, 18.0, 3.0, 40),
(5, CURDATE(), 'BREAKFAST', '水果', 100, 1.0, 25.0, 0.5, 3.0, 200),
(5, CURDATE(), 'LUNCH', '蔬菜汤', 120, 4.0, 18.0, 3.0, 5.0, 300),
(1, DATE_SUB(CURDATE(), INTERVAL 1 DAY), 'BREAKFAST', '蛋白煎饼', 280, 20.0, 30.0, 8.0, 2.0, 150),
(1, DATE_SUB(CURDATE(), INTERVAL 1 DAY), 'LUNCH', '金枪鱼', 200, 40.0, 0.0, 4.0, 0.0, 150),
(2, DATE_SUB(CURDATE(), INTERVAL 1 DAY), 'DINNER', '鸡腿', 320, 35.0, 0.0, 20.0, 0.0, 200),
(4, DATE_SUB(CURDATE(), INTERVAL 1 DAY), 'SNACK', '香蕉', 105, 1.3, 27.0, 0.4, 3.0, 120);

-- -----------------------------------------------------
-- 训练建议 (10条)
-- -----------------------------------------------------
INSERT IGNORE INTO `training_advices` (`user_id`, `advice_date`, `advice_type`, `content`, `confidence_score`) VALUES
(1, CURDATE(), 'RECOVERY', '根据您的恢复数据，建议今天进行轻度有氧训练，避免高强度力量训练。', 0.85),
(1, DATE_SUB(CURDATE(), INTERVAL 1 DAY), 'TRAINING', '您的卧推进步明显，建议尝试增加2.5kg重量。', 0.78),
(2, CURDATE(), 'NUTRITION', '蛋白质摄入略低，建议每餐增加一份蛋白质来源。', 0.82),
(2, DATE_SUB(CURDATE(), INTERVAL 2 DAY), 'RECOVERY', '睡眠质量有待提高，建议睡前避免使用电子设备。', 0.75),
(3, CURDATE(), 'TRAINING', '作为新手，建议保持当前训练强度，专注于动作标准。', 0.90),
(4, CURDATE(), 'TRAINING', '您的力量水平优秀，可以考虑尝试周期化训练。', 0.88),
(4, DATE_SUB(CURDATE(), INTERVAL 1 DAY), 'RECOVERY', '大重量训练后恢复良好，可以按计划进行下次训练。', 0.85),
(5, CURDATE(), 'NUTRITION', '建议增加蛋白质摄入以支持肌肉恢复。', 0.80),
(1, DATE_SUB(CURDATE(), INTERVAL 3 DAY), 'TRAINING', '腿部训练表现出色，建议下周尝试增加训练量。', 0.82),
(3, DATE_SUB(CURDATE(), INTERVAL 1 DAY), 'RECOVERY', '肌肉酸痛较明显，建议进行拉伸和泡沫轴放松。', 0.88);

-- -----------------------------------------------------
-- 用户成就 (15条)
-- -----------------------------------------------------
INSERT IGNORE INTO `user_achievements` (`user_id`, `name`, `description`, `icon`, `unlocked`, `unlock_time`) VALUES
(1, '健身新手', '完成第一次训练', '🏃', 1, DATE_SUB(NOW(), INTERVAL 30 DAY)),
(1, '坚持一周', '连续训练7天', '🔥', 1, DATE_SUB(NOW(), INTERVAL 23 DAY)),
(1, '力量提升', '卧推突破60kg', '💪', 1, DATE_SUB(NOW(), INTERVAL 10 DAY)),
(1, '百斤深蹲', '深蹲突破100kg', '🏋️', 0, NULL),
(2, '健身新手', '完成第一次训练', '🏃', 1, DATE_SUB(NOW(), INTERVAL 20 DAY)),
(2, '有氧达人', '累计有氧训练10小时', '🚴', 1, DATE_SUB(NOW(), INTERVAL 5 DAY)),
(3, '健身新手', '完成第一次训练', '🏃', 1, DATE_SUB(NOW(), INTERVAL 15 DAY)),
(3, '坚持一周', '连续训练7天', '🔥', 0, NULL),
(4, '健身新手', '完成第一次训练', '🏃', 1, DATE_SUB(NOW(), INTERVAL 60 DAY)),
(4, '坚持一周', '连续训练7天', '🔥', 1, DATE_SUB(NOW(), INTERVAL 53 DAY)),
(4, '力量提升', '卧推突破60kg', '💪', 1, DATE_SUB(NOW(), INTERVAL 45 DAY)),
(4, '百斤深蹲', '深蹲突破100kg', '🏋️', 1, DATE_SUB(NOW(), INTERVAL 30 DAY)),
(4, '硬拉王者', '硬拉突破120kg', '👑', 1, DATE_SUB(NOW(), INTERVAL 10 DAY)),
(5, '健身新手', '完成第一次训练', '🏃', 1, DATE_SUB(NOW(), INTERVAL 10 DAY)),
(1, '营养达人', '连续记录饮食7天', '🥗', 1, DATE_SUB(NOW(), INTERVAL 8 DAY));
