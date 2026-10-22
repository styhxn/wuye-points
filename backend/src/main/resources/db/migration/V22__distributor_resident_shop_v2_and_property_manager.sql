-- =====================================================================
-- V22 一级代理→业主小店链路改造（件数制）+ 物业公司经理
--
-- A. 一级代理→业主小店：
--   * merchants 增加 is_distributor（后台指定开通一级代理，废弃 official_certified 判定口径）；
--   * resident_merchant_listings 增加件数制列（took_quantity / sold_quantity /
--     sold_out_unlisted / quantity_remaining），废弃"名额制/卖1单即下架"；
--   * resident_merchant_applications 增加 shop_name / merchant_id：
--     审核通过时自动创建店铺主体（merchantSource=resident_shop），
--     订单归属店铺主体，配送费/履约/结算全部复用商家链路；
--   * resident_merchant_settlements 增加 quantity（按件结算）；
--   * 存量回填：已 active 的业主商户补建店铺主体 + 物业配置 + merchant_id/shop_name；
--     存量 listing 回填件数（took=1，有已结算记录则 sold=1）。
--
-- B. 物业公司经理（业主属性，不进 residents.role）：
--   * 新表 property_managers（id pm_ 前缀）；
--   * orders / distribution_records 增加经理快照列 manager_id / manager_rate
--     （与楼长快照 building_leader_id / building_leader_rate 同表同模式）；
--   * 经理从楼长分成中按比例抽成，收益写 wallet_flows（owner_type=property_manager）。
--
-- 本脚本为手工执行脚本（application-prod.yml 中 flyway.enabled=false）。
-- 执行方式：mysql -u<user> -p <db> < V22__distributor_resident_shop_v2_and_property_manager.sql
-- 幂等：表使用 CREATE TABLE IF NOT EXISTS；列使用存储过程做存在性判断，可重复执行。
-- =====================================================================

-- ---------------------------------------------------------------------
-- 通用工具：存在性判断加列
-- ---------------------------------------------------------------------
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
-- A1) merchants 一级代理标记
-- ---------------------------------------------------------------------
CALL p_wuye_add_column('merchants', 'is_distributor',
                       'is_distributor TINYINT(1) NOT NULL DEFAULT 0 COMMENT ''是否一级代理（后台开通，可维护货源商品）''');

-- ---------------------------------------------------------------------
-- A3) resident_merchant_listings 件数制
-- ---------------------------------------------------------------------
CALL p_wuye_add_column('resident_merchant_listings', 'took_quantity',
                       'took_quantity INT NOT NULL DEFAULT 1 COMMENT ''拿货件数''');
CALL p_wuye_add_column('resident_merchant_listings', 'sold_quantity',
                       'sold_quantity INT NOT NULL DEFAULT 0 COMMENT ''已卖出件数''');
CALL p_wuye_add_column('resident_merchant_listings', 'sold_out_unlisted',
                       'sold_out_unlisted TINYINT(1) NOT NULL DEFAULT 0 COMMENT ''是否因卖完自动下架''');
CALL p_wuye_add_column('resident_merchant_listings', 'quantity_remaining',
                       'quantity_remaining INT NOT NULL DEFAULT 1 COMMENT ''可售件数（未锁定未售出，下单锁定时扣减）''');

-- ---------------------------------------------------------------------
-- A5) resident_merchant_applications 店铺主体关联 + 店名
-- ---------------------------------------------------------------------
CALL p_wuye_add_column('resident_merchant_applications', 'shop_name',
                       'shop_name VARCHAR(100) NULL COMMENT ''店铺名称（默认 业主姓名的小店）''');
CALL p_wuye_add_column('resident_merchant_applications', 'merchant_id',
                       'merchant_id VARCHAR(32) NULL COMMENT ''店铺主体商家ID mch_（审核通过时自动创建）''');

-- ---------------------------------------------------------------------
-- A7) resident_merchant_settlements 结算件数
-- ---------------------------------------------------------------------
CALL p_wuye_add_column('resident_merchant_settlements', 'quantity',
                       'quantity INT NOT NULL DEFAULT 1 COMMENT ''本单结算件数''');

