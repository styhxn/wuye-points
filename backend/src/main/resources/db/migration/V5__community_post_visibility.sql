-- =============================================================================
-- V5: 社区帖子可见范围（仅一家住户可见）
-- 与 sql/manual_upgrade_gaps_20260902.sql 内容一致。幂等。
-- =============================================================================

SET @col_exists := (SELECT COUNT(*)
                    FROM information_schema.COLUMNS
                    WHERE TABLE_SCHEMA = DATABASE()
                      AND TABLE_NAME = 'community_posts'
                      AND COLUMN_NAME = 'visibility_type');
SET @ddl := IF(@col_exists = 0,
               'ALTER TABLE `community_posts` ADD COLUMN `visibility_type` varchar(20) NULL COMMENT ''可见范围 community/family''',
               'SELECT 1');
PREPARE stmt FROM @ddl; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @col_exists := (SELECT COUNT(*)
                    FROM information_schema.COLUMNS
                    WHERE TABLE_SCHEMA = DATABASE()
                      AND TABLE_NAME = 'community_posts'
                      AND COLUMN_NAME = 'visible_resident_id');
SET @ddl := IF(@col_exists = 0,
               'ALTER TABLE `community_posts` ADD COLUMN `visible_resident_id` varchar(32) NULL COMMENT ''family 可见时指定的住户ID''',
               'SELECT 1');
PREPARE stmt FROM @ddl; EXECUTE stmt; DEALLOCATE PREPARE stmt;
