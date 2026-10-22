-- =====================================================================
-- V25 业主小店店铺资料 + 组长标签化 + 论坛影子封禁（2026-10-21）
--
-- A. 需求1 业主小店店铺资料对齐普通商家：
--   * resident_merchant_applications 增加 description / business_hours /
--     contact_phone / cover_url（shop_name 已在 V22 增加，本版本不重复）；
--
-- B. 需求2 活动组组长标签化 + 申请流程：
--   * 新表 activity_leader_identities（组长身份，住户子身份/标签）；
--   * 新表 activity_leader_applications（组长申请，管理端审核）；
--   * 存量迁移：residents.role='activity_leader' → 建 active 身份记录 + role 归一为 resident；
--
-- C. 需求3 论坛影子封禁：
--   * community_posts / community_comments 增加 shadow_banned 打标列；
--   * 新表 resident_forum_bans（禁言记录，支持 expires_at 到期自动解除）；
--
-- 本脚本为手工执行脚本（application-prod.yml 中 flyway.enabled=false）。
-- 执行方式：mysql -u<user> -p <db> < V25__shop_profile_leader_tag_forum_ban.sql
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
-- A) 业主小店店铺资料
-- ---------------------------------------------------------------------
CALL p_wuye_add_column('resident_merchant_applications', 'description',
                       'description VARCHAR(500) NULL COMMENT ''店铺简介（对齐普通商家）''');
CALL p_wuye_add_column('resident_merchant_applications', 'business_hours',
                       'business_hours VARCHAR(50) NULL COMMENT ''营业时间（如 08:00-22:00）''');
CALL p_wuye_add_column('resident_merchant_applications', 'contact_phone',
                       'contact_phone VARCHAR(20) NULL COMMENT ''联系电话（不传回退店主手机号）''');
CALL p_wuye_add_column('resident_merchant_applications', 'cover_url',
                       'cover_url VARCHAR(500) NULL COMMENT ''店铺封面图URL''');

-- ---------------------------------------------------------------------
-- B1) 活动组组长身份表（标签）
-- ---------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `activity_leader_identities` (
  `id` varchar(32) NOT NULL COMMENT 'ali_xxxxxxxx',
  `resident_id` varchar(32) NOT NULL,
  `property_company_id` varchar(32) NOT NULL,
  `source` varchar(20) DEFAULT NULL COMMENT 'appoint=管理端任命 / application=申请审核通过',
  `application_id` varchar(32) DEFAULT NULL COMMENT '来源为 application 时的申请记录ID',
  `status` varchar(20) DEFAULT 'active' COMMENT 'active/revoked',
  `created_by` varchar(32) DEFAULT NULL,
  `revoked_by` varchar(32) DEFAULT NULL,
  `revoked_at` datetime DEFAULT NULL,
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_ali_resident` (`resident_id`,`status`),
  KEY `idx_ali_property` (`property_company_id`,`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='活动组组长身份表（住户子身份/标签）';

-- ---------------------------------------------------------------------
-- B2) 活动组组长申请表
-- ---------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `activity_leader_applications` (
  `id` varchar(32) NOT NULL COMMENT 'ala_xxxxxxxx',
  `resident_id` varchar(32) NOT NULL COMMENT '申请人住户ID',
  `property_company_id` varchar(32) NOT NULL,
  `community_id` varchar(32) DEFAULT NULL,
  `activity_field` varchar(50) DEFAULT NULL COMMENT '擅长领域/活动类型',
  `remark` varchar(200) DEFAULT NULL COMMENT '自我介绍/备注',
  `audit_status` varchar(20) NOT NULL DEFAULT 'pending' COMMENT 'pending/approved/rejected',
  `reject_reason` varchar(200) DEFAULT NULL,
  `activity_leader_identity_id` varchar(32) DEFAULT NULL COMMENT '审核通过后创建的组长身份ID',
  `audited_by` varchar(32) DEFAULT NULL,
  `audited_at` datetime DEFAULT NULL,
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_ala_resident` (`resident_id`),
  KEY `idx_ala_property` (`property_company_id`),
  KEY `idx_ala_status` (`audit_status`),
  KEY `idx_ala_property_status` (`property_company_id`,`audit_status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='活动组组长申请表';

-- ---------------------------------------------------------------------
-- B3) 存量迁移：role=activity_leader → 身份记录 + role 归一为 resident
--     幂等：仅对没有 active 身份记录的组长账号执行
-- ---------------------------------------------------------------------
INSERT INTO activity_leader_identities
    (id, resident_id, property_company_id, source, status, created_by, created_at)
SELECT CONCAT('ali_mig_', SUBSTRING(r.id, 1, 24)), r.id, r.property_company_id,
       'appoint', 'active', 'system', NOW()
FROM residents r
WHERE r.role = 'activity_leader'
  AND r.property_company_id IS NOT NULL
  AND NOT EXISTS (
      SELECT 1 FROM activity_leader_identities i
      WHERE i.resident_id = r.id AND i.property_company_id = r.property_company_id AND i.status = 'active'
  );

UPDATE residents
SET role = 'resident'
WHERE role = 'activity_leader';

-- ---------------------------------------------------------------------
-- C1) 论坛影子封禁打标列
-- ---------------------------------------------------------------------
CALL p_wuye_add_column('community_posts', 'shadow_banned',
                       'shadow_banned TINYINT(1) NOT NULL DEFAULT 0 COMMENT ''影子封禁：禁言作者的内容仅本人与管理端可见''');
CALL p_wuye_add_column('community_comments', 'shadow_banned',
                       'shadow_banned TINYINT(1) NOT NULL DEFAULT 0 COMMENT ''影子封禁：禁言作者的评论仅本人与管理端可见''');

-- ---------------------------------------------------------------------
-- C2) 论坛禁言记录表
-- ---------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `resident_forum_bans` (
  `id` varchar(32) NOT NULL COMMENT 'rfb_xxxxxxxx',
  `resident_id` varchar(32) NOT NULL,
  `property_company_id` varchar(32) NOT NULL,
  `community_id` varchar(32) DEFAULT NULL COMMENT '禁言时所在小区（留痕）',
  `reason` varchar(200) DEFAULT NULL COMMENT '禁言原因',
  `banned_by` varchar(32) DEFAULT NULL COMMENT '操作管理员住户ID',
  `banned_at` datetime DEFAULT NULL,
  `expires_at` datetime DEFAULT NULL COMMENT '到期时间；NULL=永久禁言',
  `status` varchar(20) DEFAULT 'active' COMMENT 'active/lifted/expired',
  `lifted_by` varchar(32) DEFAULT NULL,
  `lifted_at` datetime DEFAULT NULL,
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_rfb_resident` (`resident_id`,`status`),
  KEY `idx_rfb_property` (`property_company_id`,`status`),
  KEY `idx_rfb_expiry` (`status`,`expires_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='社区论坛禁言记录表（影子封禁）';

-- ---------------------------------------------------------------------
-- 清理工具存储过程
-- ---------------------------------------------------------------------
DROP PROCEDURE IF EXISTS p_wuye_add_column;
