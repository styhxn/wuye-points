package com.wuye.points.dto.report;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
public class ReportOrderSummaryResponse {

    private Summary summary;
    private List<ByMerchantItem> byMerchant;
    private List<ByPaymentMethodItem> byPaymentMethod;
    private List<TrendItem> trend;

    @Data
    @Builder
    public static class Summary {
        private long totalOrderCount;
        private BigDecimal totalAmount;
        private BigDecimal avgOrderAmount;
        private long completedCount;
        private long cancelledCount;
        private long pointUsedTotal;
        private BigDecimal coinUsedTotal;
        private BigDecimal cashAmountTotal;
    }

    @Data
    @Builder
    public static class ByMerchantItem {
        private String merchantId;
        private String merchantName;
        private String category;
        private long orderCount;
        private BigDecimal totalAmount;
        private BigDecimal avgOrderAmount;
    }

    @Data
    @Builder
    public static class ByPaymentMethodItem {
        private String paymentMethod;
        private long orderCount;
        private BigDecimal totalAmount;
    }

    @Data
    @Builder
    public static class TrendItem {
        private String date;
        private long orderCount;
        private BigDecimal totalAmount;
    }
}
