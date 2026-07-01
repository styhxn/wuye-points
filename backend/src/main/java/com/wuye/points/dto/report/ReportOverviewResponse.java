package com.wuye.points.dto.report;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class ReportOverviewResponse {

    private Scope scope;
    private Summary summary;
    private Comparison comparison;

    @Data
    @Builder
    public static class Scope {
        private String propertyCompanyId;
        private String propertyCompanyName;
    }

    @Data
    @Builder
    public static class Summary {
        private long merchantCount;
        private long residentCount;
        private long familyCount;
        private BigDecimal totalConsumptionAmount;
        private long totalOrderCount;
        private BigDecimal pointPoolBalance;
        private BigDecimal propertyCoinCirculation;
        private BigDecimal propertyFeeCollectionRate;
        private BigDecimal propertyFeeTotalAmount;
        private BigDecimal propertyFeePaidAmount;
        private BigDecimal propertyFeeOverdueAmount;
    }

    @Data
    @Builder
    public static class Comparison {
        private BigDecimal consumptionGrowthRate;
        private BigDecimal orderGrowthRate;
        private BigDecimal residentGrowthRate;
    }
}
