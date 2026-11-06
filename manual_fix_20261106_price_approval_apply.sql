-- ============================================================
-- 价格审批「通过后未落库」历史数据补偿（2026-11-06）
-- 背景：v8.46 之前 merchant_distribution 审批通过只写 merchants.individual_leader_rate
--       （全仓库无读取方），delivery_fee 审批通过完全不落库。
--       已 approved 的历史审批单不会自动重放，需要按本脚本手工补偿。
-- 用法：prod 已关闭 Flyway，本脚本手工执行；先跑第 1 段预览，确认无误再跑第 2/3 段。
-- 幂等：按「同一对象最近一次 approved」取值，重复执行结果一致。
-- ============================================================

-- ---------- 1) 预览：将被补偿的记录 ----------
-- 抽佣（平台抽佣比例 → merchant_property_configs.commission_rate）
SELECT pa.id            AS approval_id,
       pa.item_id       AS merchant_id,
       pa.property_company_id,
       pa.new_value     AS approved_value,
       pa.approved_at,
       mpc.commission_rate AS current_commission_rate
FROM price_approvals pa
         JOIN merchants m ON m.id = pa.item_id
         LEFT JOIN merchant_property_configs mpc
                   ON mpc.merchant_id = pa.item_id AND mpc.property_company_id = pa.property_company_id
WHERE pa.item_type = 'merchant_distribution'
  AND pa.status = 'approved'
  AND pa.approved_at = (SELECT MAX(pa2.approved_at)
                        FROM price_approvals pa2
                        WHERE pa2.item_type = 'merchant_distribution'
                          AND pa2.status = 'approved'
                          AND pa2.item_id = pa.item_id
                          AND pa2.property_company_id = pa.property_company_id)
ORDER BY pa.approved_at;

-- 配送费（→ merchants.delivery_fee）
SELECT pa.id AS approval_id, pa.item_id AS merchant_id, pa.new_value AS approved_value, pa.approved_at,
       m.delivery_fee AS current_delivery_fee
FROM price_approvals pa
         JOIN merchants m ON m.id = pa.item_id
WHERE pa.item_type = 'delivery_fee'
  AND pa.status = 'approved'
  AND pa.approved_at = (SELECT MAX(pa2.approved_at)
                        FROM price_approvals pa2
                        WHERE pa2.item_type = 'delivery_fee'
                          AND pa2.status = 'approved'
                          AND pa2.item_id = pa.item_id)
ORDER BY pa.approved_at;

-- ---------- 2) 补偿抽佣比例 ----------
-- newValue 兼容两种格式：纯数字 0.1（个体负责人接口）与 {"rate":0.1}（管理端发起申请）。
-- 只接受能解析成 0~1 的行，其余保持原值不动。
UPDATE merchant_property_configs mpc
    JOIN price_approvals pa
    ON pa.item_id = mpc.merchant_id
        AND pa.property_company_id = mpc.property_company_id
        AND pa.item_type = 'merchant_distribution'
        AND pa.status = 'approved'
SET mpc.commission_rate = CASE
    WHEN JSON_VALID(pa.new_value)
        THEN CAST(JSON_UNQUOTE(JSON_EXTRACT(pa.new_value, '$.rate')) AS DECIMAL(5, 4))
    ELSE CAST(pa.new_value AS DECIMAL(5, 4))
    END
WHERE pa.approved_at = (SELECT x.max_at
                        FROM (SELECT item_id, property_company_id, MAX(approved_at) AS max_at
                              FROM price_approvals
                              WHERE item_type = 'merchant_distribution' AND status = 'approved'
                              GROUP BY item_id, property_company_id) x
                        WHERE x.item_id = pa.item_id
                          AND x.property_company_id = pa.property_company_id)
  AND (JSON_VALID(pa.new_value) OR pa.new_value REGEXP '^(0|1|0\\.[0-9]{1,4}|1\\.0{1,4})$')
  AND (
        (JSON_VALID(pa.new_value)
            AND JSON_EXTRACT(pa.new_value, '$.rate') BETWEEN 0 AND 1)
        OR (NOT JSON_VALID(pa.new_value)
            AND CAST(pa.new_value AS DECIMAL(5, 4)) BETWEEN 0 AND 1)
  );

-- ---------- 3) 补偿配送费 ----------
-- newValue 兼容 {"amount":1.5} 与纯数字；上限按字段口径 9999.99 兜住。
UPDATE merchants m
    JOIN price_approvals pa
    ON pa.item_id = m.id
        AND pa.item_type = 'delivery_fee'
        AND pa.status = 'approved'
SET m.delivery_fee = CASE
    WHEN JSON_VALID(pa.new_value)
        THEN CAST(JSON_UNQUOTE(JSON_EXTRACT(pa.new_value, '$.amount')) AS DECIMAL(10, 2))
    ELSE CAST(pa.new_value AS DECIMAL(10, 2))
    END
WHERE pa.approved_at = (SELECT x.max_at
                        FROM (SELECT item_id, MAX(approved_at) AS max_at
                              FROM price_approvals
                              WHERE item_type = 'delivery_fee' AND status = 'approved'
                              GROUP BY item_id) x
                        WHERE x.item_id = pa.item_id)
  AND (
        (JSON_VALID(pa.new_value)
            AND JSON_EXTRACT(pa.new_value, '$.amount') BETWEEN 0 AND 9999.99)
        OR (NOT JSON_VALID(pa.new_value)
            AND pa.new_value REGEXP '^[0-9]+(\\.[0-9]{1,2})?$')
  );

-- ---------- 4) 校验：补偿后应满足 ----------
SELECT m.id, m.name, m.delivery_fee, mpc.commission_rate
FROM merchants m
         LEFT JOIN merchant_property_configs mpc ON mpc.merchant_id = m.id
WHERE m.id IN ('mch_386992df981000', 'mch_355d11a1f21000', 'mch_demo001');

-- 说明：
-- * 本脚本不改 price_approvals 状态（历史单保持 approved 留痕）。
-- * merchants.individual_leader_rate 是无人读取的历史列，脚本不动它；如需清理请单独评估。
-- * 若某商家在多个物业公司下都有配置，第 2 段按 (商家, 物业公司) 精确匹配，不会串改其它物业。
