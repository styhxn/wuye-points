-- =====================================================================
-- v8.20 平台交易明细：查询索引（可选，建议生产执行）
--
-- 功能本身不新增表/列，不执行本脚本也能运行；但交易明细会在 30 天窗口内
-- 对多张账本按 created_at / property_company_id 过滤，数据量上来后索引能明显
-- 降低 UNION 扫描成本。
--
-- 特性：
--   * 全部使用 online DDL（ADD INDEX），不锁表数据；
--   * 通过 information_schema 判断索引是否已存在，可重复执行；
--   * 不删除、不改名任何现有索引。
--
-- 执行方式：mysql -u<user> -p <数据库名> < 交易明细查询索引_v8.20_20261024.sql
-- =====================================================================

DROP PROCEDURE IF EXISTS add_tx_index_if_missing;

DELIMITER $$
CREATE PROCEDURE add_tx_index_if_missing(
    IN p_table VARCHAR(64),
    IN p_index VARCHAR(64),
    IN p_columns VARCHAR(255)
)
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM information_schema.statistics
        WHERE table_schema = DATABASE()
          AND table_name = p_table
          AND index_name = p_index
    ) THEN
        SET @ddl = CONCAT('ALTER TABLE `', p_table, '` ADD INDEX `', p_index, '` (', p_columns, ')');
        PREPARE stmt FROM @ddl;
        EXECUTE stmt;
        DEALLOCATE PREPARE stmt;
    END IF;
END$$
DELIMITER ;

-- 支付：支付列表按创建时间；退款列表按更新时间
CALL add_tx_index_if_missing('payment_records', 'idx_tx_pc_created',
    '`property_company_id`, `created_at`');
CALL add_tx_index_if_missing('payment_records', 'idx_tx_updated',
    '`updated_at`');

-- 钱包流水
CALL add_tx_index_if_missing('wallet_flows', 'idx_tx_created',
    '`created_at`');

-- 订单分账 / 微信分账
CALL add_tx_index_if_missing('distribution_records', 'idx_tx_pc_created',
    '`property_company_id`, `created_at`');
CALL add_tx_index_if_missing('wechat_split_record', 'idx_tx_created',
    '`created_at`');

-- 提现（商家 / 物业币 / 角色）
CALL add_tx_index_if_missing('merchant_withdrawals', 'idx_tx_pc_created',
    '`property_company_id`, `created_at`');
CALL add_tx_index_if_missing('coin_withdrawals', 'idx_tx_pc_created',
    '`property_company_id`, `created_at`');
CALL add_tx_index_if_missing('withdrawal_records', 'idx_tx_pc_created',
    '`property_company_id`, `created_at`');

-- 内部结算转账 / 业主小店结算
CALL add_tx_index_if_missing('merchant_transfers', 'idx_tx_created',
    '`created_at`');
CALL add_tx_index_if_missing('resident_merchant_settlements', 'idx_tx_created',
    '`created_at`');

-- 积分 / 物业币 / 食堂定向余额
CALL add_tx_index_if_missing('point_records', 'idx_tx_pc_created',
    '`property_company_id`, `created_at`');
CALL add_tx_index_if_missing('property_coin_records', 'idx_tx_pc_created',
    '`property_company_id`, `created_at`');
CALL add_tx_index_if_missing('canteen_directed_flows', 'idx_tx_created',
    '`created_at`');

DROP PROCEDURE IF EXISTS add_tx_index_if_missing;

-- 执行后核对：应全部存在
SELECT table_name, index_name
FROM information_schema.statistics
WHERE table_schema = DATABASE()
  AND index_name IN ('idx_tx_pc_created', 'idx_tx_created', 'idx_tx_updated')
GROUP BY table_name, index_name
ORDER BY table_name, index_name;
