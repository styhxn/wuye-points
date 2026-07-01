package com.wuye.points.service;

import com.wuye.points.dto.propertyfee.PropertyFeeDetailResponse;
import com.wuye.points.dto.propertyfee.PropertyFeeListItemResponse;
import com.wuye.points.entity.Community;
import com.wuye.points.entity.PropertyFee;
import com.wuye.points.entity.PropertyFeeRule;
import com.wuye.points.entity.Resident;
import com.wuye.points.util.DateTimeUtil;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Map;

@Component
public class PropertyFeeConverter {

    public PropertyFeeListItemResponse toListItem(PropertyFee fee,
                                                  Resident resident,
                                                  Community community,
                                                  PropertyFeeRule rule) {
        BigDecimal paidAmount = defaultPaidAmount(fee);
        BigDecimal remainingAmount = fee.getAmount().subtract(paidAmount).max(BigDecimal.ZERO);
        OverdueInfo overdueInfo = resolveOverdueInfo(fee, rule, remainingAmount);

        return PropertyFeeListItemResponse.builder()
                .id(fee.getId())
                .residentId(fee.getResidentId())
                .residentName(resident != null ? resident.getName() : null)
                .familyId(fee.getFamilyId())
                .communityId(fee.getCommunityId())
                .communityName(community != null ? community.getName() : null)
                .building(resident != null ? resident.getBuilding() : null)
                .room(resident != null ? resident.getRoom() : null)
                .feeType(fee.getFeeType())
                .feeTypeName(resolveFeeTypeName(fee.getFeeType()))
                .period(fee.getPeriod())
                .amount(fee.getAmount())
                .paidAmount(paidAmount)
                .remainingAmount(remainingAmount)
                .pointDeducted(defaultPointDeducted(fee))
                .coinDeducted(defaultCoinDeducted(fee))
                .paymentStatus(resolvePaymentStatus(fee))
                .dueDate(formatDate(fee.getDueDate()))
                .paidAt(DateTimeUtil.toIsoUtc(fee.getPaidAt()))
                .autoDeduct(Boolean.TRUE.equals(fee.getAutoDeduct()))
                .overdueDays(overdueInfo.overdueDays())
                .lateFee(overdueInfo.lateFee())
                .createdAt(DateTimeUtil.toIsoUtc(fee.getCreatedAt()))
                .build();
    }

    public PropertyFeeDetailResponse toDetail(PropertyFee fee,
                                              Resident resident,
                                              Community community,
                                              PropertyFeeRule rule) {
        PropertyFeeListItemResponse listItem = toListItem(fee, resident, community, rule);
        return PropertyFeeDetailResponse.builder()
                .id(listItem.getId())
                .residentId(listItem.getResidentId())
                .residentName(listItem.getResidentName())
                .familyId(listItem.getFamilyId())
                .propertyCompanyId(fee.getPropertyCompanyId())
                .communityId(listItem.getCommunityId())
                .communityName(listItem.getCommunityName())
                .building(listItem.getBuilding())
                .unit(resident != null ? resident.getUnit() : null)
                .room(listItem.getRoom())
                .feeType(listItem.getFeeType())
                .feeTypeName(listItem.getFeeTypeName())
                .period(listItem.getPeriod())
                .amount(listItem.getAmount())
                .paidAmount(listItem.getPaidAmount())
                .remainingAmount(listItem.getRemainingAmount())
                .pointDeducted(listItem.getPointDeducted())
                .coinDeducted(listItem.getCoinDeducted())
                .paymentStatus(listItem.getPaymentStatus())
                .dueDate(listItem.getDueDate())
                .paidAt(listItem.getPaidAt())
                .autoDeduct(listItem.getAutoDeduct())
                .overdueDays(listItem.getOverdueDays())
                .lateFee(listItem.getLateFee())
                .createdAt(listItem.getCreatedAt())
                .updatedAt(DateTimeUtil.toIsoUtc(fee.getUpdatedAt()))
                .build();
    }

    public String resolvePaymentStatus(PropertyFee fee) {
        BigDecimal paidAmount = defaultPaidAmount(fee);
        BigDecimal amount = fee.getAmount() != null ? fee.getAmount() : BigDecimal.ZERO;
        if (paidAmount.compareTo(amount) >= 0) {
            return "paid";
        }
        if (paidAmount.compareTo(BigDecimal.ZERO) > 0) {
            return "partial";
        }
        if (fee.getDueDate() != null && fee.getDueDate().isBefore(LocalDate.now())) {
            return "overdue";
        }
        String status = fee.getPaymentStatus();
        return StringUtils.hasText(status) ? status : "unpaid";
    }

    public String resolveFeeTypeName(String feeType) {
        if (feeType == null) {
            return null;
        }
        return switch (feeType) {
            case "property" -> "物业费";
            case "parking" -> "停车费";
            case "water" -> "水费";
            case "electricity" -> "电费";
            default -> feeType;
        };
    }

    public BigDecimal defaultPaidAmount(PropertyFee fee) {
        return fee.getPaidAmount() != null ? fee.getPaidAmount() : BigDecimal.ZERO;
    }

    public int defaultPointDeducted(PropertyFee fee) {
        return fee.getPointDeducted() != null ? fee.getPointDeducted() : 0;
    }

    public BigDecimal defaultCoinDeducted(PropertyFee fee) {
        return fee.getCoinDeducted() != null ? fee.getCoinDeducted() : BigDecimal.ZERO;
    }

    public PropertyFeeRule pickRule(Map<String, PropertyFeeRule> ruleCache,
                                    String propertyCompanyId,
                                    String feeType) {
        String cacheKey = propertyCompanyId + ":" + feeType;
        return ruleCache.get(cacheKey);
    }

    private OverdueInfo resolveOverdueInfo(PropertyFee fee,
                                           PropertyFeeRule rule,
                                           BigDecimal remainingAmount) {
        if (fee.getDueDate() == null || remainingAmount.compareTo(BigDecimal.ZERO) <= 0) {
            return new OverdueInfo(null, null);
        }
        if (!fee.getDueDate().isBefore(LocalDate.now())) {
            return new OverdueInfo(null, null);
        }
        int overdueDays = (int) ChronoUnit.DAYS.between(fee.getDueDate(), LocalDate.now());
        if (overdueDays <= 0) {
            return new OverdueInfo(null, null);
        }
        BigDecimal lateFee = null;
        if (rule != null && rule.getOverdueRate() != null
                && rule.getOverdueRate().compareTo(BigDecimal.ZERO) > 0) {
            lateFee = remainingAmount
                    .multiply(rule.getOverdueRate())
                    .multiply(BigDecimal.valueOf(overdueDays))
                    .setScale(2, RoundingMode.HALF_UP);
        }
        return new OverdueInfo(overdueDays, lateFee);
    }

    private String formatDate(LocalDate date) {
        return date != null ? date.toString() : null;
    }

    private record OverdueInfo(Integer overdueDays, BigDecimal lateFee) {
    }
}
