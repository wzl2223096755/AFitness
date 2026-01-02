-- =====================================================
-- MySQL 中文乱码完整修复脚本
-- 使用方法: mysql -u root -p < fix_encoding_complete.sql
-- =====================================================

-- 设置当前会话字符集
SET NAMES utf8mb4;
SET CHARACTER SET utf8mb4;
SET character_set_connection=utf8mb4;

-- 创建或修改数据库
CREATE DATABASE IF NOT EXISTS fitness_db 
CHARACTER SET utf8mb4 
COLLATE utf8mb4_unicode_ci;

USE fitness_db;

-- 修改数据库默认字符集
ALTER DATABASE fitness_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- 获取所有表并修改字符集
-- 注意：如果表不存在会报错，可以忽略

-- 修改已存在的表
ALTER TABLE IF EXISTS training_record CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
ALTER TABLE IF EXISTS training_plan CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
ALTER TABLE IF EXISTS users CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
ALTER TABLE IF EXISTS nutrition_record CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
ALTER TABLE IF EXISTS user_nutrition_goal CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
ALTER TABLE IF EXISTS recovery_metric CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
ALTER TABLE IF EXISTS body_record CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
ALTER TABLE IF EXISTS audit_log CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
ALTER TABLE IF EXISTS user_table CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
ALTER TABLE IF EXISTS training_records CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
ALTER TABLE IF EXISTS training_plans CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
ALTER TABLE IF EXISTS nutrition_records CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- 验证数据库字符集
SELECT '=== 数据库字符集 ===' AS info;
SELECT DEFAULT_CHARACTER_SET_NAME, DEFAULT_COLLATION_NAME 
FROM information_schema.SCHEMATA 
WHERE SCHEMA_NAME = 'fitness_db';

-- 验证表字符集
SELECT '=== 表字符集 ===' AS info;
SELECT TABLE_NAME, TABLE_COLLATION 
FROM information_schema.TABLES 
WHERE TABLE_SCHEMA = 'fitness_db';

-- 验证连接字符集
SELECT '=== 连接字符集变量 ===' AS info;
SHOW VARIABLES LIKE 'character%';

-- 测试中文
SELECT '=== 中文测试 ===' AS info;
SELECT '测试中文：健身训练' AS test_result;

SELECT '修复完成！' AS status;
