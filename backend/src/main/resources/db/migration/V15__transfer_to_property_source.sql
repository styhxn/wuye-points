-- V15: 住户「转给物业」流水 source 统一（2026-09-10）
-- 背景：住户端转给物业接口（§2.6.4）此前写入的 source 为 'transfer_property'，
--       而管理端对账查询（AdminTransferToPropertyService）按 'transfer_to_property' 过滤，
--       导致物业端「转给物业」列表查不到住户已转出的流水。
-- 本脚本将历史脏数据统一为 'transfer_to_property'，与后端代码常量保持一致。
-- 注：本脚本由 Flyway 保证只执行一次；手工执行请使用
--     sql/2026-09-10_转给物业接口修复_数据库脚本.sql（幂等版）

UPDATE `point_records`
   SET `source` = 'transfer_to_property'
 WHERE `source` = 'transfer_property';

UPDATE `property_coin_records`
   SET `source` = 'transfer_to_property'
 WHERE `source` = 'transfer_property';
