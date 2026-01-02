-- 训练记录数据 (UTF-8)
SET NAMES utf8mb4;

INSERT INTO training_records (user_id, exercise_name, sets, reps, weight, training_date, duration, notes, total_volume, training_stress, deleted) VALUES
(1, '杠铃卧推', 4, 10, 60.0, DATE_SUB(CURDATE(), INTERVAL 1 DAY), 45, '状态不错', 2400.0, 7.5, 0),
(1, '哑铃飞鸟', 3, 12, 12.0, DATE_SUB(CURDATE(), INTERVAL 1 DAY), 15, NULL, 432.0, 5.0, 0),
(1, '引体向上', 4, 8, 0.0, DATE_SUB(CURDATE(), INTERVAL 3 DAY), 20, '体重训练', 0.0, 6.0, 0),
(1, '杠铃划船', 4, 10, 50.0, DATE_SUB(CURDATE(), INTERVAL 3 DAY), 25, NULL, 2000.0, 7.0, 0),
(1, '深蹲', 4, 8, 80.0, DATE_SUB(CURDATE(), INTERVAL 5 DAY), 30, '腿部训练日', 2560.0, 8.5, 0),
(2, '哑铃推举', 3, 12, 10.0, DATE_SUB(CURDATE(), INTERVAL 2 DAY), 20, '肩部训练', 360.0, 5.5, 0),
(2, '俯卧撑', 3, 15, 0.0, DATE_SUB(CURDATE(), INTERVAL 2 DAY), 10, '自重训练', 0.0, 4.0, 0),
(2, '平板支撑', 3, 60, 0.0, DATE_SUB(CURDATE(), INTERVAL 4 DAY), 15, '核心训练', 0.0, 5.0, 0),
(3, '史密斯深蹲', 3, 10, 30.0, DATE_SUB(CURDATE(), INTERVAL 1 DAY), 25, '新手友好', 900.0, 6.0, 0),
(3, '坐姿划船', 3, 12, 25.0, DATE_SUB(CURDATE(), INTERVAL 1 DAY), 20, NULL, 900.0, 5.0, 0),
(4, '硬拉', 5, 5, 120.0, DATE_SUB(CURDATE(), INTERVAL 2 DAY), 40, '力量训练', 3000.0, 9.0, 0),
(4, '杠铃卧推', 5, 5, 100.0, DATE_SUB(CURDATE(), INTERVAL 4 DAY), 35, '大重量日', 2500.0, 8.5, 0),
(4, '深蹲', 5, 5, 140.0, DATE_SUB(CURDATE(), INTERVAL 6 DAY), 45, 'PR尝试', 3500.0, 9.5, 0),
(5, '哑铃弯举', 3, 12, 5.0, DATE_SUB(CURDATE(), INTERVAL 1 DAY), 15, '手臂训练', 180.0, 4.0, 0),
(5, '腿举', 3, 15, 40.0, DATE_SUB(CURDATE(), INTERVAL 3 DAY), 20, NULL, 1800.0, 5.5, 0);
