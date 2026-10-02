-- =====================================================================
-- V17 优惠券功能改造（独立功能模块）
--
-- 背景：原「优惠券」被当作一个普通商品（products.category='团购'）售卖，
-- 无法满足满减/折扣/立减、平台券/商家券、领券中心/定向发放等诉求。
-- 本脚本将优惠券改造为独立功能：coupon_template（券模板）+ user_coupon（用户券）。
--
-- 口径（已与产品确认）：
--   1) 券类型：满减 full_reduction / 折扣 discount / 立减 instant_reduction
--   2) 适用范围：平台券 platform（全平台通用）+ 商家券 merchant（限指定商家）
--   3) 到账方式：领券中心自助领取 claim + 商家/平台定向发放 grant
--   4) 有效期：固定起止 fixed 或 领取后 N 天 relative；含发放总量/每人限领/使用门槛
--   5) 分账口径：优惠金额由商家承担；平台佣金/快递配送费按实付金额计算，
--      不改动现有微信电商分账链路（仅调整 DistributionRecord.distributableAmount 的基数）
--
-- 本脚本为手工执行脚本（application-prod.yml 中 flyway.enabled=false）。
-- 执行方式：mysql -u<user> -p <db> < V17__coupon.sql
-- 幂等：表使用 CREATE TABLE IF NOT EXISTS；列使用存储过程做存在性判断，可重复执行。
-- =====================================================================

