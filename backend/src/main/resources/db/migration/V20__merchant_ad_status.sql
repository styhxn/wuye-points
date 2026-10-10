-- =====================================================================
-- V20 商家广告支付真实性：merchant_ads 增加状态列
--
-- 背景：原 createAd / createTargetedAd 对 paymentMethod=wechat/mock 不做任何收款，
--   直接置投放期并 fan-out 到住户，存在「未付款即投放广告」的资损漏洞。
--   本次改为：wechat 走真实微信预下单（payment_records.biz_type='ad_purchase'），
--   微信回调确认后才计费投放；mock 仅在开发环境（app.payment.mock-enabled=true）可用。
--   广告记录新增 status 区分「待支付 / 投放中 / 已取消（支付超时）」。
--
-- 本脚本为手工执行脚本（application-prod.yml 中 flyway.enabled=false）。
-- 执行方式：mysql -u<user> -p <db> < V20__merchant_ad_status.sql
-- 幂等：列使用存储过程做存在性判断，可重复执行。
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

-- 1) merchant_ads.status：pending_payment 待支付 / active 投放中 / cancelled 已取消
CALL p_wuye_add_column('merchant_ads', 'status',
                       'status VARCHAR(20) NULL COMMENT ''状态 pending_payment待支付/active投放中/cancelled已取消''');

-- 2) 存量广告（旧逻辑为即时投放，无待支付态）统一回填为 active
UPDATE merchant_ads SET status = 'active' WHERE status IS NULL;

-- 3) 广告支付流水查询索引（按 biz_type+biz_id+status 查待支付/历史）
--    payment_records 已有 (biz_type, biz_id, status) 唯一约束时该索引冗余，此处不重复创建。

DROP PROCEDURE IF EXISTS p_wuye_add_column;