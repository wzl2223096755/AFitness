-- =====================================================
-- MySQL 编码诊断脚本
-- 用于检查数据库、表、连接的字符集设置
-- =====================================================

SET NAMES utf8mb4;

-- 1. 检查 MySQL 服务器字符集配置
SELECT '=== MySQL 服务器字符集配置 ===' AS section;
SHOW VARIABLES LIKE 'character_set%';
SHOW VARIABLES LIKE 'collation%';

-- 2. 检查数据库字符集
SELECT '=== 数据库字符集 ===' AS section;
SELECT 
    SCHEMA_NAME AS '数据库名',
    DEFAULT_CHARACTER_SET_NAME AS '字符集',
    DEFAULT_COLLATION_NAME AS '排序规则'
FROM information_schema.SCHEMATA 
WHERE SCHEMA_NAME = 'fitness_db';

-- 3. 检查所有表的字符集
SELECT '=== 表字符集 ===' AS section;
SELECT 
    TABLE_NAME AS '表名',
    TABLE_COLLATION AS '排序规则'
FROM information_schema.TABLES 
WHERE TABLE_SCHEMA = 'fitness_db'
ORDER BY TABLE_NAME;

-- 4. 检查包含中文的列的字符集
SELECT '=== 列字符集详情 ===' AS section;
SELECT 
    TABLE_NAME AS '表名',
    COLUMN_NAME AS '列名',
    CHARACTER_SET_NAME AS '字符集',
    COLLATION_NAME AS '排序规则',
    DATA_TYPE AS '数据类型'
FROM information_schema.COLUMNS 
WHERE TABLE_SCHEMA = 'fitness_db' 
AND CHARACTER_SET_NAME IS NOT NULL
ORDER BY TABLE_NAME, COLUMN_NAME;

-- 5. 检查当前连接的字符集
SELECT '=== 当前连接字符集 ===' AS section;
SELECT 
    @@character_set_client AS '客户端字符集',
    @@character_set_connection AS '连接字符集',
    @@character_set_results AS '结果字符集',
    @@character_set_database AS '数据库字符集',
    @@character_set_server AS '服务器字符集';

-- 6. 测试中文插入和查询
SELECT '=== 中文测试 ===' AS section;
SELECT '测试中文：健身训练记录' AS test_chinese;

-- 7. 检查是否有乱码数据（检测非UTF8字符）
SELECT '=== 检查可能的乱码数据 ===' AS section;

-- 检查 training_record 表
SELECT 'training_record.exercise_name 可能乱码的记录:' AS check_table;
SELECT id, exercise_name, HEX(exercise_name) AS hex_value
FROM training_record 
WHERE exercise_name IS NOT NULL 
AND exercise_name != ''
AND exercise_name NOT REGEXP '^[a-zA-Z0-9 _\-\u4e00-\u9fa5]+$'
LIMIT 5;

-- 检查 training_plan 表
SELECT 'training_plan.name 可能乱码的记录:' AS check_table;
SELECT id, name, HEX(name) AS hex_value
FROM training_plan 
WHERE name IS NOT NULL 
AND name != ''
LIMIT 5;

SELECT '=== 诊断完成 ===' AS section;