-- ---------------------------------------------------------------------
-- 1) 优惠券模板表 coupon_template
-- ---------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS coupon_template
(
    id                  VARCHAR(32)   NOT NULL COMMENT '主键 cpt_',
    name                VARCHAR(100)  NOT NULL COMMENT '券名称（如 满100减20）',
    description         VARCHAR(500)           COMMENT '使用说明/描述',
    type                VARCHAR(20)   NOT NULL COMMENT '券类型 full_reduction满减/discount折扣/instant_reduction立减',
    scope               VARCHAR(20)   NOT NULL DEFAULT 'platform' COMMENT '适用范围 platform平台券/merchant商家券',
    merchant_id         VARCHAR(32)            COMMENT '商家ID（scope=merchant 时必填）mch_',
    property_company_id VARCHAR(32)            COMMENT '归属物业公司ID（商家券取商家物业；平台券可空）',
    value_amount        DECIMAL(10, 2)         COMMENT '面额：满减/立减金额（元）',
    discount_rate       DECIMAL(5, 2)          COMMENT '折扣率：8.50 表示 8.5 折（仅 type=discount）',
    max_discount_amount DECIMAL(10, 2)         COMMENT '折扣券封顶金额（仅 type=discount，可空=不封顶）',
    min_spend_amount    DECIMAL(10, 2) NOT NULL DEFAULT 0.00 COMMENT '使用门槛：订单商品金额需≥该值',
    valid_type          VARCHAR(20)   NOT NULL DEFAULT 'fixed' COMMENT '有效期类型 fixed固定起止/relative领取后N天',
    valid_start_at      DATETIME               COMMENT '有效期开始（valid_type=fixed）',
    valid_end_at        DATETIME               COMMENT '有效期结束（valid_type=fixed）',
    valid_days          INT                    COMMENT '领取后有效天数（valid_type=relative）',
    total_quantity      INT                    COMMENT '发放总量，NULL=不限量',
    claimed_quantity    INT           NOT NULL DEFAULT 0 COMMENT '已领取/已发放数量',
    per_user_limit      INT           NOT NULL DEFAULT 1 COMMENT '每人限领张数',
    status              VARCHAR(20)   NOT NULL DEFAULT 'active' COMMENT '状态 active启用/inactive停用',
    created_by          VARCHAR(32)            COMMENT '创建人（管理员ID）',
    created_at          DATETIME               COMMENT '创建时间',
    updated_at          DATETIME               COMMENT '更新时间',
    PRIMARY KEY (id),
    KEY idx_ct_status (status),
    KEY idx_ct_scope (scope),
    KEY idx_ct_merchant (merchant_id),
    KEY idx_ct_property (property_company_id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='优惠券模板';

-- ---------------------------------------------------------------------
-- 2) 用户优惠券表 user_coupon（含模板快照，模板变更不影响已发放券）
-- ---------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS user_coupon
(
    id                  VARCHAR(32)   NOT NULL COMMENT '主键 ucp_',
    template_id         VARCHAR(32)   NOT NULL COMMENT '券模板ID cpt_',
    resident_id         VARCHAR(32)   NOT NULL COMMENT '持有人（业主/居民）ID res_',
    merchant_id         VARCHAR(32)            COMMENT '限定商家ID（快照，平台券为空）',
    property_company_id VARCHAR(32)            COMMENT '归属物业公司ID（快照）',
    status              VARCHAR(20)   NOT NULL DEFAULT 'unused' COMMENT '状态 unused未使用/locked已锁定/used已使用/expired已过期',
    source              VARCHAR(20)   NOT NULL DEFAULT 'claim' COMMENT '来源 claim领券中心/grant定向发放',
    granted_by          VARCHAR(32)            COMMENT '发放人（管理员/商家ID，source=grant）',
    -- 模板快照
    name                VARCHAR(100)           COMMENT '券名称快照',
    type                VARCHAR(20)            COMMENT '券类型快照',
    value_amount        DECIMAL(10, 2)         COMMENT '面额快照',
    discount_rate       DECIMAL(5, 2)          COMMENT '折扣率快照',
    max_discount_amount DECIMAL(10, 2)         COMMENT '折扣封顶快照',
    min_spend_amount    DECIMAL(10, 2) NOT NULL DEFAULT 0.00 COMMENT '使用门槛快照',
    expire_at           DATETIME               COMMENT '过期时间（fixed=模板valid_end_at；relative=领取/发放时间+N天）',
    claimed_at          DATETIME               COMMENT '领取/发放时间',
    used_at             DATETIME               COMMENT '核销时间',
    order_id            VARCHAR(32)            COMMENT '核销订单ID ord_',
    created_at          DATETIME               COMMENT '创建时间',
    updated_at          DATETIME               COMMENT '更新时间',
    PRIMARY KEY (id),
    KEY idx_uc_resident_status (resident_id, status),
    KEY idx_uc_template_resident (template_id, resident_id),
    KEY idx_uc_order (order_id),
    KEY idx_uc_merchant (merchant_id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='用户优惠券（券实例）';

-- ---------------------------------------------------------------------
-- 3) orders 增加优惠券字段
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

-- 使用的用户券ID（user_coupon.id）
CALL p_wuye_add_column('orders', 'coupon_id',
                       'coupon_id VARCHAR(32) NULL COMMENT ''使用的用户优惠券ID ucp_''');
-- 优惠抵扣金额（由商家承担）
CALL p_wuye_add_column('orders', 'coupon_amount',
                       'coupon_amount DECIMAL(10,2) NOT NULL DEFAULT 0.00 COMMENT ''优惠券抵扣金额（商家承担）''');

DROP PROCEDURE IF EXISTS p_wuye_add_column;

-- ---------------------------------------------------------------------
-- 4) 存量处理：下架原「优惠券」商品（不物理删除，避免破坏 order_items / inquiries 引用）
--    商品 prd_357991e8e01000「优惠券10元」（category=团购，商家 mch_demo002）
-- ---------------------------------------------------------------------
UPDATE products
SET status     = 'inactive',
    updated_at = NOW()
WHERE id = 'prd_357991e8e01000';

-- 兜底：若 ID 不一致，按名称匹配同批「优惠券」类目商品（保持幂等，仅影响未删除记录）
UPDATE products
SET status     = 'inactive',
    updated_at = NOW()
WHERE deleted_at IS NULL
  AND status = 'active'
  AND category = '团购'
  AND name LIKE '%优惠券%';

-- =====================================================================
-- 执行完成。
-- 回滚（如需）：见文件末尾注释——
--   ALTER TABLE orders DROP COLUMN coupon_id, DROP COLUMN coupon_amount;
--   DROP TABLE IF EXISTS user_coupon;
--   DROP TABLE IF EXISTS coupon_template;
--   UPDATE products SET status='active' WHERE id='prd_357991e8e01000';
-- =====================================================================