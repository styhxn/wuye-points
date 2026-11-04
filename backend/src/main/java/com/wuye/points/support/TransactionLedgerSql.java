package com.wuye.points.support;

import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * 平台交易明细的统一账本 SQL。
 *
 * <p>交易数据分散在支付、钱包、分账、提现、结算、积分、物业币等多张表。
 * 本类把它们投影成同一组列后 UNION ALL，再统一筛选、排序、分页，
 * 避免在内存里合并后再分页导致的结果错乱。</p>
 *
 * <p>金额口径：{@code amount} 始终为非负数，收/支由 {@code direction} 表示；
 * 积分、物业币、定向余额使用独立 {@code unit}，禁止跨单位求和。</p>
 */
@Component
public class TransactionLedgerSql {

    public static final Set<String> CATEGORIES = Set.of(
            "payment", "refund", "wallet", "distribution", "split",
            "withdrawal", "transfer", "settlement", "point", "coin", "canteen");

    public static final Set<String> GROUPS = Set.of(
            TransactionLabels.GROUP_PAYMENT, TransactionLabels.GROUP_WALLET,
            TransactionLabels.GROUP_SETTLEMENT, TransactionLabels.GROUP_VALUE);

    /** {@link #byId} 的兜底时间窗：id 谓词已下推到分支主键，窗口只用于命中 created_at 索引 */
    private static final LocalDateTime EPOCH = LocalDateTime.of(1970, 1, 1, 0, 0);
    private static final LocalDateTime FAR_FUTURE = LocalDateTime.of(2100, 1, 1, 0, 0);

    private static final Map<String, String> GROUP_OF = Map.ofEntries(
            Map.entry("payment", TransactionLabels.GROUP_PAYMENT),
            Map.entry("refund", TransactionLabels.GROUP_PAYMENT),
            Map.entry("wallet", TransactionLabels.GROUP_WALLET),
            Map.entry("distribution", TransactionLabels.GROUP_WALLET),
            Map.entry("split", TransactionLabels.GROUP_WALLET),
            Map.entry("withdrawal", TransactionLabels.GROUP_SETTLEMENT),
            Map.entry("transfer", TransactionLabels.GROUP_SETTLEMENT),
            Map.entry("settlement", TransactionLabels.GROUP_SETTLEMENT),
            Map.entry("point", TransactionLabels.GROUP_VALUE),
            Map.entry("coin", TransactionLabels.GROUP_VALUE),
            Map.entry("canteen", TransactionLabels.GROUP_VALUE)
    );

    public record Filters(String category,
                          String group,
                          String direction,
                          String status,
                          String keyword,
                          String propertyCompanyId,
                          String id) {
    }

    public record SqlQuery(String sql, MapSqlParameterSource params) {
    }

    public static String groupOf(String category) {
        return GROUP_OF.get(category);
    }

    public SqlQuery list(Filters filters, LocalDateTime start, LocalDateTime end, int limit, int offset) {
        MapSqlParameterSource params = params(filters, start, end)
                .addValue("limit", limit)
                .addValue("offset", offset);
        String sql = "SELECT q.* FROM (" + enriched(filters, true) + ") q"
                + where(filters)
                + " ORDER BY q.occurred_at DESC, q.id DESC, q.category ASC"
                + " LIMIT :limit OFFSET :offset";
        return new SqlQuery(sql, params);
    }

    /**
     * 按「分类 + 记录ID」精确取一条流水（打印流水详情用）。
     *
     * <p>{@code id} 谓词下推到分支内部，走各来源表的主键，不会把整段账本物化后再筛；
     * 时间窗只作兜底。{@code payment} 与 {@code refund} 共用同一条 {@code payment_records.id}，
     * 所以必须连 {@code category} 一起传，只给 ID 无法区分是收款还是退款。</p>
     */
    public SqlQuery byId(String category, String id) {
        Filters filters = new Filters(category, null, null, null, null, null, id);
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("start", EPOCH)
                .addValue("end", FAR_FUTURE)
                .addValue("id", id);
        String sql = "SELECT q.* FROM (" + enriched(filters, true) + ") q"
                + " ORDER BY q.occurred_at DESC LIMIT 5";
        return new SqlQuery(sql, params);
    }

    public SqlQuery count(Filters filters, LocalDateTime start, LocalDateTime end) {
        String sql = "SELECT COUNT(*) FROM (" + enriched(filters, filters.keyword() != null) + ") q"
                + where(filters);
        return new SqlQuery(sql, params(filters, start, end));
    }

