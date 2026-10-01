-- V10 实体与库结构对齐（补齐本轮部署前漏写的迁移）
-- 幂等：列用 information_schema 守卫，表用 IF NOT EXISTS，重复执行安全。
-- 背景：distribution_records 新增 platform_fee_share 列；新增 WechatSplitReceiverAccount /
--       WechatSplitRecord 两个实体对应的表未落迁移，导致 ddl-auto=validate 启动失败。

-- 1) distribution_records.platform_fee_share
SET @db = DATABASE();
SELECT COUNT(*) INTO @col_exists FROM information_schema.columns
  WHERE table_schema = @db AND table_name = 'distribution_records' AND column_name = 'platform_fee_share';
SET @sql = IF(@col_exists = 0,
  'ALTER TABLE distribution_records ADD COLUMN platform_fee_share decimal(10,2) DEFAULT ''0.00'' COMMENT ''平台手续费分成''',
  'SELECT 1');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 2) 微信收款账户注册表
CREATE TABLE IF NOT EXISTS `wechat_split_receiver_accounts` (
  `id` varchar(32) NOT NULL COMMENT '账户注册ID',
  `owner_type` varchar(32) NOT NULL COMMENT '收款方类型(见 WechatSplitOwnerTypes)',
  `owner_id` varchar(32) NOT NULL COMMENT '实体ID;平台固定为 PLATFORM',
  `merchant_no` varchar(64) DEFAULT NULL COMMENT '微信 商户号 merchant_no',
  `account_no` varchar(64) DEFAULT NULL COMMENT '微信 账户号(account_in/account_out 使用)',
  `account_name` varchar(128) DEFAULT NULL COMMENT '账户名/户名',
  `currency` varchar(8) DEFAULT 'CNY' COMMENT '币种',
  `verified` tinyint(1) DEFAULT '0' COMMENT '是否已通过开户/绑卡校验',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_wechat_split_receiver_account_owner` (`owner_type`,`owner_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='微信 收款账户注册表';

-- 3) 微信分账流水记录表
CREATE TABLE IF NOT EXISTS `wechat_split_record` (
  `id` varchar(32) NOT NULL COMMENT '分账记录ID',
  `payment_record_id` varchar(32) DEFAULT NULL COMMENT '关联 payment_records.id',
  `out_trade_no` varchar(64) DEFAULT NULL COMMENT '微信支付单号(源支付)',
  `biz_type` varchar(32) DEFAULT NULL COMMENT '业务类型 PaymentBizType',
  `order_id` varchar(32) DEFAULT NULL COMMENT '业务单号(订单/充值单等)',
  `split_no` varchar(64) DEFAULT NULL COMMENT '自定义分账订单号(6.2/6.12 out_trade_no)',
  `wechat_sub_trade_no` varchar(64) DEFAULT NULL COMMENT '微信分账交易号 sub_trade_no',
  `payer_account` varchar(64) DEFAULT NULL COMMENT '出账方微信账户',
  `payee_account` varchar(64) DEFAULT NULL COMMENT '入账方微信账户',
  `freeze_amount` decimal(10,2) DEFAULT '0.00' COMMENT '冻结/分账金额(元)',
  `status` varchar(32) DEFAULT NULL COMMENT 'frozen_pending/frozen/finished/cancelled/reverted...',
  `freeze_resp` text COMMENT '6.2冻结返回报文',
  `finish_resp` text COMMENT '6.12完结/撤销返回报文',
  `reverse_resp` text COMMENT '6.9冲正退回返回报文',
  `remark` text COMMENT '备注/失败原因',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `group_no` varchar(32) DEFAULT NULL COMMENT '分账批次号(同笔支付所有收款方共享)',
  `owner_type` varchar(32) DEFAULT NULL COMMENT '收款方类型 PROPERTY/MERCHANT/COORDINATOR/SECTOR_LEADER/INDIVIDUAL_LEADER/COURIER/PLATFORM',
  `owner_id` varchar(32) DEFAULT NULL COMMENT '收款方实体ID(平台固定PLATFORM)',
  `currency` varchar(8) DEFAULT 'CNY' COMMENT '币种',
  `wechat_out_trade_no` varchar(64) DEFAULT NULL COMMENT '微信6.7回调分账订单号(out_trade_no)',
  `withdraw_status` varchar(32) DEFAULT NULL COMMENT '提现状态(Path B): null/withdrawing/withdrawn/withdraw_failed',
  `withdraw_resp` text COMMENT '6.4 提现接口返回报文(JSON)',
  `withdraw_trade_no` varchar(64) DEFAULT NULL COMMENT '提现幂等单号(trade_no=splitNo+_WD)',
  PRIMARY KEY (`id`),
  KEY `idx_payment_record` (`payment_record_id`),
  KEY `idx_out_trade_no` (`out_trade_no`),
  KEY `idx_order_id` (`order_id`),
  KEY `idx_split_no` (`split_no`),
  KEY `idx_sub_trade_no` (`wechat_sub_trade_no`),
  KEY `idx_status` (`status`),
  KEY `idx_group_no` (`group_no`),
  KEY `idx_owner` (`owner_type`,`owner_id`),
  KEY `idx_withdraw_status` (`withdraw_status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='微信分账流水记录表';
