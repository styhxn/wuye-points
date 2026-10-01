package com.wuye.points.service;

import com.wuye.points.context.UserContext;
import com.wuye.points.dto.stats.PointConsumptionStructureResponse;
import com.wuye.points.dto.stats.PointTrendResponse;
import com.wuye.points.exception.BizException;
import com.wuye.points.exception.ErrorCode;
import com.wuye.points.repository.PointRecordRepository;
import com.wuye.points.util.RoleGuard;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * D7 管理端积分统计服务(真实数据聚合,无假数据)
 */
@Service
@RequiredArgsConstructor
public class AdminPointStatsService {

    private static final DateTimeFormatter MONTH_FMT = DateTimeFormatter.ofPattern("yyyy-MM");
    private static final DateTimeFormatter DAY_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private final PointRecordRepository pointRecordRepository;
    private final RoleGuard roleGuard;

    /**
     * 月度趋势:按日聚合 earn/spend
     * month 缺省为当前月
     */
    @Transactional(readOnly = true)
    public PointTrendResponse getMonthlyTrend(String month, String propertyCompanyId) {
        roleGuard.requireAnyRole("platform_admin", "property_admin");
        YearMonth ym = parseMonth(month);

        String scopedPropertyCompanyId = resolveScope(propertyCompanyId);
        LocalDateTime start = ym.atDay(1).atStartOfDay();
        LocalDateTime end = ym.plusMonths(1).atDay(1).atStartOfDay();

        List<Object[]> rows = pointRecordRepository.dailyTrendByRange(scopedPropertyCompanyId, start, end);

        int totalEarned = 0;
        int totalSpent = 0;
        List<PointTrendResponse.DailyPoint> daily = new ArrayList<>();
        for (Object[] row : rows) {
            java.sql.Date sqlDate = (java.sql.Date) row[0];
            Number earned = (Number) row[1];
            Number spent = (Number) row[2];
            int e = earned != null ? earned.intValue() : 0;
            int s = spent != null ? spent.intValue() : 0;
            totalEarned += e;
            totalSpent += s;
            daily.add(PointTrendResponse.DailyPoint.builder()
                    .date(sqlDate != null ? sqlDate.toLocalDate().format(DAY_FMT) : null)
                    .earned(e)
                    .spent(s)
                    .build());
        }

        return PointTrendResponse.builder()
                .month(ym.format(MONTH_FMT))
                .totalEarned(totalEarned)
                .totalSpent(totalSpent)
                .daily(daily)
                .build();
    }

    /**
     * 消耗结构:按 source 分组聚合 spend
     */
    @Transactional(readOnly = true)
    public PointConsumptionStructureResponse getConsumptionStructure(String month, String propertyCompanyId) {
        roleGuard.requireAnyRole("platform_admin", "property_admin");
        YearMonth ym = parseMonth(month);

        String scopedPropertyCompanyId = resolveScope(propertyCompanyId);
        LocalDateTime start = ym.atDay(1).atStartOfDay();
        LocalDateTime end = ym.plusMonths(1).atDay(1).atStartOfDay();

        List<Object[]> rows = pointRecordRepository.consumptionGroupBySource(scopedPropertyCompanyId, start, end);

        int totalSpent = 0;
        List<Object[]> buffer = new ArrayList<>();
        for (Object[] row : rows) {
            String source = row[0] != null ? row[0].toString() : "unknown";
            Number amount = (Number) row[1];
            int amt = amount != null ? amount.intValue() : 0;
            totalSpent += amt;
            buffer.add(new Object[]{source, amt});
        }

        List<PointConsumptionStructureResponse.ConsumptionItem> items = new ArrayList<>();
        for (Object[] row : buffer) {
            String source = (String) row[0];
            int amount = (int) row[1];
            double percentage = totalSpent > 0 ? (double) amount / totalSpent : 0.0;
            items.add(PointConsumptionStructureResponse.ConsumptionItem.builder()
                    .source(source)
                    .amount(amount)
                    .percentage(round4(percentage))
                    .build());
        }

        return PointConsumptionStructureResponse.builder()
                .month(ym.format(MONTH_FMT))
                .totalSpent(totalSpent)
                .items(items)
                .build();
    }

    private YearMonth parseMonth(String month) {
        if (!StringUtils.hasText(month)) {
            return YearMonth.now();
        }
        try {
            return YearMonth.parse(month, MONTH_FMT);
        } catch (Exception e) {
            throw new BizException(ErrorCode.PARAM_INVALID, "month 格式应为 yyyy-MM");
        }
    }

    /**
     * 平台管理员可见全平台(propertyCompanyId=null);物业管理员限本物业
     */
    private String resolveScope(String propertyCompanyId) {
        if (roleGuard.isPlatformAdmin()) {
            return propertyCompanyId;
        }
        String scoped = UserContext.currentPropertyCompanyId();
        if (StringUtils.hasText(propertyCompanyId) && !propertyCompanyId.equals(scoped)) {
            throw new BizException(ErrorCode.PROPERTY_OPERATOR_OUT_OF_SCOPE);
        }
        return scoped;
    }

    private double round4(double v) {
        return Math.round(v * 10000.0) / 10000.0;
    }
}
