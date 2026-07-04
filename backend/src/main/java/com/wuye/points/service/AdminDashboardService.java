package com.wuye.points.service;

import com.wuye.points.dto.admin.dashboard.DashboardOverviewResponse;
import com.wuye.points.entity.PointPool;
import com.wuye.points.exception.BizException;
import com.wuye.points.exception.ErrorCode;
import com.wuye.points.repository.*;
import com.wuye.points.util.AdminSupport;
import com.wuye.points.util.RoleGuard;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

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

        long frozenCoinCount = coinFreezeRecordRepository.countByPropertyCompanyIdAndAction(propertyCompanyId, "freeze");

        long pendingAuditCount = merchantRepository.countByPropertyCompanyIdAndAuditStatus(propertyCompanyId, "pending_audit");

        DashboardOverviewResponse.Summary summary = DashboardOverviewResponse.Summary.builder()
                .totalOrders(0L)
                .totalRevenue(BigDecimal.ZERO)
                .totalResidents(totalResidents)
                .totalFamilies(totalFamilies)
                .totalMerchants(totalMerchants)
                .build();

        DashboardOverviewResponse.Trends trends = DashboardOverviewResponse.Trends.builder()
                .orderGrowthRate(BigDecimal.ZERO)
                .revenueGrowthRate(BigDecimal.ZERO)
                .newResidentGrowthRate(BigDecimal.ZERO)
                .build();

        return DashboardOverviewResponse.builder()
                .summary(summary)
                .trends(trends)
                .topMerchants(java.util.List.of())
                .recentActivity(java.util.List.of())
                .build();
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