    public SqlQuery export(Filters filters, LocalDateTime start, LocalDateTime end, int limit) {
        MapSqlParameterSource params = params(filters, start, end).addValue("limit", limit);
        String sql = "SELECT q.* FROM (" + enriched(filters, true) + ") q"
                + where(filters)
                + " ORDER BY q.occurred_at DESC, q.id DESC, q.category ASC"
                + " LIMIT :limit";
        return new SqlQuery(sql, params);
    }

    public SqlQuery summary(Filters filters, LocalDateTime start, LocalDateTime end) {
        String sql = "SELECT q.category, q.group_code, q.unit, q.direction, q.status,"
                + " COUNT(*) AS cnt,"
                + " COALESCE(SUM(q.amount), 0) AS volume_amount,"
                + " COALESCE(SUM(CASE WHEN q.direction = 'income' THEN q.amount ELSE 0 END), 0) AS income_amount,"
                + " COALESCE(SUM(CASE WHEN q.direction = 'expense' THEN q.amount ELSE 0 END), 0) AS expense_amount,"
                + " COALESCE(SUM(q.fee_amount), 0) AS fee_amount,"
                + " COALESCE(SUM(q.net_amount), 0) AS actual_amount,"
                + " COALESCE(SUM(q.platform_amount), 0) AS platform_amount"
                + " FROM (" + enriched(filters, filters.keyword() != null) + ") q"
                + where(filters)
                + " GROUP BY q.category, q.group_code, q.unit, q.direction, q.status"
                + " ORDER BY q.group_code, q.category, q.status";
        return new SqlQuery(sql, params(filters, start, end));
    }

    public SqlQuery trend(Filters filters, LocalDateTime start, LocalDateTime end) {
        String where = where(filters) + " AND q.occurred_at IS NOT NULL";
        String sql = "SELECT CAST(q.occurred_at AS DATE) AS stat_date, q.unit,"
                + " COALESCE(SUM(CASE WHEN q.direction = 'income' THEN q.amount ELSE 0 END), 0) AS income_amount,"
                + " COALESCE(SUM(CASE WHEN q.direction = 'expense' THEN q.amount ELSE 0 END), 0) AS expense_amount,"
                + " COUNT(*) AS cnt"
                + " FROM (" + enriched(filters, filters.keyword() != null) + ") q"
                + where
                + " GROUP BY CAST(q.occurred_at AS DATE), q.unit"
                + " ORDER BY stat_date, q.unit";
        return new SqlQuery(sql, params(filters, start, end));
    }

    private MapSqlParameterSource params(Filters filters, LocalDateTime start, LocalDateTime end) {
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("start", start)
                .addValue("end", end);
        if (filters.propertyCompanyId() != null) {
            params.addValue("pc", filters.propertyCompanyId());
        }
        if (filters.category() != null) {
            params.addValue("category", filters.category());
        }
        if (filters.group() != null) {
            params.addValue("groupCode", filters.group());
        }
        if (filters.direction() != null) {
            params.addValue("direction", filters.direction());
        }
        if (filters.status() != null) {
            params.addValue("status", filters.status());
        }
        if (filters.keyword() != null) {
            params.addValue("keyword", "%" + filters.keyword().toLowerCase(Locale.ROOT) + "%");
        }
        if (filters.id() != null) {
            params.addValue("id", filters.id());
        }
        return params;
    }

    /** id 谓词下推到来源表，避免整段账本物化后再筛（详情/打印是高频单条查询）。 */
    private String idFilter(String expression, Filters filters) {
        return filters.id() == null ? "" : "\n      AND " + expression + " = :id";
    }

    private String where(Filters filters) {
        StringBuilder sql = new StringBuilder(" WHERE 1=1");
        if (filters.category() != null) {
            sql.append(" AND q.category = :category");
        }
        if (filters.group() != null) {
            sql.append(" AND q.group_code = :groupCode");
        }
        if (filters.direction() != null) {
            sql.append(" AND q.direction = :direction");
        }
        if (filters.status() != null) {
            sql.append(" AND q.status = :status");
        }
        if (filters.keyword() != null) {
            sql.append(" AND (LOWER(q.id) LIKE :keyword")
                    .append(" OR LOWER(COALESCE(q.biz_id, '')) LIKE :keyword")
                    .append(" OR LOWER(COALESCE(q.order_id, '')) LIKE :keyword")
                    .append(" OR LOWER(COALESCE(q.trade_no, '')) LIKE :keyword")
                    .append(" OR LOWER(COALESCE(q.party_name, '')) LIKE :keyword")
                    .append(" OR LOWER(COALESCE(q.description, '')) LIKE :keyword")
                    .append(" OR LOWER(COALESCE(q.property_company_name, '')) LIKE :keyword)");
        }
        return sql.toString();
    }

