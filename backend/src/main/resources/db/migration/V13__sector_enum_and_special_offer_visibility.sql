-- V13: 前端联调问题修复（2026-09-08，第二轮）
-- 1) sector_leaders 增加 description 列（§39.2 / §39.4 的「描述」字段）
-- 2) sector_leaders / individual_leaders 的 sector 列注释统一为五枚举
--    cleaning/repair/security/greening/other（此前库注释为旧枚举，导致实现各自维护一份错枚举，
--    表现为「改板块时除保洁外全部 400」）
-- 3) special_offers 增加 used_quota 列（已领取数量）
-- 4) 存量历史 sector 值兜底归到 other
-- 注：Flyway 保证本脚本只执行一次，故直接使用原生 DDL；
--     手工执行请改用 sql/2026-09-08_板块枚举与特惠可见性_数据库脚本.sql（幂等版）。

ALTER TABLE `sector_leaders`
    ADD COLUMN `description` varchar(200) DEFAULT NULL COMMENT '板块描述/备注' AFTER `sector`;

ALTER TABLE `sector_leaders`
    MODIFY COLUMN `sector` varchar(50) NOT NULL COMMENT 'cleaning/repair/security/greening/other（保洁/维修/安保/绿化/其它）';

ALTER TABLE `individual_leaders`
    MODIFY COLUMN `sector` varchar(50) NOT NULL COMMENT 'cleaning/repair/security/greening/other（保洁/维修/安保/绿化/其它）';

UPDATE `sector_leaders`
   SET `sector` = 'other'
 WHERE `sector` NOT IN ('cleaning', 'repair', 'security', 'greening', 'other');

UPDATE `individual_leaders`
   SET `sector` = 'other'
 WHERE `sector` NOT IN ('cleaning', 'repair', 'security', 'greening', 'other');

UPDATE `individual_leader_applications`
   SET `sector` = 'other'
 WHERE `sector` IS NOT NULL
   AND `sector` NOT IN ('cleaning', 'repair', 'security', 'greening', 'other');

ALTER TABLE `special_offers`
    ADD COLUMN `used_quota` int NOT NULL DEFAULT 0 COMMENT '已领取数量' AFTER `per_user_quota`;
