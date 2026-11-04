-- =====================================================================
-- V35 物业币充值资金归属台账 + 商家物业币应收独立结算（2026-10-29）
--
-- 背景：
--   物业币充值现金进入平台直连商户号（direct 通道，不参与微信分账），
--   但纯物业币订单在商家特约商户号里没有现金，商家应收只能由平台内部兑付；
--   原实现既没有按物业公司归属充值现金，也没有把商家「物业币应收」单独记账，
--   导致物业承担成本击穿、商家看不到应收、内部提现可能重复出款。
--
-- 内容：
--   1) property_coin_fund_account：按物业公司的物业币现金资金账户（余额/累计入/累计出）；
--   2) property_coin_fund_ledger ：资金台账（充值入账 / 商家物业币结算 / 平台转物业）；
--   3) merchant_withdrawals.source：区分 legacy（历史内部收益）与 coin_receivable（物业币应收）；
--   4) 回填历史已支付充值流水，建立各物业公司期初资金归属。
--
-- 执行方式（生产 Flyway 关闭，手工执行）：
--   mysql -h<host> -u<user> -p --default-character-set=utf8mb4 <db> \
--     < V35__property_coin_fund_and_merchant_coin_settlement.sql
--
-- 幂等：表/列/索引均先查 information_schema，可重复执行；回填按唯一键去重。
-- =====================================================================

DROP PROCEDURE IF EXISTS p_wuye_add_column;
DROP PROCEDURE IF EXISTS p_wuye_add_index;

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

DELIMITER ;

