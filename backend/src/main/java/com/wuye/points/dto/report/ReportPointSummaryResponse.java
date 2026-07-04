package com.wuye.points.dto.report;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@Schema(description = "积分汇总报表响应")
public class ReportPointSummaryResponse {

    @Schema(description = "汇总统计")
    private Summary summary;

    @Schema(description = "趋势数据列表")
    private List<TrendItem> trend;

    @Data
    @Builder
    @Schema(description = "积分汇总统计")
    public static class Summary {
        @Schema(description = "累计发放积分", example = "1000000")
        private long totalIssued;

        @Schema(description = "累计消费积分", example = "800000")
        private long totalConsumed;

        @Schema(description = "累计过期积分", example = "50000")
        private long totalExpired;

        @Schema(description = "当前积分余额", example = "150000")
        private long currentBalance;

        @Schema(description = "积分池余额", example = "500000.00")
        private BigDecimal poolBalance;

        @Schema(description = "等价金额", example = "50000.00")
        private BigDecimal equivalentAmount;
    }

    @Data
    @Builder
    @Schema(description = "趋势数据项")
    public static class TrendItem {
        @Schema(description = "日期", example = "2026-01-01")
        private String date;

        @Schema(description = "发放积分", example = "10000")
        private long issued;

        @Schema(description = "消费积分", example = "8000")
        private long consumed;

        @Schema(description = "过期积分", example = "500")
        private long expired;
    }
}
