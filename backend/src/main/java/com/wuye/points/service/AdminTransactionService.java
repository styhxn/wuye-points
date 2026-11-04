package com.wuye.points.service;

import com.wuye.points.dto.admin.transaction.TransactionDetailResponse;
import com.wuye.points.dto.admin.transaction.TransactionItemResponse;
import com.wuye.points.dto.admin.transaction.TransactionOverviewResponse;
import com.wuye.points.dto.common.PageResponse;
import com.wuye.points.exception.BizException;
import com.wuye.points.exception.ErrorCode;
import com.wuye.points.support.TransactionLabels;
import com.wuye.points.support.TransactionLedgerSql;
import com.wuye.points.support.TransactionLedgerSql.Filters;
import com.wuye.points.support.TransactionLedgerSql.SqlQuery;
import com.wuye.points.util.AdminSupport;
import com.wuye.points.util.DateTimeUtil;
import com.wuye.points.util.RoleGuard;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * 平台交易明细（仅 platform_admin）。
 *
 * <p>把支付/退款、钱包/分账、提现/结算、积分/物业币/食堂余额四组账本汇总为
 * 「总览 + 明细 + CSV」三块能力。所有入口都做 platform_admin 硬校验，
 * 不依赖权限码，物业管理员即便被授予相关权限也看不到平台交易数据。</p>
 */
@Service
@RequiredArgsConstructor
public class AdminTransactionService {

    private static final int DEFAULT_RANGE_DAYS = 30;
    private static final int MAX_PAGE_SIZE = 100;
    private static final int MAX_EXPORT_ROWS = 50_000;
    private static final int MAX_KEYWORD_LENGTH = 100;
    private static final int MAX_DETAIL_ID_LENGTH = 64;

    /** 流水来源表：只给打印页标注数据出处，不参与查询拼接。 */
    private static final Map<String, String> SOURCE_TABLE = Map.ofEntries(
            Map.entry("payment", "payment_records"),
            Map.entry("refund", "payment_records"),
            Map.entry("wallet", "wallet_flows"),
            Map.entry("distribution", "distribution_records"),
            Map.entry("split", "wechat_split_record"),
            Map.entry("transfer", "merchant_transfers"),
            Map.entry("settlement", "resident_merchant_settlements"),
            Map.entry("point", "point_records"),
            Map.entry("coin", "property_coin_records"),
            Map.entry("canteen", "canteen_directed_flows"));

    private static final Set<String> DIRECTIONS = Set.of("income", "expense", "neutral");

    /**
     * 分组合计只汇总非重叠分类。distribution/split 是分账过程快照，settlement 与钱包入账有重叠，
     * 把它们直接加到分组金额会重复计算；它们仍会完整出现在分类明细中。
     */
    private static final Set<String> GROUP_ADDITIVE_CATEGORIES = Set.of(
            "payment", "refund", "wallet", "withdrawal", "transfer", "point", "coin", "canteen");

    private static final List<String> GROUP_ORDER = List.of(
            TransactionLabels.GROUP_PAYMENT,
            TransactionLabels.GROUP_WALLET,
            TransactionLabels.GROUP_SETTLEMENT,
            TransactionLabels.GROUP_VALUE);

    private static final List<String> CATEGORY_ORDER = List.of(
            "payment", "refund",
            "wallet", "distribution", "split",
            "withdrawal", "transfer", "settlement",
            "point", "coin", "canteen");

    private static final String CSV_HEADER = "交易时间,交易分类,分类码,所属分组,收支方向,金额,单位,"
            + "状态,状态码,业务类型,业务单号,订单号,渠道流水号,交易ID,"
            + "主体类型,主体ID,主体名称,物业公司ID,物业公司,"
            + "手续费,实收/实付,平台分成,现金部分,积分抵扣,物业币抵扣,定向余额抵扣,说明";

    private final NamedParameterJdbcTemplate jdbcTemplate;
    private final TransactionLedgerSql ledgerSql;
    private final RoleGuard roleGuard;
    private final AdminSupport adminSupport;

