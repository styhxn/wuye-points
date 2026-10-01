-- =============================================================================
-- V8: 活动组注册审核
-- 与 sql/manual_upgrade_gaps_20260902.sql 内容一致。幂等。
-- =============================================================================

SET @col_exists := (SELECT COUNT(*)
                    FROM information_schema.COLUMNS
                    WHERE TABLE_SCHEMA = DATABASE()
                      AND TABLE_NAME = 'activity_groups'
                      AND COLUMN_NAME = 'review_status');
SET @ddl := IF(@col_exists = 0,
               'ALTER TABLE `activity_groups` ADD COLUMN `review_status` varchar(20) NULL COMMENT ''注册审核状态 pending/approved/rejected''',
               'SELECT 1');
PREPARE stmt FROM @ddl; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @col_exists := (SELECT COUNT(*)
                    FROM information_schema.COLUMNS
                    WHERE TABLE_SCHEMA = DATABASE()
                      AND TABLE_NAME = 'activity_groups'
                      AND COLUMN_NAME = 'review_remark');
SET @ddl := IF(@col_exists = 0,
               'ALTER TABLE `activity_groups` ADD COLUMN `review_remark` varchar(500) NULL COMMENT ''审核备注''',
               'SELECT 1');
PREPARE stmt FROM @ddl; EXECUTE stmt; DEALLOCATE PREPARE stmt;