-- ---------------------------------------------------------------------
-- A10) 存量回填：店铺名默认值
-- ---------------------------------------------------------------------
UPDATE resident_merchant_applications a
    JOIN residents r ON r.id = a.resident_id
SET a.shop_name = CONCAT(COALESCE(NULLIF(r.name, ''), r.phone), '的小店')
WHERE (a.shop_name IS NULL OR a.shop_name = '');

UPDATE resident_merchant_applications
SET shop_name = '业主小店'
WHERE shop_name IS NULL OR shop_name = '';

-- ---------------------------------------------------------------------
-- A10) 存量回填：已 active 的业主商户自动补建店铺主体
--      商家ID确定性生成：CONCAT('mch_', LEFT(MD5(CONCAT('rma__', id)), 28))，
--      幂等（重复执行不会重复建店）。
-- ---------------------------------------------------------------------
-- 1. 平台商家库
INSERT INTO platform_merchants (id, name, contact_phone, status)
SELECT CONCAT('pmm_', LEFT(MD5(CONCAT('rma__', a.id)), 28)),
       COALESCE(NULLIF(a.shop_name, ''), CONCAT(COALESCE(NULLIF(r.name, ''), r.phone), '的小店')),
       r.phone,
       'active'
FROM resident_merchant_applications a
         JOIN residents r ON r.id = a.resident_id
WHERE a.status = 'active'
  AND a.merchant_id IS NULL
  AND NOT EXISTS (SELECT 1 FROM platform_merchants p
                  WHERE p.id = CONCAT('pmm_', LEFT(MD5(CONCAT('rma__', a.id)), 28)));

-- 2. 商家店铺主体（merchant_source=resident_shop，审核通过、启用中）
INSERT INTO merchants (id, platform_merchant_id, property_company_id, name, merchant_level,
                       level_weight, category, audit_status, rank_order, contact_phone,
                       resident_id, delivery_fee, delivery_scope, status, serve_all_communities,
                       merchant_source, is_distributor)
SELECT CONCAT('mch_', LEFT(MD5(CONCAT('rma__', a.id)), 28)),
       CONCAT('pmm_', LEFT(MD5(CONCAT('rma__', a.id)), 28)),
       a.property_company_id,
       COALESCE(NULLIF(a.shop_name, ''), CONCAT(COALESCE(NULLIF(r.name, ''), r.phone), '的小店')),
       'normal', 0, '其他', 'approved', 0, r.phone, a.resident_id,
       0.00, 'both', 'active', 1, 'resident_shop', 0
FROM resident_merchant_applications a
         JOIN residents r ON r.id = a.resident_id
WHERE a.status = 'active'
  AND a.merchant_id IS NULL
  AND NOT EXISTS (SELECT 1 FROM merchants m
                  WHERE m.id = CONCAT('mch_', LEFT(MD5(CONCAT('rma__', a.id)), 28)));

-- 3. 商家-物业配置（配送费默认0、无满免；佣金比例与积分兑换比取平台默认）
INSERT INTO merchant_property_configs (id, merchant_id, property_company_id, commission_rate,
                                       point_exchange_rate, coin_rebate_rate, coin_rebate_enabled, status)
SELECT CONCAT('mpc_', LEFT(MD5(CONCAT('rma_mpc__', a.id)), 28)),
       CONCAT('mch_', LEFT(MD5(CONCAT('rma__', a.id)), 28)),
       a.property_company_id,
       0.1000, 100.0000, 0.0000, 0, 'active'
FROM resident_merchant_applications a
WHERE a.status = 'active'
  AND a.merchant_id IS NULL
  AND NOT EXISTS (SELECT 1 FROM merchant_property_configs c
                  WHERE c.merchant_id = CONCAT('mch_', LEFT(MD5(CONCAT('rma__', a.id)), 28)));

-- 4. 回填 merchant_id
UPDATE resident_merchant_applications a
    JOIN merchants m ON m.id = CONCAT('mch_', LEFT(MD5(CONCAT('rma__', a.id)), 28))
SET a.merchant_id = m.id
WHERE a.merchant_id IS NULL
  AND a.status = 'active'
  AND m.merchant_source = 'resident_shop';

-- ---------------------------------------------------------------------
-- A10) 存量回填：listing 件数（旧逻辑为"每条 listing 1 个名额、卖 1 单即下架"）
-- ---------------------------------------------------------------------
UPDATE resident_merchant_listings
SET took_quantity = 1,
    sold_quantity = 0
