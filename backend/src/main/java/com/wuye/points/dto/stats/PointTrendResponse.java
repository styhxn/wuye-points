package com.wuye.points.dto.stats;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.util.List;

/**
 * D7 积分月度趋势响应
 */
@Data
@Builder
@Schema(description = "积分月度趋势响应")
public class PointTrendResponse {

    @Schema(description = "查询的月份(yyyy-MM)", example = "2026-07")
    private String month;

    @Schema(description = "当月发放总积分", example = "12000")
    private Integer totalEarned;

    @Schema(description = "当月消耗总积分", example = "8000")
    private Integer totalSpent;

    @Schema(description = "按日聚合明细")
    private List<DailyPoint> daily;

    @Data
    @Builder
    @Schema(description = "日维度积分明细")
    public static class DailyPoint {
        @Schema(description = "日期(yyyy-MM-dd)", example = "2026-07-01")
        private String date;

        @Schema(description = "当日发放", example = "300")
        private Integer earned;

        @Schema(description = "当日消耗", example = "200")
        private Integer spent;
    }
}