    public record CsvExport(String content, boolean truncated, int rowCount) {
    }

    private record QueryContext(Filters filters, LocalDateTime start, LocalDateTime end) {
    }

    @Transactional(readOnly = true)
    public PageResponse<TransactionItemResponse> list(String propertyCompanyId,
                                                      String startDate,
                                                      String endDate,
                                                      String group,
                                                      String category,
                                                      String direction,
                                                      String status,
                                                      String keyword,
                                                      int page,
                                                      int pageSize) {
        QueryContext context = prepare(propertyCompanyId, startDate, endDate,
                group, category, direction, status, keyword);

        if (pageSize > MAX_PAGE_SIZE) {
            throw new BizException(ErrorCode.PARAM_OUT_OF_RANGE);
        }
        int safePage = Math.max(page, 1);
        int safePageSize = Math.min(Math.max(pageSize, 1), MAX_PAGE_SIZE);

        SqlQuery countQuery = ledgerSql.count(context.filters(), context.start(), context.end());
        Long total = jdbcTemplate.queryForObject(countQuery.sql(), countQuery.params(), Long.class);
        long totalCount = total == null ? 0L : total;

        long offsetLong = (long) (safePage - 1) * safePageSize;
        int offset = offsetLong > Integer.MAX_VALUE ? Integer.MAX_VALUE : (int) offsetLong;

        if (totalCount == 0 || offsetLong >= totalCount) {
            return PageResponse.of(List.of(), safePage, safePageSize, totalCount);
        }

        SqlQuery listQuery = ledgerSql.list(context.filters(), context.start(), context.end(),
                safePageSize, offset);
        List<TransactionItemResponse> items = queryItems(listQuery);
        return PageResponse.of(items, safePage, safePageSize, totalCount);
    }

