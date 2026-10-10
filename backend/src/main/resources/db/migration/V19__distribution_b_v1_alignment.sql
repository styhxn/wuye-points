-- =====================================================================
-- V19 分成口径澄清（distribution_b_v1）与配送员取数修正
--
-- 对应前端待办：后端待办_v8.9分成口径澄清与配送员取数修正.md
--
-- A1 配送员报酬：
--   * 原先 delivery_rules.courier_per_order / property_companies.delivery_courier_per_order
--     的「固定每单金额」不再参与分账；改为「配送结算基数 × (1 − platform_delivery_share_rate)」。
--   * 配送结算基数 = 实付配送费 + 配送补贴（满额减免额）。
--   * 恒等式：配送结算基数 = 公司配送收入 + 配送员收入。
--   * 旧列 delivery_rules.courier_per_order、property_companies.delivery_courier_per_order、
--     courier_share_rate 一律保留不删，仅作历史兼容，不再参与计算。
--   * deliveries.platform_earning 由服务层落库（列已存在），无需变更结构。
--
-- B1 平台商品分成：
--   * distribution_records.platform_share 落「平台盘 × platform_share_rate」。
--       platform_share = 抽佣基础额 − 积分成本 − 物业币成本，再按比例分配。
--       一级三档守恒：物业毛 + 管理盘 + 平台商品 = 抽佣基础额（管理盘用减法吸收分币误差）。
--   * 配送侧（公司配送收入 / 配送员收入）不冗余存入 distribution_records，
--     统一以 deliveries.platform_earning / courier_earning 为唯一事实源（见第 2 节）。
--
-- 订单侧新增配送减免快照（规范§7.6：承担方/减免额在下单时落快照，结算不得重算）。
--
-- 本脚本为手工执行脚本（application-prod.yml 中 flyway.enabled=false）。
-- 执行方式：mysql -u<user> -p <db> < V19__distribution_b_v1_alignment.sql
-- 幂等：列使用存储过程做存在性判断，可重复执行；数据回填均带 WHERE 空值条件。
-- =====================================================================

DROP PROCEDURE IF EXISTS p_wuye_add_column;
DELIMITER //
CREATE PROCEDURE p_wuye_add_column(IN p_table VARCHAR(64), IN p_column VARCHAR(64), IN p_ddl VARCHAR(500))
BEGIN
    IF NOT EXISTS (SELECT 1
                   FROM information_schema.COLUMNS
                   WHERE TABLE_SCHEMA = DATABASE()
                     AND TABLE_NAME = p_table
                     AND COLUMN_NAME = p_column) THEN
        SET @ddl = CONCAT('ALTER TABLE ', p_table, ' ADD COLUMN ', p_ddl);
        PREPARE stmt FROM @ddl;
        EXECUTE stmt;
        DEALLOCATE PREPARE stmt;
    END IF;
END //
DELIMITER ;

-- ---------------------------------------------------------------------
-- 1) orders 增加「配送满额减免」快照列
-- ---------------------------------------------------------------------
CALL p_wuye_add_column('orders', 'original_delivery_fee',
                       'original_delivery_fee DECIMAL(10,2) NULL COMMENT ''满额减免前基础配送费快照''');
CALL p_wuye_add_column('orders', 'delivery_subsidy_sponsor',
                       'delivery_subsidy_sponsor VARCHAR(20) NULL COMMENT ''配送费减免承担方快照 none/merchant/property/platform''');
CALL p_wuye_add_column('orders', 'delivery_subsidy_amount',
                       'delivery_subsidy_amount DECIMAL(10,2) NULL COMMENT ''配送补贴金额快照（=满额减免额）''');

DROP PROCEDURE IF EXISTS p_wuye_add_column;

-- ---------------------------------------------------------------------
-- 2) 配送分账链数据源说明
--    公司配送收入 / 配送员收入不冗余存入 distribution_records，
--    统一以 deliveries.platform_earning / deliveries.courier_earning 为唯一事实源
--    （商家自配单 carrier_type='merchant' 无配送员收入，冗余存储会造成口径失真）。
--    deliveries.platform_earning 列已存在（V16 引入），本版本起由服务层首次写入。
-- ---------------------------------------------------------------------

-- ---------------------------------------------------------------------
-- 3) 数据回填（仅补空值，可重复执行）
--    历史订单无减免快照，按「无减免」口径回填：原配送费 = 实付配送费、无补贴。
-- ---------------------------------------------------------------------
UPDATE orders
SET original_delivery_fee = COALESCE(delivery_fee, 0.00)
WHERE original_delivery_fee IS NULL;

UPDATE orders
SET delivery_subsidy_sponsor = 'none'
WHERE delivery_subsidy_sponsor IS NULL;

UPDATE orders
SET delivery_subsidy_amount = 0.00
WHERE delivery_subsidy_amount IS NULL;

-- 说明：distribution_records.platform_share 对历史记录不回溯重算
--       （历史平台比例不可复原），从 0.00 起算，新单按新口径落库。

-- =====================================================================
-- 执行完成。
-- 回滚（如需）：
--   ALTER TABLE orders DROP COLUMN delivery_subsidy_amount,
--         DROP COLUMN delivery_subsidy_sponsor, DROP COLUMN original_delivery_fee;
-- =====================================================================