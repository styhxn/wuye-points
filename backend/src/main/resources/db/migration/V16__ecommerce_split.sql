-- =====================================================================
-- V16 电商收付通（平台收付通）分账改造
--
-- 背景：资金分账从「微信普通分账 /v3/profitsharing/*（资金先进平台归集）」
-- 切换为「微信电商收付通 /v3/ecommerce/profitsharing/*（资金直进二级商户，
-- 平台调 API 主动分账，避免二清）」。详见项目《支付分账逻辑》与电商收付通改造说明。
--
-- 本脚本为手工执行脚本（application.yml 中 flyway.enabled=false）。
-- 执行前请先在微信服务商平台完成：全新服务商商户号 + ICP/EDI 许可 + 开通平台收付通。
-- =====================================================================

-- 1) 商户 ↔ 微信二级商户号 映射表（电商收付通进件结果落库）
CREATE TABLE IF NOT EXISTS merchant_wechat_accounts
(
    id            VARCHAR(32)  NOT NULL COMMENT '主键 mwa_',
    merchant_id   VARCHAR(32)  NOT NULL COMMENT '商家ID mch_',
    sub_mchid     VARCHAR(32)  NOT NULL COMMENT '微信二级商户号（电商收付通进件后分配）',
    merchant_name VARCHAR(128)          COMMENT '二级商户主体名称（营业执照/身份证姓名）',
    status        VARCHAR(20)  NOT NULL DEFAULT 'active' COMMENT 'unbound/auditing/active/frozen',
    created_at    DATETIME              COMMENT '创建时间',
    updated_at    DATETIME              COMMENT '更新时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_merchant_id (merchant_id),
    UNIQUE KEY uk_sub_mchid (sub_mchid)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='商户微信二级商户账号（电商收付通）';

-- 2) 支付流水记录实际入账的二级商户号（下单/查询/退款/关闭时按订单商家取号）
ALTER TABLE payment_records
    ADD COLUMN sub_mchid VARCHAR(32) NULL COMMENT '该笔支付实际入账的微信二级商户号（电商收付通）' AFTER channel_order_no;
