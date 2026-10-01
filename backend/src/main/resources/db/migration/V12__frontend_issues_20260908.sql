-- V12: 前端联调问题修复（2026-09-08）
-- 1) individual_leader_applications 增加「物业 + 审核状态」复合索引，支撑 §37 申请列表按物业过滤
-- 2) 无其他结构变更：individual_leaders.sector_leader_id 在基线中已为 DEFAULT NULL，
--    individual_leader_applications 的 reject_reason / individual_leader_id / audited_at 亦已在基线中。
-- 3) 统筹（coordinator）的 promotion.push / sector.manage / sector.read 授权属种子数据，
--    手工库请执行 sql/2026-09-08_前端问题后端修复_数据库脚本.sql（幂等版）。
-- 注：Flyway 保证本脚本只执行一次，故直接使用原生 DDL。

ALTER TABLE `individual_leader_applications`
    ADD INDEX `idx_ila_property_status` (`property_company_id`, `audit_status`);
