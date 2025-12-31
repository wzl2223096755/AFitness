-- ========================================
-- 数据库清理脚本
-- 功能：删除所有数据但保留管理员账号
-- 适用于：H2数据库
-- ========================================

-- 开始事务
START TRANSACTION;

-- 1. 备份管理员账号信息到临时表
CREATE TEMPORARY TABLE admin_backup AS
SELECT * FROM user_table WHERE role = 'ADMIN';

-- 2. 删除所有表的数据（按外键依赖顺序）

-- 删除训练建议表数据
DELETE FROM training_advices;

-- 删除健身数据表数据
DELETE FROM fitness_data;

-- 删除恢复指标表数据
DELETE FROM recovery_metrics;

-- 删除恢复数据表数据
DELETE FROM recovery_data;

-- 删除有氧训练数据表数据
DELETE FROM cardio_training_data;

-- 删除力量训练数据表数据
DELETE FROM strength_training_data;

-- 删除动作详情表数据
DELETE FROM exercise_details;

-- 删除训练记录表数据
DELETE FROM training_records;

-- 删除设备表数据（如果存在）
DELETE FROM device;

-- 3. 删除所有非管理员用户
DELETE FROM user_table WHERE role != 'ADMIN';

-- 4. 重置自增ID序列（H2语法）
ALTER TABLE training_advices ALTER COLUMN id RESTART WITH 1;
ALTER TABLE fitness_data ALTER COLUMN id RESTART WITH 1;
ALTER TABLE recovery_metrics ALTER COLUMN id RESTART WITH 1;
ALTER TABLE recovery_data ALTER COLUMN id RESTART WITH 1;
ALTER TABLE cardio_training_data ALTER COLUMN id RESTART WITH 1;
ALTER TABLE strength_training_data ALTER COLUMN id RESTART WITH 1;
ALTER TABLE exercise_details ALTER COLUMN id RESTART WITH 1;
ALTER TABLE training_records ALTER COLUMN id RESTART WITH 1;

-- 5. 验证管理员账号是否保留
SELECT '管理员账号保留验证:' as info;
SELECT COUNT(*) as admin_count FROM user_table WHERE role = 'ADMIN';
SELECT id, username, email, role, created_at FROM user_table WHERE role = 'ADMIN';

-- 6. 显示清理后的表统计信息
SELECT '清理后的表数据统计:' as info;
SELECT 'training_advices' as table_name, COUNT(*) as record_count FROM training_advices
UNION ALL
SELECT 'fitness_data' as table_name, COUNT(*) as record_count FROM fitness_data
UNION ALL
SELECT 'recovery_metrics' as table_name, COUNT(*) as record_count FROM recovery_metrics
UNION ALL
SELECT 'recovery_data' as table_name, COUNT(*) as record_count FROM recovery_data
UNION ALL
SELECT 'cardio_training_data' as table_name, COUNT(*) as record_count FROM cardio_training_data
UNION ALL
SELECT 'strength_training_data' as table_name, COUNT(*) as record_count FROM strength_training_data
UNION ALL
SELECT 'exercise_details' as table_name, COUNT(*) as record_count FROM exercise_details
UNION ALL
SELECT 'training_records' as table_name, COUNT(*) as record_count FROM training_records
UNION ALL
SELECT 'user_table' as table_name, COUNT(*) as record_count FROM user_table;

-- 提交事务
COMMIT;

-- 清理临时表
DROP TABLE admin_backup;

-- ========================================
-- 清理完成信息
-- ========================================
SELECT '数据库清理完成！' as status;
SELECT '所有业务数据已删除，管理员账号已保留。' as result;