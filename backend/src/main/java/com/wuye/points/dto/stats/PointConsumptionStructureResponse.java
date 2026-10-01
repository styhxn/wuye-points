package com.wuye.points.dto.stats;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.util.List;

/**
 * D7 积分消耗结构响应(按 source 分组)
 */
@Data
@Builder
@Schema(description = "积分消耗结构响应")
public class PointConsumptionStructureResponse {

    @Schema(description = "查询的月份(yyyy-MM)", example = "2026-07")
    private String month;

    @Schema(description = "消耗总积分", example = "8000")
    private Integer totalSpent;

    @Schema(description = "按来源分组明细(按消耗量降序)")
    private List<ConsumptionItem> items;

    @Data
    @Builder
    @Schema(description = "消耗结构明细项")
    public static class ConsumptionItem {
        @Schema(description = "消耗来源(如 order/exchange/manual)", example = "order")
        private String source;

        @Schema(description = "该来源消耗总量", example = "5000")
        private Integer amount;

        @Schema(description = "占比(0~1)", example = "0.6250")
        private Double percentage;
    }
}