    private String enriched(Filters filters, boolean includeNames) {
        String union = String.join("\nUNION ALL\n", branches(filters));
        if (!includeNames) {
            return "SELECT l.*, NULL AS property_company_name, NULL AS party_name FROM ("
                    + union + "\n) l";
        }
        return """
                SELECT l.*,
                       pc.name AS property_company_name,
                       CASE LOWER(l.party_type)
                         WHEN 'resident' THEN (SELECT r.name FROM residents r WHERE r.id = l.party_id)
                         WHEN 'merchant' THEN (SELECT m.name FROM merchants m WHERE m.id = l.party_id)
                         WHEN 'property' THEN (SELECT p.name FROM property_companies p WHERE p.id = l.party_id)
                         WHEN 'coordinator' THEN (SELECT r.name FROM coordinators c JOIN residents r ON r.id = c.resident_id WHERE c.id = l.party_id)
                         WHEN 'sector_leader' THEN (SELECT r.name FROM sector_leaders s JOIN residents r ON r.id = s.resident_id WHERE s.id = l.party_id)
                         WHEN 'individual_leader' THEN (SELECT i.name FROM individual_leaders i WHERE i.id = l.party_id)
                         WHEN 'building_leader' THEN (SELECT r.name FROM building_leaders b JOIN residents r ON r.id = b.resident_id WHERE b.id = l.party_id)
                         WHEN 'property_manager' THEN (SELECT r.name FROM residents r WHERE r.id = l.party_id)
                         WHEN 'courier' THEN (SELECT c.name FROM courier_managers c WHERE c.id = l.party_id)
                         WHEN 'platform' THEN '平台'
                         ELSE NULL
                       END AS party_name
                FROM (
                """ + union + "\n) l\n"
                + "LEFT JOIN property_companies pc ON pc.id = l.property_company_id";
    }

    private List<String> branches(Filters filters) {
        List<String> branches = new ArrayList<>();
        if (include(filters, "payment")) {
            branches.add(payment(filters));
        }
        if (include(filters, "refund")) {
            branches.add(refund(filters));
        }
        if (include(filters, "wallet")) {
            branches.add(wallet(filters));
        }
        if (include(filters, "distribution")) {
            branches.add(distribution(filters));
        }
        if (include(filters, "split")) {
            branches.add(split(filters));
        }
        if (include(filters, "withdrawal")) {
            branches.add(merchantWithdrawal(filters));
            branches.add(coinWithdrawal(filters));
            branches.add(roleWithdrawal(filters));
        }
        if (include(filters, "transfer")) {
            branches.add(transfer(filters));
        }
        if (include(filters, "settlement")) {
            branches.add(settlement(filters));
        }
        if (include(filters, "point")) {
            branches.add(point(filters));
        }
        if (include(filters, "coin")) {
            branches.add(coin(filters));
        }
        if (include(filters, "canteen")) {
            branches.add(canteen(filters));
        }
        return branches;
    }

    private boolean include(Filters filters, String category) {
        if (filters.category() != null) {
            return filters.category().equals(category);
        }
        if (filters.group() != null) {
            return filters.group().equals(groupOf(category));
        }
        return true;
    }

    private String pcFilter(String expression, Filters filters) {
        return filters.propertyCompanyId() == null ? "" : "\n      AND " + expression + " = :pc";
    }

