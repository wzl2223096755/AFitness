-- ============================================================================
-- 数据迁移脚本：从 fitness_data 迁移到专用表
-- 
-- 背景说明：
-- fitness_data 表包含冗余字段，与 strength_training_data 和 recovery_data 表存在重复。
-- 此脚本将数据迁移到专用表，以消除冗余并提高数据一致性。
--
-- 执行前请确保：
-- 1. 已备份数据库
-- 2. 在非生产环境测试过此脚本
-- 3. 应用程序已更新为使用新的服务接口
-- ============================================================================

-- 步骤1：将力量训练数据迁移到 strength_training_data 表
-- 仅迁移包含力量训练字段的记录
INSERT INTO strength_training_data (
    user_id,
    timestamp,
    exercise_name,
    weight,
    sets,
    reps,
    exercise_type,
    one_rep_max,
    training_volume,
    perceived_exertion,
    created_at
)
SELECT 
    user_id,
    timestamp,
    exercise_name,
    weight,
    sets,
    reps,
    exercise_type,
    one_rep_max,
    training_volume,
    perceived_exertion,
    created_at
FROM fitness_data
WHERE exercise_name IS NOT NULL 
  AND weight IS NOT NULL
  AND sets IS NOT NULL
  AND reps IS NOT NULL
  -- 避免重复迁移：检查是否已存在相同记录
  AND NOT EXISTS (
      SELECT 1 FROM strength_training_data std 
      WHERE std.user_id = fitness_data.user_id 
        AND std.timestamp = fitness_data.timestamp
        AND std.exercise_name = fitness_data.exercise_name
  );

-- 步骤2：将恢复状态数据迁移到 recovery_data 表
-- 注意：recovery_data 表有更多必填字段，需要提供默认值
INSERT INTO recovery_data (
    user_id,
    timestamp,
    recovery_score,
    sleep_hours,
    sleep_quality,
    heart_rate_variability,
    resting_heart_rate,
    stress_level,
    created_at
)
SELECT 
    user_id,
    timestamp,
    COALESCE(recovery_score, 50),  -- 默认恢复评分50
    COALESCE(sleep_hours, 7.0),    -- 默认睡眠7小时
    5,                              -- 默认睡眠质量5（中等）
    50,                             -- 默认HRV 50
    70,                             -- 默认静息心率70
    COALESCE(stress_level, 5.0),   -- 默认压力水平5
    created_at
FROM fitness_data
WHERE recovery_score IS NOT NULL
  -- 避免重复迁移
  AND NOT EXISTS (
      SELECT 1 FROM recovery_data rd 
      WHERE rd.user_id = fitness_data.user_id 
        AND rd.timestamp = fitness_data.timestamp
  );

-- 步骤3：验证迁移结果
-- 检查力量训练数据迁移数量
SELECT 'strength_training_data 迁移记录数' AS description, COUNT(*) AS count 
FROM strength_training_data;

-- 检查恢复数据迁移数量
SELECT 'recovery_data 迁移记录数' AS description, COUNT(*) AS count 
FROM recovery_data;

-- 检查原始 fitness_data 记录数
SELECT 'fitness_data 原始记录数' AS description, COUNT(*) AS count 
FROM fitness_data;

-- ============================================================================
-- 步骤4：（可选）删除 fitness_data 表
-- 警告：仅在确认迁移成功且应用程序已更新后执行
-- ============================================================================
-- DROP TABLE IF EXISTS fitness_data;

-- ============================================================================
-- 回滚脚本（如需回滚）
-- ============================================================================
-- DELETE FROM strength_training_data WHERE created_at >= '迁移开始时间';
-- DELETE FROM recovery_data WHERE created_at >= '迁移开始时间';
