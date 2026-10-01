-- =============================================================================
-- V7: 快递负责人分成比例
-- 与 sql/manual_upgrade_gaps_20260902.sql 内容一致。幂等。
-- =============================================================================

SET @col_exists := (SELECT COUNT(*)
                    FROM information_schema.COLUMNS
                    WHERE TABLE_SCHEMA = DATABASE()
                      AND TABLE_NAME = 'courier_managers'
                      AND COLUMN_NAME = 'share_rate');
SET @ddl := IF(@col_exists = 0,
               'ALTER TABLE `courier_managers` ADD COLUMN `share_rate` decimal(10,4) NULL COMMENT ''快递负责人分成比例(0~1)''',
               'SELECT 1');
PREPARE stmt FROM @ddl; EXECUTE stmt; DEALLOCATE PREPARE stmt;
