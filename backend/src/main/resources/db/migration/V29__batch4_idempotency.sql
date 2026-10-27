-- =====================================================================
-- V29 上线修复第四批：接口幂等记录表（C1）
--
-- 用途：前端在写接口携带 X-Request-Id；同一用户 + 同一路径 + 同一 requestId
--       只执行一次，重复请求回放首次成功响应。
--
-- 执行顺序：V28 之后执行。
-- 幂等：CREATE TABLE IF NOT EXISTS。
-- =====================================================================

CREATE TABLE IF NOT EXISTS `idempotency_records` (
  `id` varchar(32) NOT NULL,
  `user_id` varchar(32) NOT NULL,
  `request_key` varchar(64) NOT NULL COMMENT 'SHA-256(method:path:requestId)',
  `request_id` varchar(128) NOT NULL COMMENT '前端 X-Request-Id 原文',
  `method` varchar(10) NOT NULL,
  `path` varchar(255) NOT NULL,
  `status` varchar(20) NOT NULL COMMENT 'processing/completed',
  `response_status` int DEFAULT NULL,
  `response_body` longtext,
  `expires_at` datetime NOT NULL,
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_idem_user_key` (`user_id`, `request_key`),
  KEY `idx_idem_expires` (`expires_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci
  COMMENT='接口幂等记录：processing 2 分钟，completed 2 小时';
