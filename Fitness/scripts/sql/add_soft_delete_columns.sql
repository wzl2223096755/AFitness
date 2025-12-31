-- ============================================================================
-- 软删除字段迁移脚本
-- 
-- 为支持软删除功能，需要在相关表中添加以下字段：
-- - deleted: 软删除标记（false=未删除，true=已删除）
-- - deleted_at: 删除时间
-- - deleted_by: 执行删除操作的用户ID
-- - updated_at: 更新时间
--
-- 执行前请确保：
-- 1. 已备份数据库
-- 2. 在非生产环境测试过此脚本
-- ============================================================================

-- 为 training_records 表添加软删除字段
ALTER TABLE training_records ADD COLUMN IF NOT EXISTS deleted BOOLEAN DEFAULT FALSE NOT NULL;
ALTER TABLE training_records ADD COLUMN IF NOT EXISTS deleted_at TIMESTAMP NULL;
ALTER TABLE training_records ADD COLUMN IF NOT EXISTS deleted_by BIGINT NULL;
ALTER TABLE training_records ADD COLUMN IF NOT EXISTS updated_at TIMESTAMP NULL;

-- 为 nutrition_records 表添加软删除字段
ALTER TABLE nutrition_records ADD COLUMN IF NOT EXISTS deleted BOOLEAN DEFAULT FALSE NOT NULL;
ALTER TABLE nutrition_records ADD COLUMN IF NOT EXISTS deleted_at TIMESTAMP NULL;
ALTER TABLE nutrition_records ADD COLUMN IF NOT EXISTS deleted_by BIGINT NULL;
ALTER TABLE nutrition_records ADD COLUMN IF NOT EXISTS updated_at TIMESTAMP NULL;

-- 创建索引以优化软删除查询性能
CREATE INDEX IF NOT EXISTS idx_training_records_deleted ON training_records(deleted);
CREATE INDEX IF NOT EXISTS idx_training_records_deleted_at ON training_records(deleted_at);
CREATE INDEX IF NOT EXISTS idx_nutrition_records_deleted ON nutrition_records(deleted);
CREATE INDEX IF NOT EXISTS idx_nutrition_records_deleted_at ON nutrition_records(deleted_at);

-- 验证迁移结果
SELECT 'training_records 表结构' AS description;
-- DESCRIBE training_records; -- MySQL
-- \d training_records; -- PostgreSQL

SELECT 'nutrition_records 表结构' AS description;
-- DESCRIBE nutrition_records; -- MySQL
-- \d nutrition_records; -- PostgreSQL
