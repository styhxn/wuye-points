package com.wuye.points.dto.distribution;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@Schema(description = "分配统计响应")
public class DistributionStatsResponse {

    @Schema(description = "汇总统计")
    private Summary summary;

    @Schema(description = "按物业公司统计")
    private List<ByPropertyItem> byProperty;

    @Schema(description = "按协调员统计")
    private List<ByCoordinatorItem> byCoordinator;

    @Schema(description = "按片区统计")
    private List<BySectorItem> bySector;

    @Data
    @Builder
    @Schema(description = "汇总统计信息")
    public static class Summary {

        @Schema(description = "总可分配金额", example = "50000.00")
        private BigDecimal totalDistributableAmount;

        @Schema(description = "物业公司总金额", example = "25000.00")
        private BigDecimal propertyAmount;

        @Schema(description = "协调员总金额", example = "12500.00")
        private BigDecimal coordinatorAmount;

        @Schema(description = "片区负责人总金额", example = "7500.00")
        private BigDecimal sectorLeaderAmount;

        @Schema(description = "个人负责人总金额", example = "5000.00")
        private BigDecimal individualLeaderAmount;
    }

    @Data
    @Builder
    @Schema(description = "按物业公司统计项")
    public static class ByPropertyItem {

        @Schema(description = "物业公司ID", example = "prop_a1c3e5")
        private String propertyCompanyId;

        @Schema(description = "物业公司名称", example = "阳光物业")
        private String propertyName;

        @Schema(description = "金额", example = "25000.00")
        private BigDecimal amount;
    }

    @Data
    @Builder
    @Schema(description = "按协调员统计项")
    public static class ByCoordinatorItem {

        @Schema(description = "协调员ID", example = "coord_abc123")
        private String coordinatorId;

        @Schema(description = "协调员姓名", example = "李四")
        private String name;

        @Schema(description = "金额", example = "12500.00")
        private BigDecimal amount;
    }

    @Data
    @Builder
    @Schema(description = "按片区统计项")
    public static class BySectorItem {

        @Schema(description = "片区编码", example = "A")
        private String sector;

        @Schema(description = "片区名称", example = "A片区")
        private String sectorName;

        @Schema(description = "金额", example = "7500.00")
        private BigDecimal amount;
    }
}
