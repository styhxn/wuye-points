package com.wuye.points.service;

import com.wuye.points.dto.admin.dashboard.DashboardOverviewResponse;
import com.wuye.points.entity.Merchant;
import com.wuye.points.entity.PointPool;
import com.wuye.points.entity.PropertyFee;
import com.wuye.points.entity.PropertyCompany;
import com.wuye.points.entity.Resident;
import com.wuye.points.exception.BizException;
import com.wuye.points.exception.ErrorCode;
import com.wuye.points.repository.*;
import com.wuye.points.util.AdminSupport;
import com.wuye.points.util.RoleGuard;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminDashboardService {

    private final ResidentRepository residentRepository;
    private final FamilyRepository familyRepository;
    private final MerchantRepository merchantRepository;
    private final PropertyFeeRepository propertyFeeRepository;
    private final PointPoolRepository pointPoolRepository;
    private final CoinFreezeRecordRepository coinFreezeRecordRepository;
    private final PropertyCoinRecordRepository propertyCoinRecordRepository;
    private final OrderRepository orderRepository;
    private final PropertyCompanyRepository propertyCompanyRepository;
    private final AdminSupport adminSupport;
    private final RoleGuard roleGuard;

    @Transactional(readOnly = true)
    public DashboardOverviewResponse getOverview(String requestedPropertyCompanyId) {
        roleGuard.requireAnyRole("property_admin", "platform_admin");

        String propertyCompanyId = adminSupport.resolvePropertyCompanyId(requestedPropertyCompanyId);
        if (propertyCompanyId == null) {
            throw new BizException(ErrorCode.PARAM_MISSING, "propertyCompanyId不能为空");
        }

        long totalResidents = residentRepository.countByPropertyCompanyId(propertyCompanyId);
        long totalFamilies = familyRepository.countByPropertyCompanyId(propertyCompanyId);
        long totalMerchants = merchantRepository.countByPropertyCompanyId(propertyCompanyId);

        long totalFees = propertyFeeRepository.countByPropertyCompanyId(propertyCompanyId);
        long paidFees = propertyFeeRepository.countByPropertyCompanyIdAndPaymentStatus(propertyCompanyId, "paid");
        BigDecimal collectionRate = totalFees > 0
                ? BigDecimal.valueOf(paidFees).divide(BigDecimal.valueOf(totalFees), 4, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        BigDecimal monthlyConsumption = calculateMonthlyConsumption(propertyCompanyId);

        PointPool pool = pointPoolRepository.findByPropertyCompanyId(propertyCompanyId)
                .orElse(null);
        BigDecimal pointPoolBalance = pool != null && pool.getBalance() != null
                ? pool.getBalance() : BigDecimal.ZERO;

        BigDecimal coinTotalIssued = propertyCoinRecordRepository.sumIssuedByPropertyCompanyId(propertyCompanyId);
        if (coinTotalIssued == null) coinTotalIssued = BigDecimal.ZERO;

        BigDecimal coinConsumed = propertyCoinRecordRepository.sumConsumedByPropertyCompanyId(propertyCompanyId);
        if (coinConsumed == null) coinConsumed = BigDecimal.ZERO;

        BigDecimal coinInCirculation = coinTotalIssued.subtract(coinConsumed);
        if (coinInCirculation.compareTo(BigDecimal.ZERO) < 0) coinInCirculation = BigDecimal.ZERO;

        long frozenCoinCount = coinFreezeRecordRepository.countByPropertyCompanyIdAndAction(propertyCompanyId, "freeze");

        long pendingAuditCount = merchantRepository.countByPropertyCompanyIdAndAuditStatus(propertyCompanyId, "pending_audit");

        // 订单统计
        long totalOrders = orderRepository.countByPropertyCompanyId(propertyCompanyId);
        BigDecimal totalRevenue = orderRepository.sumRevenueByPropertyCompanyId(propertyCompanyId);

        // 物业费统计
        BigDecimal propertyFeeAmount = propertyFeeRepository.sumAmountByPropertyCompanyId(propertyCompanyId);
        BigDecimal propertyFeeCollected = propertyFeeRepository.sumPaidAmountByPropertyCompanyId(propertyCompanyId);

        DashboardOverviewResponse.Summary summary = DashboardOverviewResponse.Summary.builder()
                .totalOrders(totalOrders)
                .totalRevenue(totalRevenue)
                .totalResidents(totalResidents)
                .totalFamilies(totalFamilies)
                .totalMerchants(totalMerchants)
                .totalCoinIssued(coinTotalIssued)
                .coinConsumed(coinConsumed)
                .coinInCirculation(coinInCirculation)
                .build();

        // 趋势计算
        DashboardOverviewResponse.Trends trends = calculateTrends(propertyCompanyId);

        // 热门商户
        List<DashboardOverviewResponse.TopMerchant> topMerchants = findTopMerchants(propertyCompanyId);

        // 最近活动
        List<DashboardOverviewResponse.RecentActivity> recentActivity = findRecentActivity(propertyCompanyId);

        return DashboardOverviewResponse.builder()
                .summary(summary)
                .trends(trends)
                .topMerchants(topMerchants)
                .recentActivity(recentActivity)
                .build();
    }

    private DashboardOverviewResponse.Trends calculateTrends(String propertyCompanyId) {
        LocalDate now = LocalDate.now();
        LocalDate thisMonthStart = now.withDayOfMonth(1);
        LocalDate lastMonthStart = thisMonthStart.minusMonths(1);

        // 本月新增住户
        long newResidentsThisMonth = residentRepository.countByPropertyCompanyIdAndCreatedAtAfter(
                propertyCompanyId, thisMonthStart.atStartOfDay());
        long newResidentsLastMonth = residentRepository.countByPropertyCompanyIdAndCreatedAtBetween(
                propertyCompanyId, lastMonthStart.atStartOfDay(), thisMonthStart.atStartOfDay());

        // 增长率计算
        BigDecimal orderGrowthRate = BigDecimal.ZERO;
        BigDecimal revenueGrowthRate = BigDecimal.ZERO;
        BigDecimal newResidentGrowthRate = BigDecimal.ZERO;

        if (newResidentsLastMonth > 0) {
            newResidentGrowthRate = BigDecimal.valueOf(newResidentsThisMonth - newResidentsLastMonth)
                    .divide(BigDecimal.valueOf(newResidentsLastMonth), 4, RoundingMode.HALF_UP);
        }

        return DashboardOverviewResponse.Trends.builder()
                .orderGrowthRate(orderGrowthRate)
                .revenueGrowthRate(revenueGrowthRate)
                .newResidentGrowthRate(newResidentGrowthRate)
                .build();
    }

    private List<DashboardOverviewResponse.TopMerchant> findTopMerchants(String propertyCompanyId) {
        List<Object[]> rows = orderRepository.findTopMerchantsByPropertyCompanyId(propertyCompanyId);
        List<DashboardOverviewResponse.TopMerchant> result = new ArrayList<>();
        int limit = Math.min(rows.size(), 5);
        for (int i = 0; i < limit; i++) {
            Object[] row = rows.get(i);
            String merchantId = (String) row[0];
            Long orderCount = ((Number) row[1]).longValue();
            BigDecimal revenue = (BigDecimal) row[2];

            Merchant merchant = merchantRepository.findById(merchantId).orElse(null);
            String merchantName = merchant != null ? merchant.getName() : merchantId;

            result.add(DashboardOverviewResponse.TopMerchant.builder()
                    .id(merchantId)
                    .name(merchantName)
                    .orderCount(orderCount)
                    .revenue(revenue)
                    .build());
        }
        return result;
    }

    private List<DashboardOverviewResponse.RecentActivity> findRecentActivity(String propertyCompanyId) {
        List<DashboardOverviewResponse.RecentActivity> activities = new ArrayList<>();

        // 最近5个订单
        List<com.wuye.points.entity.Order> recentOrders = orderRepository.findByPropertyCompanyId(
                propertyCompanyId, PageRequest.of(0, 5)).getContent();
        for (com.wuye.points.entity.Order order : recentOrders) {
            Resident resident = residentRepository.findById(order.getResidentId()).orElse(null);
            String residentName = resident != null ? resident.getName() : order.getResidentId();
            activities.add(DashboardOverviewResponse.RecentActivity.builder()
                    .type("order")
                    .description("新订单：" + residentName + " 下单")
                    .time(order.getCreatedAt() != null ? order.getCreatedAt().toString() : "")
                    .build());
        }

        // 最近5个住户
        List<Resident> recentResidents = residentRepository.findByPropertyCompanyId(propertyCompanyId);
        int count = 0;
        for (Resident r : recentResidents) {
            if (r.getCreatedAt() != null && r.getCreatedAt().isAfter(LocalDateTime.now().minusDays(7))) {
                activities.add(DashboardOverviewResponse.RecentActivity.builder()
                        .type("new_resident")
                        .description("新业主入驻：" + r.getName())
                        .time(r.getCreatedAt().toString())
                        .build());
                count++;
                if (count >= 5) break;
            }
        }

        return activities.stream()
                .sorted((a, b) -> b.getTime().compareTo(a.getTime()))
                .limit(10)
                .collect(Collectors.toList());
    }

    private BigDecimal calculateMonthlyConsumption(String propertyCompanyId) {
        LocalDateTime monthStart = LocalDate.now().withDayOfMonth(1).atStartOfDay();
        LocalDateTime monthEnd = LocalDate.now().plusMonths(1).withDayOfMonth(1).atStartOfDay();

        java.util.List<com.wuye.points.entity.Resident> residents =
                residentRepository.findByPropertyCompanyId(propertyCompanyId);

        BigDecimal total = BigDecimal.ZERO;
        for (com.wuye.points.entity.Resident resident : residents) {
            BigDecimal spent = propertyCoinRecordRepository.sumSpentBetween(
                    resident.getId(), monthStart, monthEnd);
            if (spent != null) {
                total = total.add(spent);
            }
        }
        return total;
    }
}