    private String payment(Filters filters) {
        return """
                SELECT 'payment' AS category, 'PAYMENT' AS group_code, p.id AS id,
                       COALESCE(p.paid_at, p.created_at) AS occurred_at,
                       p.property_company_id AS property_company_id,
                       CASE WHEN p.status IN ('paid', 'refunding', 'refunded', 'refund_failed', 'amount_mismatch')
                            THEN 'income' ELSE 'neutral' END AS direction,
                       'CNY' AS unit,
                       COALESCE(p.amount, 0.00) AS amount,
                       0.00 AS fee_amount,
                       COALESCE(p.cash_amount, 0.00) AS net_amount,
                       0.00 AS platform_amount,
                       p.status AS status,
                       p.biz_type AS biz_type,
                       COALESCE(p.error_msg, '') AS description,
                       CASE WHEN p.biz_type = 'order' THEN p.biz_id ELSE NULL END AS order_id,
                       p.biz_id AS biz_id,
                       COALESCE(p.channel_trade_no, p.out_trade_no) AS trade_no,
                       'resident' AS party_type,
                       p.resident_id AS party_id,
                       COALESCE(p.cash_amount, 0.00) AS cash_amount,
                       COALESCE(p.point_used, 0) AS point_used,
                       COALESCE(p.coin_used, 0.00) AS coin_used,
                       COALESCE(p.directed_balance_used, 0.00) AS directed_used
                FROM payment_records p
                WHERE p.created_at BETWEEN :start AND :end
                """ + pcFilter("p.property_company_id", filters)
                + idFilter("p.id", filters);
    }

    private String refund(Filters filters) {
        return """
                SELECT 'refund' AS category, 'PAYMENT' AS group_code, p.id AS id,
                       COALESCE(p.updated_at, p.paid_at, p.created_at) AS occurred_at,
                       p.property_company_id AS property_company_id,
                       CASE WHEN p.status = 'refund_failed' THEN 'neutral' ELSE 'expense' END AS direction,
                       'CNY' AS unit,
                       COALESCE(p.cash_amount, 0.00) AS amount,
                       0.00 AS fee_amount,
                       COALESCE(p.cash_amount, 0.00) AS net_amount,
                       0.00 AS platform_amount,
                       p.status AS status,
                       'refund' AS biz_type,
                       COALESCE(p.error_msg, '') AS description,
                       CASE WHEN p.biz_type = 'order' THEN p.biz_id ELSE NULL END AS order_id,
                       p.biz_id AS biz_id,
                       p.out_refund_no AS trade_no,
                       'resident' AS party_type,
                       p.resident_id AS party_id,
                       COALESCE(p.cash_amount, 0.00) AS cash_amount,
                       COALESCE(p.point_used, 0) AS point_used,
                       COALESCE(p.coin_used, 0.00) AS coin_used,
                       COALESCE(p.directed_balance_used, 0.00) AS directed_used
                FROM payment_records p
                WHERE p.updated_at BETWEEN :start AND :end
                  AND (p.out_refund_no IS NOT NULL OR p.status IN ('refunding', 'refunded', 'refund_failed'))
                """ + pcFilter("p.property_company_id", filters)
                + idFilter("p.id", filters);
    }