    @Transactional(readOnly = true)
    public TransactionOverviewResponse overview(String propertyCompanyId,
                                                String startDate,
                                                String endDate,
                                                String group,
                                                String category,
                                                String direction,
                                                String status,
                                                String keyword) {
        QueryContext context = prepare(propertyCompanyId, startDate, endDate,
                group, category, direction, status, keyword);

        SqlQuery summaryQuery = ledgerSql.summary(context.filters(), context.start(), context.end());
        List<SummaryRow> rows = jdbcTemplate.query(summaryQuery.sql(), summaryQuery.params(),
                (rs, rowNum) -> new SummaryRow(
                        rs.getString("category"),
                        rs.getString("group_code"),
                        rs.getString("unit"),
                        rs.getString("direction"),
                        rs.getString("status"),
                        rs.getLong("cnt"),
                        decimal(rs.getBigDecimal("volume_amount")),
                        decimal(rs.getBigDecimal("income_amount")),
                        decimal(rs.getBigDecimal("expense_amount")),
                        decimal(rs.getBigDecimal("fee_amount")),
                        decimal(rs.getBigDecimal("actual_amount")),
                        decimal(rs.getBigDecimal("platform_amount"))));

        Map<String, MutableStat> categoryStats = new LinkedHashMap<>();
        Map<String, MutableStat> groupStats = new LinkedHashMap<>();
        Map<String, StatusRow> statusRows = new LinkedHashMap<>();

        for (SummaryRow row : rows) {
            String categoryKey = row.category() + "|" + row.unit();
            MutableStat categoryStat = categoryStats.computeIfAbsent(categoryKey,
                    key -> new MutableStat(row.category(), row.group(), row.unit()));
            categoryStat.add(row);

            if (context.filters().category() != null
                    || GROUP_ADDITIVE_CATEGORIES.contains(row.category())) {
                String groupKey = row.group() + "|" + row.unit();
                MutableStat groupStat = groupStats.computeIfAbsent(groupKey,
                        key -> new MutableStat(null, row.group(), row.unit()));
                groupStat.add(row);
            }

            String statusKey = row.category() + "|" + row.status() + "|" + row.unit();
            StatusRow statusRow = statusRows.computeIfAbsent(statusKey,
                    key -> new StatusRow(row.category(), row.status(), row.unit()));
            statusRow.count += row.count();
            statusRow.amount = statusRow.amount.add(row.volume());
        }

        List<TransactionOverviewResponse.CategoryStat> categories = categoryStats.values().stream()
                .sorted(Comparator
                        .comparingInt((MutableStat stat) -> groupIndex(stat.group))
                        .thenComparingInt(stat -> categoryIndex(stat.category)))
                .map(MutableStat::toCategoryStat)
                .toList();

        List<TransactionOverviewResponse.GroupStat> groups = groupStats.values().stream()
                .sorted(Comparator
                        .comparingInt((MutableStat stat) -> groupIndex(stat.group))
                        .thenComparingInt(stat -> unitIndex(stat.unit)))
                .map(MutableStat::toGroupStat)
                .toList();

        List<TransactionOverviewResponse.StatusStat> statusBreakdown = statusRows.values().stream()
                .sorted(Comparator
                        .comparingInt((StatusRow row) -> categoryIndex(row.category))
                        .thenComparing(row -> row.status, Comparator.nullsLast(String::compareTo)))
                .map(row -> TransactionOverviewResponse.StatusStat.builder()
                        .category(row.category)
                        .categoryLabel(TransactionLabels.categoryLabel(row.category))
                        .status(row.status)
                        .statusLabel(TransactionLabels.statusLabel(row.category, row.status))
                        .count(row.count)
                        .amount(row.amount)
                        .unit(row.unit)
                        .unitLabel(TransactionLabels.unitLabel(row.unit))
                        .build())
                .toList();

        SqlQuery trendQuery = ledgerSql.trend(context.filters(), context.start(), context.end());
        List<TransactionOverviewResponse.TrendPoint> trend = jdbcTemplate.query(
                trendQuery.sql(), trendQuery.params(),
                (rs, rowNum) -> {
                    java.sql.Date date = rs.getDate("stat_date");
                    String unit = rs.getString("unit");
                    return TransactionOverviewResponse.TrendPoint.builder()
                            .date(date == null ? null : date.toLocalDate().toString())
                            .unit(unit)
                            .unitLabel(TransactionLabels.unitLabel(unit))
                            .incomeAmount(decimal(rs.getBigDecimal("income_amount")))
                            .expenseAmount(decimal(rs.getBigDecimal("expense_amount")))
                            .count(rs.getLong("cnt"))
                            .build();
                });

        return TransactionOverviewResponse.builder()
                .range(TransactionOverviewResponse.Range.builder()
                        .start(DateTimeUtil.toIsoUtc(context.start()))
                        .end(DateTimeUtil.toIsoUtc(context.end()))
                        .propertyCompanyId(context.filters().propertyCompanyId())
                        .build())
                .note("wallet 为官方钱包账本；distribution/split 为分账过程快照，settlement 与钱包入账可能重叠。"
                        + "分组合计默认只汇总非重叠分类，各分类明细与分类汇总始终完整。")
                .groups(groups)
                .categories(categories)
                .statusBreakdown(statusBreakdown)
                .trend(trend)
                .build();
    }

