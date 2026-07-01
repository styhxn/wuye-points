package com.wuye.points.repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 报表聚合查询
 */
@Repository
public class ReportStatsRepository {

    @PersistenceContext
    private EntityManager entityManager;

    public long countOrders(String propertyCompanyId, LocalDateTime start, LocalDateTime end) {
        return ((Number) buildOrderQuery(
                "SELECT COUNT(o) FROM Order o WHERE 1=1",
                propertyCompanyId, start, end, null)
                .getSingleResult()).longValue();
    }

    public BigDecimal sumOrderAmount(String propertyCompanyId, LocalDateTime start, LocalDateTime end) {
        Object result = buildOrderQuery(
                "SELECT COALESCE(SUM(o.totalAmount), 0) FROM Order o WHERE 1=1",
                propertyCompanyId, start, end, null)
                .getSingleResult();
        return toBigDecimal(result);
    }

    public BigDecimal sumConsumptionAmount(String propertyCompanyId, LocalDateTime start, LocalDateTime end) {
        Object result = buildOrderQuery(
                """
                SELECT COALESCE(SUM(o.totalAmount), 0) FROM Order o
                WHERE o.orderStatus IN ('paid', 'delivering', 'completed')
                """,
                propertyCompanyId, start, end, null)
                .getSingleResult();
        return toBigDecimal(result);
    }

    public long countOrdersByStatus(String propertyCompanyId, LocalDateTime start, LocalDateTime end, String status) {
        return ((Number) buildOrderQuery(
                "SELECT COUNT(o) FROM Order o WHERE o.orderStatus = :status",
                propertyCompanyId, start, end, Map.of("status", status))
                .getSingleResult()).longValue();
    }

    public long sumPointUsed(String propertyCompanyId, LocalDateTime start, LocalDateTime end) {
        Object result = buildOrderQuery(
                "SELECT COALESCE(SUM(o.pointUsed), 0) FROM Order o WHERE 1=1",
                propertyCompanyId, start, end, null)
                .getSingleResult();
        return result != null ? ((Number) result).longValue() : 0L;
    }

    public BigDecimal sumCoinUsed(String propertyCompanyId, LocalDateTime start, LocalDateTime end) {
        Object result = buildOrderQuery(
                "SELECT COALESCE(SUM(o.coinUsed), 0) FROM Order o WHERE 1=1",
                propertyCompanyId, start, end, null)
                .getSingleResult();
        return toBigDecimal(result);
    }

    public BigDecimal sumCashAmount(String propertyCompanyId, LocalDateTime start, LocalDateTime end) {
        Object result = buildOrderQuery(
                "SELECT COALESCE(SUM(o.cashAmount), 0) FROM Order o WHERE 1=1",
                propertyCompanyId, start, end, null)
                .getSingleResult();
        return toBigDecimal(result);
    }

    @SuppressWarnings("unchecked")
    public List<Object[]> groupOrdersByMerchant(String propertyCompanyId, LocalDateTime start, LocalDateTime end) {
        return buildOrderQuery(
                """
                SELECT o.merchantId, COUNT(o), COALESCE(SUM(o.totalAmount), 0)
                FROM Order o WHERE 1=1
                GROUP BY o.merchantId
                ORDER BY SUM(o.totalAmount) DESC
                """,
                propertyCompanyId, start, end, null)
                .getResultList();
    }

    @SuppressWarnings("unchecked")
    public List<Object[]> groupOrdersByPaymentMethod(String propertyCompanyId, LocalDateTime start, LocalDateTime end) {
        return buildOrderQuery(
                """
                SELECT o.paymentMethod, COUNT(o), COALESCE(SUM(o.totalAmount), 0)
                FROM Order o WHERE 1=1
                GROUP BY o.paymentMethod
                ORDER BY SUM(o.totalAmount) DESC
                """,
                propertyCompanyId, start, end, null)
                .getResultList();
    }

    public long sumPointByType(String propertyCompanyId, LocalDateTime start, LocalDateTime end, String pointType) {
        String jpql = switch (pointType) {
            case "earn" -> "SELECT COALESCE(SUM(r.amount), 0) FROM PointRecord r WHERE r.pointType = 'earn'";
            case "spend" -> "SELECT COALESCE(SUM(ABS(r.amount)), 0) FROM PointRecord r WHERE r.pointType = 'spend'";
            case "expire" -> "SELECT COALESCE(SUM(ABS(r.amount)), 0) FROM PointRecord r WHERE r.pointType = 'expire'";
            default -> throw new IllegalArgumentException("unsupported pointType: " + pointType);
        };
        Object result = buildPointQuery(jpql, propertyCompanyId, start, end).getSingleResult();
        return result != null ? ((Number) result).longValue() : 0L;
    }

