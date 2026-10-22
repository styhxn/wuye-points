-- =====================================================================
-- V23 电商收付通补齐表结构（split_recovery_records / merchant_transfers /
--      merchant_transfer_accounts）
--
-- 背景：V16 仅创建了 merchant_wechat_accounts；电商收付通「个人接收方不可回退」
-- 垫付追回台账与「四层树内部结算转账」三张表此前只存在于 wuye_points_latest.sql，
-- 无 Flyway 脚本——生产环境 Flyway 关闭 + ddl-auto=none，新环境极易漏建。
-- 本脚本补齐，结构与 wuye_points_latest.sql:4883 / :2612 / :2647 一致。
--
-- 幂等：全部 CREATE TABLE IF NOT EXISTS，可重复执行。
-- =====================================================================

-- 1) 个人接收方不可回退分账——平台垫付待追回台账
CREATE TABLE IF NOT EXISTS split_recovery_records
(
    id             VARCHAR(32) NOT NULL COMMENT '主键 srr_',
    split_record_id VARCHAR(32) NOT NULL COMMENT '分账记录ID wechat_split_record.id',
    order_id       VARCHAR(32) NOT NULL COMMENT '订单ID',
    owner_type     VARCHAR(32) NOT NULL COMMENT '角色：COURIER等',
    owner_id       VARCHAR(32)          DEFAULT NULL COMMENT '角色ID（快递员ID）',
    amount         DECIMAL(10, 2) NOT NULL COMMENT '待追回金额（元）',
    status         VARCHAR(24) NOT NULL DEFAULT 'pending' COMMENT 'pending(待追回)/recovered(已追回)/written_off(已核销)',
    remark         VARCHAR(500)         DEFAULT NULL COMMENT '备注（追回方式/线下处理记录）',
    created_at     TIMESTAMP   NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at     TIMESTAMP   NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_split (split_record_id),
    KEY idx_status (status)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='个人接收方不可回退分账-平台垫付待追回台账';

-- 2) 四层树角色内部结算转账单（商家转账）
CREATE TABLE IF NOT EXISTS merchant_transfers
(
    id                 VARCHAR(32) NOT NULL COMMENT '主键 mt_',
    owner_type         VARCHAR(32) NOT NULL COMMENT '角色：PROPERTY/COORDINATOR/SECTOR_LEADER/INDIVIDUAL_LEADER',
    owner_id           VARCHAR(32) NOT NULL COMMENT '角色ID',
    biz_id             VARCHAR(64)          DEFAULT NULL COMMENT '业务ID（订单号/分账 group_no）',
    amount             DECIMAL(10, 2) NOT NULL COMMENT '转账金额（元）',
    channel            VARCHAR(16) NOT NULL DEFAULT 'zero' COMMENT '转账渠道：zero/bank',
    payee_account      VARCHAR(64)          DEFAULT NULL COMMENT '收款账号（openid 或脱敏银行卡，冗余展示）',
    status             VARCHAR(24) NOT NULL DEFAULT 'pending' COMMENT 'pending/processing/success/failed/closed',
    wx_batch_id        VARCHAR(64)          DEFAULT NULL COMMENT '微信转账批次号 batch_id',
    wx_transfer_bill_id VARCHAR(64)         DEFAULT NULL COMMENT '微信转账单号 transfer_bill_id',
    fail_reason        VARCHAR(500)         DEFAULT NULL COMMENT '失败原因',
    retry_count        INT         NOT NULL DEFAULT 0 COMMENT '重试次数',
    created_at         TIMESTAMP   NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at         TIMESTAMP   NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    KEY idx_owner (owner_type, owner_id),
    KEY idx_status (status),
    KEY idx_biz (biz_id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='四层树角色内部结算转账单（商家转账）';

-- 3) 四层树角色商家转账收款账号
CREATE TABLE IF NOT EXISTS merchant_transfer_accounts
(
    id                 VARCHAR(32) NOT NULL COMMENT '主键 mta_',
    owner_type         VARCHAR(32) NOT NULL COMMENT '角色：PROPERTY/COORDINATOR/SECTOR_LEADER/INDIVIDUAL_LEADER',
    owner_id           VARCHAR(32) NOT NULL COMMENT '角色ID（物业公司/统筹/板块/个体负责人ID）',
    channel            VARCHAR(16) NOT NULL DEFAULT 'zero' COMMENT '收款渠道：zero(微信零钱)/bank(银行卡)',
    openid             VARCHAR(64)          DEFAULT NULL COMMENT '微信 openid（zero 渠道）',
    bank_account_no_enc VARCHAR(1024)        DEFAULT NULL COMMENT '银行卡号（RSA加密，bank 渠道）',
    bank_name          VARCHAR(128)         DEFAULT NULL COMMENT '开户银行全称（bank 渠道）',
    account_name_enc   VARCHAR(512)         DEFAULT NULL COMMENT '持卡人姓名（RSA加密，bank 渠道）',
    cert_serial        VARCHAR(64)          DEFAULT NULL COMMENT '加密所用微信平台证书序列号（证书轮换后需重新绑定）',
    verified           TINYINT     NOT NULL DEFAULT 0 COMMENT '是否已核验可用',
    created_at         TIMESTAMP   NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at         TIMESTAMP   NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_owner (owner_type, owner_id, channel),
    KEY idx_verified (verified)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='四层树角色商家转账收款账号';