    @Transactional(readOnly = true)
    public CsvExport exportCsv(String propertyCompanyId,
                               String startDate,
                               String endDate,
                               String group,
                               String category,
                               String direction,
                               String status,
                               String keyword) {
        QueryContext context = prepare(propertyCompanyId, startDate, endDate,
                group, category, direction, status, keyword);

        SqlQuery exportQuery = ledgerSql.export(context.filters(), context.start(), context.end(),
                MAX_EXPORT_ROWS);
        List<TransactionItemResponse> items = queryItems(exportQuery);

        StringBuilder csv = new StringBuilder("\uFEFF");
        csv.append(CSV_HEADER).append("\r\n");
        for (TransactionItemResponse item : items) {
            csv.append(csvCell(item.getOccurredAt())).append(',')
                    .append(csvCell(item.getCategoryLabel())).append(',')
                    .append(csvCell(item.getCategory())).append(',')
                    .append(csvCell(item.getGroupLabel())).append(',')
                    .append(csvCell(item.getDirectionLabel())).append(',')
                    .append(csvCell(plain(item.getAmount()))).append(',')
                    .append(csvCell(item.getUnitLabel())).append(',')
                    .append(csvCell(item.getStatusLabel())).append(',')
                    .append(csvCell(item.getStatus())).append(',')
                    .append(csvCell(item.getTypeLabel())).append(',')
                    .append(csvCell(item.getBizId())).append(',')
                    .append(csvCell(item.getOrderId())).append(',')
                    .append(csvCell(item.getTradeNo())).append(',')
                    .append(csvCell(item.getId())).append(',')
                    .append(csvCell(TransactionLabels.ownerTypeLabel(item.getPartyType()))).append(',')
                    .append(csvCell(item.getPartyId())).append(',')
                    .append(csvCell(item.getPartyName())).append(',')
                    .append(csvCell(item.getPropertyCompanyId())).append(',')
                    .append(csvCell(item.getPropertyCompanyName())).append(',')
                    .append(csvCell(plain(item.getFeeAmount()))).append(',')
                    .append(csvCell(plain(item.getNetAmount()))).append(',')
                    .append(csvCell(plain(item.getPlatformAmount()))).append(',')
                    .append(csvCell(plain(item.getCashAmount()))).append(',')
                    .append(csvCell(plain(item.getPointUsed()))).append(',')
                    .append(csvCell(plain(item.getCoinUsed()))).append(',')
                    .append(csvCell(plain(item.getDirectedUsed()))).append(',')
                    .append(csvCell(item.getDescription()))
                    .append("\r\n");
        }
        return new CsvExport(csv.toString(), items.size() >= MAX_EXPORT_ROWS, items.size());
    }

    /**
     * 单条流水详情（打印/对账用）。
     *
     * <p>{@code category} 必传：{@code payment} 与 {@code refund} 共用同一条
     * {@code payment_records.id}，只给 ID 分不清是收款还是退款，打出来的单子方向会反。</p>
     */
    @Transactional(readOnly = true)
    public TransactionDetailResponse detail(String category, String id) {
        roleGuard.requireAnyRole("platform_admin");
        if (!StringUtils.hasText(id)) {
            throw new BizException(ErrorCode.PARAM_MISSING, "id不能为空");
        }
        String trimmedId = id.trim();
        if (trimmedId.length() > MAX_DETAIL_ID_LENGTH) {
            throw new BizException(ErrorCode.PARAM_INVALID,
                    "id长度不能超过" + MAX_DETAIL_ID_LENGTH + "个字符");
        }
        String normalizedCategory = normalizeCategory(category);
        if (normalizedCategory == null) {
            throw new BizException(ErrorCode.PARAM_MISSING,
                    "category不能为空，可选: payment/refund/wallet/distribution/split/"
                            + "withdrawal/transfer/settlement/point/coin/canteen");
        }

        List<TransactionItemResponse> rows = queryItems(ledgerSql.byId(normalizedCategory, trimmedId));
        if (rows.isEmpty()) {
            throw new BizException(ErrorCode.TRANSACTION_NOT_FOUND);
        }
        TransactionItemResponse row = rows.get(0);
        String relatedKeyword = StringUtils.hasText(row.getOrderId()) ? row.getOrderId() : row.getBizId();
        return TransactionDetailResponse.builder()
                .transaction(row)
                .sourceTable(sourceTable(row))
                .relatedKeyword(relatedKeyword)
                .links(buildLinks(row, relatedKeyword))
                .build();
    }

    private String sourceTable(TransactionItemResponse row) {
        if ("withdrawal".equals(row.getCategory())) {
            if ("merchant_withdrawal".equals(row.getBizType())) {
                return "merchant_withdrawals";
            }
            if ("coin_withdrawal".equals(row.getBizType())) {
                return "coin_withdrawals";
            }
            return "withdrawal_records";
        }
        return SOURCE_TABLE.get(row.getCategory());
    }

