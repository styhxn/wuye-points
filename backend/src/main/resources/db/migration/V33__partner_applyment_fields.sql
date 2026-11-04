-- =====================================================================
-- V33 商家进件切换为「服务商特约商户进件」字段集（2026-10-28）
--
-- 背景：原 merchant_applyments 按电商收付通二级商户进件设计（/v3/ecommerce/applyments/），
-- 平台未开通收付通（需 ICP/EDI）。实际走普通服务商特约商户进件
-- POST /v3/applyment4sub/applyment/，字段集不同：
--   * 行业由 settlement_info.settlement_id + qualification_type 表达（无类目编码字段）；
--   * 企业主体必填 UBO（最终受益人）与身份证居住地址；
--   * 经营场景按 sales_scenes_type 分支（门店/小程序/公众号）；
--   * 银行开户行必须用官方银行列表编码（account_bank）；
--   * 超管类型 LEGAL/SUPER，邮箱/手机号必须加密上送；
--   * 重新提交必须更换业务申请编号（business_code）。
--
-- 幂等：列/索引先查 information_schema，可重复执行。
-- 历史数据：旧字段（merchant_category 等）保留不删除；新列默认 NULL。
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

-- 申请单标识
CALL p_wuye_add_column('merchant_applyments', 'business_code',
    "`business_code` varchar(124) DEFAULT NULL COMMENT '业务申请编号（每次重提必须更换）'");
CALL p_wuye_add_column('merchant_applyments', 'finance_institution',
    "`finance_institution` tinyint(1) DEFAULT '0' COMMENT '是否金融机构主体'");

-- 主体/执照
CALL p_wuye_add_column('merchant_applyments', 'merchant_name',
    "`merchant_name` varchar(128) DEFAULT NULL COMMENT '营业执照上的商户名称'");
CALL p_wuye_add_column('merchant_applyments', 'business_license_number',
    "`business_license_number` varchar(64) DEFAULT NULL COMMENT '统一社会信用代码/注册号'");
CALL p_wuye_add_column('merchant_applyments', 'legal_person',
    "`legal_person` varchar(100) DEFAULT NULL COMMENT '营业执照经营者/法定代表人姓名'");
CALL p_wuye_add_column('merchant_applyments', 'license_address',
    "`license_address` varchar(512) DEFAULT NULL COMMENT '营业执照注册地址'");
CALL p_wuye_add_column('merchant_applyments', 'license_period_begin',
    "`license_period_begin` varchar(10) DEFAULT NULL COMMENT '营业执照有效期开始'");
CALL p_wuye_add_column('merchant_applyments', 'license_period_end',
    "`license_period_end` varchar(10) DEFAULT NULL COMMENT '营业执照有效期结束'");

-- 法人证件补充
CALL p_wuye_add_column('merchant_applyments', 'id_card_address',
    "`id_card_address` varchar(512) DEFAULT NULL COMMENT '身份证居住地址（企业必填）'");
CALL p_wuye_add_column('merchant_applyments', 'id_card_period_begin',
    "`id_card_period_begin` varchar(10) DEFAULT NULL COMMENT '身份证有效期开始'");
CALL p_wuye_add_column('merchant_applyments', 'id_card_period_end',
    "`id_card_period_end` varchar(10) DEFAULT NULL COMMENT '身份证有效期结束'");
CALL p_wuye_add_column('merchant_applyments', 'ubo_json',
    "`ubo_json` text DEFAULT NULL COMMENT '最终受益人列表JSON（姓名/号码/地址为密文）'");

-- 经营信息与场景
CALL p_wuye_add_column('merchant_applyments', 'service_phone',
    "`service_phone` varchar(32) DEFAULT NULL COMMENT '客服电话（交易记录对买家展示）'");
CALL p_wuye_add_column('merchant_applyments', 'sales_scenes_type',
    "`sales_scenes_type` varchar(32) DEFAULT NULL COMMENT '经营场景：SALES_SCENES_STORE/MINI_PROGRAM/MP等'");
CALL p_wuye_add_column('merchant_applyments', 'sales_app_id',
    "`sales_app_id` varchar(256) DEFAULT NULL COMMENT '服务商侧小程序/公众号AppID'");
