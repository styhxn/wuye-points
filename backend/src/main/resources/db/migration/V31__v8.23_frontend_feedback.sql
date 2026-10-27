-- =====================================================================
-- v8.23 前端联调反馈修复 · 合并数据库脚本（2026-10-26）
--
-- 适用：当前服务器库 wuye_points（已核对 2026-10-25 拉取的库结构快照）。
--
-- 说明：
--   1) 本脚本是可独立执行的合并版：第 0 节包含 v8.22（V30）前置列与索引，
--      即使此前未执行 v8.22 脚本，也可直接用本脚本把库补齐；
--   2) 已执行过 v8.22 的库重复执行本脚本无副作用（列/索引均先查 information_schema）；
--   3) 生产环境 Flyway 关闭时手工执行；测试/开发环境 Flyway 开启时也会按 V31 自动执行。
--
-- 执行：
--   mysql -h<host> -P3306 -u<user> -p --default-character-set=utf8mb4 wuye_points \
--     < v8.23_前端反馈修复_20261026.sql
--
-- v8.23 代码侧修复（2.1–2.9）多数是接口字段/口径调整，不新增业务列；
-- 数据库侧只需补齐 v8.22 前置列，并增加 2 个查询索引。
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

-- =====================================================================
-- 0. v8.22（V30）前置：订单代理归属、积分转物业开关、货源供给范围、
--    活动组按季/按次、消息商品上下文
--    —— 已执行过 v8.22 脚本的库会自动跳过
-- =====================================================================

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

CALL p_wuye_add_column('property_companies', 'point_transfer_enabled',
    '`point_transfer_enabled` TINYINT(1) NOT NULL DEFAULT 0 COMMENT ''是否允许积分转物业抵扣物业费（默认关闭）''');
CALL p_wuye_add_column('property_companies', 'point_transfer_min_years',
    '`point_transfer_min_years` INT NOT NULL DEFAULT 5 COMMENT ''积分转物业所需最低入住年限''');

CALL p_wuye_add_column('distributor_products', 'supply_scope',
    '`supply_scope` VARCHAR(20) NOT NULL DEFAULT ''company'' COMMENT ''供给范围 company=本物业公司 platform=全平台''');

CALL p_wuye_add_column('activity_groups', 'quarterly_fee',
    '`quarterly_fee` DECIMAL(10,2) NULL COMMENT ''按季度收费金额''');
CALL p_wuye_add_column('activity_groups', 'per_time_fee',
    '`per_time_fee` DECIMAL(10,2) NULL COMMENT ''按次收费金额''');

CALL p_wuye_add_column('messages', 'related_product_id',
    '`related_product_id` VARCHAR(32) NULL COMMENT ''关联商品ID（商品详情联系店主）''');
CALL p_wuye_add_column('messages', 'related_listing_id',
    '`related_listing_id` VARCHAR(32) NULL COMMENT ''关联业主上架ID（业主小店商品联系店主）''');

-- 存量代销订单回填（仅填 NULL，不覆盖业务已写入的值）
UPDATE orders o
JOIN resident_merchant_listings l ON l.id = o.listing_id
JOIN distributor_products dp ON dp.id = l.distributor_product_id
SET o.source_type = 'distributor',
    o.distributor_id = dp.distributor_merchant_id,
    o.resident_merchant_id = o.merchant_id,
    o.settlement_owner = 'distributor'
WHERE o.listing_id IS NOT NULL
  AND o.source_type IS NULL;

-- =====================================================================
-- 1. v8.23 数据兜底（列已由上方补齐；这里只处理历史 NULL/空值）
-- =====================================================================

UPDATE distributor_products
SET supply_scope = 'company'
WHERE supply_scope IS NULL OR supply_scope = '';

UPDATE property_companies
SET point_transfer_enabled = 0
WHERE point_transfer_enabled IS NULL;

UPDATE property_companies
SET point_transfer_min_years = 5
WHERE point_transfer_min_years IS NULL OR point_transfer_min_years < 0;

-- 说明：历史家庭转账流水保留原值 source='transfer'（point_type='transfer'），
--       后端查询同时兼容 family_transfer，不做数据改写，避免影响历史对账。

-- =====================================================================
-- 2. v8.23 查询索引
--    - 代理购买记录：按代理商 + 状态 + 下单时间筛选/排序
--    - 广告列表：按商家 + 创建时间倒序
--    - 货源选品池：按状态 + 供给范围 + 代理商家过滤
-- =====================================================================

CALL p_wuye_add_index('orders', 'idx_orders_distributor_status_created',
    '`distributor_id`, `order_status`, `created_at`');
CALL p_wuye_add_index('merchant_ads', 'idx_merchant_ads_merchant_created',
    '`merchant_id`, `created_at`');
CALL p_wuye_add_index('distributor_products', 'idx_distributor_products_supply',
    '`status`, `supply_scope`, `distributor_merchant_id`');

DROP PROCEDURE IF EXISTS p_wuye_add_column;
DROP PROCEDURE IF EXISTS p_wuye_add_index;

-- =====================================================================
-- 3. 执行后自检（可选，手工执行时会输出结果）
-- =====================================================================

SELECT 'orders.source_type' AS check_item,
       COUNT(*) AS present
FROM information_schema.COLUMNS
WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'orders' AND COLUMN_NAME = 'source_type'
UNION ALL
SELECT 'property_companies.point_transfer_enabled', COUNT(*)
FROM information_schema.COLUMNS
WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'property_companies' AND COLUMN_NAME = 'point_transfer_enabled'
UNION ALL
SELECT 'distributor_products.supply_scope', COUNT(*)
FROM information_schema.COLUMNS
WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'distributor_products' AND COLUMN_NAME = 'supply_scope'
UNION ALL
SELECT 'orders.idx_orders_distributor_status_created', COUNT(*)
FROM information_schema.STATISTICS
WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'orders' AND INDEX_NAME = 'idx_orders_distributor_status_created'
UNION ALL
SELECT 'merchant_ads.idx_merchant_ads_merchant_created', COUNT(*)
FROM information_schema.STATISTICS
WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'merchant_ads' AND INDEX_NAME = 'idx_merchant_ads_merchant_created'
UNION ALL
SELECT 'distributor_products.idx_distributor_products_supply', COUNT(*)
FROM information_schema.STATISTICS
WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'distributor_products' AND INDEX_NAME = 'idx_distributor_products_supply';
