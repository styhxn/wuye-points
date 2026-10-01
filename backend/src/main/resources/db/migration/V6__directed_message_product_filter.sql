-- =============================================================================
-- V6: 定向推送增加产品维度筛选
-- 与 sql/manual_upgrade_gaps_20260902.sql 内容一致。幂等。
-- =============================================================================

SET @col_exists := (SELECT COUNT(*)
                    FROM information_schema.COLUMNS
                    WHERE TABLE_SCHEMA = DATABASE()
                      AND TABLE_NAME = 'directed_message_tasks'
                      AND COLUMN_NAME = 'product_ids');
SET @ddl := IF(@col_exists = 0,
               'ALTER TABLE `directed_message_tasks` ADD COLUMN `product_ids` json NULL COMMENT ''按产品购买筛选的ID数组''',
               'SELECT 1');
PREPARE stmt FROM @ddl; EXECUTE stmt; DEALLOCATE PREPARE stmt;
