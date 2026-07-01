package com.wuye.points.service;

import com.wuye.points.dto.distribution.DistributionStatsResponse;
import com.wuye.points.dto.report.ReportOrderSummaryResponse;
import com.wuye.points.dto.report.ReportOverviewResponse;
import com.wuye.points.dto.report.ReportPointSummaryResponse;
import com.wuye.points.entity.Merchant;
import com.wuye.points.entity.PropertyCompany;
import com.wuye.points.exception.BizException;
import com.wuye.points.exception.ErrorCode;
import com.wuye.points.repository.FamilyRepository;
import com.wuye.points.repository.MerchantRepository;
import com.wuye.points.repository.PropertyCompanyRepository;
import com.wuye.points.repository.ReportStatsRepository;
import com.wuye.points.repository.ResidentRepository;
import com.wuye.points.util.PermissionGuard;
import com.wuye.points.util.RoleGuard;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.io.OutputStreamWriter;
import java.io.Writer;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReportService {

    private static final Set<String> GROUP_BY_VALUES = Set.of("day", "week", "month");
    private static final Set<String> EXPORT_TYPES = Set.of(
            "overview", "point-summary", "order-summary", "distribution-summary");
    private static final int MONEY_SCALE = 2;
    private static final int RATE_SCALE = 4;

    private final ReportStatsRepository reportStatsRepository;
    private final PropertyCompanyRepository propertyCompanyRepository;
    private final MerchantRepository merchantRepository;
    private final ResidentRepository residentRepository;
    private final FamilyRepository familyRepository;
    private final DistributionService distributionService;
    private final RoleGuard roleGuard;
    private final PermissionGuard permissionGuard;

    @Transactional(readOnly = true)
    public ReportOverviewResponse getOverview(String propertyCompanyId, String startDate, String endDate) {
        requireReportRole();
        String scopedPropertyCompanyId = resolveScopedPropertyCompanyId(propertyCompanyId);
        DateRange range = resolveDateRange(startDate, endDate, null);

        ReportOverviewResponse.Scope scope = buildScope(scopedPropertyCompanyId);
        ReportOverviewResponse.Summary summary = buildOverviewSummary(scopedPropertyCompanyId, range);
        ReportOverviewResponse.Comparison comparison = buildComparison(scopedPropertyCompanyId, range);

        return ReportOverviewResponse.builder()
                .scope(scope)
                .summary(summary)
                .comparison(comparison)
                .build();
    }

    @Transactional(readOnly = true)
    public ReportPointSummaryResponse getPointSummary(
            String propertyCompanyId,
            String startDate,
            String endDate,
            String groupBy) {
        requireReportRole();
        String scopedPropertyCompanyId = resolveScopedPropertyCompanyId(propertyCompanyId);
        DateRange range = resolveDateRange(startDate, endDate, null);
        String resolvedGroupBy = resolveGroupBy(groupBy);

        long totalIssued = reportStatsRepository.sumPointByType(
                scopedPropertyCompanyId, range.start(), range.end(), "earn");
        long totalConsumed = reportStatsRepository.sumPointByType(
                scopedPropertyCompanyId, range.start(), range.end(), "spend");
        long totalExpired = reportStatsRepository.sumPointByType(
                scopedPropertyCompanyId, range.start(), range.end(), "expire");
        long currentBalance = reportStatsRepository.sumFamilyPointBalance(scopedPropertyCompanyId);
        BigDecimal poolBalance = reportStatsRepository.sumPointPoolBalance(scopedPropertyCompanyId);
        BigDecimal pointToFeeRate = resolvePointToFeeRate(scopedPropertyCompanyId);

        ReportPointSummaryResponse.Summary summary = ReportPointSummaryResponse.Summary.builder()
                .totalIssued(totalIssued)
                .totalConsumed(totalConsumed)
                .totalExpired(totalExpired)
                .currentBalance(currentBalance)
                .poolBalance(poolBalance)
                .equivalentAmount(money(BigDecimal.valueOf(currentBalance).multiply(pointToFeeRate)))
                .build();

        List<ReportPointSummaryResponse.TrendItem> trend = buildPointTrend(
                scopedPropertyCompanyId, range, resolvedGroupBy);

        return ReportPointSummaryResponse.builder()
                .summary(summary)
                .trend(trend)
                .build();
    }

    @Transactional(readOnly = true)
    public ReportOrderSummaryResponse getOrderSummary(
            String propertyCompanyId,
            String startDate,
            String endDate,
            String groupBy) {
        requireReportRole();
        String scopedPropertyCompanyId = resolveScopedPropertyCompanyId(propertyCompanyId);
        DateRange range = resolveDateRange(startDate, endDate, null);
        String resolvedGroupBy = resolveGroupBy(groupBy);

        long totalOrderCount = reportStatsRepository.countOrders(
                scopedPropertyCompanyId, range.start(), range.end());
        BigDecimal totalAmount = reportStatsRepository.sumOrderAmount(
                scopedPropertyCompanyId, range.start(), range.end());
        long completedCount = reportStatsRepository.countOrdersByStatus(
                scopedPropertyCompanyId, range.start(), range.end(), "completed");
        long cancelledCount = reportStatsRepository.countOrdersByStatus(
                scopedPropertyCompanyId, range.start(), range.end(), "cancelled");

        ReportOrderSummaryResponse.Summary summary = ReportOrderSummaryResponse.Summary.builder()
                .totalOrderCount(totalOrderCount)
                .totalAmount(totalAmount)
                .avgOrderAmount(calcAvg(totalAmount, totalOrderCount))
                .completedCount(completedCount)
                .cancelledCount(cancelledCount)
                .pointUsedTotal(reportStatsRepository.sumPointUsed(
                        scopedPropertyCompanyId, range.start(), range.end()))
                .coinUsedTotal(reportStatsRepository.sumCoinUsed(
                        scopedPropertyCompanyId, range.start(), range.end()))
                .cashAmountTotal(reportStatsRepository.sumCashAmount(
                        scopedPropertyCompanyId, range.start(), range.end()))
                .build();

        List<ReportOrderSummaryResponse.ByMerchantItem> byMerchant = buildByMerchant(
                scopedPropertyCompanyId, range);
        List<ReportOrderSummaryResponse.ByPaymentMethodItem> byPaymentMethod = buildByPaymentMethod(
                scopedPropertyCompanyId, range);
        List<ReportOrderSummaryResponse.TrendItem> trend = buildOrderTrend(
                scopedPropertyCompanyId, range, resolvedGroupBy);

        return ReportOrderSummaryResponse.builder()
                .summary(summary)
                .byMerchant(byMerchant)
                .byPaymentMethod(byPaymentMethod)
                .trend(trend)
                .build();
    }

    @Transactional(readOnly = true)
    public DistributionStatsResponse getDistributionSummary(
            String propertyCompanyId,
            String startDate,
            String endDate,
            String dimension) {
        requireReportRole();
        String scopedPropertyCompanyId = resolveScopedPropertyCompanyId(propertyCompanyId);
        String resolvedDimension = StringUtils.hasText(dimension) ? dimension : "summary";
        if (!Set.of("summary", "detail").contains(resolvedDimension)) {
            throw new BizException(ErrorCode.PARAM_INVALID, "dimension不合法");
        }
        return distributionService.getStats(startDate, endDate, resolvedDimension, scopedPropertyCompanyId);
    }

    @Transactional(readOnly = true)
    public StreamingResponseBody exportReport(
            String type,
            String format,
            String month,
            String startDate,
            String endDate,
            String propertyCompanyId) {
        requireReportRole();
        if (!StringUtils.hasText(type) || !EXPORT_TYPES.contains(type)) {
            throw new BizException(ErrorCode.PARAM_INVALID, "type不合法");
        }
        if (!"csv".equals(format)) {
            throw new BizException(ErrorCode.PARAM_INVALID, "format仅支持csv");
        }

        String scopedPropertyCompanyId = resolveScopedPropertyCompanyId(propertyCompanyId);
        DateRange range = resolveDateRange(startDate, endDate, month);
        String filename = buildExportFilename(type, month, range);

        return outputStream -> {
            Writer writer = new OutputStreamWriter(outputStream, StandardCharsets.UTF_8);
            writer.write('\ufeff');
            switch (type) {
                case "overview" -> writeOverviewCsv(writer, scopedPropertyCompanyId, range);
                case "point-summary" -> writePointSummaryCsv(writer, scopedPropertyCompanyId, range);
                case "order-summary" -> writeOrderSummaryCsv(writer, scopedPropertyCompanyId, range);
                case "distribution-summary" -> writeDistributionSummaryCsv(writer, scopedPropertyCompanyId, range);
                default -> throw new BizException(ErrorCode.PARAM_INVALID, "type不合法");
            }
            writer.flush();
        };
    }

    public String resolveExportFilename(String type, String month, String startDate, String endDate, String propertyCompanyId) {
        requireReportRole();
        DateRange range = resolveDateRange(startDate, endDate, month);
        return buildExportFilename(type, month, range);
    }

    private void writeOverviewCsv(Writer writer, String propertyCompanyId, DateRange range) throws java.io.IOException {
        ReportOverviewResponse overview = buildOverviewForExport(propertyCompanyId, range);
        writer.write("指标,数值\n");
        writer.write("物业公司ID," + csv(overview.getScope().getPropertyCompanyId()) + "\n");
        writer.write("物业公司名称," + csv(overview.getScope().getPropertyCompanyName()) + "\n");
        writer.write("商家数," + overview.getSummary().getMerchantCount() + "\n");
        writer.write("住户数," + overview.getSummary().getResidentCount() + "\n");
        writer.write("家庭数," + overview.getSummary().getFamilyCount() + "\n");
        writer.write("消费总额," + overview.getSummary().getTotalConsumptionAmount() + "\n");
        writer.write("订单总数," + overview.getSummary().getTotalOrderCount() + "\n");
        writer.write("积分池余额," + overview.getSummary().getPointPoolBalance() + "\n");
        writer.write("物业币流通量," + overview.getSummary().getPropertyCoinCirculation() + "\n");
        writer.write("物业费收缴率," + overview.getSummary().getPropertyFeeCollectionRate() + "\n");
        writer.write("物业费应缴总额," + overview.getSummary().getPropertyFeeTotalAmount() + "\n");
        writer.write("物业费已缴总额," + overview.getSummary().getPropertyFeePaidAmount() + "\n");
        writer.write("物业费欠费总额," + overview.getSummary().getPropertyFeeOverdueAmount() + "\n");
        writer.write("消费增长率," + overview.getComparison().getConsumptionGrowthRate() + "\n");
        writer.write("订单增长率," + overview.getComparison().getOrderGrowthRate() + "\n");
        writer.write("住户增长率," + overview.getComparison().getResidentGrowthRate() + "\n");
    }

    private void writePointSummaryCsv(Writer writer, String propertyCompanyId, DateRange range) throws java.io.IOException {
        ReportPointSummaryResponse pointSummary = getPointSummary(
                propertyCompanyId,
                toIso(range.start()),
                toIso(range.end()),
                "month");
        writer.write("指标,数值\n");
        writer.write("发放总量," + pointSummary.getSummary().getTotalIssued() + "\n");
        writer.write("消费总量," + pointSummary.getSummary().getTotalConsumed() + "\n");
        writer.write("过期总量," + pointSummary.getSummary().getTotalExpired() + "\n");
        writer.write("当前余额," + pointSummary.getSummary().getCurrentBalance() + "\n");
        writer.write("积分池余额," + pointSummary.getSummary().getPoolBalance() + "\n");
        writer.write("折合金额," + pointSummary.getSummary().getEquivalentAmount() + "\n");
        writer.write("\n日期,发放,消费,过期\n");
        for (ReportPointSummaryResponse.TrendItem item : pointSummary.getTrend()) {
            writer.write(csv(item.getDate()) + "," + item.getIssued() + "," + item.getConsumed() + "," + item.getExpired() + "\n");
        }
    }

    private void writeOrderSummaryCsv(Writer writer, String propertyCompanyId, DateRange range) throws java.io.IOException {
        ReportOrderSummaryResponse orderSummary = getOrderSummary(
                propertyCompanyId,
                toIso(range.start()),
                toIso(range.end()),
                "month");
        writer.write("指标,数值\n");
        writer.write("订单总数," + orderSummary.getSummary().getTotalOrderCount() + "\n");
        writer.write("订单总额," + orderSummary.getSummary().getTotalAmount() + "\n");
        writer.write("平均客单价," + orderSummary.getSummary().getAvgOrderAmount() + "\n");
        writer.write("已完成订单," + orderSummary.getSummary().getCompletedCount() + "\n");
        writer.write("已取消订单," + orderSummary.getSummary().getCancelledCount() + "\n");
        writer.write("积分抵扣总量," + orderSummary.getSummary().getPointUsedTotal() + "\n");
        writer.write("物业币抵扣总量," + orderSummary.getSummary().getCoinUsedTotal() + "\n");
        writer.write("现金支付总量," + orderSummary.getSummary().getCashAmountTotal() + "\n");
        writer.write("\n商家ID,商家名称,分类,订单数,总额,平均客单价\n");
        for (ReportOrderSummaryResponse.ByMerchantItem item : orderSummary.getByMerchant()) {
            writer.write(csv(item.getMerchantId()) + ","
                    + csv(item.getMerchantName()) + ","
                    + csv(item.getCategory()) + ","
                    + item.getOrderCount() + ","
                    + item.getTotalAmount() + ","
                    + item.getAvgOrderAmount() + "\n");
        }
        writer.write("\n支付方式,订单数,总额\n");
        for (ReportOrderSummaryResponse.ByPaymentMethodItem item : orderSummary.getByPaymentMethod()) {
            writer.write(csv(item.getPaymentMethod()) + ","
                    + item.getOrderCount() + ","
                    + item.getTotalAmount() + "\n");
        }
    }

    private void writeDistributionSummaryCsv(Writer writer, String propertyCompanyId, DateRange range) throws java.io.IOException {
        DistributionStatsResponse stats = getDistributionSummary(
                propertyCompanyId,
                toIso(range.start()),
                toIso(range.end()),
                "detail");
        writer.write("指标,数值\n");
        writer.write("可分成总额," + stats.getSummary().getTotalDistributableAmount() + "\n");
        writer.write("物业分成," + stats.getSummary().getPropertyAmount() + "\n");
        writer.write("统筹分成," + stats.getSummary().getCoordinatorAmount() + "\n");
        writer.write("板块负责人分成," + stats.getSummary().getSectorLeaderAmount() + "\n");
        writer.write("个体负责人分成," + stats.getSummary().getIndividualLeaderAmount() + "\n");
        writer.write("\n物业ID,物业名称,分成金额\n");
        for (DistributionStatsResponse.ByPropertyItem item : stats.getByProperty()) {
            writer.write(csv(item.getPropertyCompanyId()) + ","
                    + csv(item.getPropertyName()) + ","
                    + item.getAmount() + "\n");
        }
        writer.write("\n统筹ID,姓名,分成金额\n");
        for (DistributionStatsResponse.ByCoordinatorItem item : stats.getByCoordinator()) {
            writer.write(csv(item.getCoordinatorId()) + ","
                    + csv(item.getName()) + ","
                    + item.getAmount() + "\n");
        }
        writer.write("\n板块,板块名称,分成金额\n");
        for (DistributionStatsResponse.BySectorItem item : stats.getBySector()) {
            writer.write(csv(item.getSector()) + ","
                    + csv(item.getSectorName()) + ","
                    + item.getAmount() + "\n");
        }
    }

    private ReportOverviewResponse buildOverviewForExport(String propertyCompanyId, DateRange range) {
        return ReportOverviewResponse.builder()
                .scope(buildScope(propertyCompanyId))
                .summary(buildOverviewSummary(propertyCompanyId, range))
                .comparison(buildComparison(propertyCompanyId, range))
                .build();
    }

    private ReportOverviewResponse.Scope buildScope(String propertyCompanyId) {
        if (!StringUtils.hasText(propertyCompanyId)) {
            return ReportOverviewResponse.Scope.builder()
                    .propertyCompanyId(null)
                    .propertyCompanyName("全平台")
                    .build();
        }
        PropertyCompany company = propertyCompanyRepository.findById(propertyCompanyId).orElse(null);
        return ReportOverviewResponse.Scope.builder()
                .propertyCompanyId(propertyCompanyId)
                .propertyCompanyName(company != null ? company.getName() : null)
                .build();
    }

    private ReportOverviewResponse.Summary buildOverviewSummary(String propertyCompanyId, DateRange range) {
        long merchantCount = countMerchants(propertyCompanyId);
        long residentCount = countResidents(propertyCompanyId);
        long familyCount = countFamilies(propertyCompanyId);

        BigDecimal totalConsumptionAmount = reportStatsRepository.sumConsumptionAmount(
                propertyCompanyId, range.start(), range.end());
        long totalOrderCount = reportStatsRepository.countOrders(
                propertyCompanyId, range.start(), range.end());
        BigDecimal pointPoolBalance = reportStatsRepository.sumPointPoolBalance(propertyCompanyId);
        BigDecimal propertyCoinCirculation = reportStatsRepository.sumFamilyCoinBalance(propertyCompanyId);

        BigDecimal propertyFeeTotalAmount = reportStatsRepository.sumPropertyFeeAmount(
                propertyCompanyId, range.start(), range.end());
        BigDecimal propertyFeePaidAmount = reportStatsRepository.sumPropertyFeePaidAmount(
                propertyCompanyId, range.start(), range.end());
        BigDecimal propertyFeeOverdueAmount = reportStatsRepository.sumPropertyFeeOverdueAmount(
                propertyCompanyId, range.start(), range.end());

        return ReportOverviewResponse.Summary.builder()
                .merchantCount(merchantCount)
                .residentCount(residentCount)
                .familyCount(familyCount)
                .totalConsumptionAmount(totalConsumptionAmount)
                .totalOrderCount(totalOrderCount)
                .pointPoolBalance(pointPoolBalance)
                .propertyCoinCirculation(propertyCoinCirculation)
                .propertyFeeCollectionRate(calcCollectionRate(propertyFeeTotalAmount, propertyFeePaidAmount))
                .propertyFeeTotalAmount(propertyFeeTotalAmount)
                .propertyFeePaidAmount(propertyFeePaidAmount)
                .propertyFeeOverdueAmount(propertyFeeOverdueAmount)
                .build();
    }

    private ReportOverviewResponse.Comparison buildComparison(String propertyCompanyId, DateRange range) {
        DateRange currentPeriod = range.start() != null && range.end() != null
                ? range
                : currentMonthRange();
        DateRange previous = previousRange(currentPeriod);

        BigDecimal currentConsumption = reportStatsRepository.sumConsumptionAmount(
                propertyCompanyId, currentPeriod.start(), currentPeriod.end());
        BigDecimal previousConsumption = reportStatsRepository.sumConsumptionAmount(
                propertyCompanyId, previous.start(), previous.end());

        long currentOrders = reportStatsRepository.countOrders(
                propertyCompanyId, currentPeriod.start(), currentPeriod.end());
        long previousOrders = reportStatsRepository.countOrders(
                propertyCompanyId, previous.start(), previous.end());

        long currentResidents = reportStatsRepository.countResidentsCreated(
                propertyCompanyId, currentPeriod.start(), currentPeriod.end());
        long previousResidents = reportStatsRepository.countResidentsCreated(
                propertyCompanyId, previous.start(), previous.end());

        return ReportOverviewResponse.Comparison.builder()
                .consumptionGrowthRate(calcGrowthRate(currentConsumption, previousConsumption))
                .orderGrowthRate(calcGrowthRate(
                        BigDecimal.valueOf(currentOrders), BigDecimal.valueOf(previousOrders)))
                .residentGrowthRate(calcGrowthRate(
                        BigDecimal.valueOf(currentResidents), BigDecimal.valueOf(previousResidents)))
                .build();
    }

    private List<ReportPointSummaryResponse.TrendItem> buildPointTrend(
            String propertyCompanyId, DateRange range, String groupBy) {
        if (range.start() == null || range.end() == null) {
            return List.of();
        }
        return reportStatsRepository.groupPointTrend(
                        propertyCompanyId, range.start(), range.end(), groupBy)
                .stream()
                .map(row -> ReportPointSummaryResponse.TrendItem.builder()
                        .date(String.valueOf(row[0]))
                        .issued(toLong(row[1]))
                        .consumed(toLong(row[2]))
                        .expired(toLong(row[3]))
                        .build())
                .toList();
    }

    private List<ReportOrderSummaryResponse.TrendItem> buildOrderTrend(
            String propertyCompanyId, DateRange range, String groupBy) {
        if (range.start() == null || range.end() == null) {
            return List.of();
        }
        return reportStatsRepository.groupOrderTrend(
                        propertyCompanyId, range.start(), range.end(), groupBy)
                .stream()
                .map(row -> ReportOrderSummaryResponse.TrendItem.builder()
                        .date(String.valueOf(row[0]))
                        .orderCount(toLong(row[1]))
                        .totalAmount(toBigDecimal(row[2]))
                        .build())
                .toList();
    }

    private List<ReportOrderSummaryResponse.ByMerchantItem> buildByMerchant(
            String propertyCompanyId, DateRange range) {
        List<Object[]> rows = reportStatsRepository.groupOrdersByMerchant(
                propertyCompanyId, range.start(), range.end());
        if (rows.isEmpty()) {
            return List.of();
        }

        List<String> merchantIds = rows.stream()
                .map(row -> String.valueOf(row[0]))
                .toList();
        Map<String, Merchant> merchantMap = merchantRepository.findAllById(merchantIds).stream()
                .collect(Collectors.toMap(Merchant::getId, Function.identity()));

        return rows.stream()
                .map(row -> {
                    String merchantId = String.valueOf(row[0]);
                    long orderCount = toLong(row[1]);
                    BigDecimal totalAmount = toBigDecimal(row[2]);
                    Merchant merchant = merchantMap.get(merchantId);
                    return ReportOrderSummaryResponse.ByMerchantItem.builder()
                            .merchantId(merchantId)
                            .merchantName(merchant != null ? merchant.getName() : null)
                            .category(merchant != null ? merchant.getCategory() : null)
                            .orderCount(orderCount)
                            .totalAmount(totalAmount)
                            .avgOrderAmount(calcAvg(totalAmount, orderCount))
                            .build();
                })
                .toList();
    }

    private List<ReportOrderSummaryResponse.ByPaymentMethodItem> buildByPaymentMethod(
            String propertyCompanyId, DateRange range) {
        return reportStatsRepository.groupOrdersByPaymentMethod(
                        propertyCompanyId, range.start(), range.end())
                .stream()
                .map(row -> ReportOrderSummaryResponse.ByPaymentMethodItem.builder()
                        .paymentMethod(row[0] != null ? String.valueOf(row[0]) : null)
                        .orderCount(toLong(row[1]))
                        .totalAmount(toBigDecimal(row[2]))
                        .build())
                .toList();
    }

    private long countMerchants(String propertyCompanyId) {
        if (StringUtils.hasText(propertyCompanyId)) {
            return merchantRepository.countByPropertyCompanyId(propertyCompanyId);
        }
        return merchantRepository.count();
    }

    private long countResidents(String propertyCompanyId) {
        if (StringUtils.hasText(propertyCompanyId)) {
            return residentRepository.countByPropertyCompanyId(propertyCompanyId);
        }
        return residentRepository.count();
    }

    private long countFamilies(String propertyCompanyId) {
        if (StringUtils.hasText(propertyCompanyId)) {
            return familyRepository.countByPropertyCompanyId(propertyCompanyId);
        }
        return familyRepository.count();
    }

    private BigDecimal resolvePointToFeeRate(String propertyCompanyId) {
        if (!StringUtils.hasText(propertyCompanyId)) {
            return new BigDecimal("0.0100");
        }
        return propertyCompanyRepository.findById(propertyCompanyId)
                .map(PropertyCompany::getPointToFeeRate)
                .filter(rate -> rate != null && rate.compareTo(BigDecimal.ZERO) > 0)
                .orElse(new BigDecimal("0.0100"));
    }

    private void requireReportRole() {
        permissionGuard.requirePermissionOrAdmin("reports.view");
    }

    private String resolveScopedPropertyCompanyId(String propertyCompanyId) {
        return roleGuard.resolveListPropertyCompanyId(propertyCompanyId);
    }

    private String resolveGroupBy(String groupBy) {
        if (!StringUtils.hasText(groupBy)) {
            return "month";
        }
        if (!GROUP_BY_VALUES.contains(groupBy)) {
            throw new BizException(ErrorCode.PARAM_INVALID, "groupBy不合法");
        }
        return groupBy;
    }

    private DateRange resolveDateRange(String startDate, String endDate, String month) {
        if (StringUtils.hasText(month)) {
            try {
                YearMonth yearMonth = YearMonth.parse(month, DateTimeFormatter.ofPattern("yyyy-MM"));
                LocalDateTime start = yearMonth.atDay(1).atStartOfDay();
                LocalDateTime end = yearMonth.plusMonths(1).atDay(1).atStartOfDay();
                return new DateRange(start, end);
            } catch (DateTimeParseException ex) {
                throw new BizException(ErrorCode.PARAM_INVALID, "month格式应为YYYY-MM");
            }
        }
        LocalDateTime start = parseIsoDate(startDate);
        LocalDateTime end = parseIsoDate(endDate);
        return new DateRange(start, end);
    }

    private DateRange previousRange(DateRange current) {
        LocalDateTime start = current.start();
        LocalDateTime end = current.end();
        if (start == null || end == null) {
            YearMonth previousMonth = YearMonth.now(ZoneOffset.UTC).minusMonths(1);
            LocalDateTime previousStart = previousMonth.atDay(1).atStartOfDay();
            LocalDateTime previousEnd = previousMonth.plusMonths(1).atDay(1).atStartOfDay();
            return new DateRange(previousStart, previousEnd);
        }
        long days = ChronoUnit.DAYS.between(start, end);
        if (days <= 0) {
            days = 30;
        }
        LocalDateTime previousEnd = start;
        LocalDateTime previousStart = previousEnd.minusDays(days);
        return new DateRange(previousStart, previousEnd);
    }

    private DateRange currentMonthRange() {
        YearMonth currentMonth = YearMonth.now(ZoneOffset.UTC);
        LocalDateTime start = currentMonth.atDay(1).atStartOfDay();
        LocalDateTime end = currentMonth.plusMonths(1).atDay(1).atStartOfDay();
        return new DateRange(start, end);
    }

    private String buildExportFilename(String type, String month, DateRange range) {
        String suffix;
        if (StringUtils.hasText(month)) {
            suffix = month;
        } else if (range.start() != null) {
            suffix = range.start().toLocalDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        } else {
            suffix = LocalDate.now(ZoneOffset.UTC).format(DateTimeFormatter.ofPattern("yyyy-MM"));
        }
        return type + "-" + suffix + ".csv";
    }

    private BigDecimal calcCollectionRate(BigDecimal total, BigDecimal paid) {
        if (total == null || total.compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ZERO.setScale(RATE_SCALE, RoundingMode.HALF_UP);
        }
        return paid.divide(total, RATE_SCALE, RoundingMode.HALF_UP);
    }

    private BigDecimal calcAvg(BigDecimal total, long count) {
        if (count <= 0) {
            return BigDecimal.ZERO.setScale(MONEY_SCALE, RoundingMode.HALF_UP);
        }
        return total.divide(BigDecimal.valueOf(count), MONEY_SCALE, RoundingMode.HALF_UP);
    }

    private BigDecimal calcGrowthRate(BigDecimal current, BigDecimal previous) {
        if (previous == null || previous.compareTo(BigDecimal.ZERO) == 0) {
            return current.compareTo(BigDecimal.ZERO) > 0
                    ? BigDecimal.ONE.setScale(RATE_SCALE, RoundingMode.HALF_UP)
                    : BigDecimal.ZERO.setScale(RATE_SCALE, RoundingMode.HALF_UP);
        }
        return current.subtract(previous)
                .divide(previous, RATE_SCALE, RoundingMode.HALF_UP);
    }

    private LocalDateTime parseIsoDate(String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        try {
            return LocalDateTime.ofInstant(Instant.parse(value), ZoneOffset.UTC);
        } catch (Exception ex) {
            throw new BizException(ErrorCode.PARAM_INVALID, "日期格式不正确");
        }
    }

    private String toIso(LocalDateTime value) {
        return value != null ? value.atOffset(ZoneOffset.UTC).toInstant().toString() : null;
    }

    private String csv(String value) {
        if (value == null) {
            return "";
        }
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }

    private long toLong(Object value) {
        return value != null ? ((Number) value).longValue() : 0L;
    }

    private BigDecimal toBigDecimal(Object value) {
        if (value == null) {
            return BigDecimal.ZERO.setScale(MONEY_SCALE, RoundingMode.HALF_UP);
        }
        if (value instanceof BigDecimal bd) {
            return bd.setScale(MONEY_SCALE, RoundingMode.HALF_UP);
        }
        return new BigDecimal(value.toString()).setScale(MONEY_SCALE, RoundingMode.HALF_UP);
    }

    private BigDecimal money(BigDecimal value) {
        return value.setScale(MONEY_SCALE, RoundingMode.HALF_UP);
    }

    private record DateRange(LocalDateTime start, LocalDateTime end) {
    }
}