    private String wallet(Filters filters) {
        return """
                SELECT 'wallet' AS category, 'WALLET' AS group_code, w.id AS id,
                       w.created_at AS occurred_at,
                       CASE LOWER(w.owner_type)
                         WHEN 'merchant' THEN (SELECT m.property_company_id FROM merchants m WHERE m.id = w.owner_id)
                         WHEN 'property' THEN w.owner_id
                         WHEN 'platform' THEN (SELECT o.property_company_id FROM orders o WHERE o.id = w.order_id)
                         WHEN 'coordinator' THEN (SELECT c.property_company_id FROM coordinators c WHERE c.id = w.owner_id)
                         WHEN 'sector_leader' THEN (SELECT s.property_company_id FROM sector_leaders s WHERE s.id = w.owner_id)
                         WHEN 'individual_leader' THEN (SELECT i.property_company_id FROM individual_leaders i WHERE i.id = w.owner_id)
                         WHEN 'building_leader' THEN (SELECT b.property_company_id FROM building_leaders b WHERE b.id = w.owner_id)
                         WHEN 'property_manager' THEN (SELECT r.property_company_id FROM residents r WHERE r.id = w.owner_id)
                         WHEN 'courier' THEN (SELECT c.property_company_id FROM courier_managers c WHERE c.id = w.owner_id)
                         ELSE NULL
                       END AS property_company_id,
                       CASE WHEN w.amount < 0 THEN 'expense' ELSE 'income' END AS direction,
                       'CNY' AS unit,
                       ABS(w.amount) AS amount,
                       0.00 AS fee_amount,
                       ABS(w.amount) AS net_amount,
                       0.00 AS platform_amount,
                       CASE WHEN w.entry_type = 'distribution_reversal' THEN 'reversed' ELSE w.balance_type END AS status,
                       w.entry_type AS biz_type,
                       '' AS description,
                       w.order_id AS order_id,
                       COALESCE(w.distribution_record_id, w.delivery_id) AS biz_id,
                       w.idempotency_key AS trade_no,
                       LOWER(w.owner_type) AS party_type,
                       w.owner_id AS party_id,
                       0.00 AS cash_amount,
                       0 AS point_used,
                       0.00 AS coin_used,
                       0.00 AS directed_used
                FROM wallet_flows w
                WHERE w.created_at BETWEEN :start AND :end
                """ + pcFilter("""
                       CASE LOWER(w.owner_type)
                         WHEN 'merchant' THEN (SELECT m.property_company_id FROM merchants m WHERE m.id = w.owner_id)
                         WHEN 'property' THEN w.owner_id
                         WHEN 'platform' THEN (SELECT o.property_company_id FROM orders o WHERE o.id = w.order_id)
                         WHEN 'coordinator' THEN (SELECT c.property_company_id FROM coordinators c WHERE c.id = w.owner_id)
                         WHEN 'sector_leader' THEN (SELECT s.property_company_id FROM sector_leaders s WHERE s.id = w.owner_id)
                         WHEN 'individual_leader' THEN (SELECT i.property_company_id FROM individual_leaders i WHERE i.id = w.owner_id)
                         WHEN 'building_leader' THEN (SELECT b.property_company_id FROM building_leaders b WHERE b.id = w.owner_id)
                         WHEN 'property_manager' THEN (SELECT r.property_company_id FROM residents r WHERE r.id = w.owner_id)
                         WHEN 'courier' THEN (SELECT c.property_company_id FROM courier_managers c WHERE c.id = w.owner_id)
                         ELSE NULL
                       END""", filters)
                + idFilter("w.id", filters);
    }

    private String distribution(Filters filters) {
        return """
                SELECT 'distribution' AS category, 'WALLET' AS group_code, d.id AS id,
                       d.created_at AS occurred_at,
                       d.property_company_id AS property_company_id,
                       CASE WHEN d.distributable_amount > 0 THEN 'income' ELSE 'neutral' END AS direction,
                       'CNY' AS unit,
                       COALESCE(d.distributable_amount, 0.00) AS amount,
                       0.00 AS fee_amount,
                       COALESCE(d.total_amount, 0.00) AS net_amount,
                       COALESCE(d.platform_share, 0.00) AS platform_amount,
                       'settled' AS status,
                       'order_distribution' AS biz_type,
                       '' AS description,
                       d.order_id AS order_id,
                       d.merchant_id AS biz_id,
                       NULL AS trade_no,
                       'merchant' AS party_type,
                       d.merchant_id AS party_id,
                       0.00 AS cash_amount,
                       0 AS point_used,
                       0.00 AS coin_used,
                       0.00 AS directed_used
                FROM distribution_records d
                WHERE d.created_at BETWEEN :start AND :end
                """ + pcFilter("d.property_company_id", filters)
                + idFilter("d.id", filters);
    }

    private String split(Filters filters) {
        return """
                SELECT 'split' AS category, 'WALLET' AS group_code, s.id AS id,
                       s.created_at AS occurred_at,
                       COALESCE(
                         (SELECT p.property_company_id FROM payment_records p WHERE p.id = s.payment_record_id),
                         (SELECT o.property_company_id FROM orders o WHERE o.id = s.order_id)
                       ) AS property_company_id,
                       CASE WHEN s.status IN ('frozen_pending', 'frozen', 'finished') THEN 'income' ELSE 'neutral' END AS direction,
                       'CNY' AS unit,
                       COALESCE(s.freeze_amount, 0.00) AS amount,
                       0.00 AS fee_amount,
                       COALESCE(s.freeze_amount, 0.00) AS net_amount,
                       0.00 AS platform_amount,
                       s.status AS status,
                       LOWER(s.owner_type) AS biz_type,
                       COALESCE(s.remark, '') AS description,
                       s.order_id AS order_id,
                       s.group_no AS biz_id,
                       COALESCE(s.wechat_sub_trade_no, s.withdraw_trade_no) AS trade_no,
                       LOWER(s.owner_type) AS party_type,
                       s.owner_id AS party_id,
                       0.00 AS cash_amount,
                       0 AS point_used,
                       0.00 AS coin_used,
                       0.00 AS directed_used
                FROM wechat_split_record s
                WHERE s.created_at BETWEEN :start AND :end
                """ + pcFilter("""
                       COALESCE(
                         (SELECT p.property_company_id FROM payment_records p WHERE p.id = s.payment_record_id),
                         (SELECT o.property_company_id FROM orders o WHERE o.id = s.order_id)
                       )""", filters)
                + idFilter("s.id", filters);
    }

