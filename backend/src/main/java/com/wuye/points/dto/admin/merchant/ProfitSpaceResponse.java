package com.wuye.points.dto.admin.merchant;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Schema(description = "商家利润空间响应")
@Data
@Builder
public class ProfitSpaceResponse {

    @Schema(description = "平台商家ID")
    private String platformMerchantId;

    @Schema(description = "平台商家名称")
    private String platformMerchantName;

    @Schema(description = "物业公司ID")
    private String propertyCompanyId;

    @Schema(description = "物业公司名称")
    private String propertyName;

    @Schema(description = "统计周期")
    private String period;

    @Schema(description = "开始日期")
    private String startDate;

    @Schema(description = "结束日期")
    private String endDate;

    @Schema(description = "指标数据")
    private Metrics metrics;

    @Schema(description = "分成明细")
    private Breakdown breakdown;

    @Schema(description = "同比数据")
    private Comparison comparison;

    @Data
    @Builder
    public static class Metrics {
        @Schema(description = "总订单数")
        private Long totalOrders;

        @Schema(description = "总收入")
        private BigDecimal totalRevenue;

        @Schema(description = "佣金总额")
        private BigDecimal totalCommission;

        @Schema(description = "配送费总额")
        private BigDecimal totalDeliveryFee;

        @Schema(description = "积分成本")
        private BigDecimal totalPointCost;

        @Schema(description = "物业币成本")
        private BigDecimal totalCoinCost;

        @Schema(description = "净利润")
        private BigDecimal netProfit;

        @Schema(description = "利润率")
        private BigDecimal profitMargin;
    }

    @Data
    @Builder
    public static class Breakdown {
        @Schema(description = "物业分成")
        private BigDecimal propertyShare;

        @Schema(description = "统筹分成")
        private BigDecimal coordinatorShare;

        @Schema(description = "片区负责人分成")
        private BigDecimal sectorLeaderShare;

        @Schema(description = "个人负责人分成")
        private BigDecimal individualLeaderShare;

        @Schema(description = "平台分成")
        private BigDecimal platformShare;
    }

    @Data
    @Builder
    public static class Comparison {
        @Schema(description = "上期收入")
        private BigDecimal lastPeriodRevenue;

        @Schema(description = "收入增长率")
        private BigDecimal revenueGrowthRate;

        @Schema(description = "上期利润")
        private BigDecimal lastPeriodProfit;

        @Schema(description = "利润增长率")
        private BigDecimal profitGrowthRate;
    }
}
