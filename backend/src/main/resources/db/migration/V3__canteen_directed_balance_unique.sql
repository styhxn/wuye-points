-- =============================================================================
-- V3：社区食堂定向余额账户唯一约束
--
-- 背景：CanteenService 中定向余额账户采用「查不到就新建」的写法。
-- 并发首次充值时两个事务可能都查不到、各自插入一条，导致同一 (住户, 主商家)
-- 出现两条账户记录，后续按账户扣减时只会命中其中一条，造成余额错乱。
--
-- 修复：加唯一索引，从数据库层面保证账户唯一。代码侧已改用
-- findByResidentIdAndMainMerchantIdForUpdate 行锁，唯一索引作为最终兜底。
-- =============================================================================

-- 先清理历史重复数据：同一 (resident_id, main_merchant_id) 只保留 id 最小的一条。
-- 被删除账户的余额已合并到保留记录，避免资金丢失。
UPDATE `canteen_directed_balances` keep
    JOIN (
        SELECT resident_id, main_merchant_id, MIN(id) AS keep_id, SUM(balance) AS total_balance
        FROM `canteen_directed_balances`
        GROUP BY resident_id, main_merchant_id
        HAVING COUNT(*) > 1
    ) dup
    ON keep.resident_id = dup.resident_id
        AND keep.main_merchant_id = dup.main_merchant_id
        AND keep.id = dup.keep_id
SET keep.balance = dup.total_balance;

DELETE dup
FROM `canteen_directed_balances` dup
         JOIN (
    SELECT resident_id, main_merchant_id, MIN(id) AS keep_id
    FROM `canteen_directed_balances`
    GROUP BY resident_id, main_merchant_id
    HAVING COUNT(*) > 1
) k
              ON dup.resident_id = k.resident_id
                  AND dup.main_merchant_id = k.main_merchant_id
                  AND dup.id > k.keep_id;

SET @idx_exists := (SELECT COUNT(*)
                    FROM information_schema.STATISTICS
                    WHERE TABLE_SCHEMA = DATABASE()
                      AND TABLE_NAME = 'canteen_directed_balances'
                      AND INDEX_NAME = 'uk_canteen_directed_balance');
SET @ddl := IF(@idx_exists = 0,
               'CREATE UNIQUE INDEX `uk_canteen_directed_balance` ON `canteen_directed_balances` (`resident_id`, `main_merchant_id`)',
               'SELECT 1');
PREPARE stmt FROM @ddl; EXECUTE stmt; DEALLOCATE PREPARE stmt;
