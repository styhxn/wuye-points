-- =====================================================================
-- V21 单元楼长绑定粒度调整：由「小区 + 楼栋」改为「小区 + 单元」
--
-- 背景（前端反馈）：
--   * 原实现中楼长按「小区 + 楼栋(building)」绑定，同一楼栋同一时刻仅一名 active 楼长；
--     实际业务要求楼长按「单元」划分——同一小区内以单元号（residents.unit）为准，
--     与楼栋无关：1号楼2单元 与 2号楼2单元 视为同一个楼长岗位。
--
-- 变更内容：
--   1) building_leaders 新增 unit 列（与 residents.unit 对齐），作为楼长绑定粒度；
--   2) 历史数据按 resident_id 回填 unit（取住户当前 unit）；
--   3) 唯一性/匹配口径改为「小区 + 单元」；索引 idx_bl_building → idx_bl_unit；
--   4) 删除已废弃的 building 列（不再参与任何绑定/匹配）。
--
-- 说明：
--   * orders.building_leader_id / building_leader_rate、
--     distribution_records.building_leader_* 为订单快照列，与本次绑定口径无关，保持不变；
--     历史订单仍按快照结算，不受影响。
--   * 本脚本为手工执行脚本（application-prod.yml 中 flyway.enabled=false）。
--   * 执行方式：mysql -u<user> -p <db> < V21__building_leader_unit_binding.sql
--   * 幂等：加列/删列/索引均做存在性判断，可重复执行。
-- =====================================================================

-- ---------------------------------------------------------------------
-- 通用工具：存在性判断加列 / 删列
-- ---------------------------------------------------------------------
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

DROP PROCEDURE IF EXISTS p_wuye_drop_column;
DELIMITER //
CREATE PROCEDURE p_wuye_drop_column(IN p_table VARCHAR(64), IN p_column VARCHAR(64))
BEGIN
    IF EXISTS (SELECT 1
               FROM information_schema.COLUMNS
               WHERE TABLE_SCHEMA = DATABASE()
                 AND TABLE_NAME = p_table
                 AND COLUMN_NAME = p_column) THEN
        SET @ddl = CONCAT('ALTER TABLE ', p_table, ' DROP COLUMN ', p_column);
        PREPARE stmt FROM @ddl;
        EXECUTE stmt;
        DEALLOCATE PREPARE stmt;
    END IF;
END //
DELIMITER ;

-- ---------------------------------------------------------------------
-- 1) 新增 unit 列（先允许 NULL，回填后再置 NOT NULL）
-- ---------------------------------------------------------------------
CALL p_wuye_add_column('building_leaders', 'unit',
                       'unit VARCHAR(20) NULL COMMENT ''单元（与 residents.unit 对齐，楼长绑定粒度）'' AFTER community_id');

-- ---------------------------------------------------------------------
-- 2) 历史数据回填：取楼长对应住户的当前 unit
-- ---------------------------------------------------------------------
UPDATE building_leaders bl
    JOIN residents r ON r.id = bl.resident_id
SET bl.unit = r.unit
WHERE (bl.unit IS NULL OR bl.unit = '')
  AND r.unit IS NOT NULL
  AND r.unit <> '';

-- 仍无单元的历史记录：置空串（fail-safe，不会误命中任何真实单元），
-- 需管理端撤销后按单元重新指派；仅对历史脏数据生效，正常数据不受影响。
UPDATE building_leaders SET unit = '' WHERE unit IS NULL;

-- ---------------------------------------------------------------------
-- 3) unit 置为 NOT NULL
-- ---------------------------------------------------------------------
SET @unit_nullable = (SELECT COUNT(1)
                      FROM information_schema.COLUMNS
                      WHERE TABLE_SCHEMA = DATABASE()
                        AND TABLE_NAME = 'building_leaders'
                        AND COLUMN_NAME = 'unit'
                        AND IS_NULLABLE = 'YES');
SET @sql = IF(@unit_nullable = 1,
              'ALTER TABLE building_leaders MODIFY COLUMN unit VARCHAR(20) NOT NULL COMMENT ''单元（与 residents.unit 对齐，楼长绑定粒度）''',
              'SELECT 1');
PREPARE stmt3 FROM @sql;
EXECUTE stmt3;
DEALLOCATE PREPARE stmt3;

-- ---------------------------------------------------------------------
-- 4) 索引调整：降级 idx_bl_building，新增 idx_bl_unit(community_id, unit, status)
-- ---------------------------------------------------------------------
SET @idx = (SELECT COUNT(1)
            FROM information_schema.STATISTICS
            WHERE TABLE_SCHEMA = DATABASE()
              AND TABLE_NAME = 'building_leaders'
              AND INDEX_NAME = 'idx_bl_building');
SET @sql = IF(@idx > 0, 'ALTER TABLE building_leaders DROP INDEX idx_bl_building', 'SELECT 1');
PREPARE stmt4 FROM @sql;
EXECUTE stmt4;
DEALLOCATE PREPARE stmt4;

SET @idx = (SELECT COUNT(1)
            FROM information_schema.STATISTICS
            WHERE TABLE_SCHEMA = DATABASE()
              AND TABLE_NAME = 'building_leaders'
              AND INDEX_NAME = 'idx_bl_unit');
SET @sql = IF(@idx = 0, 'ALTER TABLE building_leaders ADD INDEX idx_bl_unit (community_id, unit, status)', 'SELECT 1');
PREPARE stmt5 FROM @sql;
EXECUTE stmt5;
DEALLOCATE PREPARE stmt5;

-- ---------------------------------------------------------------------
-- 5) 删除已废弃的 building 列
-- ---------------------------------------------------------------------
CALL p_wuye_drop_column('building_leaders', 'building');

-- ---------------------------------------------------------------------
-- 6) 表注释同步
-- ---------------------------------------------------------------------
ALTER TABLE building_leaders COMMENT ='单元楼长（单元推广分成角色，绑定小区+单元）';

DROP PROCEDURE IF EXISTS p_wuye_add_column;
DROP PROCEDURE IF EXISTS p_wuye_drop_column;

-- =====================================================================
-- 执行完成。
-- 校验：
--   SHOW COLUMNS FROM building_leaders;              -- 应含 unit，不含 building
--   SELECT id, community_id, unit, status FROM building_leaders;
--
-- 回滚（如需，仅结构，数据需自行评估）：
--   ALTER TABLE building_leaders ADD COLUMN building VARCHAR(20) NULL AFTER community_id;
--   UPDATE building_leaders bl JOIN residents r ON r.id = bl.resident_id SET bl.building = r.building;
--   ALTER TABLE building_leaders DROP INDEX idx_bl_unit;
--   ALTER TABLE building_leaders ADD INDEX idx_bl_building (community_id, building, status);
--   ALTER TABLE building_leaders DROP COLUMN unit;
-- =====================================================================