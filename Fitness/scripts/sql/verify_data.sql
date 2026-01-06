-- =====================================================
-- AFitness 数据验证脚本
-- 用于检查数据库数据完整性
-- =====================================================

SET NAMES utf8mb4;

-- =====================================================
-- 1. 基础统计
-- =====================================================

SELECT '========== 数据统计 ==========' AS section;

SELECT '用户统计' AS category, 
       COUNT(*) AS total,
       SUM(CASE WHEN role = 'ADMIN' THEN 1 ELSE 0 END) AS admins,
       SUM(CASE WHEN role = 'USER' THEN 1 ELSE 0 END) AS users
FROM user_table;

SELECT '训练数据统计' AS category,
       (SELECT COUNT(*) FROM training_records) AS training_records,
       (SELECT COUNT(*) FROM strength_training_data) AS strength_data,
       (SELECT COUNT(*) FROM cardio_training_data) AS cardio_data;

SELECT '恢复数据统计' AS category,
       (SELECT COUNT(*) FROM recovery_data) AS recovery_data,
       (SELECT COUNT(*) FROM recovery_metrics) AS recovery_metrics;

SELECT '其他数据统计' AS category,
       (SELECT COUNT(*) FROM nutrition_records) AS nutrition,
       (SELECT COUNT(*) FROM training_advices) AS advices,
       (SELECT COUNT(*) FROM user_achievements) AS achievements;

-- =====================================================
-- 2. 用户详情
-- =====================================================

SELECT '========== 用户详情 ==========' AS section;

SELECT id, username, email, role, experience_level, points, 
       DATE(created_at) AS created_date
FROM user_table
ORDER BY id;

-- =====================================================
-- 3. 训练计划概览
-- =====================================================

SELECT '========== 训练计划 ==========' AS section;

SELECT tp.id, tp.name, u.username AS owner, tp.status, tp.goal, tp.level,
       tp.days_per_week, tp.duration_weeks
FROM training_plans tp
JOIN user_table u ON tp.user_id = u.id
ORDER BY tp.id;

-- =====================================================
-- 4. 各用户训练记录数
-- =====================================================

SELECT '========== 用户训练统计 ==========' AS section;

SELECT u.username,
       COUNT(DISTINCT tr.id) AS training_records,
       COUNT(DISTINCT std.id) AS strength_sessions,
       COUNT(DISTINCT ctd.id) AS cardio_sessions,
       COALESCE(SUM(tr.total_volume), 0) AS total_volume
FROM user_table u
LEFT JOIN training_records tr ON u.id = tr.user_id
LEFT JOIN strength_training_data std ON u.id = std.user_id
LEFT JOIN cardio_training_data ctd ON u.id = ctd.user_id
GROUP BY u.id, u.username
ORDER BY u.id;

-- =====================================================
-- 5. 恢复状态概览
-- =====================================================

SELECT '========== 恢复状态 ==========' AS section;

SELECT u.username,
       COUNT(rd.id) AS recovery_records,
       ROUND(AVG(rd.recovery_score), 1) AS avg_recovery_score,
       ROUND(AVG(rd.sleep_hours), 1) AS avg_sleep_hours
FROM user_table u
LEFT JOIN recovery_data rd ON u.id = rd.user_id
GROUP BY u.id, u.username
ORDER BY u.id;

-- =====================================================
-- 6. 成就统计
-- =====================================================

SELECT '========== 成就统计 ==========' AS section;

SELECT u.username,
       COUNT(ua.id) AS total_achievements,
       SUM(CASE WHEN ua.unlocked = 1 THEN 1 ELSE 0 END) AS unlocked,
       SUM(CASE WHEN ua.unlocked = 0 THEN 1 ELSE 0 END) AS locked
FROM user_table u
LEFT JOIN user_achievements ua ON u.id = ua.user_id
GROUP BY u.id, u.username
ORDER BY u.id;

-- =====================================================
-- 7. 数据完整性检查
-- =====================================================

SELECT '========== 数据完整性检查 ==========' AS section;

-- 检查孤儿记录
SELECT '孤儿训练记录' AS check_type, COUNT(*) AS count
FROM training_records tr
WHERE NOT EXISTS (SELECT 1 FROM user_table u WHERE u.id = tr.user_id);

SELECT '孤儿恢复数据' AS check_type, COUNT(*) AS count
FROM recovery_data rd
WHERE NOT EXISTS (SELECT 1 FROM user_table u WHERE u.id = rd.user_id);

-- 检查用户设置完整性
SELECT '缺少设置的用户' AS check_type, COUNT(*) AS count
FROM user_table u
WHERE NOT EXISTS (SELECT 1 FROM user_settings us WHERE us.user_id = u.id);

SELECT '========== 验证完成 ==========' AS section;
