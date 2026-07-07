package com.wuye.points.dto.delivery;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@Schema(description = "管理端送货管理概览响应")
public class AdminDeliveryOverviewResponse {

    @Schema(description = "今日统计")
    private TodayStats todayStats;

    @Schema(description = "快递员列表")
    private List<CourierItem> couriers;

    @Schema(description = "最新订单动态")
    private List<RecentDelivery> recentDeliveries;

    @Data
    @Builder
    @Schema(description = "今日统计")
    public static class TodayStats {
        @Schema(description = "今日订单数", example = "12")
        private long todayOrders;

        @Schema(description = "订单增长率", example = "+2")
        private long orderGrowth;

        @Schema(description = "在线快递员数", example = "3")
        private long onlineCouriers;

        @Schema(description = "总快递员数", example = "5")
        private long totalCouriers;

        @Schema(description = "今日配送费", example = "156.50")
        private BigDecimal todayDeliveryFee;

        @Schema(description = "运力负荷百分比", example = "65.5")
        private BigDecimal capacityLoad;
    }

    @Data
    @Builder
    @Schema(description = "快递员项")
    public static class CourierItem {
        @Schema(description = "快递员ID", example = "res_courier01")
        private String id;

        @Schema(description = "快递员姓名", example = "吴十")
        private String name;

        @Schema(description = "今日完成单数", example = "5")
        private long todayCompleted;

        @Schema(description = "本月收入", example = "1250.00")
        private BigDecimal monthIncome;

        @Schema(description = "状态", example = "online")
        private String status;
    }

    @Data
    @Builder
    @Schema(description = "最近配送动态")
    public static class RecentDelivery {
        @Schema(description = "配送单ID", example = "dlv_001")
        private String id;

        @Schema(description = "时间", example = "2026-07-05 14:30:00")
        private String time;

        @Schema(description = "用户姓名", example = "张先生")
        private String residentName;

        @Schema(description = "商品描述", example = "可口可乐x2")
        private String productDesc;

        @Schema(description = "配送费", example = "1.00")
        private BigDecimal fee;

        @Schema(description = "状态", example = "delivered")
        private String status;
    }
}
