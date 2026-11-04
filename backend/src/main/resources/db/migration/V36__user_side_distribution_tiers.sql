-- =====================================================================
-- V36 分账链新增「用户侧先扣档」+ 现金支付订单返积分补账（2026-11-03）
--
-- 背景（老板 2026-11-02 分账逻辑图）：
--   图里 100 元订单抽佣 5 元后，先切「用户积分 10% + 用户奖励金 10%」共 20%，
--   剩下 4 元才分给物业公司 / 统筹。原实现这两笔按用户实付金额另算、不从平台盘扣，
--   属于链路级差异，不是比例差异。本脚本把这两档做成物业公司级可配比例并进链。
--
--   同时修一个缺陷：含现金支付的订单在微信回调 completeChannelPayment 里只扣减
--   用户使用的积分/物业币，从未调用 earnPoints/earnCoins，导致真实付费订单
--   point_earned / coin_earned 恒为 0。发放口径改按盘算之后必须先把发放接进回调。
--
-- 内容：
--   1) property_companies.user_point_share_rate   默认 0.1000 —— 用户积分预算占抽佣基础额比例
--   2) property_companies.user_reward_share_rate  默认 0.1000 —— 用户奖励金预算占抽佣基础额比例
--   3) distribution_records 四列快照：两档比例 + 两档实际扣减金额（审计与冲正用）
--   4) orders 两列快照：下单时锁定的比例，避免支付与完成之间改配置导致对不平
--
-- 口径变更（代码侧同步，见 DistributionService.buildShareBreakdown）：
--   可分配额 = 抽佣基础额 − 积分兑付成本 − 用户积分预算 − 用户奖励金预算
--   一级三档（平台 / 物业 / 管理盘）仍对「可分配额」分配，合计 100% 校验不变。
--   merchant_property_configs.coin_rebate_rate / coin_rebate_enabled 自本版本起
--   不再参与订单返现，由 user_reward_share_rate 统一取代（列保留不删，同 V19
--   处理 courier_per_order 的方式）；OrderService 中「抽佣率须 ≥20% 才返物业币」
--   的硬门槛一并移除。
--
-- 执行方式（生产 Flyway 关闭，手工执行）：
--   mysql -h<host> -u<user> -p --default-character-set=utf8mb4 <db> \
--     < V36__user_side_distribution_tiers.sql
--
-- 幂等：列先查 information_schema，可重复执行；回填只补空值。
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

-- ---------------------------------------------------------------------
-- 1) 物业公司：用户侧两档比例
-- ---------------------------------------------------------------------
CALL p_wuye_add_column('property_companies', 'user_point_share_rate',
                       'user_point_share_rate DECIMAL(5,4) DEFAULT 0.1000 COMMENT ''用户积分预算占抽佣基础额比例（分账链先扣档）''');
CALL p_wuye_add_column('property_companies', 'user_reward_share_rate',
                       'user_reward_share_rate DECIMAL(5,4) DEFAULT 0.1000 COMMENT ''用户奖励金预算占抽佣基础额比例（分账链先扣档）''');

-- ---------------------------------------------------------------------
-- 2) 分账记录：比例与金额快照
-- ---------------------------------------------------------------------
CALL p_wuye_add_column('distribution_records', 'user_point_share_rate',
                       'user_point_share_rate DECIMAL(5,4) NULL COMMENT ''结算时用户积分预算比例快照''');
CALL p_wuye_add_column('distribution_records', 'user_reward_share_rate',
                       'user_reward_share_rate DECIMAL(5,4) NULL COMMENT ''结算时用户奖励金预算比例快照''');
CALL p_wuye_add_column('distribution_records', 'user_point_budget',
                       'user_point_budget DECIMAL(10,2) NULL COMMENT ''本次从平台盘切出的用户积分预算（元）''');
CALL p_wuye_add_column('distribution_records', 'user_reward_budget',
                       'user_reward_budget DECIMAL(10,2) NULL COMMENT ''本次从平台盘切出的用户奖励金预算（元）''');

-- ---------------------------------------------------------------------
-- 3) 订单：支付时锁定的比例快照
-- ---------------------------------------------------------------------
CALL p_wuye_add_column('orders', 'user_point_share_rate',
                       'user_point_share_rate DECIMAL(5,4) NULL COMMENT ''支付时锁定的用户积分预算比例''');
CALL p_wuye_add_column('orders', 'user_reward_share_rate',
                       'user_reward_share_rate DECIMAL(5,4) NULL COMMENT ''支付时锁定的用户奖励金预算比例''');

-- ---------------------------------------------------------------------
-- 4) 回填：本脚本之前建的物业公司没有该列值时按图里口径补 10%。
--    注意 distribution_records / orders 的历史行**不回填**（保持 NULL）：
--    历史订单按老口径结算，calculation_version 已区分新旧，不得回溯重算。
-- ---------------------------------------------------------------------
UPDATE property_companies
SET user_point_share_rate = 0.1000
WHERE user_point_share_rate IS NULL;

UPDATE property_companies
SET user_reward_share_rate = 0.1000
WHERE user_reward_share_rate IS NULL;

DROP PROCEDURE IF EXISTS p_wuye_add_column;

-- =====================================================================
-- 执行完成。校验：
--   SHOW COLUMNS FROM property_companies LIKE 'user_%_share_rate';
--   SHOW COLUMNS FROM distribution_records LIKE 'user_%_budget';
--   SELECT id, user_point_share_rate, user_reward_share_rate FROM property_companies;
-- =====================================================================