CALL p_wuye_add_column('merchant_applyments', 'sales_sub_app_id',
    "`sales_sub_app_id` varchar(256) DEFAULT NULL COMMENT '商家侧小程序/公众号AppID'");
CALL p_wuye_add_column('merchant_applyments', 'sales_pics',
    "`sales_pics` varchar(1024) DEFAULT NULL COMMENT '场景截图media_id（逗号分隔）'");
CALL p_wuye_add_column('merchant_applyments', 'biz_store_name',
    "`biz_store_name` varchar(50) DEFAULT NULL COMMENT '线下门店名称'");
CALL p_wuye_add_column('merchant_applyments', 'biz_address_code',
    "`biz_address_code` varchar(16) DEFAULT NULL COMMENT '门店省市编码'");
CALL p_wuye_add_column('merchant_applyments', 'biz_store_address',
    "`biz_store_address` varchar(512) DEFAULT NULL COMMENT '门店详细地址'");
CALL p_wuye_add_column('merchant_applyments', 'store_entrance_pics',
    "`store_entrance_pics` varchar(1024) DEFAULT NULL COMMENT '门头照片media_id（逗号分隔）'");
CALL p_wuye_add_column('merchant_applyments', 'store_indoor_pics',
    "`store_indoor_pics` varchar(1024) DEFAULT NULL COMMENT '店内照片media_id（逗号分隔）'");

-- 结算规则
CALL p_wuye_add_column('merchant_applyments', 'settlement_id',
    "`settlement_id` varchar(32) DEFAULT NULL COMMENT '入驻结算规则ID（平台配置）'");
CALL p_wuye_add_column('merchant_applyments', 'qualification_type',
    "`qualification_type` varchar(200) DEFAULT NULL COMMENT '所属行业名称（平台配置）'");
CALL p_wuye_add_column('merchant_applyments', 'qualifications',
    "`qualifications` varchar(1024) DEFAULT NULL COMMENT '特殊资质图片media_id（逗号分隔）'");

-- 超级管理员
CALL p_wuye_add_column('merchant_applyments', 'contact_type',
    "`contact_type` varchar(16) DEFAULT NULL COMMENT '超管类型：LEGAL法人'");
CALL p_wuye_add_column('merchant_applyments', 'contact_email_enc',
    "`contact_email_enc` varchar(1024) DEFAULT NULL COMMENT '联系邮箱（RSA加密）'");

-- 结算账户补充
CALL p_wuye_add_column('merchant_applyments', 'account_bank',
    "`account_bank` varchar(128) DEFAULT NULL COMMENT '开户银行（银行列表account_bank值）'");
CALL p_wuye_add_column('merchant_applyments', 'bank_alias_code',
    "`bank_alias_code` varchar(32) DEFAULT NULL COMMENT '银行别名编码（查询支行用）'");
CALL p_wuye_add_column('merchant_applyments', 'bank_branch_id',
    "`bank_branch_id` varchar(128) DEFAULT NULL COMMENT '支行联行号bank_branch_id'");

CALL p_wuye_add_index('merchant_applyments', 'idx_business_code', '`business_code`');

DROP PROCEDURE IF EXISTS p_wuye_add_column;
DROP PROCEDURE IF EXISTS p_wuye_add_index;

-- 验证：应返回 30 行新增列 + 1 行索引
SELECT COLUMN_NAME, COLUMN_TYPE, IS_NULLABLE
FROM information_schema.COLUMNS
WHERE TABLE_SCHEMA = DATABASE()
  AND TABLE_NAME = 'merchant_applyments'
  AND COLUMN_NAME IN (
    'business_code','finance_institution','merchant_name','business_license_number','legal_person',
    'license_address','license_period_begin','license_period_end','id_card_address',
    'id_card_period_begin','id_card_period_end','ubo_json','service_phone','sales_scenes_type',
    'sales_app_id','sales_sub_app_id','sales_pics','biz_store_name','biz_address_code',
    'biz_store_address','store_entrance_pics','store_indoor_pics','settlement_id',
    'qualification_type','qualifications','contact_type','contact_email_enc',
    'account_bank','bank_alias_code','bank_branch_id')
ORDER BY COLUMN_NAME;
