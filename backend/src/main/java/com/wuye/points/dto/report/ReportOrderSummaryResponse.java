package com.wuye.points.dto.report;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@Schema(description = "订单汇总报表响应")
public class ReportOrderSummaryResponse {

    @Schema(description = "汇总统计")
    private Summary summary;

    @Schema(description = "按商户统计列表")
    private List<ByMerchantItem> byMerchant;

    @Schema(description = "按支付方式统计列表")
    private List<ByPaymentMethodItem> byPaymentMethod;

    @Schema(description = "趋势数据列表")
    private List<TrendItem> trend;

    @Data
    @Builder
    @Schema(description = "订单汇总统计")
    public static class Summary {
        @Schema(description = "订单总数", example = "8500")
        private long totalOrderCount;

        @Schema(description = "订单总金额", example = "1250000.00")
        private BigDecimal totalAmount;

        @Schema(description = "平均订单金额", example = "147.06")
        private BigDecimal avgOrderAmount;

        @Schema(description = "已完成订单数", example = "7800")
        private long completedCount;

        @Schema(description = "已取消订单数", example = "700")
        private long cancelledCount;

        @Schema(description = "累计使用积分", example = "500000")
        private long pointUsedTotal;

        @Schema(description = "累计使用物业币", example = "30000.00")
        private BigDecimal coinUsedTotal;

        @Schema(description = "累计现金支付金额", example = "920000.00")
        private BigDecimal cashAmountTotal;
    }

    @Data
    @Builder
    @Schema(description = "按商户统计项")
    public static class ByMerchantItem {
        @Schema(description = "商户ID", example = "mer_p5q6r")
        private String merchantId;

        @Schema(description = "商户名称", example = "家政服务中心")
        private String merchantName;

        @Schema(description = "商户分类", example = "生活服务")
        private String category;

        @Schema(description = "订单数量", example = "320")
        private long orderCount;

        @Schema(description = "订单总金额", example = "48000.00")
        private BigDecimal totalAmount;

        @Schema(description = "平均订单金额", example = "150.00")
        private BigDecimal avgOrderAmount;
    }

    @Data
    @Builder
    @Schema(description = "按支付方式统计项")
    public static class ByPaymentMethodItem {
        @Schema(description = "支付方式", example = "积分+现金")
        private String paymentMethod;

        @Schema(description = "订单数量", example = "2500")
        private long orderCount;

        @Schema(description = "订单总金额", example = "375000.00")
        private BigDecimal totalAmount;
    }

    @Data
    @Builder
    @Schema(description = "趋势数据项")
    public static class TrendItem {
        @Schema(description = "日期", example = "2026-01-01")
        private String date;

        @Schema(description = "订单数量", example = "280")
        private long orderCount;

        @Schema(description = "订单总金额", example = "42000.00")
        private BigDecimal totalAmount;
    }
}