WHERE took_quantity IS NULL OR took_quantity < 1;

UPDATE resident_merchant_listings l
    JOIN (SELECT listing_id, COUNT(1) AS cnt FROM resident_merchant_settlements
          WHERE status = 'settled' GROUP BY listing_id) s ON s.listing_id = l.id
SET l.sold_quantity = LEAST(l.took_quantity, s.cnt)
WHERE l.sold_quantity = 0;

UPDATE resident_merchant_listings
SET quantity_remaining = GREATEST(took_quantity - sold_quantity, 0)
WHERE quantity_remaining IS NULL OR quantity_remaining < 0;

-- ---------------------------------------------------------------------
-- B1) 物业公司经理表
--     经理为「住户 + 经理」复合身份：residents.role 保持 'resident' 不变，
--     经理身份由 property_managers 表的 active 记录判定。
--     同一物业公司同一时刻仅一名 active（应用层校验，参照 building_leaders 模式）。
-- ---------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS property_managers
(
    id                  VARCHAR(32)  NOT NULL COMMENT '主键 pm_',
    resident_id         VARCHAR(32)  NOT NULL COMMENT '经理住户ID res_',
    property_company_id VARCHAR(32)  NOT NULL COMMENT '物业公司ID pc_',
    commission_rate     DECIMAL(5, 4) NOT NULL DEFAULT 0.0000 COMMENT '从楼长分成中抽成的比例（0~1，仅平台/物业可设置）',
    description         VARCHAR(500)          COMMENT '备注/说明',
    status              VARCHAR(20)  NOT NULL DEFAULT 'active' COMMENT '状态 active启用/inactive停用',
    created_at          DATETIME     NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at          DATETIME     NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    KEY idx_pmr_resident (resident_id),
    KEY idx_pmr_property (property_company_id, status)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='物业公司经理（从楼长分成中按比例抽成）';

-- ---------------------------------------------------------------------
-- B4) 订单/分成记录增加经理快照列（与楼长快照同表同模式）
-- ---------------------------------------------------------------------
CALL p_wuye_add_column('orders', 'manager_id',
                       'manager_id VARCHAR(32) NULL COMMENT ''下单时绑定的物业经理ID（快照）pm_''');
CALL p_wuye_add_column('orders', 'manager_rate',
                       'manager_rate DECIMAL(5,4) NULL COMMENT ''下单时物业经理抽成比例快照（占楼长分成的比例）''');

CALL p_wuye_add_column('distribution_records', 'manager_id',
                       'manager_id VARCHAR(32) NULL COMMENT ''物业经理ID pm_''');
CALL p_wuye_add_column('distribution_records', 'manager_rate',
                       'manager_rate DECIMAL(5,4) NULL COMMENT ''物业经理抽成比例快照''');

-- ---------------------------------------------------------------------
-- B5) 提现类型注释补充 property_manager（列 varchar(30)，无需结构变更）
-- ---------------------------------------------------------------------
ALTER TABLE withdrawal_records
    MODIFY COLUMN withdrawal_type VARCHAR(30) NOT NULL
        COMMENT '提现类型: individual_leader/courier/coordinator/sector_leader/building_leader/resident_seller/property_manager';

DROP PROCEDURE IF EXISTS p_wuye_add_column;

-- =====================================================================
-- 执行完成。
-- 回滚（如需）：
--   ALTER TABLE orders DROP COLUMN manager_rate, DROP COLUMN manager_id;
--   ALTER TABLE distribution_records DROP COLUMN manager_rate, DROP COLUMN manager_id;
--   DROP TABLE IF EXISTS property_managers;
--   ALTER TABLE resident_merchant_settlements DROP COLUMN quantity;
--   ALTER TABLE resident_merchant_applications DROP COLUMN merchant_id, DROP COLUMN shop_name;
--   ALTER TABLE resident_merchant_listings DROP COLUMN quantity_remaining,
--         DROP COLUMN sold_out_unlisted, DROP COLUMN sold_quantity, DROP COLUMN took_quantity;
--   ALTER TABLE merchants DROP COLUMN is_distributor;
-- =====================================================================
