-- V24: 一级代理申请表
-- 2026-10-20 前端诉求 R1/R5：补齐一级代理「申请」落点。
-- 用户自助提交申请 → 管理端审核 → 通过后由后端同事务创建/关联 merchants 载体并置 is_distributor=1、audit_status=approved。
-- merchants.is_distributor 字段已由 V22 引入，此处不再重复。
CREATE TABLE IF NOT EXISTS `distributor_applications` (
  `id` varchar(32) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '主键 dap_xxx',
  `resident_id` varchar(32) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '申请人住户ID',
  `property_company_id` varchar(32) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '物业公司ID',
  `contact_name` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '联系人姓名',
  `contact_phone` varchar(20) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '联系电话',
  `business_scope` varchar(30) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '经营品类 fresh/daily/grain/other',
  `service_scope` varchar(20) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '服务范围 community/city',
  `remark` varchar(200) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '备注',
  `audit_status` varchar(20) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'pending' COMMENT 'pending/approved/rejected',
  `reject_reason` varchar(200) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '拒绝原因',
  `distributor_merchant_id` varchar(32) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '审核通过后创建/关联的商家载体ID',
  `audited_by` varchar(32) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '审核人ID',
  `audited_at` datetime DEFAULT NULL COMMENT '审核时间',
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_dap_resident` (`resident_id`),
  KEY `idx_dap_property` (`property_company_id`),
  KEY `idx_dap_status` (`audit_status`),
  KEY `idx_dap_property_status` (`property_company_id`,`audit_status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='一级代理申请表';