    public long sumFamilyPointBalance(String propertyCompanyId) {
        String jpql = "SELECT COALESCE(SUM(f.pointBalance), 0) FROM Family f WHERE 1=1";
        Query query = entityManager.createQuery(appendPropertyFilter(jpql, "f", propertyCompanyId));
        bindPropertyCompanyId(query, propertyCompanyId);
        Object result = query.getSingleResult();
        return result != null ? ((Number) result).longValue() : 0L;
    }

    public BigDecimal sumPointPoolBalance(String propertyCompanyId) {
        String jpql = "SELECT COALESCE(SUM(p.balance), 0) FROM PointPool p WHERE 1=1";
        Query query = entityManager.createQuery(appendPropertyFilter(jpql, "p", propertyCompanyId));
        bindPropertyCompanyId(query, propertyCompanyId);
        return toBigDecimal(query.getSingleResult());
    }

    public BigDecimal sumFamilyCoinBalance(String propertyCompanyId) {
        String jpql = "SELECT COALESCE(SUM(f.coinBalance), 0) FROM Family f WHERE 1=1";
        Query query = entityManager.createQuery(appendPropertyFilter(jpql, "f", propertyCompanyId));
        bindPropertyCompanyId(query, propertyCompanyId);
        return toBigDecimal(query.getSingleResult());
    }

    public BigDecimal sumPropertyFeeAmount(String propertyCompanyId, LocalDateTime start, LocalDateTime end) {
        Object result = buildFeeQuery(
                "SELECT COALESCE(SUM(f.amount), 0) FROM PropertyFee f WHERE 1=1",
                propertyCompanyId, start, end, null)
                .getSingleResult();
        return toBigDecimal(result);
    }

    public BigDecimal sumPropertyFeePaidAmount(String propertyCompanyId, LocalDateTime start, LocalDateTime end) {
        Object result = buildFeeQuery(
                "SELECT COALESCE(SUM(f.paidAmount), 0) FROM PropertyFee f WHERE 1=1",
                propertyCompanyId, start, end, null)
                .getSingleResult();
        return toBigDecimal(result);
    }

    public BigDecimal sumPropertyFeeOverdueAmount(String propertyCompanyId, LocalDateTime start, LocalDateTime end) {
        Object result = buildFeeQuery(
                """
                SELECT COALESCE(SUM(f.amount - COALESCE(f.paidAmount, 0)), 0) FROM PropertyFee f
                WHERE f.paymentStatus IN ('unpaid', 'partial', 'overdue')
                """,
                propertyCompanyId, start, end, null)
                .getSingleResult();
        return toBigDecimal(result);
    }

    public long countResidentsCreated(String propertyCompanyId, LocalDateTime start, LocalDateTime end) {
        return ((Number) buildResidentQuery(
                "SELECT COUNT(r) FROM Resident r WHERE 1=1",
                propertyCompanyId, start, end)
                .getSingleResult()).longValue();
    }

    @SuppressWarnings("unchecked")
    public List<Object[]> groupPointTrend(String propertyCompanyId, LocalDateTime start, LocalDateTime end, String groupBy) {
        String dateExpr = trendDateExpression("r.createdAt", groupBy);
        String sql = """
                SELECT %s AS period,
                       COALESCE(SUM(CASE WHEN r.point_type = 'earn' THEN r.amount ELSE 0 END), 0),
                       COALESCE(SUM(CASE WHEN r.point_type = 'spend' THEN ABS(r.amount) ELSE 0 END), 0),
                       COALESCE(SUM(CASE WHEN r.point_type = 'expire' THEN ABS(r.amount) ELSE 0 END), 0)
                FROM point_records r
                WHERE 1=1
                """.formatted(dateExpr);
        Query query = buildNativePointQuery(sql, propertyCompanyId, start, end);
        return query.getResultList();
    }

    @SuppressWarnings("unchecked")
    public List<Object[]> groupOrderTrend(String propertyCompanyId, LocalDateTime start, LocalDateTime end, String groupBy) {
        String dateExpr = trendDateExpression("o.created_at", groupBy);
        String sql = """
                SELECT %s AS period,
                       COUNT(o.id),
                       COALESCE(SUM(o.total_amount), 0)
                FROM orders o
                WHERE 1=1
                """.formatted(dateExpr);
        Query query = buildNativeOrderQuery(sql, propertyCompanyId, start, end);
        return query.getResultList();
    }

    private Query buildOrderQuery(String jpql, String propertyCompanyId,
                                  LocalDateTime start, LocalDateTime end,
                                  Map<String, Object> extraParams) {
        String fullJpql = appendPropertyFilter(jpql, "o", propertyCompanyId);
        fullJpql = appendDateFilter(fullJpql, "o", start, end);
        Query query = entityManager.createQuery(fullJpql);
        bindPropertyCompanyId(query, propertyCompanyId);
        bindDateRange(query, start, end);
        if (extraParams != null) {
            extraParams.forEach(query::setParameter);
        }
        return query;
    }

