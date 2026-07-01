package com.wuye.points.dto.report;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
public class ReportPointSummaryResponse {

    private Summary summary;
    private List<TrendItem> trend;

    @Data
    @Builder
    public static class Summary {
        private long totalIssued;
        private long totalConsumed;
        private long totalExpired;
        private long currentBalance;
        private BigDecimal poolBalance;
        private BigDecimal equivalentAmount;
    }

    @Data
    @Builder
    public static class TrendItem {
        private String date;
        private long issued;
        private long consumed;
        private long expired;
    }
}
