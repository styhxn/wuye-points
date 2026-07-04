-- ============================================================
-- 物业积分服务系统 - 数据库部署脚本
-- 用法: mysql -u root -p < deploy-database.sql
-- 日期: 2026-07-01
-- ============================================================

-- 1. 创建数据库（如已存在则跳过）
CREATE DATABASE IF NOT EXISTS wuye_points
  DEFAULT CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;

-- 2. 创建应用专用用户（如已存在则跳过）
--    密码: wuye@2026  （请在生产环境修改）
CREATE USER IF NOT EXISTS 'wuye_app'@'%' IDENTIFIED BY 'wuye@2026';
GRANT ALL PRIVILEGES ON wuye_points.* TO 'wuye_app'@'%';
FLUSH PRIVILEGES;

-- 3. 使用目标数据库
USE wuye_points;

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ============================================================
-- 基础架构模块
-- ============================================================

CREATE TABLE IF NOT EXISTS property_companies (
    id VARCHAR(32) PRIMARY KEY COMMENT 'pc_xxxxxxxx',
    name VARCHAR(100) NOT NULL COMMENT '名称',
    logo_url VARCHAR(500) COMMENT 'Logo',
    contact_phone VARCHAR(20) COMMENT '联系电话',
    address VARCHAR(200) COMMENT '地址',
    property_share_rate DECIMAL(5,4) DEFAULT 0.3000 COMMENT '物业分成比例',
    coordinator_share_rate DECIMAL(5,4) DEFAULT 0.7000 COMMENT '统筹分成比例',
    sector_leader_rate DECIMAL(5,4) DEFAULT 0.1000 COMMENT '板块负责人比例',
    individual_leader_rate DECIMAL(5,4) DEFAULT 0.0000 COMMENT '个体负责人比例',
    coin_display_enabled BOOLEAN DEFAULT TRUE COMMENT '物业币卡片全局显示',
    coin_issue_mode VARCHAR(20) DEFAULT 'auto' COMMENT '发放模式',
    coin_expiry_days INT DEFAULT 365 COMMENT '物业币有效期天数',
    coin_freeze_default BOOLEAN DEFAULT FALSE COMMENT '新用户物业币默认冻结',
    delivery_base_fee DECIMAL(10,2) DEFAULT 0.00 COMMENT '配送起步价',
    delivery_courier_share_rate DECIMAL(5,4) DEFAULT 1.0000 COMMENT '快递员配送费分成比例',
    delivery_courier_per_order DECIMAL(10,2) DEFAULT 0.35 COMMENT '快递员每单报酬',
    withdrawal_fee_rate DECIMAL(5,4) DEFAULT 0.0060 COMMENT '提现手续费率',
    point_to_fee_rate DECIMAL(10,4) DEFAULT 0.0100 COMMENT '积分抵扣物业费比率',
    two_year_clear_enabled BOOLEAN DEFAULT TRUE COMMENT '2年欠费积分清零开关',
    neighbor_daily_contact_limit INT DEFAULT 2 COMMENT '邻居私聊每日限制',
    status VARCHAR(20) DEFAULT 'active' COMMENT 'active/inactive',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='物业公司表';

CREATE TABLE IF NOT EXISTS communities (
    id VARCHAR(32) PRIMARY KEY COMMENT 'com_xxxxxxxx',
    property_company_id VARCHAR(32) NOT NULL,
    name VARCHAR(100) NOT NULL,
    address VARCHAR(200),
    total_buildings INT,
    total_units INT,
    status VARCHAR(20) DEFAULT 'active',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (property_company_id) REFERENCES property_companies(id),
    INDEX idx_property_company (property_company_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='小区表';

CREATE TABLE IF NOT EXISTS residents (
    id VARCHAR(32) PRIMARY KEY COMMENT 'res_xxxxxxxx',
    openid VARCHAR(100) UNIQUE COMMENT '微信openid',
    unionid VARCHAR(100) COMMENT '微信unionid',
    phone VARCHAR(20),
    name VARCHAR(50),
    avatar_url VARCHAR(500),
    gender INT DEFAULT 0 COMMENT '0未知 1男 2女',
    age INT COMMENT '年龄',
    marital_status VARCHAR(20) COMMENT 'single/married/divorced/widowed',
    has_children BOOLEAN DEFAULT FALSE,
    user_type VARCHAR(20) DEFAULT 'owner' COMMENT 'owner业主/tenant租住人员',
    role VARCHAR(30) DEFAULT 'resident' COMMENT '见角色枚举',
    property_company_id VARCHAR(32),
    community_id VARCHAR(32),
    building VARCHAR(20),
    unit VARCHAR(20),
    room VARCHAR(20),
    family_id VARCHAR(32),
    family_relation VARCHAR(20) COMMENT 'owner/spouse/child/parent/sibling/other',
    point_balance INT DEFAULT 0,
    coin_balance DECIMAL(10,2) DEFAULT 0.00,
    coin_frozen BOOLEAN DEFAULT FALSE COMMENT '物业币是否冻结',
    coin_hidden BOOLEAN DEFAULT FALSE COMMENT '物业币卡片是否隐藏',
    wechat_bound BOOLEAN DEFAULT FALSE,
    status VARCHAR(20) DEFAULT 'active' COMMENT 'active/frozen/disabled',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (property_company_id) REFERENCES property_companies(id),
    FOREIGN KEY (community_id) REFERENCES communities(id),
    INDEX idx_property_company (property_company_id),
    INDEX idx_community (community_id),
    INDEX idx_family (family_id),
    INDEX idx_phone (phone),
    INDEX idx_openid (openid),
    INDEX idx_role (role)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='业主/住户表';

CREATE TABLE IF NOT EXISTS families (
    id VARCHAR(32) PRIMARY KEY COMMENT 'fam_xxxxxxxx',
    name VARCHAR(50),
    owner_id VARCHAR(32) NOT NULL COMMENT '户主ID',
    property_company_id VARCHAR(32) NOT NULL,
    community_id VARCHAR(32),
    building VARCHAR(20),
    unit VARCHAR(20),
    room VARCHAR(20),
    member_count INT DEFAULT 1,
    point_balance INT DEFAULT 0 COMMENT '家庭积分池',
    coin_balance DECIMAL(10,2) DEFAULT 0.00 COMMENT '家庭物业币池',
    vote_weight INT DEFAULT 1 COMMENT '投票权重',
    audit_status VARCHAR(20) DEFAULT 'pending' COMMENT 'pending/approved/rejected',
    status VARCHAR(20) DEFAULT 'active',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (owner_id) REFERENCES residents(id),
    FOREIGN KEY (property_company_id) REFERENCES property_companies(id),
    INDEX idx_owner (owner_id),
    INDEX idx_property_company (property_company_id),
    INDEX idx_audit_status (audit_status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='家庭表';

-- ============================================================
-- 商家体系模块
-- ============================================================

CREATE TABLE IF NOT EXISTS platform_merchants (
    id VARCHAR(32) PRIMARY KEY COMMENT 'pm_xxxxxxxx',
    name VARCHAR(100) NOT NULL COMMENT '商家名称',
    description TEXT,
    cover_url VARCHAR(500),
    address VARCHAR(200),
    contact_phone VARCHAR(20),
    category VARCHAR(50) COMMENT '外卖/周围商家/自营/农家/外卖小吃/团购',
    business_hours VARCHAR(50),
    status VARCHAR(20) DEFAULT 'active',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_category (category),
    INDEX idx_name (name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='平台级商家库';

CREATE TABLE IF NOT EXISTS merchants (
    id VARCHAR(32) PRIMARY KEY COMMENT 'mch_xxxxxxxx',
    platform_merchant_id VARCHAR(32) NOT NULL COMMENT '关联平台商家库ID',
    property_company_id VARCHAR(32) NOT NULL COMMENT '挂接的物业公司',
    name VARCHAR(100) NOT NULL COMMENT '商家名称',
    description TEXT,
    cover_urls JSON COMMENT '封面图片数组',
    video_url VARCHAR(500),
    qr_code_url VARCHAR(500) COMMENT '专属收款二维码',
    merchant_level VARCHAR(30) DEFAULT 'normal' COMMENT 'official_certified/property_certified/normal',
    level_weight INT DEFAULT 0 COMMENT '分级排序权重',
    category VARCHAR(50) COMMENT '分类',
    audit_status VARCHAR(20) DEFAULT 'pending_audit' COMMENT 'pending_audit/approved/rejected',
    rank_order INT DEFAULT 0 COMMENT '手动拖拽排序值',
    business_hours VARCHAR(50),
    contact_phone VARCHAR(20),
    address VARCHAR(200),
    delivery_fee DECIMAL(10,2) DEFAULT 0.00 COMMENT '商家设定的配送费',
    free_delivery_threshold DECIMAL(10,2) COMMENT '满额免配送费',
    status VARCHAR(20) DEFAULT 'active',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (platform_merchant_id) REFERENCES platform_merchants(id),
    FOREIGN KEY (property_company_id) REFERENCES property_companies(id),
    UNIQUE KEY uk_platform_property (platform_merchant_id, property_company_id),
    INDEX idx_property_company (property_company_id),
    INDEX idx_level (merchant_level),
    INDEX idx_audit_status (audit_status),
    INDEX idx_category (category),
    INDEX idx_rank (rank_order)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商家表';

CREATE TABLE IF NOT EXISTS merchant_property_configs (
    id VARCHAR(32) PRIMARY KEY COMMENT 'mpc_xxxxxxxx',
    merchant_id VARCHAR(32) NOT NULL,
    property_company_id VARCHAR(32) NOT NULL,
    commission_rate DECIMAL(5,4) NOT NULL COMMENT '抽佣比例',
    point_exchange_rate DECIMAL(10,4) COMMENT '积分兑换比',
    member_discount_price DECIMAL(10,2) COMMENT '业主折扣价',
    coin_rebate_rate DECIMAL(5,4) DEFAULT 0.0000 COMMENT '物业币返现比',
    coin_rebate_enabled BOOLEAN DEFAULT FALSE COMMENT '是否启用物业币返现',
    status VARCHAR(20) DEFAULT 'active',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (merchant_id) REFERENCES merchants(id),
    FOREIGN KEY (property_company_id) REFERENCES property_companies(id),
    UNIQUE KEY uk_merchant_property (merchant_id, property_company_id),
    INDEX idx_property_company (property_company_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商家-物业参数配置表';

CREATE TABLE IF NOT EXISTS products (
    id VARCHAR(32) PRIMARY KEY COMMENT 'prd_xxxxxxxx',
    merchant_id VARCHAR(32) NOT NULL,
    name VARCHAR(100) NOT NULL,
    description TEXT,
    cover_url VARCHAR(500),
    price DECIMAL(10,2) NOT NULL,
    member_price DECIMAL(10,2) COMMENT '业主专属折扣价',
    point_price INT COMMENT '积分兑换价格',
    stock INT DEFAULT 0,
    category VARCHAR(50),
    status VARCHAR(20) DEFAULT 'active' COMMENT 'active/inactive/sold_out',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (merchant_id) REFERENCES merchants(id),
    INDEX idx_merchant (merchant_id),
    INDEX idx_category (category)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商品表';

-- ============================================================
-- 订单与分成模块
-- ============================================================

CREATE TABLE IF NOT EXISTS orders (
    id VARCHAR(32) PRIMARY KEY COMMENT 'ord_xxxxxxxx',
    order_no VARCHAR(32) UNIQUE,
    resident_id VARCHAR(32) NOT NULL,
    merchant_id VARCHAR(32) NOT NULL,
    property_company_id VARCHAR(32) NOT NULL COMMENT '按住户所属物业归属',
    family_id VARCHAR(32),
    order_status VARCHAR(20) DEFAULT 'pending' COMMENT 'pending/paid/delivering/completed/cancelled/refunding/refunded',
    total_amount DECIMAL(10,2) NOT NULL,
    delivery_fee DECIMAL(10,2) DEFAULT 0.00,
    payment_method VARCHAR(20) COMMENT 'property_coin/point/wechat/mixed',
    point_used INT DEFAULT 0 COMMENT '混合支付使用的积分',
    coin_used DECIMAL(10,2) DEFAULT 0.00 COMMENT '混合支付使用的物业币',
    cash_amount DECIMAL(10,2) DEFAULT 0.00 COMMENT '混合支付现金部分',
    paid_at TIMESTAMP,
    delivered_at TIMESTAMP,
    completed_at TIMESTAMP,
    cancelled_at TIMESTAMP,
    cancel_reason VARCHAR(200) COMMENT '取消原因',
    point_earned INT DEFAULT 0 COMMENT '本次获得积分',
    coin_earned DECIMAL(10,2) DEFAULT 0.00 COMMENT '本次获得物业币返现',
    courier_id VARCHAR(32),
    address VARCHAR(200),
    contact_phone VARCHAR(20),
    remark VARCHAR(500),
    version INT DEFAULT 0 COMMENT '乐观锁',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (resident_id) REFERENCES residents(id),
    FOREIGN KEY (merchant_id) REFERENCES merchants(id),
    FOREIGN KEY (property_company_id) REFERENCES property_companies(id),
    FOREIGN KEY (family_id) REFERENCES families(id),
    INDEX idx_resident (resident_id),
    INDEX idx_merchant (merchant_id),
    INDEX idx_property_company (property_company_id),
    INDEX idx_status (order_status),
    INDEX idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='订单表';

CREATE TABLE IF NOT EXISTS order_items (
    id VARCHAR(32) PRIMARY KEY COMMENT 'oi_xxxxxxxx',
    order_id VARCHAR(32) NOT NULL,
    product_id VARCHAR(32) NOT NULL,
    product_name VARCHAR(100) COMMENT '冗余',
    product_cover VARCHAR(500) COMMENT '冗余',
    quantity INT NOT NULL,
    unit_price DECIMAL(10,2) NOT NULL,
    total_price DECIMAL(10,2) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (order_id) REFERENCES orders(id) ON DELETE CASCADE,
    INDEX idx_order (order_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='订单商品表';

CREATE TABLE IF NOT EXISTS distribution_records (
    id VARCHAR(32) PRIMARY KEY COMMENT 'dr_xxxxxxxx',
    order_id VARCHAR(32) NOT NULL,
    merchant_id VARCHAR(32) NOT NULL,
    property_company_id VARCHAR(32) NOT NULL COMMENT '住户所属物业',
    resident_id VARCHAR(32) NOT NULL,
    total_amount DECIMAL(10,2) NOT NULL COMMENT '消费金额P',
    commission_rate DECIMAL(5,4) NOT NULL COMMENT '商家在该物业的抽佣比例R_T',
    point_cost DECIMAL(10,2) DEFAULT 0.00 COMMENT '积分成本',
    coin_cost DECIMAL(10,2) DEFAULT 0.00 COMMENT '物业币返现成本',
    distributable_amount DECIMAL(10,2) NOT NULL COMMENT '可分利润',
    property_share DECIMAL(10,2) NOT NULL COMMENT '物业公司收入',
    coordinator_share DECIMAL(10,2) NOT NULL COMMENT '统筹收入',
    sector_leader_share DECIMAL(10,2) DEFAULT 0.00 COMMENT '板块负责人收入',
    individual_leader_share DECIMAL(10,2) DEFAULT 0.00 COMMENT '个体负责人收入',
    coordinator_id VARCHAR(32) COMMENT '统筹ID',
    sector_leader_id VARCHAR(32) COMMENT '板块负责人ID',
    individual_leader_id VARCHAR(32) COMMENT '个体负责人ID',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (order_id) REFERENCES orders(id),
    FOREIGN KEY (merchant_id) REFERENCES merchants(id),
    FOREIGN KEY (property_company_id) REFERENCES property_companies(id),
    FOREIGN KEY (resident_id) REFERENCES residents(id),
    INDEX idx_order (order_id),
    INDEX idx_merchant (merchant_id),
    INDEX idx_property_company (property_company_id),
    INDEX idx_coordinator (coordinator_id),
    INDEX idx_sector_leader (sector_leader_id),
    INDEX idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='四级分成记录表';

-- ============================================================
-- 积分体系模块
-- ============================================================

CREATE TABLE IF NOT EXISTS point_records (
    id VARCHAR(32) PRIMARY KEY COMMENT 'pt_xxxxxxxx',
    resident_id VARCHAR(32) NOT NULL,
    family_id VARCHAR(32),
    property_company_id VARCHAR(32) NOT NULL,
    point_type VARCHAR(30) NOT NULL COMMENT 'earn/spend/transfer/expire/pool_in',
    amount INT NOT NULL COMMENT '正数增加，负数减少',
    balance INT NOT NULL COMMENT '操作后余额',
    source VARCHAR(30) COMMENT 'order/activity/sign_in/manual/merchant_grant/fee_deduct/pool_diff',
    source_id VARCHAR(32),
    description VARCHAR(200),
    operator_id VARCHAR(32),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (resident_id) REFERENCES residents(id),
    FOREIGN KEY (family_id) REFERENCES families(id),
    FOREIGN KEY (property_company_id) REFERENCES property_companies(id),
    INDEX idx_resident (resident_id),
    INDEX idx_family (family_id),
    INDEX idx_property_company (property_company_id),
    INDEX idx_source (source, source_id),
    INDEX idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='积分记录表';

CREATE TABLE IF NOT EXISTS point_rules (
    id VARCHAR(32) PRIMARY KEY COMMENT 'prr_xxxxxxxx',
    name VARCHAR(100) NOT NULL,
    description TEXT,
    point_type VARCHAR(30) NOT NULL COMMENT 'earn/spend/redeem/expire',
    source VARCHAR(30) NOT NULL,
    rate DECIMAL(10,4) COMMENT '兑换比率',
    min_amount DECIMAL(10,2),
    max_amount DECIMAL(10,2),
    min_points INT,
    max_points INT,
    daily_limit INT,
    monthly_limit INT,
    expire_days INT COMMENT '过期天数(730=2年)',
    property_company_id VARCHAR(32) COMMENT 'null为全局',
    status VARCHAR(20) DEFAULT 'active',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (property_company_id) REFERENCES property_companies(id),
    INDEX idx_property_company (property_company_id),
    INDEX idx_type (point_type),
    INDEX idx_source (source)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='积分规则表';

CREATE TABLE IF NOT EXISTS point_pools (
    id VARCHAR(32) PRIMARY KEY COMMENT 'pp_xxxxxxxx',
    property_company_id VARCHAR(32) NOT NULL,
    balance DECIMAL(10,2) DEFAULT 0.00 COMMENT '积分池资金余额',
    total_in DECIMAL(10,2) DEFAULT 0.00 COMMENT '累计流入',
    total_out DECIMAL(10,2) DEFAULT 0.00 COMMENT '累计流出',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (property_company_id) REFERENCES property_companies(id),
    UNIQUE KEY uk_property_company (property_company_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='积分池表';

CREATE TABLE IF NOT EXISTS point_pool_records (
    id VARCHAR(32) PRIMARY KEY COMMENT 'ppr_xxxxxxxx',
    property_company_id VARCHAR(32) NOT NULL,
    pool_type VARCHAR(30) NOT NULL COMMENT 'pool_in/pool_out',
    amount DECIMAL(10,2) NOT NULL,
    balance DECIMAL(10,2) NOT NULL,
    source VARCHAR(30) COMMENT 'exchange_diff/coin_expired/fee_clear/spend',
    source_id VARCHAR(32),
    description VARCHAR(200),
    operator_id VARCHAR(32),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (property_company_id) REFERENCES property_companies(id),
    INDEX idx_property_company (property_company_id),
    INDEX idx_source (source),
    INDEX idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='积分池进出记录表';

-- ============================================================
-- 物业币体系模块
-- ============================================================

CREATE TABLE IF NOT EXISTS property_coin_records (
    id VARCHAR(32) PRIMARY KEY COMMENT 'coin_xxxxxxxx',
    resident_id VARCHAR(32) NOT NULL,
    family_id VARCHAR(32),
    property_company_id VARCHAR(32) NOT NULL,
    coin_type VARCHAR(30) NOT NULL COMMENT 'earn/spend/recharge/withdraw/expire/freeze/unfreeze',
    amount DECIMAL(10,2) NOT NULL,
    balance DECIMAL(10,2) NOT NULL,
    source VARCHAR(30) COMMENT 'order/property_fee/manual/coin_expired',
    source_id VARCHAR(32),
    description VARCHAR(200),
    operator_id VARCHAR(32),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (resident_id) REFERENCES residents(id),
    FOREIGN KEY (family_id) REFERENCES families(id),
    FOREIGN KEY (property_company_id) REFERENCES property_companies(id),
    INDEX idx_resident (resident_id),
    INDEX idx_family (family_id),
    INDEX idx_property_company (property_company_id),
    INDEX idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='物业币记录表';

CREATE TABLE IF NOT EXISTS property_coin_rules (
    id VARCHAR(32) PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    description TEXT,
    coin_type VARCHAR(30) NOT NULL,
    source VARCHAR(30) NOT NULL,
    rate DECIMAL(10,4),
    min_amount DECIMAL(10,2),
    max_amount DECIMAL(10,2),
    daily_limit DECIMAL(10,2),
    monthly_limit DECIMAL(10,2),
    property_company_id VARCHAR(32),
    status VARCHAR(20) DEFAULT 'active',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (property_company_id) REFERENCES property_companies(id),
    INDEX idx_property_company (property_company_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='物业币规则表';

CREATE TABLE IF NOT EXISTS coin_freeze_records (
    id VARCHAR(32) PRIMARY KEY COMMENT 'cfr_xxxxxxxx',
    resident_id VARCHAR(32) NOT NULL,
    property_company_id VARCHAR(32) NOT NULL,
    action VARCHAR(20) NOT NULL COMMENT 'freeze/unfreeze',
    amount DECIMAL(10,2) COMMENT '涉及金额',
    reason VARCHAR(200),
    operator_id VARCHAR(32) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (resident_id) REFERENCES residents(id),
    FOREIGN KEY (property_company_id) REFERENCES property_companies(id),
    INDEX idx_resident (resident_id),
    INDEX idx_property_company (property_company_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='物业币冻结/解冻记录表';

CREATE TABLE IF NOT EXISTS coin_expiry_records (
    id VARCHAR(32) PRIMARY KEY COMMENT 'cer_xxxxxxxx',
    resident_id VARCHAR(32) NOT NULL,
    property_company_id VARCHAR(32) NOT NULL,
    expired_amount DECIMAL(10,2) NOT NULL COMMENT '清零金额',
    pool_in_amount DECIMAL(10,2) NOT NULL COMMENT '进入积分池金额',
    expired_at TIMESTAMP NOT NULL COMMENT '过期时间',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (resident_id) REFERENCES residents(id),
    FOREIGN KEY (property_company_id) REFERENCES property_companies(id),
    INDEX idx_resident (resident_id),
    INDEX idx_property_company (property_company_id),
    INDEX idx_expired_at (expired_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='物业币过期清零记录表';

-- ============================================================
-- 物业费模块
-- ============================================================

CREATE TABLE IF NOT EXISTS property_fee_rules (
    id VARCHAR(32) PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    fee_type VARCHAR(30) NOT NULL COMMENT 'property/parking/water/electricity',
    calculation_method VARCHAR(30) NOT NULL COMMENT 'fixed/per_square_meter/per_unit',
    unit_price DECIMAL(10,2) NOT NULL,
    unit VARCHAR(20),
    billing_cycle VARCHAR(20) NOT NULL COMMENT 'monthly/quarterly/yearly',
    auto_deduct_enabled BOOLEAN DEFAULT FALSE,
    overdue_rate DECIMAL(5,4) COMMENT '日滞纳金比例',
    property_company_id VARCHAR(32),
    status VARCHAR(20) DEFAULT 'active',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (property_company_id) REFERENCES property_companies(id),
    INDEX idx_property_company (property_company_id),
    INDEX idx_fee_type (fee_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='物业费规则表';

CREATE TABLE IF NOT EXISTS property_fees (
    id VARCHAR(32) PRIMARY KEY,
    resident_id VARCHAR(32) NOT NULL,
    family_id VARCHAR(32),
    property_company_id VARCHAR(32) NOT NULL,
    community_id VARCHAR(32),
    fee_type VARCHAR(30) NOT NULL,
    period VARCHAR(20) NOT NULL COMMENT '账期如2026-Q3',
    amount DECIMAL(10,2) NOT NULL,
    paid_amount DECIMAL(10,2) DEFAULT 0.00,
    point_deducted INT DEFAULT 0 COMMENT '积分抵扣数量',
    coin_deducted DECIMAL(10,2) DEFAULT 0.00 COMMENT '物业币抵扣金额',
    payment_status VARCHAR(20) DEFAULT 'unpaid' COMMENT 'unpaid/partial/paid/overdue',
    due_date DATE,
    paid_at TIMESTAMP,
    auto_deduct BOOLEAN DEFAULT FALSE,
    late_fee DECIMAL(10,2) DEFAULT 0.00 COMMENT '滞纳金',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (resident_id) REFERENCES residents(id),
    FOREIGN KEY (family_id) REFERENCES families(id),
    FOREIGN KEY (property_company_id) REFERENCES property_companies(id),
    FOREIGN KEY (community_id) REFERENCES communities(id),
    INDEX idx_resident (resident_id),
    INDEX idx_property_company (property_company_id),
    INDEX idx_status (payment_status),
    INDEX idx_period (period)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='物业费表';

-- ============================================================
-- 配送模块
-- ============================================================

CREATE TABLE IF NOT EXISTS delivery_rules (
    id VARCHAR(32) PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    base_fee DECIMAL(10,2) NOT NULL COMMENT '物业设定起步价',
    per_kg_fee DECIMAL(10,2),
    free_threshold DECIMAL(10,2),
    max_weight DECIMAL(10,2),
    max_distance DECIMAL(10,2),
    courier_share_rate DECIMAL(5,4) DEFAULT 1.0000,
    courier_per_order DECIMAL(10,2) DEFAULT 0.35 COMMENT '快递员每单报酬',
    timeout_minutes INT DEFAULT 30,
    property_company_id VARCHAR(32),
    status VARCHAR(20) DEFAULT 'active',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (property_company_id) REFERENCES property_companies(id),
    INDEX idx_property_company (property_company_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='配送规则表';

CREATE TABLE IF NOT EXISTS deliveries (
    id VARCHAR(32) PRIMARY KEY COMMENT 'dlv_xxxxxxxx',
    order_id VARCHAR(32) NOT NULL,
    courier_id VARCHAR(32) COMMENT '快递员ID',
    property_company_id VARCHAR(32) NOT NULL,
    status VARCHAR(20) DEFAULT 'pending' COMMENT 'pending/accepted/delivering/completed/failed/property_fallback',
    fee DECIMAL(10,2) NOT NULL COMMENT '配送费',
    courier_earning DECIMAL(10,2) COMMENT '快递员收入',
    pickup_address VARCHAR(200),
    delivery_address VARCHAR(200),
    contact_phone VARCHAR(20),
    accepted_at TIMESTAMP,
    picked_at TIMESTAMP,
    delivered_at TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (order_id) REFERENCES orders(id),
    FOREIGN KEY (courier_id) REFERENCES residents(id),
    FOREIGN KEY (property_company_id) REFERENCES property_companies(id),
    INDEX idx_order (order_id),
    INDEX idx_courier (courier_id),
    INDEX idx_property_company (property_company_id),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='配送表';

-- ============================================================
-- 公告与消息模块
-- ============================================================

CREATE TABLE IF NOT EXISTS announcements (
    id VARCHAR(32) PRIMARY KEY COMMENT 'ann_xxxxxxxx',
    title VARCHAR(200) NOT NULL,
    content TEXT NOT NULL,
    announcement_type VARCHAR(30) NOT NULL COMMENT 'property/community/platform/system/merchant/activity',
    property_company_id VARCHAR(32),
    community_id VARCHAR(32),
    merchant_id VARCHAR(32),
    publisher_id VARCHAR(32) NOT NULL,
    publisher_role VARCHAR(30) NOT NULL,
    cover_urls JSON,
    target_roles JSON COMMENT '目标角色',
    target_buildings JSON COMMENT '目标楼栋',
    collect_enabled BOOLEAN DEFAULT FALSE COMMENT '是否启用信息收集',
    collect_fields JSON COMMENT '收集字段定义',
    status VARCHAR(20) DEFAULT 'published' COMMENT 'draft/published/archived',
    published_at TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (property_company_id) REFERENCES property_companies(id),
    INDEX idx_property_company (property_company_id),
    INDEX idx_type (announcement_type),
    INDEX idx_status (status),
    INDEX idx_published_at (published_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='公告表';

CREATE TABLE IF NOT EXISTS announcement_collects (
    id VARCHAR(32) PRIMARY KEY,
    announcement_id VARCHAR(32) NOT NULL,
    resident_id VARCHAR(32) NOT NULL,
    collected_data JSON NOT NULL COMMENT '业主填写的资料',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (announcement_id) REFERENCES announcements(id),
    FOREIGN KEY (resident_id) REFERENCES residents(id),
    UNIQUE KEY uk_announcement_resident (announcement_id, resident_id),
    INDEX idx_announcement (announcement_id),
    INDEX idx_resident (resident_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='公告信息收集表';

CREATE TABLE IF NOT EXISTS messages (
    id VARCHAR(32) PRIMARY KEY COMMENT 'msg_xxxxxxxx',
    from_id VARCHAR(32) NOT NULL,
    to_id VARCHAR(32),
    to_role VARCHAR(30) COMMENT '群发时的目标角色',
    message_type VARCHAR(30) NOT NULL COMMENT 'property/platform/system/community/resident/technician',
    content TEXT NOT NULL,
    chat_type VARCHAR(20) COMMENT 'private/group/broadcast',
    group_id VARCHAR(32),
    read_status VARCHAR(20) DEFAULT 'unread',
    property_company_id VARCHAR(32),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (from_id) REFERENCES residents(id),
    INDEX idx_from (from_id),
    INDEX idx_to (to_id),
    INDEX idx_type (message_type),
    INDEX idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='消息表';

CREATE TABLE IF NOT EXISTS neighbor_contact_limits (
    id VARCHAR(32) PRIMARY KEY,
    resident_id VARCHAR(32) NOT NULL,
    property_company_id VARCHAR(32) NOT NULL,
    contact_date DATE NOT NULL,
    contact_count INT DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (resident_id) REFERENCES residents(id),
    FOREIGN KEY (property_company_id) REFERENCES property_companies(id),
    UNIQUE KEY uk_resident_date (resident_id, contact_date),
    INDEX idx_resident (resident_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='邻居私聊每日限制表';

-- ============================================================
-- 社区服务模块
-- ============================================================

CREATE TABLE IF NOT EXISTS community_services (
    id VARCHAR(32) PRIMARY KEY COMMENT 'svc_xxxxxxxx',
    name VARCHAR(100) NOT NULL,
    description TEXT,
    cover_urls JSON,
    category VARCHAR(50) NOT NULL COMMENT 'cleaning_repair/canteen/tutoring/elderly_care/medical/pet_medical/life_service/dating',
    price DECIMAL(10,2),
    member_price DECIMAL(10,2) COMMENT '业主折扣价',
    price_unit VARCHAR(20) COMMENT '次/小时/平方米/月/年',
    provider_id VARCHAR(32),
    provider_type VARCHAR(20) COMMENT 'merchant/resident/individual_leader',
    property_company_id VARCHAR(32) NOT NULL,
    rank_order INT DEFAULT 0 COMMENT '手动拖拽排名',
    is_subscription BOOLEAN DEFAULT FALSE COMMENT '是否支持包月/包年',
    status VARCHAR(20) DEFAULT 'active',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (provider_id) REFERENCES residents(id),
    FOREIGN KEY (property_company_id) REFERENCES property_companies(id),
    INDEX idx_property_company (property_company_id),
    INDEX idx_category (category),
    INDEX idx_rank (rank_order)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='社区服务表';

CREATE TABLE IF NOT EXISTS service_orders (
    id VARCHAR(32) PRIMARY KEY,
    service_id VARCHAR(32) NOT NULL,
    resident_id VARCHAR(32) NOT NULL,
    provider_id VARCHAR(32),
    property_company_id VARCHAR(32) NOT NULL,
    order_status VARCHAR(20) DEFAULT 'pending',
    amount DECIMAL(10,2) NOT NULL,
    payment_method VARCHAR(20),
    service_time TIMESTAMP,
    address VARCHAR(200),
    contact_phone VARCHAR(20),
    remark VARCHAR(500),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (service_id) REFERENCES community_services(id),
    FOREIGN KEY (resident_id) REFERENCES residents(id),
    FOREIGN KEY (property_company_id) REFERENCES property_companies(id),
    INDEX idx_resident (resident_id),
    INDEX idx_provider (provider_id),
    INDEX idx_property_company (property_company_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='服务订单表';

CREATE TABLE IF NOT EXISTS service_subscriptions (
    id VARCHAR(32) PRIMARY KEY,
    service_id VARCHAR(32) NOT NULL,
    resident_id VARCHAR(32) NOT NULL,
    property_company_id VARCHAR(32) NOT NULL,
    sub_type VARCHAR(20) NOT NULL COMMENT 'monthly/yearly',
    amount DECIMAL(10,2) NOT NULL,
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    status VARCHAR(20) DEFAULT 'active' COMMENT 'active/expired/cancelled',
    auto_renew BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (service_id) REFERENCES community_services(id),
    FOREIGN KEY (resident_id) REFERENCES residents(id),
    FOREIGN KEY (property_company_id) REFERENCES property_companies(id),
    INDEX idx_resident (resident_id),
    INDEX idx_service (service_id),
    INDEX idx_end_date (end_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='服务订阅表';

-- ============================================================
-- 活动组模块
-- ============================================================

CREATE TABLE IF NOT EXISTS activity_groups (
    id VARCHAR(32) PRIMARY KEY COMMENT 'act_xxxxxxxx',
    name VARCHAR(100) NOT NULL,
    description TEXT,
    cover_url VARCHAR(500),
    leader_id VARCHAR(32) NOT NULL,
    property_company_id VARCHAR(32) NOT NULL,
    community_id VARCHAR(32),
    member_count INT DEFAULT 0,
    monthly_fee DECIMAL(10,2) DEFAULT 0.00 COMMENT '月费',
    yearly_fee DECIMAL(10,2) DEFAULT 0.00 COMMENT '年费',
    status VARCHAR(20) DEFAULT 'active',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (leader_id) REFERENCES residents(id),
    FOREIGN KEY (property_company_id) REFERENCES property_companies(id),
    INDEX idx_leader (leader_id),
    INDEX idx_property_company (property_company_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='活动组表';

CREATE TABLE IF NOT EXISTS activity_group_subscriptions (
    id VARCHAR(32) PRIMARY KEY,
    group_id VARCHAR(32) NOT NULL,
    resident_id VARCHAR(32) NOT NULL,
    property_company_id VARCHAR(32) NOT NULL,
    sub_type VARCHAR(20) NOT NULL COMMENT 'monthly/yearly',
    amount DECIMAL(10,2) NOT NULL,
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    status VARCHAR(20) DEFAULT 'active',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (group_id) REFERENCES activity_groups(id),
    FOREIGN KEY (resident_id) REFERENCES residents(id),
    FOREIGN KEY (property_company_id) REFERENCES property_companies(id),
    INDEX idx_group (group_id),
    INDEX idx_resident (resident_id),
    INDEX idx_end_date (end_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='活动组订阅表';

CREATE TABLE IF NOT EXISTS activity_group_members (
    id VARCHAR(32) PRIMARY KEY,
    group_id VARCHAR(32) NOT NULL,
    resident_id VARCHAR(32) NOT NULL,
    role VARCHAR(20) DEFAULT 'member' COMMENT 'leader/member',
    joined_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    status VARCHAR(20) DEFAULT 'active',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (group_id) REFERENCES activity_groups(id) ON DELETE CASCADE,
    FOREIGN KEY (resident_id) REFERENCES residents(id),
    UNIQUE KEY uk_group_resident (group_id, resident_id),
    INDEX idx_group (group_id),
    INDEX idx_resident (resident_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='活动组成员表';

-- ============================================================
-- 意见箱模块
-- ============================================================

CREATE TABLE IF NOT EXISTS opinion_boxes (
    id VARCHAR(32) PRIMARY KEY,
    title VARCHAR(200) NOT NULL,
    content TEXT NOT NULL,
    publisher_id VARCHAR(32) NOT NULL,
    family_id VARCHAR(32) NOT NULL COMMENT '按家庭限制每月1条',
    property_company_id VARCHAR(32) NOT NULL,
    start_time TIMESTAMP NOT NULL,
    end_time TIMESTAMP NOT NULL,
    total_votes INT DEFAULT 0,
    support_count INT DEFAULT 0,
    oppose_count INT DEFAULT 0,
    abstain_count INT DEFAULT 0,
    status VARCHAR(20) DEFAULT 'active' COMMENT 'active/ended/closed',
    result_published BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (publisher_id) REFERENCES residents(id),
    FOREIGN KEY (family_id) REFERENCES families(id),
    FOREIGN KEY (property_company_id) REFERENCES property_companies(id),
    INDEX idx_property_company (property_company_id),
    INDEX idx_family (family_id),
    INDEX idx_status (status),
    INDEX idx_end_time (end_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='意见箱表';

CREATE TABLE IF NOT EXISTS vote_records (
    id VARCHAR(32) PRIMARY KEY,
    opinion_box_id VARCHAR(32) NOT NULL,
    resident_id VARCHAR(32) NOT NULL,
    family_id VARCHAR(32) NOT NULL,
    vote_option VARCHAR(20) NOT NULL COMMENT 'support/oppose/abstain',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (opinion_box_id) REFERENCES opinion_boxes(id),
    FOREIGN KEY (resident_id) REFERENCES residents(id),
    FOREIGN KEY (family_id) REFERENCES families(id),
    UNIQUE KEY uk_opinion_family (opinion_box_id, family_id) COMMENT '一户一票',
    INDEX idx_opinion_box (opinion_box_id),
    INDEX idx_family (family_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='投票记录表';

-- ============================================================
-- 统筹与板块模块
-- ============================================================

CREATE TABLE IF NOT EXISTS coordinators (
    id VARCHAR(32) PRIMARY KEY COMMENT 'coo_xxxxxxxx',
    resident_id VARCHAR(32) NOT NULL,
    property_company_id VARCHAR(32) NOT NULL,
    commission_rate DECIMAL(5,4) DEFAULT 0.7000 COMMENT '统筹分成比例',
    status VARCHAR(20) DEFAULT 'active',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (resident_id) REFERENCES residents(id),
    FOREIGN KEY (property_company_id) REFERENCES property_companies(id),
    UNIQUE KEY uk_resident_property (resident_id, property_company_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='统筹表';

CREATE TABLE IF NOT EXISTS sector_leaders (
    id VARCHAR(32) PRIMARY KEY COMMENT 'sl_xxxxxxxx',
    resident_id VARCHAR(32) NOT NULL,
    property_company_id VARCHAR(32) NOT NULL,
    sector VARCHAR(50) NOT NULL COMMENT 'elderly/medical/education/cleaning/canteen/pet/life_service',
    commission_rate DECIMAL(5,4) DEFAULT 0.1000 COMMENT '从统筹中抽取的比例',
    coordinator_id VARCHAR(32) COMMENT '所属统筹',
    status VARCHAR(20) DEFAULT 'active',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (resident_id) REFERENCES residents(id),
    FOREIGN KEY (property_company_id) REFERENCES property_companies(id),
    FOREIGN KEY (coordinator_id) REFERENCES coordinators(id),
    UNIQUE KEY uk_resident_property_sector (resident_id, property_company_id, sector),
    INDEX idx_property_company (property_company_id),
    INDEX idx_sector (sector)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='板块负责人表';

CREATE TABLE IF NOT EXISTS individual_leaders (
    id VARCHAR(32) PRIMARY KEY COMMENT 'il_xxxxxxxx',
    resident_id VARCHAR(32) NOT NULL,
    property_company_id VARCHAR(32) NOT NULL,
    sector_leader_id VARCHAR(32) NOT NULL COMMENT '所属板块负责人',
    sector VARCHAR(50) NOT NULL,
    name VARCHAR(50) COMMENT '个体名称',
    commission_rate DECIMAL(5,4) DEFAULT 0.0000 COMMENT '从板块中抽取的比例',
    status VARCHAR(20) DEFAULT 'active',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (resident_id) REFERENCES residents(id),
    FOREIGN KEY (property_company_id) REFERENCES property_companies(id),
    FOREIGN KEY (sector_leader_id) REFERENCES sector_leaders(id),
    INDEX idx_property_company (property_company_id),
    INDEX idx_sector_leader (sector_leader_id),
    INDEX idx_sector (sector)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='个体负责人表';

-- ============================================================
-- 权限体系模块
-- ============================================================

CREATE TABLE IF NOT EXISTS permissions (
    id VARCHAR(32) PRIMARY KEY COMMENT 'perm_xxxxxxxx',
    code VARCHAR(50) NOT NULL UNIQUE COMMENT '权限编码',
    name VARCHAR(100) NOT NULL COMMENT '权限名称',
    description VARCHAR(200),
    module VARCHAR(50) COMMENT '所属模块',
    status VARCHAR(20) DEFAULT 'active',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_code (code),
    INDEX idx_module (module)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='权限池表';

CREATE TABLE IF NOT EXISTS role_presets (
    id VARCHAR(32) PRIMARY KEY COMMENT 'rp_xxxxxxxx',
    name VARCHAR(50) NOT NULL COMMENT '预设名称',
    code VARCHAR(50) NOT NULL UNIQUE COMMENT 'coordinator/sector_leader等',
    description VARCHAR(200),
    property_company_id VARCHAR(32) COMMENT 'null为平台级预设',
    status VARCHAR(20) DEFAULT 'active',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (property_company_id) REFERENCES property_companies(id),
    INDEX idx_code (code),
    INDEX idx_property_company (property_company_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色快捷预设表';

CREATE TABLE IF NOT EXISTS role_preset_permissions (
    id VARCHAR(32) PRIMARY KEY,
    preset_id VARCHAR(32) NOT NULL,
    permission_id VARCHAR(32) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (preset_id) REFERENCES role_presets(id) ON DELETE CASCADE,
    FOREIGN KEY (permission_id) REFERENCES permissions(id),
    UNIQUE KEY uk_preset_permission (preset_id, permission_id),
    INDEX idx_preset (preset_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='预设-权限关联表';

CREATE TABLE IF NOT EXISTS user_permissions (
    id VARCHAR(32) PRIMARY KEY,
    resident_id VARCHAR(32) NOT NULL,
    permission_id VARCHAR(32) NOT NULL,
    granted BOOLEAN NOT NULL COMMENT 'TRUE=勾选/FALSE=取消',
    property_company_id VARCHAR(32) NOT NULL,
    operator_id VARCHAR(32) NOT NULL COMMENT '操作人',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (resident_id) REFERENCES residents(id),
    FOREIGN KEY (permission_id) REFERENCES permissions(id),
    FOREIGN KEY (property_company_id) REFERENCES property_companies(id),
    UNIQUE KEY uk_resident_permission (resident_id, permission_id),
    INDEX idx_resident (resident_id),
    INDEX idx_property_company (property_company_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户权限表';

CREATE TABLE IF NOT EXISTS permission_change_logs (
    id VARCHAR(32) PRIMARY KEY,
    resident_id VARCHAR(32) NOT NULL COMMENT '被操作用户',
    operator_id VARCHAR(32) NOT NULL COMMENT '操作人',
    property_company_id VARCHAR(32) NOT NULL,
    action VARCHAR(20) NOT NULL COMMENT 'grant/revoke/apply_preset',
    permission_code VARCHAR(50) COMMENT '权限编码',
    preset_code VARCHAR(50) COMMENT '应用的预设编码',
    before_value VARCHAR(200) COMMENT '变更前',
    after_value VARCHAR(200) COMMENT '变更后',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (resident_id) REFERENCES residents(id),
    FOREIGN KEY (operator_id) REFERENCES residents(id),
    FOREIGN KEY (property_company_id) REFERENCES property_companies(id),
    INDEX idx_resident (resident_id),
    INDEX idx_operator (operator_id),
    INDEX idx_property_company (property_company_id),
    INDEX idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='权限变更日志表';

-- ============================================================
-- 商家积分流程模块
-- ============================================================

CREATE TABLE IF NOT EXISTS merchant_point_purchases (
    id VARCHAR(32) PRIMARY KEY COMMENT 'mpp_xxxxxxxx',
    merchant_id VARCHAR(32) NOT NULL,
    property_company_id VARCHAR(32) NOT NULL,
    point_amount INT NOT NULL COMMENT '购买积分数量',
    pay_amount DECIMAL(10,2) NOT NULL COMMENT '支付金额',
    audit_status VARCHAR(20) DEFAULT 'pending' COMMENT 'pending/approved/rejected',
    audited_at TIMESTAMP,
    auditor_id VARCHAR(32),
    remaining_points INT COMMENT '剩余未录入积分',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (merchant_id) REFERENCES merchants(id),
    FOREIGN KEY (property_company_id) REFERENCES property_companies(id),
    INDEX idx_merchant (merchant_id),
    INDEX idx_property_company (property_company_id),
    INDEX idx_audit_status (audit_status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商家积分购买表';

CREATE TABLE IF NOT EXISTS merchant_point_grants (
    id VARCHAR(32) PRIMARY KEY COMMENT 'mpg_xxxxxxxx',
    merchant_id VARCHAR(32) NOT NULL,
    purchase_id VARCHAR(32) NOT NULL COMMENT '关联购买记录',
    resident_id VARCHAR(32) NOT NULL COMMENT '接收用户',
    family_id VARCHAR(32) COMMENT '家庭ID',
    property_company_id VARCHAR(32) NOT NULL COMMENT '按住户所属物业',
    point_amount INT NOT NULL COMMENT '录入积分数量',
    exchange_rate DECIMAL(10,4) COMMENT '该物业兑换比',
    description VARCHAR(200),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (merchant_id) REFERENCES merchants(id),
    FOREIGN KEY (purchase_id) REFERENCES merchant_point_purchases(id),
    FOREIGN KEY (resident_id) REFERENCES residents(id),
    FOREIGN KEY (family_id) REFERENCES families(id),
    FOREIGN KEY (property_company_id) REFERENCES property_companies(id),
    INDEX idx_merchant (merchant_id),
    INDEX idx_resident (resident_id),
    INDEX idx_property_company (property_company_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商家积分录入表';

CREATE TABLE IF NOT EXISTS merchant_withdrawals (
    id VARCHAR(32) PRIMARY KEY COMMENT 'mw_xxxxxxxx',
    merchant_id VARCHAR(32) NOT NULL,
    property_company_id VARCHAR(32) NOT NULL,
    amount DECIMAL(10,2) NOT NULL COMMENT '提现金额',
    fee_rate DECIMAL(5,4) NOT NULL COMMENT '手续费率',
    fee_amount DECIMAL(10,2) NOT NULL COMMENT '手续费',
    actual_amount DECIMAL(10,2) NOT NULL COMMENT '实际到账',
    audit_status VARCHAR(20) DEFAULT 'pending' COMMENT 'pending/approved/rejected/completed',
    audited_at TIMESTAMP,
    auditor_id VARCHAR(32),
    completed_at TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (merchant_id) REFERENCES merchants(id),
    FOREIGN KEY (property_company_id) REFERENCES property_companies(id),
    INDEX idx_merchant (merchant_id),
    INDEX idx_property_company (property_company_id),
    INDEX idx_audit_status (audit_status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商家提现表';

CREATE TABLE IF NOT EXISTS coin_withdrawals (
    id VARCHAR(32) PRIMARY KEY,
    merchant_id VARCHAR(32) NOT NULL,
    property_company_id VARCHAR(32) NOT NULL,
    coin_amount DECIMAL(10,2) NOT NULL COMMENT '兑换物业币数量',
    cash_amount DECIMAL(10,2) NOT NULL COMMENT '兑换现金金额',
    fee_rate DECIMAL(5,4) NOT NULL,
    fee_amount DECIMAL(10,2) NOT NULL,
    actual_amount DECIMAL(10,2) NOT NULL,
    audit_status VARCHAR(20) DEFAULT 'pending',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (merchant_id) REFERENCES merchants(id),
    FOREIGN KEY (property_company_id) REFERENCES property_companies(id),
    INDEX idx_merchant (merchant_id),
    INDEX idx_property_company (property_company_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='物业币兑换现金表';

-- ============================================================
-- 文件模块
-- ============================================================

CREATE TABLE IF NOT EXISTS files (
    id VARCHAR(32) PRIMARY KEY COMMENT 'file_xxxxxxxx',
    original_name VARCHAR(200) NOT NULL,
    stored_name VARCHAR(200) NOT NULL,
    url VARCHAR(500) NOT NULL,
    thumbnail_url VARCHAR(500),
    mime_type VARCHAR(50),
    size BIGINT,
    category VARCHAR(30) COMMENT 'avatar/announcement/merchant/service/activity',
    uploader_id VARCHAR(32),
    property_company_id VARCHAR(32),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_uploader (uploader_id),
    INDEX idx_category (category),
    INDEX idx_property_company (property_company_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='文件表';

-- ============================================================
-- 操作日志模块
-- ============================================================

CREATE TABLE IF NOT EXISTS operation_logs (
    id VARCHAR(32) PRIMARY KEY,
    operator_id VARCHAR(32) NOT NULL,
    property_company_id VARCHAR(32),
    module VARCHAR(50) NOT NULL,
    action VARCHAR(50) NOT NULL,
    target_type VARCHAR(50),
    target_id VARCHAR(32),
    before_data JSON,
    after_data JSON,
    ip_address VARCHAR(50),
    user_agent VARCHAR(500),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (operator_id) REFERENCES residents(id),
    FOREIGN KEY (property_company_id) REFERENCES property_companies(id),
    INDEX idx_operator (operator_id),
    INDEX idx_property_company (property_company_id),
    INDEX idx_module (module),
    INDEX idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='操作日志表';

-- ============================================================
-- 数据初始化（测试环境）
-- 所有测试用户默认密码: admin123456
-- ============================================================

-- BCrypt hash for "admin123456"
-- $2b$10$36uecJYOkhVkyg3wc9BJLuDZWCpL9AgcgFNREFlE3JeoIbpL7jDJS

-- ============================================================
-- 物业公司 & 小区 & 积分池
-- ============================================================

INSERT IGNORE INTO property_companies (id, name, contact_phone, address, status,
    property_share_rate, coordinator_share_rate, sector_leader_rate, individual_leader_rate,
    delivery_base_fee, delivery_courier_per_order, withdrawal_fee_rate, point_to_fee_rate,
    coin_expiry_days, neighbor_daily_contact_limit)
VALUES
('pc_demo001', '华帝物业管理有限公司', '0427-8888888', '辽宁省盘锦市兴隆台区', 'active',
 0.3000, 0.7000, 0.1000, 0.0000,
 2.00, 0.35, 0.0060, 0.0100,
 365, 2);

INSERT IGNORE INTO communities (id, property_company_id, name, address, total_buildings, total_units, status) VALUES
('com_demo001', 'pc_demo001', '华帝·阳光花园', '辽宁省盘锦市兴隆台区阳光路1号', 10, 500, 'active'),
('com_demo002', 'pc_demo001', '华帝·翠湖名苑', '辽宁省盘锦市兴隆台区翠湖路88号', 8, 320, 'active');

INSERT IGNORE INTO point_pools (id, property_company_id, balance, total_in, total_out) VALUES
('pp_demo001', 'pc_demo001', 50000.00, 80000.00, 30000.00);

-- ============================================================
-- 用户数据（各角色）
-- ============================================================

INSERT IGNORE INTO residents (id, name, phone, password, role, user_type, property_company_id, community_id, building, unit, room, point_balance, coin_balance, status) VALUES
-- 平台管理员
('res_admin01', '平台管理员', '13800138000', '$2b$10$36uecJYOkhVkyg3wc9BJLuDZWCpL9AgcgFNREFlE3JeoIbpL7jDJS', 'platform_admin', 'owner', 'pc_demo001', NULL, NULL, NULL, NULL, 0, 0.00, 'active'),
-- 物业管理员
('res_propadmin01', '物业管理员-刘芳', '13900000001', '$2b$10$36uecJYOkhVkyg3wc9BJLuDZWCpL9AgcgFNREFlE3JeoIbpL7jDJS', 'property_admin', 'owner', 'pc_demo001', 'com_demo001', NULL, NULL, NULL, 0, 0.00, 'active'),
-- 统筹
('res_coo01', '统筹-张伟', '13900000002', '$2b$10$36uecJYOkhVkyg3wc9BJLuDZWCpL9AgcgFNREFlE3JeoIbpL7jDJS', 'coordinator', 'owner', 'pc_demo001', 'com_demo001', NULL, NULL, NULL, 1200, 150.00, 'active'),
-- 板块负责人
('res_sec01', '板块负责人-李娜', '13900000003', '$2b$10$36uecJYOkhVkyg3wc9BJLuDZWCpL9AgcgFNREFlE3JeoIbpL7jDJS', 'sector_leader', 'owner', 'pc_demo001', 'com_demo001', NULL, NULL, NULL, 800, 80.00, 'active'),
-- 个体负责人
('res_ind01', '个体负责人-王强', '13900000004', '$2b$10$36uecJYOkhVkyg3wc9BJLuDZWCpL9AgcgFNREFlE3JeoIbpL7jDJS', 'individual_leader', 'owner', 'pc_demo001', 'com_demo001', '1', '1', NULL, 500, 50.00, 'active'),
-- 业主1
('res_001', '业主-赵六', '13900000005', '$2b$10$36uecJYOkhVkyg3wc9BJLuDZWCpL9AgcgFNREFlE3JeoIbpL7jDJS', 'resident', 'owner', 'pc_demo001', 'com_demo001', '3', '2', '201', 3000, 200.00, 'active'),
-- 业主2
('res_002', '业主-钱七', '13900000006', '$2b$10$36uecJYOkhVkyg3wc9BJLuDZWCpL9AgcgFNREFlE3JeoIbpL7jDJS', 'resident', 'owner', 'pc_demo001', 'com_demo001', '3', '2', '202', 1500, 100.00, 'active'),
-- 业主3
('res_003', '业主-孙八', '13900000010', '$2b$10$36uecJYOkhVkyg3wc9BJLuDZWCpL9AgcgFNREFlE3JeoIbpL7jDJS', 'resident', 'owner', 'pc_demo001', 'com_demo002', '5', '1', '101', 2000, 150.00, 'active'),
-- 租户
('res_004', '租户-周九', '13900000007', '$2b$10$36uecJYOkhVkyg3wc9BJLuDZWCpL9AgcgFNREFlE3JeoIbpL7jDJS', 'resident', 'tenant', 'pc_demo001', 'com_demo001', '4', '3', '301', 500, 30.00, 'active'),
-- 商家
('res_mch01', '商家-陈老板', '13900000008', '$2b$10$36uecJYOkhVkyg3wc9BJLuDZWCpL9AgcgFNREFlE3JeoIbpL7jDJS', 'merchant', 'owner', 'pc_demo001', NULL, NULL, NULL, NULL, 0, 0.00, 'active'),
-- 快递员
('res_courier01', '快递员-吴十', '13900000009', '$2b$10$36uecJYOkhVkyg3wc9BJLuDZWCpL9AgcgFNREFlE3JeoIbpL7jDJS', 'courier', 'owner', 'pc_demo001', 'com_demo001', NULL, NULL, NULL, 200, 15.00, 'active'),
-- 活动组组长
('res_act01', '活动组长-郑美丽', '13900000011', '$2b$10$36uecJYOkhVkyg3wc9BJLuDZWCpL9AgcgFNREFlE3JeoIbpL7jDJS', 'activity_leader', 'owner', 'pc_demo001', 'com_demo001', '2', '1', '101', 600, 40.00, 'active'),
-- 技工
('res_tech01', '技工-刘师傅', '13900000012', '$2b$10$36uecJYOkhVkyg3wc9BJLuDZWCpL9AgcgFNREFlE3JeoIbpL7jDJS', 'technician', 'owner', 'pc_demo001', 'com_demo001', NULL, NULL, NULL, 300, 20.00, 'active');

-- ============================================================
-- 家庭数据
-- ============================================================

INSERT IGNORE INTO families (id, name, owner_id, property_company_id, community_id, building, unit, room, member_count, point_balance, coin_balance, audit_status, status) VALUES
('fam_001', '赵六家庭', 'res_001', 'pc_demo001', 'com_demo001', '3', '2', '201', 3, 5000, 300.00, 'approved', 'active'),
('fam_002', '钱七家庭', 'res_002', 'pc_demo001', 'com_demo001', '3', '2', '202', 2, 2500, 150.00, 'approved', 'active'),
('fam_003', '孙八家庭', 'res_003', 'pc_demo001', 'com_demo002', '5', '1', '101', 2, 3000, 200.00, 'approved', 'active');

-- 关联家庭成员
UPDATE residents SET family_id = 'fam_001', family_relation = 'owner' WHERE id = 'res_001';
UPDATE residents SET family_id = 'fam_002', family_relation = 'owner' WHERE id = 'res_002';
UPDATE residents SET family_id = 'fam_003', family_relation = 'owner' WHERE id = 'res_003';

-- ============================================================
-- 统筹 / 板块负责人 / 个体负责人关联
-- ============================================================

INSERT IGNORE INTO coordinators (id, resident_id, property_company_id, commission_rate, status) VALUES
('coo_001', 'res_coo01', 'pc_demo001', 0.7000, 'active');

INSERT IGNORE INTO sector_leaders (id, resident_id, property_company_id, sector, commission_rate, coordinator_id, status) VALUES
('sl_001', 'res_sec01', 'pc_demo001', 'cleaning_repair', 0.1000, 'coo_001', 'active'),
('sl_002', 'res_sec01', 'pc_demo001', 'life_service', 0.1000, 'coo_001', 'active');

INSERT IGNORE INTO individual_leaders (id, resident_id, property_company_id, sector_leader_id, sector, name, commission_rate, status) VALUES
('il_001', 'res_ind01', 'pc_demo001', 'sl_001', 'cleaning_repair', '王强家政', 0.0000, 'active');

-- ============================================================
-- 商家数据
-- ============================================================

-- 平台商家库
INSERT IGNORE INTO platform_merchants (id, name, description, address, contact_phone, category, business_hours, status) VALUES
('pm_restaurant01', '口福餐厅', '地道东北菜，食材新鲜，分量十足', '盘锦市兴隆台区步行街56号', '0427-6666666', '外卖小吃', '09:00-22:00', 'active'),
('pm_store01', '便民超市', '生活日用品、食品饮料一站式购物', '盘锦市兴隆台区阳光路10号', '0427-7777777', '周围商家', '08:00-21:00', 'active'),
('pm_farm01', '绿色农场直供', '有机蔬菜、土鸡蛋、散养鸡鸭', '盘锦市大洼区田家镇', '13700001111', '农家', '07:00-18:00', 'active');

-- 物业挂接商家
INSERT IGNORE INTO merchants (id, platform_merchant_id, property_company_id, name, description, category, merchant_level, level_weight, audit_status, rank_order, business_hours, contact_phone, address, delivery_fee, free_delivery_threshold, status) VALUES
('mch_demo001', 'pm_restaurant01', 'pc_demo001', '口福餐厅(华帝店)', '口福餐厅华帝阳光花园配送点，东北家常菜，午餐晚餐均可配送', '外卖小吃', 'property_certified', 10, 'approved', 1, '10:30-13:30, 17:00-20:30', '0427-6666666', '盘锦市兴隆台区步行街56号', 2.00, 20.00, 'active'),
('mch_demo002', 'pm_store01', 'pc_demo001', '便民超市(华帝店)', '小区门口便民超市，日用品、零食饮料应有尽有', '周围商家', 'normal', 5, 'approved', 2, '08:00-21:00', '0427-7777777', '盘锦市兴隆台区阳光路10号', 0.00, NULL, 'active'),
('mch_demo003', 'pm_farm001', 'pc_demo001', '绿色农场直供(华帝站)', '当日采摘当日配送，新鲜有机蔬菜直达小区', '农家', 'official_certified', 20, 'approved', 3, '07:00-12:00', '13700001111', '盘锦市大洼区田家镇', 3.00, 30.00, 'active');

-- 商家-物业参数配置
INSERT IGNORE INTO merchant_property_configs (id, merchant_id, property_company_id, commission_rate, point_exchange_rate, member_discount_price, coin_rebate_rate, coin_rebate_enabled, status) VALUES
('mpc_001', 'mch_demo001', 'pc_demo001', 0.1500, 100.0000, NULL, 0.0200, TRUE, 'active'),
('mpc_002', 'mch_demo002', 'pc_demo001', 0.1000, 200.0000, NULL, 0.0100, TRUE, 'active'),
('mpc_003', 'mch_demo003', 'pc_demo001', 0.2000, 50.0000, NULL, 0.0300, TRUE, 'active');

-- ============================================================
-- 商品数据
-- ============================================================

INSERT IGNORE INTO products (id, merchant_id, name, description, price, member_price, point_price, stock, category, status) VALUES
-- 口福餐厅菜品
('prd_001', 'mch_demo001', '宫保鸡丁', '经典川味宫保鸡丁，鸡肉鲜嫩，花生酥脆', 28.00, 25.00, 2500, 100, '热菜', 'active'),
('prd_002', 'mch_demo001', '红烧肉', '秘制红烧肉，肥而不腻，入口即化', 35.00, 32.00, 3000, 80, '热菜', 'active'),
('prd_003', 'mch_demo001', '东北拉皮', '地道东北大拉皮，酸爽开胃', 18.00, 16.00, 1500, 120, '凉菜', 'active'),
('prd_004', 'mch_demo001', '酸菜白肉血肠', '东北传统杀猪菜，满满家乡味', 48.00, 45.00, 4000, 50, '炖菜', 'active'),
('prd_005', 'mch_demo001', '蛋炒饭', '粒粒分明的蛋炒饭，简单美味', 12.00, 10.00, 800, 200, '主食', 'active'),
-- 便民超市商品
('prd_006', 'mch_demo002', '纸巾套装(3包)', '维达抽纸3包装，柔软亲肤', 15.00, 14.00, 1000, 300, '日用品', 'active'),
('prd_007', 'mch_demo002', '洗衣液(2kg)', '碧浪洗衣液2kg装，洁净留香', 39.90, 36.00, 3000, 150, '清洁用品', 'active'),
('prd_008', 'mch_demo002', '矿泉水(24瓶)', '农夫山泉550ml×24瓶', 28.00, 26.00, 2000, 200, '饮品', 'active'),
-- 绿色农场商品
('prd_009', 'mch_demo003', '有机小白菜(500g)', '当日采摘有机小白菜，新鲜翠绿', 8.00, 7.00, 500, 100, '蔬菜', 'active'),
('prd_010', 'mch_demo003', '土鸡蛋(30枚)', '散养土鸡蛋30枚装，蛋黄橙红', 38.00, 35.00, 2500, 80, '蛋类', 'active'),
('prd_011', 'mch_demo003', '有机西红柿(1kg)', '自然熟有机西红柿，酸甜多汁', 12.00, 10.00, 800, 120, '蔬菜', 'active');

-- ============================================================
-- 积分规则
-- ============================================================

INSERT IGNORE INTO point_rules (id, name, description, point_type, source, rate, min_amount, daily_limit, monthly_limit, property_company_id, status) VALUES
('prr_001', '消费获得积分', '每消费1元获得10积分', 'earn', 'order', 10.0000, 1.00, 5000, 50000, NULL, 'active'),
('prr_002', '每日签到', '每日签到获得5积分', 'earn', 'sign_in', NULL, NULL, 5, 150, NULL, 'active'),
('prr_003', '物业费抵扣', '100积分可抵扣1元物业费', 'spend', 'fee_deduct', 0.0100, NULL, NULL, NULL, NULL, 'active'),
('prr_004', '积分兑换商品', '积分按商品设定价格兑换', 'redeem', 'merchant_grant', NULL, NULL, NULL, NULL, NULL, 'active');

-- ============================================================
-- 物业币规则
-- ============================================================

INSERT IGNORE INTO property_coin_rules (id, name, description, coin_type, source, rate, property_company_id, status) VALUES
('pcr_001', '消费返物业币', '消费金额按2%返物业币', 'earn', 'order', 0.0200, NULL, 'active'),
('pcr_002', '物业币抵扣', '物业币可直接抵扣消费', 'spend', 'order', 1.0000, NULL, 'active');

-- ============================================================
-- 物业费规则
-- ============================================================

INSERT IGNORE INTO property_fee_rules (id, name, fee_type, calculation_method, unit_price, unit, billing_cycle, auto_deduct_enabled, overdue_rate, property_company_id, status) VALUES
('pfr_001', '物业管理费', 'property', 'per_square_meter', 1.50, '元/平方米/月', 'monthly', FALSE, 0.0005, 'pc_demo001', 'active'),
('pfr_002', '停车费', 'parking', 'fixed', 200.00, '元/月', 'monthly', FALSE, 0.0003, 'pc_demo001', 'active'),
('pfr_003', '水费', 'water', 'per_unit', 4.50, '元/吨', 'monthly', FALSE, NULL, 'pc_demo001', 'active'),
('pfr_004', '电费', 'electricity', 'per_unit', 0.56, '元/度', 'monthly', FALSE, NULL, 'pc_demo001', 'active');

-- ============================================================
-- 物业费账单（示例）
-- ============================================================

INSERT IGNORE INTO property_fees (id, resident_id, family_id, property_company_id, community_id, fee_type, period, amount, paid_amount, payment_status, due_date) VALUES
('pf_001', 'res_001', 'fam_001', 'pc_demo001', 'com_demo001', 'property', '2026-Q3', 360.00, 360.00, 'paid', '2026-07-31'),
('pf_002', 'res_001', 'fam_001', 'pc_demo001', 'com_demo001', 'parking', '2026-Q3', 200.00, 0.00, 'unpaid', '2026-07-31'),
('pf_003', 'res_002', 'fam_002', 'pc_demo001', 'com_demo001', 'property', '2026-Q3', 360.00, 180.00, 'partial', '2026-07-31'),
('pf_004', 'res_003', 'fam_003', 'pc_demo001', 'com_demo002', 'property', '2026-Q3', 300.00, 0.00, 'unpaid', '2026-07-31');

-- ============================================================
-- 社区服务
-- ============================================================

INSERT IGNORE INTO community_services (id, name, description, category, price, member_price, price_unit, provider_id, provider_type, property_company_id, rank_order, is_subscription, status) VALUES
('svc_001', '家政清洁', '专业家政团队，上门清洁服务，含擦窗、拖地、厨房卫生间深度清洁', 'cleaning_repair', 80.00, 65.00, '次', 'res_ind01', 'individual_leader', 'pc_demo001', 1, FALSE, 'active'),
('svc_002', '老年陪护', '专业护理人员上门陪护，含日常起居照料、健康监测', 'elderly_care', 150.00, 120.00, '小时', NULL, NULL, 'pc_demo001', 2, TRUE, 'active'),
('svc_003', '家电维修', '空调、冰箱、洗衣机等家用电器维修', 'cleaning_repair', 50.00, 40.00, '次', NULL, NULL, 'pc_demo001', 3, FALSE, 'active'),
('svc_004', '社区食堂', '健康营养午餐，荤素搭配，每日更新菜单', 'canteen', 15.00, 12.00, '份', NULL, NULL, 'pc_demo001', 4, FALSE, 'active'),
('svc_005', '宠物托管', '专业宠物看护，含喂食、遛弯、清洁', 'pet_medical', 50.00, 40.00, '天', NULL, NULL, 'pc_demo001', 5, FALSE, 'active');

-- ============================================================
-- 积分记录（示例交易流水）
-- ============================================================

INSERT IGNORE INTO point_records (id, resident_id, family_id, property_company_id, point_type, amount, balance, source, description, created_at) VALUES
('pt_001', 'res_001', 'fam_001', 'pc_demo001', 'earn', 500, 500, 'order', '订单消费获得积分', '2026-06-01 12:00:00'),
('pt_002', 'res_001', 'fam_001', 'pc_demo001', 'earn', 200, 700, 'sign_in', '连续签到奖励', '2026-06-02 08:30:00'),
('pt_003', 'res_001', 'fam_001', 'pc_demo001', 'spend', -100, 600, 'fee_deduct', '物业费积分抵扣', '2026-06-05 10:00:00'),
('pt_004', 'res_001', 'fam_001', 'pc_demo001', 'earn', 1000, 1600, 'order', '大额订单奖励', '2026-06-10 18:00:00'),
('pt_005', 'res_001', 'fam_001', 'pc_demo001', 'earn', 500, 2100, 'order', '订单消费获得积分', '2026-06-15 12:30:00'),
('pt_006', 'res_002', 'fam_002', 'pc_demo001', 'earn', 300, 300, 'sign_in', '签到奖励', '2026-06-01 09:00:00'),
('pt_007', 'res_002', 'fam_002', 'pc_demo001', 'earn', 500, 800, 'order', '订单消费获得积分', '2026-06-08 19:00:00'),
('pt_008', 'res_003', 'fam_003', 'pc_demo001', 'earn', 400, 400, 'order', '首次下单奖励', '2026-06-05 11:00:00'),
('pt_009', 'res_003', 'fam_003', 'pc_demo001', 'earn', 600, 1000, 'order', '订单消费获得积分', '2026-06-12 14:00:00');

-- ============================================================
-- 物业币记录（示例交易流水）
-- ============================================================

INSERT IGNORE INTO property_coin_records (id, resident_id, family_id, property_company_id, coin_type, amount, balance, source, description, created_at) VALUES
('coin_001', 'res_001', 'fam_001', 'pc_demo001', 'earn', 50.00, 50.00, 'order', '订单消费返物业币', '2026-06-01 12:00:00'),
('coin_002', 'res_001', 'fam_001', 'pc_demo001', 'earn', 30.00, 80.00, 'order', '订单消费返物业币', '2026-06-10 18:00:00'),
('coin_003', 'res_001', 'fam_001', 'pc_demo001', 'spend', -20.00, 60.00, 'order', '物业币抵扣消费', '2026-06-15 12:30:00'),
('coin_004', 'res_002', 'fam_002', 'pc_demo001', 'earn', 20.00, 20.00, 'order', '订单消费返物业币', '2026-06-08 19:00:00');

-- ============================================================
-- 权限池（34个核心权限）
-- ============================================================

INSERT IGNORE INTO permissions (id, code, name, module) VALUES
('perm_001', 'announcement.publish', '发布公告', 'announcement'),
('perm_002', 'merchant.audit', '商家审核', 'merchant'),
('perm_003', 'merchant.kickout', '踢出商家', 'merchant'),
('perm_004', 'rank.drag', '排名拖拽', 'rank'),
('perm_005', 'promotion.push', '特惠推送', 'promotion'),
('perm_006', 'activity_group.manage', '活动组管理', 'activity_group'),
('perm_007', 'sector.manage', '板块管理', 'sector'),
('perm_008', 'individual.manage', '个体负责人管理', 'individual'),
('perm_009', 'delivery.grab', '送货抢单', 'delivery'),
('perm_010', 'data.view', '数据查看', 'data'),
('perm_011', 'notice.collect', '通知征集', 'notice'),
('perm_012', 'refund.approve', '退费审批', 'refund'),
('perm_013', 'points.input', '积分录入', 'points'),
('perm_014', 'coin.freeze', '物业币冻结', 'coin'),
('perm_015', 'coin.display_control', '物业币显示控制', 'coin'),
('perm_016', 'config.set', '参数配置', 'config'),
('perm_017', 'property_fee.manage', '物业费管理', 'property_fee'),
('perm_018', 'reports.view', '报表查看', 'reports'),
('perm_019', 'user.manage', '用户管理', 'user'),
('perm_020', 'permission.manage', '权限管理', 'permission'),
('perm_021', 'permission.read', '权限查看', 'permission'),
('perm_022', 'pool.read', '积分池查看', 'pool'),
('perm_023', 'pool.adjust', '积分池调整', 'pool'),
('perm_024', 'coordinator.read', '统筹查看', 'coordinator'),
('perm_025', 'coordinator.manage', '统筹管理', 'coordinator'),
('perm_026', 'sector.read', '板块负责人查看', 'sector'),
('perm_027', 'distribution.read', '分成记录查看', 'distribution'),
('perm_028', 'coin.audit', '物业币兑换审核', 'coin'),
('perm_029', 'point.clear_run', '2年积分清零', 'points'),
('perm_030', 'log.read', '操作日志查看', 'log'),
('perm_031', 'merchant.create', '创建平台商家', 'merchant'),
('perm_032', 'merchant.read', '平台商家查看', 'merchant'),
('perm_033', 'merchant.update', '更新平台商家', 'merchant'),
('perm_034', 'property_company.manage', '物业公司管理', 'property_company');

-- ============================================================
-- 角色快捷预设（6个）
-- ============================================================

INSERT IGNORE INTO role_presets (id, name, code, description) VALUES
('rp_001', '统筹', 'coordinator', '统筹标准权限包'),
('rp_002', '板块负责人', 'sector_leader', '板块负责人标准权限包'),
('rp_003', '活动组组长', 'activity_leader', '组长标准权限包'),
('rp_004', '快递员', 'courier', '快递员标准权限包'),
('rp_005', '社区管理员', 'community_admin', '社区标准权限包'),
('rp_006', '物业员工', 'property_staff', '物业内部人员全权限');

-- 统筹预设权限
INSERT IGNORE INTO role_preset_permissions (id, preset_id, permission_id) VALUES
('rpp_001', 'rp_001', 'perm_001'),
('rpp_002', 'rp_001', 'perm_004'),
('rpp_003', 'rp_001', 'perm_005'),
('rpp_004', 'rp_001', 'perm_006'),
('rpp_005', 'rp_001', 'perm_007'),
('rpp_006', 'rp_001', 'perm_002');

-- 板块负责人预设权限
INSERT IGNORE INTO role_preset_permissions (id, preset_id, permission_id) VALUES
('rpp_007', 'rp_002', 'perm_008'),
('rpp_008', 'rp_002', 'perm_006'),
('rpp_009', 'rp_002', 'perm_005');

-- 快递员预设权限
INSERT IGNORE INTO role_preset_permissions (id, preset_id, permission_id) VALUES
('rpp_010', 'rp_004', 'perm_009');

-- 社区管理员预设权限
INSERT IGNORE INTO role_preset_permissions (id, preset_id, permission_id) VALUES
('rpp_011', 'rp_005', 'perm_001'),
('rpp_012', 'rp_005', 'perm_011'),
('rpp_013', 'rp_005', 'perm_019'),
('rpp_014', 'rp_005', 'perm_010'),
('rpp_015', 'rp_005', 'perm_017');

-- 物业员工预设权限（全权限）
INSERT IGNORE INTO role_preset_permissions (id, preset_id, permission_id) VALUES
('rpp_016', 'rp_006', 'perm_001'),
('rpp_017', 'rp_006', 'perm_002'),
('rpp_018', 'rp_006', 'perm_003'),
('rpp_019', 'rp_006', 'perm_004'),
('rpp_020', 'rp_006', 'perm_005'),
('rpp_021', 'rp_006', 'perm_006'),
('rpp_022', 'rp_006', 'perm_007'),
('rpp_023', 'rp_006', 'perm_008'),
('rpp_024', 'rp_006', 'perm_009'),
('rpp_025', 'rp_006', 'perm_010'),
('rpp_026', 'rp_006', 'perm_011'),
('rpp_027', 'rp_006', 'perm_012'),
('rpp_028', 'rp_006', 'perm_013'),
('rpp_029', 'rp_006', 'perm_014'),
('rpp_030', 'rp_006', 'perm_015'),
('rpp_031', 'rp_006', 'perm_016'),
('rpp_032', 'rp_006', 'perm_017'),
('rpp_033', 'rp_006', 'perm_018'),
('rpp_034', 'rp_006', 'perm_019'),
('rpp_035', 'rp_006', 'perm_020'),
('rpp_036', 'rp_006', 'perm_021'),
('rpp_037', 'rp_006', 'perm_022'),
('rpp_038', 'rp_006', 'perm_023'),
('rpp_039', 'rp_006', 'perm_024'),
('rpp_040', 'rp_006', 'perm_025'),
('rpp_041', 'rp_006', 'perm_026'),
('rpp_042', 'rp_006', 'perm_027'),
('rpp_043', 'rp_006', 'perm_028'),
('rpp_044', 'rp_006', 'perm_029'),
('rpp_045', 'rp_006', 'perm_030'),
('rpp_046', 'rp_006', 'perm_031'),
('rpp_047', 'rp_006', 'perm_032'),
('rpp_048', 'rp_006', 'perm_033'),
('rpp_049', 'rp_006', 'perm_034');

-- ============================================================
-- 公告数据
-- ============================================================

INSERT IGNORE INTO announcements (id, title, content, announcement_type, property_company_id, community_id, publisher_id, publisher_role, status, published_at) VALUES
('ann_001', '关于2026年7月物业费缴纳通知', '尊敬的业主：2026年第三季度物业费已出账，请于7月31日前完成缴纳。支持微信、物业币、积分抵扣等多种方式。', 'property', 'pc_demo001', NULL, 'res_propadmin01', 'property_admin', 'published', '2026-07-01 09:00:00'),
('ann_002', '阳光花园小区停水通知', '因自来水管道检修，7月5日9:00-17:00暂停供水，请提前储水。给您带来不便，敬请谅解。', 'community', 'pc_demo001', 'com_demo001', 'res_propadmin01', 'property_admin', 'published', '2026-07-03 08:00:00'),
('ann_003', '社区夏令营活动报名开始', '暑期社区青少年夏令营开始报名！包含书法、绘画、编程等课程，物业币抵扣50%。名额有限，先到先得。', 'activity', 'pc_demo001', NULL, 'res_coo01', 'coordinator', 'published', '2026-07-02 10:00:00');

-- ============================================================
-- 特惠推送模块
-- ============================================================

CREATE TABLE IF NOT EXISTS special_offers (
    id VARCHAR(32) PRIMARY KEY COMMENT 'so_xxxxxxxx',
    title VARCHAR(200) NOT NULL COMMENT '标题',
    content TEXT NOT NULL COMMENT '内容',
    target_type VARCHAR(30) NOT NULL COMMENT '推送对象类型：all/tag/building/role',
    target_tags JSON COMMENT '推送标签（JSON数组）',
    publisher_id VARCHAR(32) NOT NULL COMMENT '发布者ID',
    publisher_role VARCHAR(30) NOT NULL COMMENT '发布者角色',
    property_company_id VARCHAR(32) NOT NULL COMMENT '物业公司ID',
    community_id VARCHAR(32) COMMENT '小区ID',
    merchant_id VARCHAR(32) COMMENT '商家ID',
    cover_url VARCHAR(500) COMMENT '封面图片',
    discount_info VARCHAR(500) COMMENT '优惠信息',
    start_time TIMESTAMP NULL COMMENT '开始时间',
    end_time TIMESTAMP NULL COMMENT '结束时间',
    status VARCHAR(20) DEFAULT 'draft' COMMENT '状态：draft/published/ended/archived',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_special_offers_property (property_company_id),
    INDEX idx_special_offers_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='特惠推送表';

SET FOREIGN_KEY_CHECKS = 1;

-- ============================================================
-- 部署完成
-- ============================================================
-- 数据库: wuye_points
-- 应用账号: wuye_app / wuye@2026
-- 表数量: 49 张
-- 初始数据:
--   物业公司: 1家
--   小区: 2个
--   用户: 12人（平台管理员1 + 物业管理员1 + 统筹1 + 板块负责人1 + 个体负责人1 + 业主3 + 租户1 + 商家1 + 快递员1 + 活动组长1）
--   家庭: 3个
--   商家: 平台商家3家 + 挂接商家3家
--   商品: 11个
--   积分规则: 4条
--   物业币规则: 2条
--   物业费规则: 4条
--   物业费账单: 4条
--   社区服务: 5个
--   积分记录: 9条
--   物业币记录: 4条
--   公告: 3条
--   权限: 34个
--   角色预设: 6个（含完整权限关联）
-- 管理员: 13800138000 / admin123456
-- ============================================================
