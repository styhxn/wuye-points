-- =====================================================================
-- V38 微信进件表列宽对齐（2026-11-05 / v8.39）——【仅作留档】
--
-- 生产 spring.flyway.enabled=false（application-prod.yml 硬编码），本文件
-- 不参与服务器部署；实际交付路径是项目 sql/ 目录下的单文件手工脚本：
--   sql/manual_upgrade_20261105_applyment_column_width.sql
--
-- 线上故障：merchant_applyments.bank_account_type varchar(20) 存不下
--   BANK_ACCOUNT_TYPE_CORPORATE(27 字符)，保存草稿 INSERT 被 MySQL 拒绝，
--   兜底成 409「数据冲突，请刷新后重试」。
--
-- 本脚本只扩不缩、不改列名/类型/非空/默认值/注释，不动数据；
-- 仅在当前长度小于目标值时 ALTER，可重复执行。
-- 配套：MerchantApplyment 的 @Column(length) 与 ApplymentSubmitRequest 的
-- @Size(max) 已同步，GlobalExceptionHandler 对 Data truncation 改可读文案。
-- =====================================================================

DROP PROCEDURE IF EXISTS p_wuye_widen_varchar;

DELIMITER //

CREATE PROCEDURE p_wuye_widen_varchar(
    IN p_table   VARCHAR(64),
    IN p_column  VARCHAR(64),
    IN p_min_len INT,
    IN p_ddl     VARCHAR(2000))
BEGIN
    IF EXISTS (
        SELECT 1
        FROM information_schema.COLUMNS
        WHERE TABLE_SCHEMA = DATABASE()
          AND TABLE_NAME = p_table
          AND COLUMN_NAME = p_column
          AND DATA_TYPE = 'varchar'
          AND CHARACTER_MAXIMUM_LENGTH < p_min_len
    ) THEN
        SET @ddl = CONCAT('ALTER TABLE `', p_table, '` MODIFY COLUMN ', p_ddl);
        PREPARE stmt FROM @ddl;
        EXECUTE stmt;
        DEALLOCATE PREPARE stmt;
    END IF;
END //

DELIMITER ;

CALL p_wuye_widen_varchar('merchant_applyments', 'bank_account_type', 32,
    "`bank_account_type` varchar(32) DEFAULT NULL COMMENT '结算账户类型：BANK_ACCOUNT_TYPE_CORPORATE(对公)/BANK_ACCOUNT_TYPE_PERSONAL(个人)'");

CALL p_wuye_widen_varchar('merchant_applyments', 'organization_type', 32,
    "`organization_type` varchar(32) DEFAULT NULL COMMENT '主体类型：ENTERPRISE/INDIVIDUAL/MICRO(小微)/GOVERNMENT'");
CALL p_wuye_widen_varchar('merchant_applyments', 'sales_scenes_type', 64,
    "`sales_scenes_type` varchar(64) DEFAULT NULL COMMENT '经营场景：SALES_SCENES_STORE/MINI_PROGRAM/MP等'");
CALL p_wuye_widen_varchar('merchant_applyments', 'contact_type', 32,
    "`contact_type` varchar(32) DEFAULT NULL COMMENT '超管类型：LEGAL法人'");
CALL p_wuye_widen_varchar('merchant_applyments', 'business_code', 124,
    "`business_code` varchar(124) DEFAULT NULL COMMENT '业务申请编号（每次重提必须更换）'");

CALL p_wuye_widen_varchar('merchant_applyments', 'license_period_begin', 32,
    "`license_period_begin` varchar(32) DEFAULT NULL COMMENT '营业执照有效期开始'");
CALL p_wuye_widen_varchar('merchant_applyments', 'license_period_end', 32,
    "`license_period_end` varchar(32) DEFAULT NULL COMMENT '营业执照有效期结束'");
CALL p_wuye_widen_varchar('merchant_applyments', 'id_card_period_begin', 32,
    "`id_card_period_begin` varchar(32) DEFAULT NULL COMMENT '身份证有效期开始'");
CALL p_wuye_widen_varchar('merchant_applyments', 'id_card_period_end', 32,
    "`id_card_period_end` varchar(32) DEFAULT NULL COMMENT '身份证有效期结束'");

CALL p_wuye_widen_varchar('merchant_applyments', 'biz_address_code', 32,
    "`biz_address_code` varchar(32) DEFAULT NULL COMMENT '门店省市编码'");
CALL p_wuye_widen_varchar('merchant_applyments', 'bank_alias_code', 64,
    "`bank_alias_code` varchar(64) DEFAULT NULL COMMENT '银行别名编码（查询支行用）'");
CALL p_wuye_widen_varchar('merchant_applyments', 'bank_branch_id', 255,
    "`bank_branch_id` varchar(255) DEFAULT NULL COMMENT '支行联行号bank_branch_id'");

CALL p_wuye_widen_varchar('merchant_applyments', 'business_license_media', 255,
    "`business_license_media` varchar(255) DEFAULT NULL COMMENT '营业执照 media_id（小微可空）'");
CALL p_wuye_widen_varchar('merchant_applyments', 'id_card_copy_media', 255,
    "`id_card_copy_media` varchar(255) DEFAULT NULL COMMENT '身份证人像面 media_id'");
CALL p_wuye_widen_varchar('merchant_applyments', 'id_card_national_media', 255,
    "`id_card_national_media` varchar(255) DEFAULT NULL COMMENT '身份证国徽面 media_id'");

DROP PROCEDURE IF EXISTS p_wuye_widen_varchar;

SELECT COLUMN_NAME, COLUMN_TYPE, CHARACTER_MAXIMUM_LENGTH AS chars
FROM information_schema.COLUMNS
WHERE TABLE_SCHEMA = DATABASE()
  AND TABLE_NAME = 'merchant_applyments'
  AND COLUMN_NAME IN (
      'bank_account_type','organization_type','sales_scenes_type','contact_type','business_code',
      'license_period_begin','license_period_end','id_card_period_begin','id_card_period_end',
      'biz_address_code','bank_alias_code','bank_branch_id',
      'business_license_media','id_card_copy_media','id_card_national_media')
ORDER BY COLUMN_NAME;
