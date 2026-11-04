-- =====================================================================
-- V34 进件身份证居住地址密文 + 降低行宽（2026-10-28）
--
-- 1) 官方《提交申请单》要求 id_card_address 使用微信支付公钥加密，新增 id_card_address_enc。
-- 2) MySQL 行宽上限 65535 字节（utf8mb4 下 varchar(1024) 占 4096 字节）。
--    V33 已把 merchant_applyments 行宽推到临界值，直接再加 varchar(1024) 会报
--    ERROR 1118 (42000): Row size too large。
--    这里先把加密串/CSV/URL 等长文本列改为 TEXT（TEXT 行内仅占约 9-12 字节指针），
--    再新增 TEXT 列；数据原样保留，不删除任何列。
--
-- 幂等：列/类型先查 information_schema，可重复执行。
-- =====================================================================

DROP PROCEDURE IF EXISTS p_wuye_add_column;
DROP PROCEDURE IF EXISTS p_wuye_text_column;

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

CREATE PROCEDURE p_wuye_text_column(IN p_table VARCHAR(64), IN p_column VARCHAR(64), IN p_comment VARCHAR(255))
BEGIN
    IF EXISTS (SELECT 1
               FROM information_schema.COLUMNS
               WHERE TABLE_SCHEMA = DATABASE()
                 AND TABLE_NAME = p_table
                 AND COLUMN_NAME = p_column
                 AND DATA_TYPE = 'varchar') THEN
        SET @ddl = CONCAT('ALTER TABLE `', p_table, '` MODIFY COLUMN `', p_column,
                          '` text DEFAULT NULL COMMENT ''', p_comment, '''');
        PREPARE stmt FROM @ddl;
        EXECUTE stmt;
        DEALLOCATE PREPARE stmt;
    END IF;
END //

DELIMITER ;

-- 先把长 varchar 改为 TEXT，释放行宽（数据不变）
CALL p_wuye_text_column('merchant_applyments', 'id_card_number_enc', '身份证号（RSA加密）');
CALL p_wuye_text_column('merchant_applyments', 'bank_account_number_enc', '银行账号（RSA加密）');
CALL p_wuye_text_column('merchant_applyments', 'contact_email_enc', '联系邮箱（RSA加密）');
CALL p_wuye_text_column('merchant_applyments', 'legal_validation_url', '法人验证链接（旧字段，保留）');
CALL p_wuye_text_column('merchant_applyments', 'sign_url', '超级管理员签约链接');
CALL p_wuye_text_column('merchant_applyments', 'sales_pics', '场景截图media_id（逗号分隔）');
CALL p_wuye_text_column('merchant_applyments', 'store_entrance_pics', '门头照片media_id（逗号分隔）');
CALL p_wuye_text_column('merchant_applyments', 'store_indoor_pics', '店内照片media_id（逗号分隔）');
CALL p_wuye_text_column('merchant_applyments', 'qualifications', '特殊资质图片media_id（逗号分隔）');

-- 再新增密文列（TEXT，避免再次触发 65535 行宽限制）
CALL p_wuye_add_column('merchant_applyments', 'id_card_address_enc',
    "`id_card_address_enc` text DEFAULT NULL COMMENT '身份证居住地址（RSA加密，企业必填）'");

DROP PROCEDURE IF EXISTS p_wuye_add_column;
DROP PROCEDURE IF EXISTS p_wuye_text_column;

-- 验证：下面 10 列都应存在，且 DATA_TYPE 均为 text
SELECT COLUMN_NAME, COLUMN_TYPE, DATA_TYPE, IS_NULLABLE
FROM information_schema.COLUMNS
WHERE TABLE_SCHEMA = DATABASE()
  AND TABLE_NAME = 'merchant_applyments'
  AND COLUMN_NAME IN (
    'id_card_address_enc','id_card_number_enc','bank_account_number_enc',
    'contact_email_enc','legal_validation_url','sign_url','sales_pics',
    'store_entrance_pics','store_indoor_pics','qualifications')
ORDER BY COLUMN_NAME;
