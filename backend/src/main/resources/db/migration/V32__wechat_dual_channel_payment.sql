-- =====================================================================
-- V32 微信支付双通道：支付流水记录通道与收款商户号（2026-10-27）
--
-- 背景：平台现有两个独立微信商户主体，凭据不可混用：
--   * 服务商 1752012787（sp_mchid）：商城订单收款 + 官方服务商分账；
--   * 直连商户 1665249236：平台自营业务（物业费/充值/食堂/广告/服务预约等）。
-- 支付流水必须固化下单通道，后续查单/退款/关单/分账按 pay_mode 路由，
-- 避免运营调整配置后把老流水发到错误的商户主体。
--
-- 内容：
--   1) payment_records 增加 pay_mode（direct/partner/ecommerce）；
--   2) payment_records 增加 mch_id（下单时实际收款商户号快照）；
--   3) 增加 (pay_mode, status) 联合索引，供按通道对账与补偿扫描。
--
-- 执行方式（生产 Flyway 关闭，手工执行）：
--   mysql -h<host> -u<user> -p --default-character-set=utf8mb4 <db> \
--     < V32__wechat_dual_channel_payment.sql
--
-- 幂等：列/索引均先查 information_schema，可重复执行。
-- 历史数据：老流水的 pay_mode 保持 NULL，代码按 sub_mchid 推断（无 sub_mchid → direct）。
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

CALL p_wuye_add_column('payment_records', 'pay_mode',
    "`pay_mode` varchar(20) DEFAULT NULL COMMENT '支付通道：direct直连/partner服务商/ecommerce收付通'");
CALL p_wuye_add_column('payment_records', 'mch_id',
    "`mch_id` varchar(32) DEFAULT NULL COMMENT '实际收款微信商户号（下单快照）'");
CALL p_wuye_add_index('payment_records', 'idx_payment_records_mode_status', '`pay_mode`, `status`');

DROP PROCEDURE IF EXISTS p_wuye_add_column;
DROP PROCEDURE IF EXISTS p_wuye_add_index;

-- 执行后核对（应各返回 1）
SELECT 'payment_records.pay_mode' AS item, COUNT(*) AS ok
FROM information_schema.COLUMNS
WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'payment_records' AND COLUMN_NAME = 'pay_mode'
UNION ALL
SELECT 'payment_records.mch_id', COUNT(*)
FROM information_schema.COLUMNS
WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'payment_records' AND COLUMN_NAME = 'mch_id'
UNION ALL
SELECT 'payment_records.idx_payment_records_mode_status', COUNT(*)
FROM information_schema.STATISTICS
WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'payment_records' AND INDEX_NAME = 'idx_payment_records_mode_status';
