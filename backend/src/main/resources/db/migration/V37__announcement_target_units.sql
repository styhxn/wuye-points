-- =====================================================================
-- V37 楼长「发布单元通知」：公告定向粒度补「单元」维度（2026-11-03）
--
-- 背景：楼长工作台「本单元运营 · 发布单元通知」此前无后端接口。老板定的口径是
--   复用 announcements 表（不新建单元通知表），因此只补一列定向粒度。
--
-- 现状：announcements 已有 target_roles / target_buildings 两列，语义都是
--   「NULL = 不限、否则 JSON 数组内元素命中即可见」，但**没有 unit 维度**——
--   楼长岗位绑定是 community_id + unit（V21 起与楼栋无关），只按楼栋定向
--   会把通知发给整栋楼其他单元。本脚本补 target_units。
--
-- 内容：
--   1) announcements.target_units VARCHAR(500) NULL —— 定向单元列表（存 JSON 数组文本）；
--      楼长发布时后端强制写 [本人 active 楼长记录的 unit]，前端不可传。
--   2) 存量行不回填（保持 NULL = 不限单元），历史公告可见范围不变。
--
-- 为什么用 varchar 而不是跟 target_roles/target_buildings 一样的 json：
--   单元可见性靠 `target_units LIKE '%"5单元"%'` 判定，而 H2（测试库 MODE=MySQL）把 json 列
--   读回成二次编码字符串（["5单元"] 变成 "[\"5单元\"]"），LIKE 与 JSON 解析双双失效，
--   这条越权路径就没法进回归测试。存 varchar 在 MySQL / H2 两边文本一致，行为可测。
--
-- 不新建表、不加索引（列表过滤走既有 idx_property_company/idx_status）。
-- messages.card_type / card_id 两列 V1 基线里已存在（此前实体未映射），
-- 「分享商品给本单元」直接复用，本脚本无需改动 messages。
--
-- 执行方式（生产 Flyway 关闭，手工执行）：
--   mysql -h<host> -u<user> -p --default-character-set=utf8mb4 <db> \
--     < V37__announcement_target_units.sql
--
-- 幂等：列先查 information_schema，可重复执行。
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
        SET @ddl = CONCAT('ALTER TABLE `', p_table, '` ADD COLUMN ', p_ddl);
        PREPARE stmt FROM @ddl;
        EXECUTE stmt;
        DEALLOCATE PREPARE stmt;
    END IF;
END //
DELIMITER ;

CALL p_wuye_add_column('announcements', 'target_units',
                       'target_units VARCHAR(500) DEFAULT NULL COMMENT ''定向单元列表JSON文本（楼长单元通知写本单元；NULL=不限单元）''');

DROP PROCEDURE IF EXISTS p_wuye_add_column;

-- =====================================================================
-- 执行完成。校验：
--   SHOW COLUMNS FROM announcements LIKE 'target_units';
--   SELECT COUNT(*) FROM announcements WHERE target_units IS NOT NULL;  -- 应为 0
-- =====================================================================
