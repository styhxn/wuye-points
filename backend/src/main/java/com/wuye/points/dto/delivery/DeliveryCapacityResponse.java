package com.wuye.points.dto.delivery;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@Schema(description = "运力效能分析响应")
public class DeliveryCapacityResponse {

    @Schema(description = "时间维度", example = "24h")
    private String dimension;

    @Schema(description = "峰值时段", example = "14:00")
    private String peakHour;

    @Schema(description = "小时粒度数据")
    private List<HourlyData> hourlyData;

    @Data
    @Builder
    @Schema(description = "小时粒度数据")
    public static class HourlyData {
        @Schema(description = "小时（0-23）", example = "14")
        private int hour;

        @Schema(description = "时段标签", example = "14:00")
        private String label;

        @Schema(description = "配送单数", example = "8")
        private long deliveryCount;

        @Schema(description = "平均响应时间（分钟）", example = "12.5")
        private BigDecimal avgResponseMinutes;
    }
}
