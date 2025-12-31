-- ========================================
-- AFitness 演示数据初始化脚本
-- ========================================

-- 1. 基础数据已在 schema 脚本中通过 INSERT INTO user_table 插入
-- admin (id: 1), testuser (id: 2), demo (id: 3)

-- 2. 插入一些训练记录 (为 testuser, id: 2)
INSERT INTO training_records (user_id, exercise_name, sets, reps, weight, training_date, duration, notes, total_volume, training_stress) VALUES
(2, '杠铃卧推', 4, 8, 80.0, '2025-12-20', 60, '感觉状态不错', 2560.0, 75.0),
(2, '深蹲', 4, 10, 100.0, '2025-12-22', 75, '腿部训练日', 4000.0, 85.0),
(2, '硬拉', 3, 5, 120.0, '2025-12-24', 50, '拉力日', 1800.0, 90.0),
(2, '引体向上', 4, 12, 0.0, '2025-12-26', 45, '自重训练', 0.0, 65.0);

-- 3. 插入力量训练详细数据
INSERT INTO strength_training_data (user_id, timestamp, exercise_name, weight, sets, reps, exercise_type, one_rep_max, training_volume, perceived_exertion) VALUES
(2, '2025-12-20 10:00:00', '杠铃卧推', 80.0, 4, 8, 'Chest', 100.0, 2560.0, 8),
(2, '2025-12-22 10:00:00', '深蹲', 100.0, 4, 10, 'Legs', 133.0, 4000.0, 9),
(2, '2025-12-24 10:00:00', '硬拉', 120.0, 3, 5, 'Back', 140.0, 1800.0, 9);

-- 4. 插入恢复数据
INSERT INTO recovery_data (user_id, timestamp, recovery_score, sleep_hours, sleep_quality, heart_rate_variability, resting_heart_rate, muscle_soreness, stress_level) VALUES
(2, '2025-12-21 07:00:00', 85, 8.0, 8, 65, 58, 3.0, 2.0),
(2, '2025-12-23 07:00:00', 70, 6.5, 6, 55, 62, 7.0, 4.0),
(2, '2025-12-25 07:00:00', 90, 8.5, 9, 72, 55, 2.0, 1.0);

-- 5. 插入恢复指标 (日记形式)
INSERT INTO recovery_metrics (user_id, record_date, muscle_soreness, sleep_quality, resting_heart_rate, subjective_energy) VALUES
(2, '2025-12-26', 2, 4, 56, 4),
(2, '2025-12-27', 1, 5, 54, 5);

-- 6. 插入一个健身计划
INSERT INTO training_plans (id, user_id, name, description, start_date, end_date, status, is_weekly) VALUES
(1, 2, '增肌进阶计划', '针对胸背腿的大重量分化训练', '2025-12-01', '2026-02-01', 'ACTIVE', FALSE);

-- 7. 插入计划每日安排
INSERT INTO training_plan_days (id, plan_id, day_of_week, day_name, notes) VALUES
(1, 1, 0, '周一', '胸部和三头肌'),
(2, 1, 1, '周二', '背部和二头肌'),
(3, 1, 2, '周三', '休息日'),
(4, 1, 3, '周四', '腿部训练'),
(5, 1, 4, '周五', '肩部和核心'),
(6, 1, 5, '周六', '有氧/补缺'),
(7, 1, 6, '周日', '休息日');

-- 8. 插入计划动作详情
INSERT INTO training_plan_exercises (day_id, name, sets, reps, duration_minutes, intensity, target_muscles, rest_time, notes, order_index, completed) VALUES
(1, '杠铃卧推', 4, 8, 0, 8, '胸肌', 90, '控制离心', 1, TRUE),
(1, '上斜哑铃卧推', 3, 10, 0, 7, '上胸', 60, '注意挤压', 2, TRUE),
(1, '绳索下压', 3, 12, 0, 6, '三头肌', 45, '长头训练', 3, FALSE),
(2, '引体向上', 4, 8, 0, 8, '背阔肌', 120, '全幅度', 1, FALSE),
(2, '杠铃划船', 4, 10, 0, 8, '中背部', 90, '保持躯干稳定', 2, FALSE),
(4, '深蹲', 4, 8, 0, 9, '股四头肌', 150, '蹲深一点', 1, FALSE),
(4, '腿举', 3, 12, 0, 8, '腿部', 90, '脚位偏上', 2, FALSE);

-- 9. 插入健身数据 (综合状态)
INSERT INTO fitness_data (user_id, timestamp, exercise_name, weight, sets, reps, exercise_type, recovery_score, recovery_status, sleep_hours, stress_level) VALUES
(2, '2025-12-27 20:00:00', '深蹲', 110.0, 3, 8, 'Legs', 88, 'Excellent', 8, 2);

-- 10. 插入训练建议
INSERT INTO training_advices (user_id, advice_date, advice_type, content, confidence_score) VALUES
(2, '2025-12-28', 'TRAINING', '根据你最近的恢复数据，建议今天可以进行高强度腿部训练。', 0.95),
(2, '2025-12-28', 'RECOVERY', '你的睡眠质量有所下降，建议今晚提前1小时休息。', 0.88);