    /**
     * 关联接口指引：只列后端已存在、且 platform_admin 可访问的路径，
     * 打印页顺着 links 取数即可拼出整张单据，不需要前端自己猜路径。
     */
    private List<TransactionDetailResponse.Link> buildLinks(TransactionItemResponse row, String relatedKeyword) {
        List<TransactionDetailResponse.Link> links = new ArrayList<>();
        if (StringUtils.hasText(relatedKeyword)) {
            links.add(link("同一单据的全部流水",
                    "/admin/transactions?keyword=" + relatedKeyword + "&pageSize=100"));
        }
        String orderId = row.getOrderId();
        if (StringUtils.hasText(orderId)) {
            links.add(link("订单详情", "/orders/" + orderId));
        }
        switch (row.getCategory()) {
            case "payment", "refund" -> {
                if (StringUtils.hasText(orderId)) {
                    links.add(link("退货审核列表", "/admin/orders/refunds?keyword=" + orderId));
                }
            }
            case "wallet", "distribution", "split" -> {
                if (StringUtils.hasText(orderId)) {
                    links.add(link("订单分成记录", "/distribution/records?orderId=" + orderId));
                }
            }
            case "withdrawal" -> {
                if ("merchant_withdrawal".equals(row.getBizType()) && StringUtils.hasText(row.getPartyId())) {
                    links.add(link("商家提现审核列表",
                            "/admin/merchant-withdrawals?merchantId=" + row.getPartyId()));
                } else if (StringUtils.hasText(row.getBizId())) {
                    links.add(link("多角色提现审核列表", "/admin/role-withdrawals?keyword=" + row.getBizId()));
                }
            }
            case "settlement" -> links.add(link("业主小店结算明细", "/admin/resident-merchant-settlements"));
            case "canteen" -> {
                if (StringUtils.hasText(row.getPartyId())) {
                    links.add(link("食堂定向余额流水",
                            "/admin/canteen/flows?residentId=" + row.getPartyId()));
                }
            }
            case "point", "coin" -> {
                if (StringUtils.hasText(row.getPartyId())) {
                    links.add(link("住户详情", "/residents/" + row.getPartyId()));
                }
            }
            default -> {
            }
        }
        return links;
    }

    private static TransactionDetailResponse.Link link(String label, String path) {
        return TransactionDetailResponse.Link.builder().label(label).method("GET").path(path).build();
    }

    private QueryContext prepare(String propertyCompanyId,
                                 String startDate,
                                 String endDate,
                                 String group,
                                 String category,
                                 String direction,
                                 String status,
                                 String keyword) {
        roleGuard.requireAnyRole("platform_admin");

        String resolvedPropertyCompanyId = adminSupport.resolvePropertyCompanyId(propertyCompanyId);

        String normalizedGroup = normalizeGroup(group);
        String normalizedCategory = normalizeCategory(category);
        if (normalizedCategory != null && normalizedGroup != null
                && !normalizedGroup.equals(TransactionLedgerSql.groupOf(normalizedCategory))) {
            throw new BizException(ErrorCode.PARAM_INVALID, "category与group不匹配");
        }

        String normalizedDirection = null;
        if (StringUtils.hasText(direction)) {
            normalizedDirection = direction.trim().toLowerCase(Locale.ROOT);
            if (!DIRECTIONS.contains(normalizedDirection)) {
                throw new BizException(ErrorCode.PARAM_INVALID,
                        "direction值无效，可选: income/expense/neutral");
            }
        }

        String normalizedStatus = StringUtils.hasText(status)
                ? status.trim().toLowerCase(Locale.ROOT) : null;
        String normalizedKeyword = StringUtils.hasText(keyword) ? keyword.trim() : null;
        if (normalizedKeyword != null && normalizedKeyword.length() > MAX_KEYWORD_LENGTH) {
            throw new BizException(ErrorCode.PARAM_INVALID,
                    "keyword长度不能超过" + MAX_KEYWORD_LENGTH + "个字符");
        }

        LocalDateTime end = adminSupport.parseIsoDateTime(endDate, true);
        if (end == null) {
            end = LocalDateTime.now();
        }
        LocalDateTime start = adminSupport.parseIsoDateTime(startDate, false);
        if (start == null) {
            start = end.toLocalDate().minusDays(DEFAULT_RANGE_DAYS - 1L).atStartOfDay();
        }
        if (start.isAfter(end)) {
            throw new BizException(ErrorCode.PARAM_INVALID, "开始时间不能晚于结束时间");
        }

        Filters filters = new Filters(normalizedCategory, normalizedGroup, normalizedDirection,
                normalizedStatus, normalizedKeyword, resolvedPropertyCompanyId, null);
        return new QueryContext(filters, start, end);
    }

