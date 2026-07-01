package com.wuye.points.dto.propertycompany;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class PropertyCompanyCurrentResponse {

    private String id;
    private String name;
    private String logoUrl;
    private String contactPhone;
    private String address;
    private DistributionRates distributionRates;
    private Boolean coinDisplayEnabled;
    private String coinIssueMode;
    private Integer coinExpiryDays;
    private DeliveryConfig deliveryConfig;
    private BigDecimal withdrawalFeeRate;
    private BigDecimal pointToFeeRate;
    private Boolean twoYearClearEnabled;
    private Integer neighborDailyContactLimit;

    @Data
    @Builder
    public static class DistributionRates {
        private BigDecimal propertyRate;
        private BigDecimal coordinatorRate;
        private BigDecimal sectorLeaderRate;
        private BigDecimal individualLeaderRate;
    }

    @Data
    @Builder
    public static class DeliveryConfig {
        private BigDecimal baseFee;
        private BigDecimal courierShareRate;
        private BigDecimal courierPerOrder;
    }
}
