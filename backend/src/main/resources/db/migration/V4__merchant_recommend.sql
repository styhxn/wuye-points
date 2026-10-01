-- =============================================================================
-- V4: 商家官方推荐字段
-- 与 sql/manual_upgrade_gaps_20260902.sql 内容一致（手工执行版）。
-- 幂等：已存在列则跳过。
-- =============================================================================

SET @col_exists := (SELECT COUNT(*)
                    FROM information_schema.COLUMNS
                    WHERE TABLE_SCHEMA = DATABASE()
                      AND TABLE_NAME = 'merchants'
                      AND COLUMN_NAME = 'is_recommended');
SET @ddl := IF(@col_exists = 0,
               'ALTER TABLE `merchants` ADD COLUMN `is_recommended` tinyint(1) NULL COMMENT ''是否官方推荐''',
               'SELECT 1');
PREPARE stmt FROM @ddl; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @col_exists := (SELECT COUNT(*)
                    FROM information_schema.COLUMNS
                    WHERE TABLE_SCHEMA = DATABASE()
                      AND TABLE_NAME = 'merchants'
                      AND COLUMN_NAME = 'recommended_sort');
SET @ddl := IF(@col_exists = 0,
               'ALTER TABLE `merchants` ADD COLUMN `recommended_sort` int NULL COMMENT ''推荐排序，越大越靠前''',
               'SELECT 1');
PREPARE stmt FROM @ddl; EXECUTE stmt; DEALLOCATE PREPARE stmt;