    private String normalizeGroup(String group) {
        if (!StringUtils.hasText(group)) {
            return null;
        }
        String normalized = group.trim().toUpperCase(Locale.ROOT);
        if (!TransactionLedgerSql.GROUPS.contains(normalized)) {
            throw new BizException(ErrorCode.PARAM_INVALID,
                    "group值无效，可选: PAYMENT/WALLET/SETTLEMENT/VALUE");
        }
        return normalized;
    }

    private String normalizeCategory(String category) {
        if (!StringUtils.hasText(category)) {
            return null;
        }
        String normalized = category.trim().toLowerCase(Locale.ROOT);
        if (!TransactionLedgerSql.CATEGORIES.contains(normalized)) {
            throw new BizException(ErrorCode.PARAM_INVALID,
                    "category值无效，可选: payment/refund/wallet/distribution/split/"
                            + "withdrawal/transfer/settlement/point/coin/canteen");
        }
        return normalized;
    }

    private List<TransactionItemResponse> queryItems(SqlQuery query) {
        return jdbcTemplate.query(query.sql(), query.params(), this::mapItem);
    }

    private TransactionItemResponse mapItem(ResultSet rs, int rowNum) throws SQLException {
        String category = rs.getString("category");
        String group = rs.getString("group_code");
        String unit = rs.getString("unit");
        String direction = rs.getString("direction");
        String status = rs.getString("status");
        String bizType = rs.getString("biz_type");
        return TransactionItemResponse.builder()
                .id(rs.getString("id"))
                .category(category)
                .categoryLabel(TransactionLabels.categoryLabel(category))
                .group(group)
                .groupLabel(TransactionLabels.groupLabel(group))
                .occurredAt(DateTimeUtil.toIsoUtc(toLocalDateTime(rs.getTimestamp("occurred_at"))))
                .direction(direction)
                .directionLabel(TransactionLabels.directionLabel(direction))
                .unit(unit)
                .unitLabel(TransactionLabels.unitLabel(unit))
                .amount(decimal(rs.getBigDecimal("amount")))
                .feeAmount(decimal(rs.getBigDecimal("fee_amount")))
                .netAmount(decimal(rs.getBigDecimal("net_amount")))
                .platformAmount(decimal(rs.getBigDecimal("platform_amount")))
                .status(status)
                .statusLabel(TransactionLabels.statusLabel(category, status))
                .bizType(bizType)
                .typeLabel(TransactionLabels.typeLabel(category, bizType))
                .description(rs.getString("description"))
                .orderId(rs.getString("order_id"))
                .bizId(rs.getString("biz_id"))
                .tradeNo(rs.getString("trade_no"))
                .partyType(rs.getString("party_type"))
                .partyId(rs.getString("party_id"))
                .partyName(rs.getString("party_name"))
                .propertyCompanyId(rs.getString("property_company_id"))
                .propertyCompanyName(rs.getString("property_company_name"))
                .cashAmount(decimal(rs.getBigDecimal("cash_amount")))
                .pointUsed(decimal(rs.getBigDecimal("point_used")))
                .coinUsed(decimal(rs.getBigDecimal("coin_used")))
                .directedUsed(decimal(rs.getBigDecimal("directed_used")))
                .build();
    }

    private static int groupIndex(String group) {
        int index = GROUP_ORDER.indexOf(group);
        return index < 0 ? GROUP_ORDER.size() : index;
    }

