-- V14: 前端问题汇总修复（2026-09-09）
-- 1) 履约链路历史数据回填：deliveries.carrier_type -> courier；
--    orders.requires_delivery / fulfillment_mode 按「团购-食堂 none / 有配送单 courier_hall /
--    近7天已支付未发单 pending_choice+30min」口径回填
-- 2) 板块负责人(sector_leader)角色预设补齐 promotion.push（后端已按角色放行，此处保证权限包一致）
-- 注：本脚本由 Flyway 保证只执行一次；手工执行请使用
--     sql/2026-09-09_履约链路与板块特惠权限_数据库脚本.sql（幂等版）

UPDATE `deliveries`
   SET `carrier_type` = 'courier'
 WHERE `carrier_type` IS NULL
    OR `carrier_type` = '';

UPDATE `orders` o
  LEFT JOIN `merchants` m ON m.`id` = o.`merchant_id`
   SET o.`requires_delivery`    = 0,
       o.`fulfillment_mode`     = 'none',
       o.`fulfillment_deadline` = NULL
 WHERE o.`fulfillment_mode` IS NULL
   AND (
        m.`merchant_type` = 'canteen'
        OR (
            EXISTS (SELECT 1 FROM `order_items` oi
                     LEFT JOIN `products` p ON p.`id` = oi.`product_id`
                    WHERE oi.`order_id` = o.`id`)
            AND NOT EXISTS (SELECT 1 FROM `order_items` oi
                             LEFT JOIN `products` p ON p.`id` = oi.`product_id`
                            WHERE oi.`order_id` = o.`id`
                              AND IFNULL(p.`category`, '') <> '团购')
        )
   );

UPDATE `orders` o
   SET o.`fulfillment_mode` = 'courier_hall'
 WHERE o.`fulfillment_mode` IS NULL
   AND EXISTS (SELECT 1 FROM `deliveries` d WHERE d.`order_id` = o.`id`);

UPDATE `orders` o
   SET o.`fulfillment_mode`     = 'pending_choice',
       o.`fulfillment_deadline` = DATE_ADD(IFNULL(o.`paid_at`, o.`created_at`), INTERVAL 30 MINUTE)
 WHERE o.`fulfillment_mode` IS NULL
   AND o.`order_status` IN ('paid', 'delivering')
   AND NOT EXISTS (SELECT 1 FROM `deliveries` d WHERE d.`order_id` = o.`id`)
   AND IFNULL(o.`paid_at`, o.`created_at`) >= DATE_SUB(NOW(), INTERVAL 7 DAY);

UPDATE `orders`
   SET `requires_delivery` = 1
 WHERE `requires_delivery` IS NULL;

INSERT IGNORE INTO `role_preset_permissions` (`id`, `preset_id`, `permission_id`, `created_at`, `updated_at`)
SELECT CONCAT('rpp_sec_push_', LEFT(MD5(p.`id`), 12)), p.`id`, pm.`id`, NOW(), NOW()
  FROM `role_presets` p
  JOIN `permissions` pm ON pm.`code` = 'promotion.push'
 WHERE p.`code` = 'sector_leader';
