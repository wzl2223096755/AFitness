-- 修复用户表结构，添加缺失的字段
-- 执行前请备份数据库

-- 添加 age 字段
ALTER TABLE user_table ADD COLUMN age INT DEFAULT 25;

-- 添加 weight 字段  
ALTER TABLE user_table ADD COLUMN weight DOUBLE DEFAULT 70.0;

-- 验证表结构
DESCRIBE user_table;