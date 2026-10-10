-- =====================================================================
-- V18 单元楼长角色 + 业主卖货独立钱包
--
-- 需求一：新增单元楼长（building_leader）身份
--   * 楼长为「住户 + 楼长」复合身份：residents.role 保持 'resident' 不变，
--     楼长身份由 building_leaders 表的 active 记录判定，住户功能不受影响。
--   * 楼长与活动组无关，是与「小区 + 楼栋(building)」绑定的推广角色；
--     该楼栋内住户产生消费时，楼长按比例分成。
--   * 物业公司指定/新增/删除楼长。
--   * 分成分成定向绑定：订单创建那一刻快照 building_leader_id / building_leader_rate，
--     后续修改/撤销楼长不影响已产生订单（不会把分成改判给新楼长）。
--   * 分成来源：从「物业公司分成(property_share)」中定向扣除，
--     property_share 相应减少，保持总额守恒，不影响统筹/板块/个体链路。
--
-- 需求二：业主卖货独立钱包
--   * 业主商城（业主商户上架商品）成交后的差价收益 reseller_profit 原仅落
--     resident_merchant_settlements，无提现入口。
--   * 本次新增「业主卖货钱包」：可提现余额 = 累计卖货收益 − 已占用提现，
--     仅用于提现，不可消费。复用 withdrawal_records（withdrawal_type='resident_seller'），
--     不新增余额冗余列，实时聚合，口径与 WithdrawableService 一致。
--
-- 本脚本为手工执行脚本（application-prod.yml 中 flyway.enabled=false）。
-- 执行方式：mysql -u<user> -p <db> < V18__building_leader_and_seller_wallet.sql
-- 幂等：表使用 CREATE TABLE IF NOT EXISTS；列使用存储过程做存在性判断，可重复执行。
-- =====================================================================

-- ---------------------------------------------------------------------
-- 1) 单元楼长表 building_leaders
--    同一小区+楼栋允许存在历史记录（撤销后重指派），由服务层保证同一时刻仅一条 active。
-- ---------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS building_leaders
(
    id                  VARCHAR(32)  NOT NULL COMMENT '主键 bl_',
    resident_id         VARCHAR(32)  NOT NULL COMMENT '楼长住户ID res_',
    property_company_id VARCHAR(32)  NOT NULL COMMENT '物业公司ID pc_',
    community_id        VARCHAR(32)  NOT NULL COMMENT '小区ID',
    building            VARCHAR(20)  NOT NULL COMMENT '楼栋（与 residents.building 对齐）',
    commission_rate     DECIMAL(5, 4) NOT NULL DEFAULT 0.1000 COMMENT '楼长分成比例（占物业公司分成的比例，0~1）',
    description         VARCHAR(500)          COMMENT '备注/说明',
    status              VARCHAR(20)  NOT NULL DEFAULT 'active' COMMENT '状态 active启用/inactive停用',
    created_at          DATETIME     NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at          DATETIME     NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    KEY idx_bl_building (community_id, building, status),
    KEY idx_bl_resident (resident_id),
    KEY idx_bl_property (property_company_id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='单元楼长（楼栋推广分成角色）';

-- ---------------------------------------------------------------------
-- 2) orders 增加楼长绑定快照列
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

-- 下单时绑定的楼长ID（快照，订单产生即固定）
CALL p_wuye_add_column('orders', 'building_leader_id',
                       'building_leader_id VARCHAR(32) NULL COMMENT ''下单时绑定的楼长ID（快照）bl_''');
-- 下单时楼长分成比例快照
CALL p_wuye_add_column('orders', 'building_leader_rate',
                       'building_leader_rate DECIMAL(5,4) NULL COMMENT ''下单时楼长分成比例快照（占物业分成的比例）''');

-- ---------------------------------------------------------------------
-- 3) distribution_records 增加楼长分成列
-- ---------------------------------------------------------------------
CALL p_wuye_add_column('distribution_records', 'building_leader_share',
                       'building_leader_share DECIMAL(10,2) NOT NULL DEFAULT 0.00 COMMENT ''楼长分成（从物业分成中扣除）''');
CALL p_wuye_add_column('distribution_records', 'building_leader_id',
                       'building_leader_id VARCHAR(32) NULL COMMENT ''楼长ID bl_''');
CALL p_wuye_add_column('distribution_records', 'building_leader_rate',
                       'building_leader_rate DECIMAL(5,4) NULL COMMENT ''楼长分成比例快照''');

-- 楼长分账查询索引
SET @idx_exists = (SELECT COUNT(1)
                   FROM information_schema.STATISTICS
                   WHERE TABLE_SCHEMA = DATABASE()
                     AND TABLE_NAME = 'distribution_records'
                     AND INDEX_NAME = 'idx_dr_building_leader');
SET @sql = IF(@idx_exists = 0,
              'ALTER TABLE distribution_records ADD INDEX idx_dr_building_leader (building_leader_id)',
              'SELECT 1');
PREPARE stmt2 FROM @sql;
EXECUTE stmt2;
DEALLOCATE PREPARE stmt2;

-- ---------------------------------------------------------------------
-- 4) withdrawal_records 提现类型注释补充（含 building_leader / resident_seller）
--    withdrawal_type 列为 varchar(30)，无需结构变更，仅刷新注释。
-- ---------------------------------------------------------------------
ALTER TABLE withdrawal_records
    MODIFY COLUMN withdrawal_type VARCHAR(30) NOT NULL
        COMMENT '提现类型: individual_leader/courier/coordinator/sector_leader/building_leader/resident_seller';

DROP PROCEDURE IF EXISTS p_wuye_add_column;

-- ---------------------------------------------------------------------
-- 5) 数据修正：楼长为「住户 + 楼长」复合身份
--    楼长本质是特殊住户额外叠加的推广身份，不应替换住户角色：
--    residents.role 必须保持 'resident'，否则所有 requireAnyRole("resident")
--    的住户功能（社区论坛、报事报修、卖货等）都会因鉴权失败而不可用。
--    楼长身份改由 building_leaders 表的 active 记录判定。
--    （历史误将 role 改写为 building_leader 的账号在此回滚为 resident，天然幂等。）
-- ---------------------------------------------------------------------
UPDATE residents SET role = 'resident' WHERE role = 'building_leader';

-- =====================================================================
-- 执行完成。
-- 回滚（如需）：
--   ALTER TABLE distribution_records DROP INDEX idx_dr_building_leader;
--   ALTER TABLE distribution_records DROP COLUMN building_leader_rate,
--         DROP COLUMN building_leader_id, DROP COLUMN building_leader_share;
--   ALTER TABLE orders DROP COLUMN building_leader_rate, DROP COLUMN building_leader_id;
--   DROP TABLE IF EXISTS building_leaders;
-- =====================================================================