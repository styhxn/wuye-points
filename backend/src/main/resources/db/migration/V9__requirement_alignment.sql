-- =============================================================================
-- V9: 需求对齐合并迁移（大体逻辑.docx）
-- 说明：本文件为 V9~V12 的合并版，一次执行即可完成全部需求对齐变更。
-- 若数据库已单独执行过 V9~V12 中的某些文件，请勿重复执行本文件。
-- =============================================================================

-- -----------------------------------------------------------------------------
-- 1) 奖励金/积分发放审批工作流（原 V9）
-- -----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `reward_grant_applications` (
    `id`                   varchar(32)  NOT NULL,
    `applicant_id`         varchar(32)  NOT NULL COMMENT '申请人ID',
    `applicant_role`       varchar(20)  NOT NULL COMMENT '申请人角色 resident/merchant',
    `target_resident_id`   varchar(32)  NOT NULL COMMENT '目标住户ID(发放对象)',
    `target_resident_name` varchar(50)  NULL COMMENT '目标住户姓名快照',
    `grant_type`           varchar(20)  NOT NULL DEFAULT 'points' COMMENT '发放类型 points(积分)/reward(奖励金)',
    `point_amount`         int          NOT NULL COMMENT '发放数量',
    `attribution_mode`     varchar(20)  NOT NULL COMMENT '归属模式 property(到物业公司账号)/resident(业主使用)',
    `reason`               varchar(255) NULL COMMENT '申请理由',
    `audit_status`         varchar(20)  NOT NULL COMMENT 'pending_audit/approved/rejected',
    `approver_id`          varchar(32)  NULL COMMENT '审批人ID',
    `approver_remark`      varchar(255) NULL COMMENT '审批备注',
    `audited_at`           datetime     NULL COMMENT '审批时间',
    `property_company_id`  varchar(32)  NOT NULL COMMENT '物业公司ID',
    `created_at`           datetime     NULL,
    `updated_at`           datetime     NULL,
    PRIMARY KEY (`id`),
    KEY `idx_rga_applicant` (`applicant_id`),
    KEY `idx_rga_pc_status` (`property_company_id`, `audit_status`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '奖励金/积分发放申请';

-- -----------------------------------------------------------------------------
-- 2) 商家归属个体负责人 + 商家分成比例（原 V10）
-- -----------------------------------------------------------------------------
ALTER TABLE `merchants`
    ADD COLUMN `individual_leader_id` varchar(32) NULL COMMENT '归属个体负责人ID' AFTER `recommended_sort`,
    ADD COLUMN `individual_leader_rate` decimal(5,4) NULL COMMENT '个体负责人分成比例(0~1)' AFTER `individual_leader_id`;

ALTER TABLE `merchants` ADD INDEX `idx_merchants_il` (`individual_leader_id`);

-- -----------------------------------------------------------------------------
-- 3) 导航栏配置 / 物业银行卡 / 区域配额（原 V11）
-- -----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `navigation_items` (
    `id`                  varchar(32)  NOT NULL,
    `position`            varchar(20)  NOT NULL COMMENT 'bottom/top',
    `module`              varchar(30)  NOT NULL DEFAULT 'home' COMMENT '模块 home/service/activity/resident_merchant',
    `title`               varchar(50)  NOT NULL,
    `icon`                varchar(200) NULL,
    `link_type`           varchar(20)  NOT NULL COMMENT 'page/tab/h5/none',
    `link_url`            varchar(200) NULL,
    `sort_order`          int          NOT NULL,
    `enabled`             tinyint(1)   NOT NULL,
    `visible_roles`       varchar(200) NULL COMMENT '可见角色，逗号分隔',
    `property_company_id` varchar(32)  NULL COMMENT '空=平台级默认',
    `created_at`          datetime     NULL,
    `updated_at`          datetime     NULL,
    PRIMARY KEY (`id`),
    KEY `idx_nav_pc` (`property_company_id`, `enabled`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '手机端导航栏配置';

CREATE TABLE IF NOT EXISTS `property_bank_cards` (
    `id`                  varchar(32)  NOT NULL,
    `property_company_id` varchar(32)  NOT NULL,
    `bank_name`           varchar(100) NOT NULL,
    `branch_name`         varchar(100) NULL,
    `account_name`        varchar(50)  NOT NULL,
    `account_no`          varchar(50)  NOT NULL,
    `purpose`             varchar(20)  NOT NULL DEFAULT 'points' COMMENT '用途 points(积分钱)/reward(奖励金)',
    `is_default`          tinyint(1)   NOT NULL,
    `status`              varchar(20)  NOT NULL,
    `remark`              varchar(200) NULL,
    `created_at`          datetime     NULL,
    `updated_at`          datetime     NULL,
    PRIMARY KEY (`id`),
    KEY `idx_pbc_pc` (`property_company_id`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '物业公司收款银行卡';

CREATE TABLE IF NOT EXISTS `region_quotas` (
    `id`                  varchar(32)   NOT NULL,
    `property_company_id` varchar(32)   NOT NULL,
    `sector`              varchar(50)   NOT NULL,
    `quota_type`          varchar(20)   NOT NULL COMMENT 'point/amount',
    `quota_amount`        decimal(12,2) NOT NULL,
    `used_amount`         decimal(12,2) NOT NULL,
    `period_start`        date          NOT NULL,
    `period_end`          date          NOT NULL,
    `status`              varchar(20)   NOT NULL,
    `remark`              varchar(200)  NULL,
    `created_at`          datetime      NULL,
    `updated_at`          datetime      NULL,
    PRIMARY KEY (`id`),
    KEY `idx_rq_pc_sector` (`property_company_id`, `sector`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '区域/板块额度配额';

-- -----------------------------------------------------------------------------
-- 4) 需求对齐字段（原 V12）
-- -----------------------------------------------------------------------------
-- 归属开关按物业公司设置（积分/奖励金分别配置）
ALTER TABLE `property_companies`
    ADD COLUMN `points_attribution_mode` varchar(20) NULL COMMENT '积分归属 property(到物业公司账号)/resident(业主使用,钱到平台)' AFTER `coin_enabled`,
    ADD COLUMN `reward_attribution_mode` varchar(20) NULL COMMENT '奖励金归属 property(到物业公司账号)/resident(业主使用,需物业同意)' AFTER `points_attribution_mode`;

-- 一级经销商商品服务区间（本市/小区）
ALTER TABLE `distributor_products`
    ADD COLUMN `service_scope` varchar(20) NULL COMMENT '服务区间 city(本市)/community(小区)' AFTER `billing_cycle`;

-- 推荐产品
ALTER TABLE `products`
    ADD COLUMN `is_recommended` tinyint(1) NULL COMMENT '是否平台/物业推荐' AFTER `deleted_at`,
    ADD COLUMN `recommended_sort` int NULL COMMENT '推荐排序，越大越靠前' AFTER `is_recommended`;

-- 业主商户卖货：订单关联上架记录
ALTER TABLE `orders`
    ADD COLUMN `listing_id` varchar(32) NULL COMMENT '业主商户上架记录ID(来自 resident_merchant_listings)' AFTER `version`;
