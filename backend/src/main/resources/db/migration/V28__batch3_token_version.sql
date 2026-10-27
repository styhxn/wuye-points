-- =====================================================================
-- V28 上线修复第三批：令牌版本号（登出/强制下线即时失效）
--
-- 内容：
--   residents.token_version：登录/刷新时写入 JWT 的 tv 声明；
--   登出或强制下线时 +1，旧 access/refresh token 立即失效。
--
-- 执行顺序：V27 之后执行。
-- 幂等：列存在则跳过。
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

CALL p_wuye_add_column('residents', 'token_version',
    '`token_version` INT NOT NULL DEFAULT 0 COMMENT ''令牌版本号，登出/强制下线时+1''');

UPDATE residents SET token_version = 0 WHERE token_version IS NULL;

DROP PROCEDURE IF EXISTS p_wuye_add_column;
