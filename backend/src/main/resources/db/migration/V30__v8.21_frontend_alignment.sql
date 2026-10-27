-- =====================================================================
-- V30 v8.21 前后端联调改造（2026-10-26）
--
-- 内容：
--   1) 订单增加代理货源归属字段：业主小店代销一级代理货源时，订单/结算面向一级代理商；
--   2) 物业公司增加“积分转物业费”开关与最低年限（默认关闭、5 年）；
--   3) 一级代理商货源增加供给范围（platform=全平台，company=本物业公司）；
--   4) 活动组增加按季度/按次计费字段；
--   5) 商品会话增加商品上下文（product_id/listing_id）。
--
-- 执行方式（生产 Flyway 关闭，手工执行）：
--   mysql -h<host> -u<user> -p --default-character-set=utf8mb4 <db> \
--     < V30__v8.21_frontend_alignment.sql
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

-- 1) 订单：代理货源归属
CALL p_wuye_add_column('orders', 'source_type',
    '`source_type` VARCHAR(20) NULL COMMENT ''订单来源 mall=商城/店铺直购 distributor=业主小店代销一级代理货源''');
CALL p_wuye_add_column('orders', 'distributor_id',
    '`distributor_id` VARCHAR(32) NULL COMMENT ''一级代理商商家ID（代销订单的订单/结算主体）''');
CALL p_wuye_add_column('orders', 'resident_merchant_id',
    '`resident_merchant_id` VARCHAR(32) NULL COMMENT ''业主小店店铺主体ID（代销场景仅展示与佣金归属）''');
CALL p_wuye_add_column('orders', 'settlement_owner',
    '`settlement_owner` VARCHAR(20) NULL COMMENT ''结算归属方 merchant/distributor''');
CALL p_wuye_add_index('orders', 'idx_orders_distributor_created',
    '`distributor_id`, `created_at`');

-- 2) 物业公司：积分转物业费开关与年限
CALL p_wuye_add_column('property_companies', 'point_transfer_enabled',
    '`point_transfer_enabled` TINYINT(1) NOT NULL DEFAULT 0 COMMENT ''是否允许积分转物业抵扣物业费（默认关闭）''');
CALL p_wuye_add_column('property_companies', 'point_transfer_min_years',
    '`point_transfer_min_years` INT NOT NULL DEFAULT 5 COMMENT ''积分转物业所需最低入住年限''');

-- 3) 一级货源供给范围
CALL p_wuye_add_column('distributor_products', 'supply_scope',
    '`supply_scope` VARCHAR(20) NOT NULL DEFAULT ''company'' COMMENT ''供给范围 company=本物业公司 platform=全平台''');

-- 4) 活动组：按季度/按次计费
CALL p_wuye_add_column('activity_groups', 'quarterly_fee',
    '`quarterly_fee` DECIMAL(10,2) NULL COMMENT ''按季度收费金额''');
CALL p_wuye_add_column('activity_groups', 'per_time_fee',
    '`per_time_fee` DECIMAL(10,2) NULL COMMENT ''按次收费金额''');

-- 5) 商品会话上下文（消息可携带商品/上架ID）
CALL p_wuye_add_column('messages', 'related_product_id',
    '`related_product_id` VARCHAR(32) NULL COMMENT ''关联商品ID（商品详情联系店主）''');
CALL p_wuye_add_column('messages', 'related_listing_id',
    '`related_listing_id` VARCHAR(32) NULL COMMENT ''关联业主上架ID（业主小店商品联系店主）''');

-- 6) 存量代销订单回填（仅填 NULL，不覆盖业务已写入的值）
UPDATE orders o
JOIN resident_merchant_listings l ON l.id = o.listing_id
JOIN distributor_products dp ON dp.id = l.distributor_product_id
SET o.source_type = 'distributor',
    o.distributor_id = dp.distributor_merchant_id,
    o.resident_merchant_id = o.merchant_id,
    o.settlement_owner = 'distributor'
WHERE o.listing_id IS NOT NULL
  AND o.source_type IS NULL;

DROP PROCEDURE IF EXISTS p_wuye_add_column;
DROP PROCEDURE IF EXISTS p_wuye_add_index;
