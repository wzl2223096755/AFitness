-- 完整的用户表更新脚本
-- 添加缺失的字段到user_table表

-- 1. 添加gender字段
ALTER TABLE user_table ADD COLUMN IF NOT EXISTS gender VARCHAR(10);

-- 2. 添加height字段
ALTER TABLE user_table ADD COLUMN IF NOT EXISTS height INT;

-- 3. 添加experience_level字段
ALTER TABLE user_table ADD COLUMN IF NOT EXISTS experience_level VARCHAR(20);

-- 4. 确保updated_at字段存在
ALTER TABLE user_table ADD COLUMN IF NOT EXISTS updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP;

-- 5. 显示更新后的表结构
DESCRIBE user_table;

-- 6. 验证数据完整性
SELECT COUNT(*) as total_users FROM user_table;

-- 7. 显示示例数据
SELECT id, username, email, age, weight, gender, height, experience_level, role, created_at, updated_at 
FROM user_table 
LIMIT 5;