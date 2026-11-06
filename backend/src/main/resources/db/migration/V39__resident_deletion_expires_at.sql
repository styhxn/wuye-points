-- =====================================================================
-- V39 账号注销冷静期独立列（2026-11-05 / 整改清单条目9、条目10）
--
-- 背景：申请注销时后端把「15 天冷静期到期时间」写进了 residents.deleted_at，
--   而 deleted_at 同时是全站「已软删除」的判据（AuthInterceptor 登录拦截、
--   getResidentOrThrow、微信认领候选、住户列表 Specification 都按它过滤），
--   两个语义互斥导致三条线上后果：
--     1) 提交注销后本人被判定「账号被禁用」，无法登录也无法撤回；
--     2) 管理端审核走 getResidentOrThrow，deleted_at 非空即 404，approve/reject 必失败；
--     3) 没有到期自动注销，账号永久卡在 pending_deletion。
--
-- 口径（老板 2026-11-05 确认）：冷静期内允许登录并自助撤回，到期由调度器自动注销。
--   因此冷静期时间改由 deletion_expires_at 承载，deleted_at 只在真正注销时写。
--
-- 内容：
--   1) residents.deletion_expires_at DATETIME NULL —— 注销申请冷静期到期时间；
--   2) 存量自愈：status='pending_deletion' 的行把 deleted_at 搬到 deletion_expires_at
--      并清空 deleted_at（到期行由 AccountDeletionScheduler 下一轮自动完成注销）。
--
-- ⚠️ 生产 spring.flyway.enabled=false，本文件仅留档；实际执行走
--   sql/manual_upgrade_20261105_resident_deletion_expires_at.sql（纯 ALTER+UPDATE，
--   不建存储过程：wuye_points 库 log_bin_trust_function_creators=OFF 且 wuye_app 无 SUPER，
--   CREATE PROCEDURE 会报 1418）。
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
        SET @ddl = CONCAT('ALTER TABLE ', p_table, ' ADD COLUMN ', p_ddl);
        PREPARE stmt FROM @ddl;
        EXECUTE stmt;
        DEALLOCATE PREPARE stmt;
    END IF;
END //
DELIMITER ;

CALL p_wuye_add_column('residents', 'deletion_expires_at',
                       'deletion_expires_at DATETIME NULL COMMENT ''注销申请冷静期到期时间''');

DROP PROCEDURE IF EXISTS p_wuye_add_column;

-- 存量自愈：deleted_at 曾被当作冷静期时间使用
UPDATE residents
SET deletion_expires_at = COALESCE(deletion_expires_at, deleted_at),
    deleted_at = NULL
WHERE status = 'pending_deletion'
  AND deleted_at IS NOT NULL;