    private Query buildPointQuery(String jpql, String propertyCompanyId,
                                  LocalDateTime start, LocalDateTime end) {
        String fullJpql = appendPropertyFilter(jpql, "r", propertyCompanyId);
        fullJpql = appendDateFilter(fullJpql, "r", start, end);
        Query query = entityManager.createQuery(fullJpql);
        bindPropertyCompanyId(query, propertyCompanyId);
        bindDateRange(query, start, end);
        return query;
    }

    private Query buildFeeQuery(String jpql, String propertyCompanyId,
                                LocalDateTime start, LocalDateTime end,
                                Map<String, Object> extraParams) {
        String fullJpql = appendPropertyFilter(jpql, "f", propertyCompanyId);
        fullJpql = appendDateFilter(fullJpql, "f", start, end);
        Query query = entityManager.createQuery(fullJpql);
        bindPropertyCompanyId(query, propertyCompanyId);
        bindDateRange(query, start, end);
        if (extraParams != null) {
            extraParams.forEach(query::setParameter);
        }
        return query;
    }

    private Query buildResidentQuery(String jpql, String propertyCompanyId,
                                     LocalDateTime start, LocalDateTime end) {
        String fullJpql = appendPropertyFilter(jpql, "r", propertyCompanyId);
        fullJpql = appendDateFilter(fullJpql, "r", start, end);
        Query query = entityManager.createQuery(fullJpql);
        bindPropertyCompanyId(query, propertyCompanyId);
        bindDateRange(query, start, end);
        return query;
    }

    private Query buildNativePointQuery(String sql, String propertyCompanyId,
                                        LocalDateTime start, LocalDateTime end) {
        String fullSql = appendNativePropertyFilter(sql, "r", propertyCompanyId);
        fullSql = appendNativeDateFilter(fullSql, "r", start, end);
        fullSql += " GROUP BY period ORDER BY period";
        Query query = entityManager.createNativeQuery(fullSql);
        bindPropertyCompanyId(query, propertyCompanyId);
        bindDateRange(query, start, end);
        return query;
    }

    private Query buildNativeOrderQuery(String sql, String propertyCompanyId,
                                        LocalDateTime start, LocalDateTime end) {
        String fullSql = appendNativePropertyFilter(sql, "o", propertyCompanyId);
        fullSql = appendNativeDateFilter(fullSql, "o", start, end);
        fullSql += " GROUP BY period ORDER BY period";
        Query query = entityManager.createNativeQuery(fullSql);
        bindPropertyCompanyId(query, propertyCompanyId);
        bindDateRange(query, start, end);
        return query;
    }

    private String trendDateExpression(String column, String groupBy) {
        return switch (groupBy) {
            case "day" -> "DATE_FORMAT(" + column + ", '%Y-%m-%d')";
            case "week" -> "DATE_FORMAT(" + column + ", '%x-W%v')";
            case "month" -> "DATE_FORMAT(" + column + ", '%Y-%m')";
            default -> "DATE_FORMAT(" + column + ", '%Y-%m')";
        };
    }

    private String appendPropertyFilter(String jpql, String alias, String propertyCompanyId) {
        if (propertyCompanyId != null) {
            return jpql + " AND " + alias + ".propertyCompanyId = :propertyCompanyId";
        }
        return jpql;
    }

    private String appendNativePropertyFilter(String sql, String alias, String propertyCompanyId) {
        if (propertyCompanyId != null) {
            return sql + " AND " + alias + ".property_company_id = :propertyCompanyId";
        }
        return sql;
    }

    private String appendDateFilter(String jpql, String alias, LocalDateTime start, LocalDateTime end) {
        List<String> parts = new ArrayList<>();
        if (start != null) {
            parts.add(" AND " + alias + ".createdAt >= :startDate");
        }
        if (end != null) {
            parts.add(" AND " + alias + ".createdAt < :endDate");
        }
        return jpql + String.join("", parts);
    }

    private String appendNativeDateFilter(String sql, String alias, LocalDateTime start, LocalDateTime end) {
        List<String> parts = new ArrayList<>();
        if (start != null) {
            parts.add(" AND " + alias + ".created_at >= :startDate");
        }
        if (end != null) {
            parts.add(" AND " + alias + ".created_at < :endDate");
        }
        return sql + String.join("", parts);
    }

    private void bindPropertyCompanyId(Query query, String propertyCompanyId) {
        if (propertyCompanyId != null) {
            query.setParameter("propertyCompanyId", propertyCompanyId);
        }
    }

    private void bindDateRange(Query query, LocalDateTime start, LocalDateTime end) {
        if (start != null) {
            query.setParameter("startDate", start);
        }
        if (end != null) {
            query.setParameter("endDate", end);
        }
    }

    private BigDecimal toBigDecimal(Object value) {
        if (value == null) {
            return BigDecimal.ZERO;
        }
        if (value instanceof BigDecimal bd) {
            return bd;
        }
        return new BigDecimal(value.toString());
    }
}