    private String merchantWithdrawal(Filters filters) {
        return """
                SELECT 'withdrawal' AS category, 'SETTLEMENT' AS group_code, m.id AS id,
                       COALESCE(m.completed_at, m.disburse_at, m.audited_at, m.created_at) AS occurred_at,
                       m.property_company_id AS property_company_id,
                       CASE WHEN m.audit_status = 'completed' OR m.disburse_status = 'success'
                            THEN 'expense' ELSE 'neutral' END AS direction,
                       'CNY' AS unit,
                       COALESCE(m.amount, 0.00) AS amount,
                       COALESCE(m.fee_amount, 0.00) AS fee_amount,
                       COALESCE(m.actual_amount, 0.00) AS net_amount,
                       COALESCE(m.platform_fee_share, 0.00) AS platform_amount,
                       CASE WHEN m.disburse_status = 'success' THEN 'completed'
                            WHEN m.disburse_status = 'failed' THEN 'disburse_failed'
                            ELSE m.audit_status END AS status,
                       'merchant_withdrawal' AS biz_type,
                       COALESCE(m.disburse_remark, '') AS description,
                       NULL AS order_id,
                       m.id AS biz_id,
                       m.disburse_trade_no AS trade_no,
                       'merchant' AS party_type,
                       m.merchant_id AS party_id,
                       0.00 AS cash_amount,
                       0 AS point_used,
                       0.00 AS coin_used,
                       0.00 AS directed_used
                FROM merchant_withdrawals m
                WHERE m.created_at BETWEEN :start AND :end
                """ + pcFilter("m.property_company_id", filters)
                + idFilter("m.id", filters);
    }

    private String coinWithdrawal(Filters filters) {
        return """
                SELECT 'withdrawal' AS category, 'SETTLEMENT' AS group_code, c.id AS id,
                       c.created_at AS occurred_at,
                       c.property_company_id AS property_company_id,
                       CASE WHEN c.audit_status = 'completed' THEN 'expense' ELSE 'neutral' END AS direction,
                       'CNY' AS unit,
                       COALESCE(c.cash_amount, 0.00) AS amount,
                       COALESCE(c.fee_amount, 0.00) AS fee_amount,
                       COALESCE(c.actual_amount, 0.00) AS net_amount,
                       0.00 AS platform_amount,
                       c.audit_status AS status,
                       'coin_withdrawal' AS biz_type,
                       CONCAT('物业币兑换：', COALESCE(c.coin_amount, 0.00), ' 物业币') AS description,
                       NULL AS order_id,
                       c.id AS biz_id,
                       NULL AS trade_no,
                       'merchant' AS party_type,
                       c.merchant_id AS party_id,
                       0.00 AS cash_amount,
                       0 AS point_used,
                       0.00 AS coin_used,
                       0.00 AS directed_used
                FROM coin_withdrawals c
                WHERE c.created_at BETWEEN :start AND :end
                """ + pcFilter("c.property_company_id", filters)
                + idFilter("c.id", filters);
    }

    private String roleWithdrawal(Filters filters) {
        return """
                SELECT 'withdrawal' AS category, 'SETTLEMENT' AS group_code, w.id AS id,
                       COALESCE(w.completed_at, w.disburse_at, w.audited_at, w.created_at) AS occurred_at,
                       w.property_company_id AS property_company_id,
                       CASE WHEN w.audit_status = 'completed' OR w.disburse_status = 'success'
                            THEN 'expense' ELSE 'neutral' END AS direction,
                       'CNY' AS unit,
                       COALESCE(w.amount, 0.00) AS amount,
                       COALESCE(w.fee_amount, 0.00) AS fee_amount,
                       COALESCE(w.actual_amount, 0.00) AS net_amount,
                       COALESCE(w.platform_fee_share, 0.00) AS platform_amount,
                       CASE WHEN w.disburse_status = 'success' THEN 'completed'
                            WHEN w.disburse_status = 'failed' THEN 'disburse_failed'
                            ELSE w.audit_status END AS status,
                       w.withdrawal_type AS biz_type,
                       COALESCE(w.disburse_remark, '') AS description,
                       NULL AS order_id,
                       w.id AS biz_id,
                       w.disburse_trade_no AS trade_no,
                       'resident' AS party_type,
                       w.resident_id AS party_id,
                       0.00 AS cash_amount,
                       0 AS point_used,
                       0.00 AS coin_used,
                       0.00 AS directed_used
                FROM withdrawal_records w
                WHERE w.created_at BETWEEN :start AND :end
                """ + pcFilter("w.property_company_id", filters)
                + idFilter("w.id", filters);
    }

