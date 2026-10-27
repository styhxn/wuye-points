-- =====================================================================
-- V27 上线修复第二批：物业币过期去重 + 结构漂移兜底
--
-- 内容：
--   1) property_coin_records.expired_amount：按流水记录已过期金额，防止定时任务重复扣减；
--   2) merchant_ads.status：兜底执行 V20 的广告状态列（幂等）；
--   3) wechat_split_record.receiver_account/amount：历史遗留 NOT NULL 列改为可空，
--      避免分账开关打开后本地流水 INSERT 失败（实体未映射这两列）。
--
-- 执行顺序：V26 之后执行。
-- 幂等：列/索引均先查 information_schema，可重复执行。
-- =====================================================================

DROP PROCEDURE IF EXISTS p_wuye_add_column;
DROP PROCEDURE IF EXISTS p_wuye_add_index;
DROP PROCEDURE IF EXISTS p_wuye_make_nullable;

DELIMITER //

CREATE PROCEDURE p_wuye_add_column(IN p_table VARCHAR(64), IN p_column VARCHAR(64), IN p_ddl VARCHAR(500))
BEGIN
    IF NOT EXISTS (SELECT 1
                   FROM information_schema.COLUMNS
                   WHERE TABLE_SCHEMA = DATABASE()
                     AND TABLE_NAME = p_table
                     AND COLUMN_NAME = p_column) THEN
        SET @ddl = CONCAT('ALTER TABLE `', p_table, '` ADD COLUMN ', p_ddl);
        PREPARE stmt FROM @ddl;
        EXECUTE stmt;
        DEALLOCATE PREPARE stmt;
    END IF;
END //

CREATE PROCEDURE p_wuye_add_index(IN p_table VARCHAR(64), IN p_index VARCHAR(64), IN p_columns VARCHAR(255))
BEGIN
    IF NOT EXISTS (SELECT 1
                   FROM information_schema.STATISTICS
                   WHERE TABLE_SCHEMA = DATABASE()
                     AND TABLE_NAME = p_table
                     AND INDEX_NAME = p_index) THEN
        SET @ddl = CONCAT('ALTER TABLE `', p_table, '` ADD INDEX `', p_index, '` (', p_columns, ')');
        PREPARE stmt FROM @ddl;
        EXECUTE stmt;
        DEALLOCATE PREPARE stmt;
    END IF;
END //

CREATE PROCEDURE p_wuye_make_nullable(IN p_table VARCHAR(64), IN p_column VARCHAR(64), IN p_definition VARCHAR(500))
BEGIN
    IF EXISTS (SELECT 1
               FROM information_schema.COLUMNS
               WHERE TABLE_SCHEMA = DATABASE()
                 AND TABLE_NAME = p_table
                 AND COLUMN_NAME = p_column
                 AND IS_NULLABLE = 'NO') THEN
        SET @ddl = CONCAT('ALTER TABLE `', p_table, '` MODIFY COLUMN ', p_definition);
        PREPARE stmt FROM @ddl;
        EXECUTE stmt;
        DEALLOCATE PREPARE stmt;
    END IF;
END //

DELIMITER ;

-- 1) 物业币流水：已过期金额（默认 0，存量记录视为未过期）
CALL p_wuye_add_column('property_coin_records', 'expired_amount',
    '`expired_amount` DECIMAL(10,2) NOT NULL DEFAULT 0 COMMENT ''该笔 earn/recharge 已过期清零金额''');

-- 2) 过期任务索引
CALL p_wuye_add_index('property_coin_records', 'idx_coin_records_expiry_personal',
    '`resident_id`, `coin_type`, `created_at`');
CALL p_wuye_add_index('property_coin_records', 'idx_coin_records_expiry_family',
    '`family_id`, `coin_type`, `created_at`');

-- 3) 兜底 V20：merchant_ads.status（若已执行则跳过）
CALL p_wuye_add_column('merchant_ads', 'status',
    '`status` VARCHAR(20) NULL COMMENT ''状态 pending_payment待支付/active投放中/cancelled已取消''');
UPDATE merchant_ads SET status = 'active' WHERE status IS NULL;

-- 4) 分账表历史 NOT NULL 漂移：实体未映射这两列，改为可空避免 INSERT 失败
CALL p_wuye_make_nullable('wechat_split_record', 'receiver_account',
    '`receiver_account` VARCHAR(64) NULL COMMENT ''接收方账户（历史列，实体未映射）''');
CALL p_wuye_make_nullable('wechat_split_record', 'amount',
    '`amount` DECIMAL(10,2) NULL COMMENT ''分账金额（历史列，实体未映射）''');

DROP PROCEDURE IF EXISTS p_wuye_add_column;
DROP PROCEDURE IF EXISTS p_wuye_add_index;
DROP PROCEDURE IF EXISTS p_wuye_make_nullable;
