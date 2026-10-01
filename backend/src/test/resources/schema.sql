-- 测试环境专用：ShedLock 锁表（H2 语法）
--
-- 生产环境该表由 Flyway 迁移脚本 V2 创建（MySQL 语法），测试环境关闭了 Flyway、
-- 使用 Hibernate create-drop 自动生成实体表，因此需要单独提供这张非实体表的定义。
CREATE TABLE IF NOT EXISTS shedlock (
    name       VARCHAR(64)  NOT NULL,
    lock_until TIMESTAMP(3) NULL,
    locked_at  TIMESTAMP(3) NULL,
    locked_by  VARCHAR(255) NULL,
    PRIMARY KEY (name)
);
