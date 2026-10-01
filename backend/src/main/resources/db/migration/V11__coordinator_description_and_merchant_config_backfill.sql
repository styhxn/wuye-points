-- V11: 前端问题修复（2026-09-06）
-- 1) coordinators 增加 description 列（统筹描述/备注）
-- 2) 为缺失挂接配置的商家补齐 merchant_property_configs（60006 修复）
-- 注：Flyway 保证本脚本只执行一次，故直接使用原生 DDL；
--     手工执行请改用 sql/2026-09-06_frontend_issues_fix.sql（幂等版）。

ALTER TABLE `coordinators`
    ADD COLUMN `description` varchar(200) DEFAULT NULL COMMENT '统筹描述/备注' AFTER `commission_rate`;

INSERT INTO `merchant_property_configs`
    (`id`, `merchant_id`, `property_company_id`, `commission_rate`,
     `point_exchange_rate`, `member_discount_price`, `coin_rebate_rate`,
     `coin_rebate_enabled`, `status`, `created_at`, `updated_at`)
SELECT CONCAT('mpc_fix_', SUBSTRING(m.`id`, 5)),
       m.`id`,
       m.`property_company_id`,
       0.1000,
       100.0000,
       NULL,
       0.0000,
       0,
       'active',
       NOW(),
       NOW()
FROM `merchants` m
WHERE NOT EXISTS (
    SELECT 1 FROM `merchant_property_configs` c
    WHERE c.`merchant_id` = m.`id`
      AND c.`property_company_id` = m.`property_company_id`
)
ON DUPLICATE KEY UPDATE `updated_at` = `updated_at`;