CREATE TABLE IF NOT EXISTS `property_coin_fund_account` (
  `property_company_id` varchar(32) NOT NULL COMMENT '物业公司ID',
  `balance` decimal(12,2) NOT NULL DEFAULT '0.00' COMMENT '当前平台代管物业币现金余额',
  `total_in` decimal(12,2) NOT NULL DEFAULT '0.00' COMMENT '累计充值入账',
  `total_out` decimal(12,2) NOT NULL DEFAULT '0.00' COMMENT '累计出账（商家结算/转物业）',
  `created_at` datetime(6) DEFAULT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  PRIMARY KEY (`property_company_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='物业币现金资金账户（按物业公司）';

CREATE TABLE IF NOT EXISTS `property_coin_fund_ledger` (
  `id` varchar(32) NOT NULL,
  `property_company_id` varchar(32) NOT NULL COMMENT '物业公司ID',
  `entry_type` varchar(32) NOT NULL COMMENT 'recharge_in/merchant_settlement/property_transfer/adjustment',
  `direction` varchar(10) NOT NULL COMMENT 'credit/debit',
  `amount` decimal(10,2) NOT NULL COMMENT '发生金额（正数）',
  `balance_after` decimal(12,2) NOT NULL DEFAULT '0.00' COMMENT '记账后余额（回填历史为0）',
  `biz_type` varchar(32) DEFAULT NULL COMMENT '业务类型：coin_recharge/merchant_withdrawal/property_transfer/manual',
  `biz_id` varchar(64) DEFAULT NULL COMMENT '业务单号（充值流水/提现单/转账单）',
  `resident_id` varchar(32) DEFAULT NULL COMMENT '充值住户',
  `merchant_id` varchar(32) DEFAULT NULL COMMENT '结算商家',
  `description` varchar(255) DEFAULT NULL,
  `operator_id` varchar(32) DEFAULT NULL,
  `transfer_proof` varchar(255) DEFAULT NULL COMMENT '平台转物业的转账凭证',
  `created_at` datetime(6) DEFAULT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_coin_fund_biz` (`biz_type`,`biz_id`,`entry_type`),
  KEY `idx_coin_fund_property_created` (`property_company_id`,`created_at`),
  KEY `idx_coin_fund_merchant` (`merchant_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='物业币现金资金台账';

CALL p_wuye_add_column('merchant_withdrawals', 'source',
    "`source` varchar(30) NOT NULL DEFAULT 'legacy' COMMENT '资金来源：legacy历史内部收益/coin_receivable物业币应收'");
CALL p_wuye_add_index('merchant_withdrawals', 'idx_merchant_withdrawals_source', '`merchant_id`, `source`, `audit_status`');

DROP PROCEDURE IF EXISTS p_wuye_add_column;
DROP PROCEDURE IF EXISTS p_wuye_add_index;

-- 为全部物业公司建账户（新物业公司由代码在首次充值时自动建账）。
INSERT INTO `property_coin_fund_account` (`property_company_id`, `created_at`, `updated_at`)
SELECT c.`id`, NOW(6), NOW(6)
FROM `property_companies` c
ON DUPLICATE KEY UPDATE `updated_at` = VALUES(`updated_at`);

-- 回填历史充值：已支付的物业币充值流水，按物业公司记一笔充值入账。
-- 历史 recharge 现金均进入平台直连商户号，平台对物业公司形成等额代管资金。
INSERT INTO `property_coin_fund_account` (`property_company_id`, `created_at`, `updated_at`)
SELECT DISTINCT p.`property_company_id`, NOW(6), NOW(6)
FROM `payment_records` p
WHERE p.`biz_type` = 'coin_recharge'
  AND p.`status` = 'paid'
  AND p.`property_company_id` IS NOT NULL
ON DUPLICATE KEY UPDATE `updated_at` = VALUES(`updated_at`);

INSERT INTO `property_coin_fund_ledger`
    (`id`, `property_company_id`, `entry_type`, `direction`, `amount`, `balance_after`,
     `biz_type`, `biz_id`, `resident_id`, `description`, `created_at`, `updated_at`)
SELECT CONCAT('pcf_', SUBSTRING(MD5(p.`id`), 1, 28)),
       p.`property_company_id`, 'recharge_in', 'credit', p.`amount`, 0.00,
       'coin_recharge', p.`id`, p.`resident_id`, '历史物业币充值入账（V35 回填）',
       COALESCE(p.`paid_at`, p.`created_at`), NOW(6)
FROM `payment_records` p
WHERE p.`biz_type` = 'coin_recharge'
  AND p.`status` = 'paid'
  AND p.`property_company_id` IS NOT NULL
  AND p.`amount` > 0
  AND NOT EXISTS (
      SELECT 1 FROM `property_coin_fund_ledger` l
      WHERE l.`biz_type` = 'coin_recharge'
        AND l.`biz_id` = p.`id`
        AND l.`entry_type` = 'recharge_in'
  );

UPDATE `property_coin_fund_account` a
LEFT JOIN (
    SELECT `property_company_id`,
           SUM(CASE WHEN `direction` = 'credit' THEN `amount` ELSE 0 END) AS total_in,
           SUM(CASE WHEN `direction` = 'debit' THEN `amount` ELSE 0 END) AS total_out
    FROM `property_coin_fund_ledger`
    GROUP BY `property_company_id`
) l ON l.`property_company_id` = a.`property_company_id`
SET a.`total_in` = COALESCE(l.`total_in`, 0),
    a.`total_out` = COALESCE(l.`total_out`, 0),
    a.`balance` = COALESCE(l.`total_in`, 0) - COALESCE(l.`total_out`, 0),
    a.`updated_at` = NOW(6);

-- 执行后核对
SELECT 'property_coin_fund_account' AS item, COUNT(*) AS ok
FROM information_schema.TABLES
WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'property_coin_fund_account'
UNION ALL
SELECT 'property_coin_fund_ledger', COUNT(*)
FROM information_schema.TABLES
WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'property_coin_fund_ledger'
UNION ALL
SELECT 'merchant_withdrawals.source', COUNT(*)
FROM information_schema.COLUMNS
WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'merchant_withdrawals' AND COLUMN_NAME = 'source';
