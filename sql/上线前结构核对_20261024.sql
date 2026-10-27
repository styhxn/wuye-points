-- =====================================================================
-- 上线前结构核对（只读）
-- 用途：导入基线库并执行 V26/V27 后，确认关键列/表是否存在。
-- present=1 表示存在/符合预期；present=0 表示缺失，需要先补脚本。
-- =====================================================================

SELECT 'orders.cancelled_by' AS item,
       COUNT(*) AS present
FROM information_schema.COLUMNS
WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'orders' AND COLUMN_NAME = 'cancelled_by'
UNION ALL
SELECT 'orders.refund_status', COUNT(*)
FROM information_schema.COLUMNS
WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'orders' AND COLUMN_NAME = 'refund_status'
UNION ALL
SELECT 'orders.refund_fail_reason', COUNT(*)
FROM information_schema.COLUMNS
WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'orders' AND COLUMN_NAME = 'refund_fail_reason'
UNION ALL
SELECT 'point_records.family_amount', COUNT(*)
FROM information_schema.COLUMNS
WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'point_records' AND COLUMN_NAME = 'family_amount'
UNION ALL
SELECT 'point_records.family_balance', COUNT(*)
FROM information_schema.COLUMNS
WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'point_records' AND COLUMN_NAME = 'family_balance'
UNION ALL
SELECT 'property_coin_records.family_amount', COUNT(*)
FROM information_schema.COLUMNS
WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'property_coin_records' AND COLUMN_NAME = 'family_amount'
UNION ALL
SELECT 'property_coin_records.family_balance', COUNT(*)
FROM information_schema.COLUMNS
WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'property_coin_records' AND COLUMN_NAME = 'family_balance'
UNION ALL
SELECT 'property_coin_records.expired_amount', COUNT(*)
FROM information_schema.COLUMNS
WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'property_coin_records' AND COLUMN_NAME = 'expired_amount'
UNION ALL
SELECT 'merchant_ads.status', COUNT(*)
FROM information_schema.COLUMNS
WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'merchant_ads' AND COLUMN_NAME = 'status'
UNION ALL
SELECT 'merchant_applyments table', COUNT(*)
FROM information_schema.TABLES
WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'merchant_applyments'
UNION ALL
SELECT 'wechat_split_record.receiver_account nullable', COUNT(*)
FROM information_schema.COLUMNS
WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'wechat_split_record'
  AND COLUMN_NAME = 'receiver_account' AND IS_NULLABLE = 'YES'
UNION ALL
SELECT 'wechat_split_record.amount nullable', COUNT(*)
FROM information_schema.COLUMNS
WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'wechat_split_record'
  AND COLUMN_NAME = 'amount' AND IS_NULLABLE = 'YES'
UNION ALL
SELECT 'residents.token_version', COUNT(*)
FROM information_schema.COLUMNS
WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'residents' AND COLUMN_NAME = 'token_version'
UNION ALL
SELECT 'idempotency_records table', COUNT(*)
FROM information_schema.TABLES
WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'idempotency_records'
UNION ALL
SELECT 'idempotency_records.uk_idem_user_key', COUNT(*)
FROM information_schema.STATISTICS
WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'idempotency_records'
  AND INDEX_NAME = 'uk_idem_user_key';

-- 期望：以上 present 全部为 1。
