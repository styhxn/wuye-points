-- =====================================================================
-- V26 上线修复第一批：退款状态 / 取消来源 / 家庭池流水快照
--
-- 背景：
--   1) B1/B2 需要订单独立的退款状态与取消来源，避免「已取消」吞掉退款信息；
--   2) A3 修复家庭池双记后，积分/物业币流水需要记录本次变动中家庭池的部分及变动后家庭余额，
--      退款/冲正才能按原路径退回，不再靠猜测。
--
-- 执行方式（生产 Flyway 关闭，手工执行）：
--   mysql -h<host> -u<user> -p --default-character-set=utf8mb4 <db> \
--     < V26__batch1_refund_family_snapshot.sql
--
-- 幂等：列/索引均先查 information_schema，可重复执行。
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

-- 1) 订单：退款状态 / 退款失败原因 / 取消来源
CALL p_wuye_add_column('orders', 'cancelled_by',
    '`cancelled_by` VARCHAR(20) NULL COMMENT ''取消来源 user用户/system系统超时/merchant商家拒单''');
CALL p_wuye_add_column('orders', 'refund_status',
    '`refund_status` VARCHAR(20) NULL COMMENT ''退款状态 null/refunding/refunded/failed''');
CALL p_wuye_add_column('orders', 'refund_fail_reason',
    '`refund_fail_reason` VARCHAR(255) NULL COMMENT ''退款失败原因''');

-- 2) 积分流水：家庭池变动部分 + 变动后家庭余额快照
CALL p_wuye_add_column('point_records', 'family_amount',
    '`family_amount` INT NULL COMMENT ''本次变动中家庭池部分：正数进入家庭池，负数从家庭池扣除，0仅个人池''');
CALL p_wuye_add_column('point_records', 'family_balance',
    '`family_balance` INT NULL COMMENT ''变动后的家庭积分池余额快照''');

-- 3) 物业币流水：家庭池变动部分 + 变动后家庭余额快照
CALL p_wuye_add_column('property_coin_records', 'family_amount',
    '`family_amount` DECIMAL(10,2) NULL COMMENT ''本次变动中家庭池部分：正数进入家庭池，负数从家庭池扣除，0仅个人池''');
CALL p_wuye_add_column('property_coin_records', 'family_balance',
    '`family_balance` DECIMAL(10,2) NULL COMMENT ''变动后的家庭物业币池余额快照''');

-- 4) 退款/冲正按订单溯源索引
CALL p_wuye_add_index('point_records', 'idx_point_records_source_lookup',
    '`source_id`, `source`, `point_type`');
CALL p_wuye_add_index('property_coin_records', 'idx_coin_records_source_lookup',
    '`source_id`, `source`, `coin_type`');

-- 5) 存量订单回填（仅填 NULL，不覆盖业务已写入的值）
UPDATE orders
SET cancelled_by = 'system'
WHERE order_status = 'cancelled'
  AND cancelled_by IS NULL
  AND cancel_reason = '支付超时自动关闭';

UPDATE orders
SET cancelled_by = 'merchant'
WHERE order_status = 'cancelled'
  AND cancelled_by IS NULL
  AND (cancel_reason LIKE '%商家核实拒绝%' OR verified_by IS NOT NULL);

UPDATE orders
SET cancelled_by = 'user'
WHERE order_status = 'cancelled'
  AND cancelled_by IS NULL;

UPDATE orders
SET refund_status = 'refunded'
WHERE order_status = 'refunded'
  AND refund_status IS NULL;

UPDATE orders
SET refund_status = 'refunding'
WHERE order_status = 'refunding'
  AND refund_status IS NULL;

DROP PROCEDURE IF EXISTS p_wuye_add_column;
DROP PROCEDURE IF EXISTS p_wuye_add_index;

-- =====================================================================
-- 历史双记余额只读核对（默认注释，不自动改数）
-- 用于发现旧版本「个人与家庭同时入账」留下的异常；冲正前请结合明细人工确认。
-- =====================================================================
-- 1) 家庭成员个人余额非 0（家庭建立/加入时个人余额应已并入家庭池）：
-- SELECT r.id, r.name, r.family_id, r.point_balance, r.coin_balance
-- FROM residents r
-- WHERE r.family_id IS NOT NULL
--   AND (COALESCE(r.point_balance, 0) <> 0 OR COALESCE(r.coin_balance, 0) <> 0)
-- ORDER BY r.family_id, r.id;
--
-- 2) 家庭池余额与流水汇总差异（正数/负数均需人工核对）：
-- SELECT f.id, f.name,
--        f.point_balance,
--        COALESCE((SELECT SUM(CASE WHEN p.point_type = 'earn' THEN p.amount
--                                  WHEN p.point_type = 'spend' THEN -ABS(p.amount)
--                                  ELSE 0 END)
--                  FROM point_records p WHERE p.family_id = f.id), 0) AS point_flow,
--        f.coin_balance,
--        COALESCE((SELECT SUM(CASE WHEN c.coin_type IN ('earn', 'recharge') THEN c.amount
--                                  WHEN c.coin_type = 'spend' THEN -ABS(c.amount)
--                                  ELSE 0 END)
--                  FROM property_coin_records c WHERE c.family_id = f.id), 0) AS coin_flow
-- FROM families f
-- HAVING f.point_balance <> point_flow OR f.coin_balance <> coin_flow;