    private String transfer(Filters filters) {
        return """
                SELECT 'transfer' AS category, 'SETTLEMENT' AS group_code, t.id AS id,
                       t.created_at AS occurred_at,
                       CASE LOWER(t.owner_type)
                         WHEN 'property' THEN t.owner_id
                         WHEN 'merchant' THEN (SELECT m.property_company_id FROM merchants m WHERE m.id = t.owner_id)
                         WHEN 'coordinator' THEN (SELECT c.property_company_id FROM coordinators c WHERE c.id = t.owner_id)
                         WHEN 'sector_leader' THEN (SELECT s.property_company_id FROM sector_leaders s WHERE s.id = t.owner_id)
                         WHEN 'individual_leader' THEN (SELECT i.property_company_id FROM individual_leaders i WHERE i.id = t.owner_id)
                         ELSE NULL
                       END AS property_company_id,
                       CASE WHEN t.status = 'success' THEN 'expense' ELSE 'neutral' END AS direction,
                       'CNY' AS unit,
                       COALESCE(t.amount, 0.00) AS amount,
                       0.00 AS fee_amount,
                       COALESCE(t.amount, 0.00) AS net_amount,
                       0.00 AS platform_amount,
                       t.status AS status,
                       LOWER(t.owner_type) AS biz_type,
                       COALESCE(t.fail_reason, '') AS description,
                       CASE WHEN t.biz_id LIKE 'ord%' THEN t.biz_id ELSE NULL END AS order_id,
                       t.biz_id AS biz_id,
                       COALESCE(t.wx_transfer_bill_id, t.wx_batch_id) AS trade_no,
                       LOWER(t.owner_type) AS party_type,
                       t.owner_id AS party_id,
                       0.00 AS cash_amount,
                       0 AS point_used,
                       0.00 AS coin_used,
                       0.00 AS directed_used
                FROM merchant_transfers t
                WHERE t.created_at BETWEEN :start AND :end
                """ + pcFilter("""
                       CASE LOWER(t.owner_type)
                         WHEN 'property' THEN t.owner_id
                         WHEN 'merchant' THEN (SELECT m.property_company_id FROM merchants m WHERE m.id = t.owner_id)
                         WHEN 'coordinator' THEN (SELECT c.property_company_id FROM coordinators c WHERE c.id = t.owner_id)
                         WHEN 'sector_leader' THEN (SELECT s.property_company_id FROM sector_leaders s WHERE s.id = t.owner_id)
                         WHEN 'individual_leader' THEN (SELECT i.property_company_id FROM individual_leaders i WHERE i.id = t.owner_id)
                         ELSE NULL
                       END""", filters)
                + idFilter("t.id", filters);
    }

    private String settlement(Filters filters) {
        return """
                SELECT 'settlement' AS category, 'SETTLEMENT' AS group_code, r.id AS id,
                       COALESCE(r.settle_at, r.created_at) AS occurred_at,
                       (SELECT o.property_company_id FROM orders o WHERE o.id = r.order_id) AS property_company_id,
                       CASE WHEN r.status = 'reversed' THEN 'neutral' ELSE 'income' END AS direction,
                       'CNY' AS unit,
                       COALESCE(r.reseller_profit, 0.00) AS amount,
                       0.00 AS fee_amount,
                       COALESCE(r.order_amount, 0.00) AS net_amount,
                       COALESCE(r.platform_commission, 0.00) AS platform_amount,
                       r.status AS status,
                       'resident_shop_settlement' AS biz_type,
                       '' AS description,
                       r.order_id AS order_id,
                       r.listing_id AS biz_id,
                       NULL AS trade_no,
                       'resident' AS party_type,
                       r.resident_id AS party_id,
                       0.00 AS cash_amount,
                       0 AS point_used,
                       0.00 AS coin_used,
                       0.00 AS directed_used
                FROM resident_merchant_settlements r
                WHERE r.created_at BETWEEN :start AND :end
                """ + pcFilter("(SELECT o.property_company_id FROM orders o WHERE o.id = r.order_id)", filters)
                + idFilter("r.id", filters);
    }