    private static int categoryIndex(String category) {
        int index = CATEGORY_ORDER.indexOf(category);
        return index < 0 ? CATEGORY_ORDER.size() : index;
    }

    private static int unitIndex(String unit) {
        return switch (unit == null ? "" : unit) {
            case "CNY" -> 0;
            case "CNY_DIRECTED" -> 1;
            case "POINT" -> 2;
            case "COIN" -> 3;
            default -> 4;
        };
    }

    private static LocalDateTime toLocalDateTime(java.sql.Timestamp timestamp) {
        return timestamp == null ? null : timestamp.toLocalDateTime();
    }

    private static BigDecimal decimal(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }

    private static String plain(BigDecimal value) {
        return value == null ? "" : value.stripTrailingZeros().toPlainString();
    }

    /**
     * CSV 单元格转义：同时防止 Excel 把以 = + - @ 开头的文本当公式执行。
     */
    private static String csvCell(String value) {
        if (value == null || value.isEmpty()) {
            return "";
        }
        String text = value;
        char first = text.charAt(0);
        if (first == '=' || first == '+' || first == '-' || first == '@' || first == '\t' || first == '\r') {
            text = "'" + text;
        }
        if (text.contains(",") || text.contains("\"") || text.contains("\n") || text.contains("\r")) {
            return "\"" + text.replace("\"", "\"\"") + "\"";
        }
        return text;
    }

    private record SummaryRow(String category,
                              String group,
                              String unit,
                              String direction,
                              String status,
                              long count,
                              BigDecimal volume,
                              BigDecimal income,
                              BigDecimal expense,
                              BigDecimal fee,
                              BigDecimal actual,
                              BigDecimal platform) {
    }

    private static final class MutableStat {
        private final String category;
        private final String group;
        private final String unit;
        private long count;
        private BigDecimal volume = BigDecimal.ZERO;
        private BigDecimal income = BigDecimal.ZERO;
        private BigDecimal expense = BigDecimal.ZERO;
        private BigDecimal fee = BigDecimal.ZERO;
        private BigDecimal actual = BigDecimal.ZERO;
        private BigDecimal platform = BigDecimal.ZERO;

        private MutableStat(String category, String group, String unit) {
            this.category = category;
            this.group = group;
            this.unit = unit;
        }

        private void add(SummaryRow row) {
            count += row.count();
            volume = volume.add(row.volume());
            income = income.add(row.income());
            expense = expense.add(row.expense());
            fee = fee.add(row.fee());
            actual = actual.add(row.actual());
            platform = platform.add(row.platform());
        }

        private TransactionOverviewResponse.CategoryStat toCategoryStat() {
            return TransactionOverviewResponse.CategoryStat.builder()
                    .category(category)
                    .categoryLabel(TransactionLabels.categoryLabel(category))
                    .group(group)
                    .groupLabel(TransactionLabels.groupLabel(group))
                    .unit(unit)
                    .unitLabel(TransactionLabels.unitLabel(unit))
                    .count(count)
                    .volumeAmount(volume)
                    .incomeAmount(income)
                    .expenseAmount(expense)
                    .feeAmount(fee)
                    .actualAmount(actual)
                    .platformAmount(platform)
                    .build();
        }

        private TransactionOverviewResponse.GroupStat toGroupStat() {
            return TransactionOverviewResponse.GroupStat.builder()
                    .group(group)
                    .groupLabel(TransactionLabels.groupLabel(group))
                    .unit(unit)
                    .unitLabel(TransactionLabels.unitLabel(unit))
                    .count(count)
                    .volumeAmount(volume)
                    .incomeAmount(income)
                    .expenseAmount(expense)
                    .feeAmount(fee)
                    .actualAmount(actual)
                    .platformAmount(platform)
                    .build();
        }
    }

    private static final class StatusRow {
        private final String category;
        private final String status;
        private final String unit;
        private long count;
        private BigDecimal amount = BigDecimal.ZERO;

        private StatusRow(String category, String status, String unit) {
            this.category = category;
            this.status = status;
            this.unit = unit;
        }
    }
}
