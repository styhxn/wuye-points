-- =============================================================================
-- V2：并发安全与幂等加固
--
-- 背景（对应可维护性评估 P0-7 / P0-9 / P0-10 / P0-13）：
--   1. 15 个定时任务无任何互斥，多实例部署会重复出账；
--   2. 提现申请/审核为无锁 check-then-act，并发审核可重复退币；
--   3. 库存扣减"先查后改"无锁，存在超卖；
--   4. payment_records.out_trade_no 无唯一约束，支付回调并发重推可能重复入账。
--
-- 幂等性说明：MySQL 的 DDL 会隐式提交，Flyway 无法整体回滚，
-- 因此所有结构性变更均先查 information_schema，仅在目标不存在时执行，
-- 保证脚本可重复执行（重跑不会因 "Duplicate column name" 中断）。
-- =============================================================================

-- -----------------------------------------------------------------------------
-- 1. ShedLock 表：分布式定时任务锁存储
-- -----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `shedlock` (
    `name`       varchar(64)  NOT NULL COMMENT '锁名称，与 @SchedulerLock(name) 一致',
    `lock_until` timestamp(3) NULL DEFAULT NULL COMMENT '锁到期时间',
    `locked_at`  timestamp(3) NULL DEFAULT NULL COMMENT '加锁时间',
    `locked_by`  varchar(255) DEFAULT NULL COMMENT '持锁实例标识',
    PRIMARY KEY (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='ShedLock 分布式任务锁';


-- -----------------------------------------------------------------------------
-- 2. payment_records：out_trade_no 唯一约束
--    先清理历史重复数据（同一商户订单号只应存在一条，保留 id 最小的一条），
--    再建立唯一索引，为支付回调幂等提供数据库级保证。
-- -----------------------------------------------------------------------------
DELETE p1
FROM `payment_records` p1
         INNER JOIN `payment_records` p2
                    ON p1.`out_trade_no` = p2.`out_trade_no`
                        AND p1.`id` > p2.`id`
WHERE p1.`out_trade_no` IS NOT NULL;

SET @idx_exists := (SELECT COUNT(*)
                    FROM information_schema.STATISTICS
                    WHERE TABLE_SCHEMA = DATABASE()
                      AND TABLE_NAME = 'payment_records'
                      AND INDEX_NAME = 'uk_payment_records_out_trade_no');
SET @ddl := IF(@idx_exists = 0,
               'CREATE UNIQUE INDEX `uk_payment_records_out_trade_no` ON `payment_records` (`out_trade_no`)',
               'SELECT 1');
PREPARE stmt FROM @ddl; EXECUTE stmt; DEALLOCATE PREPARE stmt;


-- -----------------------------------------------------------------------------
-- 3. point_records / property_coin_records：幂等键
--    定时任务（积分清零、物业币过期）生成的扣减流水原先无幂等保护，
--    重推或重跑会重复出账。新增幂等键并建唯一索引拦截重复写入。
--    允许为 NULL（历史数据），MySQL 唯一索引对多个 NULL 不冲突。
-- -----------------------------------------------------------------------------
SET @col_exists := (SELECT COUNT(*)
                    FROM information_schema.COLUMNS
                    WHERE TABLE_SCHEMA = DATABASE()
                      AND TABLE_NAME = 'point_records'
                      AND COLUMN_NAME = 'idempotency_key');
SET @ddl := IF(@col_exists = 0,
               'ALTER TABLE `point_records` ADD COLUMN `idempotency_key` varchar(64) NULL COMMENT ''幂等键，防重复出账流水'' AFTER `operator_id`',
               'SELECT 1');
PREPARE stmt FROM @ddl; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @idx_exists := (SELECT COUNT(*)
                    FROM information_schema.STATISTICS
                    WHERE TABLE_SCHEMA = DATABASE()
                      AND TABLE_NAME = 'point_records'
                      AND INDEX_NAME = 'uk_point_records_idempotency');
SET @ddl := IF(@idx_exists = 0,
               'CREATE UNIQUE INDEX `uk_point_records_idempotency` ON `point_records` (`idempotency_key`)',
               'SELECT 1');
PREPARE stmt FROM @ddl; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @col_exists := (SELECT COUNT(*)
                    FROM information_schema.COLUMNS
                    WHERE TABLE_SCHEMA = DATABASE()
                      AND TABLE_NAME = 'property_coin_records'
                      AND COLUMN_NAME = 'idempotency_key');
SET @ddl := IF(@col_exists = 0,
               'ALTER TABLE `property_coin_records` ADD COLUMN `idempotency_key` varchar(64) NULL COMMENT ''幂等键，防重复出账流水'' AFTER `operator_id`',
               'SELECT 1');
PREPARE stmt FROM @ddl; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @idx_exists := (SELECT COUNT(*)
                    FROM information_schema.STATISTICS
                    WHERE TABLE_SCHEMA = DATABASE()
                      AND TABLE_NAME = 'property_coin_records'
                      AND INDEX_NAME = 'uk_coin_records_idempotency');
SET @ddl := IF(@idx_exists = 0,
               'CREATE UNIQUE INDEX `uk_coin_records_idempotency` ON `property_coin_records` (`idempotency_key`)',
               'SELECT 1');
PREPARE stmt FROM @ddl; EXECUTE stmt; DEALLOCATE PREPARE stmt;


-- -----------------------------------------------------------------------------
-- 4. 乐观锁版本号
--    products：库存扣减需防超卖（原为无锁的 read-modify-write）
--    coin_withdrawals：提现审核需防并发重复退币
-- -----------------------------------------------------------------------------
SET @col_exists := (SELECT COUNT(*)
                    FROM information_schema.COLUMNS
                    WHERE TABLE_SCHEMA = DATABASE()
                      AND TABLE_NAME = 'products'
                      AND COLUMN_NAME = 'version');
SET @ddl := IF(@col_exists = 0,
               'ALTER TABLE `products` ADD COLUMN `version` bigint NOT NULL DEFAULT 0 COMMENT ''乐观锁版本号''',
               'SELECT 1');
PREPARE stmt FROM @ddl; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @col_exists := (SELECT COUNT(*)
                    FROM information_schema.COLUMNS
                    WHERE TABLE_SCHEMA = DATABASE()
                      AND TABLE_NAME = 'coin_withdrawals'
                      AND COLUMN_NAME = 'version');
SET @ddl := IF(@col_exists = 0,
               'ALTER TABLE `coin_withdrawals` ADD COLUMN `version` bigint NOT NULL DEFAULT 0 COMMENT ''乐观锁版本号''',
               'SELECT 1');
PREPARE stmt FROM @ddl; EXECUTE stmt; DEALLOCATE PREPARE stmt;
