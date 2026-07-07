package com.wuye.points.dto.admin.dashboard;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@Schema(description = "管理后台仪表盘概览响应")
public class DashboardOverviewResponse {

    @Schema(description = "汇总信息")
    private Summary summary;

    @Schema(description = "趋势数据")
    private Trends trends;

    @Schema(description = "热门商户列表")
    private List<TopMerchant> topMerchants;

    @Schema(description = "最近活动列表")
    private List<RecentActivity> recentActivity;

    @Data
    @Builder
    @Schema(description = "汇总信息")
    public static class Summary {
        @Schema(description = "总订单数", example = "1000")
        private long totalOrders;

        @Schema(description = "总营收", example = "50000.00")
        private BigDecimal totalRevenue;

        @Schema(description = "住户总数", example = "500")
        private long totalResidents;

        @Schema(description = "家庭总数", example = "200")
        private long totalFamilies;

        @Schema(description = "商户总数", example = "50")
        private long totalMerchants;

        @Schema(description = "物业币发行总量", example = "482000")
        private BigDecimal totalCoinIssued;

        @Schema(description = "物业币已消费", example = "120540")
        private BigDecimal coinConsumed;

        @Schema(description = "物业币流通中", example = "361460")
        private BigDecimal coinInCirculation;
    }

    @Data
    @Builder
    @Schema(description = "趋势数据")
    public static class Trends {
        @Schema(description = "订单增长率", example = "0.15")
        private BigDecimal orderGrowthRate;

        @Schema(description = "营收增长率", example = "0.20")
        private BigDecimal revenueGrowthRate;

        @Schema(description = "新增住户增长率", example = "0.10")
        private BigDecimal newResidentGrowthRate;
    }

    @Data
    @Builder
    @Schema(description = "热门商户")
    public static class TopMerchant {
        @Schema(description = "商户ID", example = "mch_xxx")
        private String id;

        @Schema(description = "商户名称", example = "xxx")
        private String name;

        @Schema(description = "订单数", example = "100")
        private long orderCount;

        @Schema(description = "营收", example = "5000.00")
        private BigDecimal revenue;
    }

    @Data
    @Builder
    @Schema(description = "最近活动")
    public static class RecentActivity {
        @Schema(description = "活动类型", example = "order")
        private String type;

        @Schema(description = "活动描述", example = "xxx")
        private String description;

        @Schema(description = "活动时间", example = "2026-01-01T00:00:00.000Z")
        private String time;
    }
}