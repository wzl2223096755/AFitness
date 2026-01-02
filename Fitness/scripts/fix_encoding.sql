-- =====================================================
-- 修复 MySQL 中文乱码问题 - 完整版
-- 运行前请先备份数据库！
-- 使用方法: mysql -u root -p < fix_encoding.sql
-- =====================================================

-- 设置客户端连接字符集
SET NAMES utf8mb4;
SET CHARACTER SET utf8mb4;

-- 选择数据库
USE fitness_db;

-- 1. 修改数据库字符集
ALTER DATABASE fitness_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- 2. 修改所有表的字符集
ALTER TABLE training_record CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
ALTER TABLE training_plan CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
ALTER TABLE users CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
ALTER TABLE nutrition_record CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
ALTER TABLE user_nutrition_goal CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
ALTER TABLE recovery_metric CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
ALTER TABLE body_record CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
ALTER TABLE audit_log CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- 3. 修复 training_record 表
UPDATE training_record SET exercise_name = CONVERT(CAST(CONVERT(exercise_name USING latin1) AS BINARY) USING utf8mb4) WHERE exercise_name IS NOT NULL AND exercise_name != '';
UPDATE training_record SET notes = CONVERT(CAST(CONVERT(notes USING latin1) AS BINARY) USING utf8mb4) WHERE notes IS NOT NULL AND notes != '';
UPDATE training_record SET exercise_type = CONVERT(CAST(CONVERT(exercise_type USING latin1) AS BINARY) USING utf8mb4) WHERE exercise_type IS NOT NULL AND exercise_type != '';

-- 4. 修复 training_plan 表
UPDATE training_plan SET name = CONVERT(CAST(CONVERT(name USING latin1) AS BINARY) USING utf8mb4) WHERE name IS NOT NULL AND name != '';
UPDATE training_plan SET description = CONVERT(CAST(CONVERT(description USING latin1) AS BINARY) USING utf8mb4) WHERE description IS NOT NULL AND description != '';
UPDATE training_plan SET goal = CONVERT(CAST(CONVERT(goal USING latin1) AS BINARY) USING utf8mb4) WHERE goal IS NOT NULL AND goal != '';

-- 5. 修复 users 表
UPDATE users SET username = CONVERT(CAST(CONVERT(username USING latin1) AS BINARY) USING utf8mb4) WHERE username IS NOT NULL AND username != '';

-- 6. 修复 nutrition_record 表
UPDATE nutrition_record SET food_name = CONVERT(CAST(CONVERT(food_name USING latin1) AS BINARY) USING utf8mb4) WHERE food_name IS NOT NULL AND food_name != '';
UPDATE nutrition_record SET meal_type = CONVERT(CAST(CONVERT(meal_type USING latin1) AS BINARY) USING utf8mb4) WHERE meal_type IS NOT NULL AND meal_type != '';
UPDATE nutrition_record SET notes = CONVERT(CAST(CONVERT(notes USING latin1) AS BINARY) USING utf8mb4) WHERE notes IS NOT NULL AND notes != '';

-- 7. 修复 audit_log 表
UPDATE audit_log SET action = CONVERT(CAST(CONVERT(action USING latin1) AS BINARY) USING utf8mb4) WHERE action IS NOT NULL AND action != '';
UPDATE audit_log SET details = CONVERT(CAST(CONVERT(details USING latin1) AS BINARY) USING utf8mb4) WHERE details IS NOT NULL AND details != '';

-- 8. 验证结果
SELECT '=== training_record ===' AS info;
SELECT id, exercise_name FROM training_record LIMIT 5;

SELECT '=== training_plan ===' AS info;
SELECT id, name FROM training_plan LIMIT 5;

SELECT '=== nutrition_record ===' AS info;
SELECT id, food_name FROM nutrition_record LIMIT 5;


-- =====================================================
-- 9. 检查数据库和表的字符集设置
-- =====================================================
SELECT '=== 数据库字符集 ===' AS info;
SELECT DEFAULT_CHARACTER_SET_NAME, DEFAULT_COLLATION_NAME 
FROM information_schema.SCHEMATA 
WHERE SCHEMA_NAME = 'fitness_db';

SELECT '=== 表字符集 ===' AS info;
SELECT TABLE_NAME, TABLE_COLLATION 
FROM information_schema.TABLES 
WHERE TABLE_SCHEMA = 'fitness_db';

SELECT '=== 连接字符集 ===' AS info;
SHOW VARIABLES LIKE 'character%';
SHOW VARIABLES LIKE 'collation%';

-- =====================================================
-- 10. 如果上述转换失败，尝试直接更新（谨慎使用）
-- 仅在数据显示为问号或方块时使用
-- =====================================================
-- 如果数据是双重编码（UTF-8被当作Latin1存储），使用以下方法：
-- UPDATE training_record SET exercise_name = CONVERT(BINARY CONVERT(exercise_name USING latin1) USING utf8mb4) WHERE exercise_name REGEXP '[^\x00-\x7F]';

-- =====================================================
-- 完成提示
-- =====================================================
SELECT '=== 编码修复完成 ===' AS info;
SELECT '请检查上面的验证结果，确认中文显示正常' AS message;
