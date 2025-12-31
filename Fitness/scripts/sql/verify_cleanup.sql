-- ========================================
-- 数据库清理结果验证脚本
-- 功能：验证数据库清理后的状态
-- ========================================

-- 1. 检查用户表状态
SELECT '=== 用户表状态 ===' as section;
SELECT COUNT(*) as total_users FROM user_table;
SELECT COUNT(*) as admin_users FROM user_table WHERE role = 'ADMIN';
SELECT COUNT(*) as regular_users FROM user_table WHERE role = 'USER';

-- 显示所有用户信息
SELECT '所有用户信息:' as info;
SELECT id, username, email, role, created_at FROM user_table;

-- 2. 检查各业务表的数据量
SELECT '=== 业务表数据统计 ===' as section;
SELECT 'training_records' as table_name, COUNT(*) as record_count FROM training_records
UNION ALL
SELECT 'exercise_details' as table_name, COUNT(*) as record_count FROM exercise_details
UNION ALL
SELECT 'strength_training_data' as table_name, COUNT(*) as record_count FROM strength_training_data
UNION ALL
SELECT 'cardio_training_data' as table_name, COUNT(*) as record_count FROM cardio_training_data
UNION ALL
SELECT 'recovery_data' as table_name, COUNT(*) as record_count FROM recovery_data
UNION ALL
SELECT 'recovery_metrics' as table_name, COUNT(*) as record_count FROM recovery_metrics
UNION ALL
SELECT 'fitness_data' as table_name, COUNT(*) as record_count FROM fitness_data
UNION ALL
SELECT 'training_advices' as table_name, COUNT(*) as record_count FROM training_advices;

-- 3. 检查设备表（如果存在）
SELECT '=== 设备表状态 ===' as section;
SELECT COUNT(*) as device_count FROM device;

-- 4. 验证清理结果
SELECT '=== 清理结果总结 ===' as section;
SELECT 
    CASE 
        WHEN (SELECT COUNT(*) FROM user_table WHERE role = 'ADMIN') > 0 THEN '✅ 管理员账号已保留'
        ELSE '❌ 管理员账号丢失'
    END as admin_status,
    CASE 
        WHEN (SELECT COUNT(*) FROM user_table WHERE role = 'USER') = 0 THEN '✅ 普通用户已清理'
        ELSE '❌ 普通用户未清理'
    END as regular_users_status,
    CASE 
        WHEN (SELECT COUNT(*) FROM training_records) = 0 THEN '✅ 训练记录已清理'
        ELSE '❌ 训练记录未清理'
    END as training_records_status;

-- 5. 显示当前时间
SELECT '验证时间: ' || CURRENT_TIMESTAMP as current_time;