    private String point(Filters filters) {
        return """
                SELECT 'point' AS category, 'VALUE' AS group_code, p.id AS id,
                       p.created_at AS occurred_at,
                       p.property_company_id AS property_company_id,
                       CASE WHEN p.amount >= 0 THEN 'income' ELSE 'expense' END AS direction,
                       'POINT' AS unit,
                       CAST(ABS(p.amount) AS DECIMAL(18, 2)) AS amount,
                       0.00 AS fee_amount,
                       CAST(ABS(p.amount) AS DECIMAL(18, 2)) AS net_amount,
                       0.00 AS platform_amount,
                       p.point_type AS status,
                       COALESCE(p.source, 'point') AS biz_type,
                       COALESCE(p.description, '') AS description,
                       CASE WHEN p.source_id LIKE 'ord%' THEN p.source_id ELSE NULL END AS order_id,
                       p.source_id AS biz_id,
                       NULL AS trade_no,
                       'resident' AS party_type,
                       p.resident_id AS party_id,
                       0.00 AS cash_amount,
                       0 AS point_used,
                       0.00 AS coin_used,
                       0.00 AS directed_used
                FROM point_records p
                WHERE p.created_at BETWEEN :start AND :end
                """ + pcFilter("p.property_company_id", filters)
                + idFilter("p.id", filters);
    }

    private String coin(Filters filters) {
        return """
                SELECT 'coin' AS category, 'VALUE' AS group_code, c.id AS id,
                       c.created_at AS occurred_at,
                       c.property_company_id AS property_company_id,
                       CASE WHEN c.amount >= 0 THEN 'income' ELSE 'expense' END AS direction,
                       'COIN' AS unit,
                       ABS(c.amount) AS amount,
                       0.00 AS fee_amount,
                       ABS(c.amount) AS net_amount,
                       0.00 AS platform_amount,
                       c.coin_type AS status,
                       COALESCE(c.source, 'coin') AS biz_type,
                       COALESCE(c.description, '') AS description,
                       CASE WHEN c.source_id LIKE 'ord%' THEN c.source_id ELSE NULL END AS order_id,
                       c.source_id AS biz_id,
                       NULL AS trade_no,
                       'resident' AS party_type,
                       c.resident_id AS party_id,
                       0.00 AS cash_amount,
                       0 AS point_used,
                       0.00 AS coin_used,
                       0.00 AS directed_used
                FROM property_coin_records c
                WHERE c.created_at BETWEEN :start AND :end
                """ + pcFilter("c.property_company_id", filters)
                + idFilter("c.id", filters);
    }

    private String canteen(Filters filters) {
        return """
                SELECT 'canteen' AS category, 'VALUE' AS group_code, c.id AS id,
                       c.created_at AS occurred_at,
                       (SELECT m.property_company_id FROM merchants m WHERE m.id = c.main_merchant_id) AS property_company_id,
                       CASE WHEN c.flow_type = 'refund_back' THEN 'income' ELSE 'expense' END AS direction,
                       'CNY_DIRECTED' AS unit,
                       COALESCE(c.amount, 0.00) AS amount,
                       0.00 AS fee_amount,
                       COALESCE(c.amount, 0.00) AS net_amount,
                       0.00 AS platform_amount,
                       c.flow_type AS status,
                       c.flow_type AS biz_type,
                       '' AS description,
                       c.order_id AS order_id,
                       c.merchant_id AS biz_id,
                       NULL AS trade_no,
                       'resident' AS party_type,
                       c.resident_id AS party_id,
                       0.00 AS cash_amount,
                       0 AS point_used,
                       0.00 AS coin_used,
                       0.00 AS directed_used
                FROM canteen_directed_flows c
                WHERE c.created_at BETWEEN :start AND :end
                """ + pcFilter("(SELECT m.property_company_id FROM merchants m WHERE m.id = c.main_merchant_id)", filters)
                + idFilter("c.id", filters);
    }
}
