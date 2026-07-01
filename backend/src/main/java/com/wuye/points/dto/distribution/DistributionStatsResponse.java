package com.wuye.points.dto.distribution;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
public class DistributionStatsResponse {

    private Summary summary;
    private List<ByPropertyItem> byProperty;
    private List<ByCoordinatorItem> byCoordinator;
    private List<BySectorItem> bySector;

    @Data
    @Builder
    public static class Summary {
        private BigDecimal totalDistributableAmount;
        private BigDecimal propertyAmount;
        private BigDecimal coordinatorAmount;
        private BigDecimal sectorLeaderAmount;
        private BigDecimal individualLeaderAmount;
    }

    @Data
    @Builder
    public static class ByPropertyItem {
        private String propertyCompanyId;
        private String propertyName;
        private BigDecimal amount;
    }

    @Data
    @Builder
    public static class ByCoordinatorItem {
        private String coordinatorId;
        private String name;
        private BigDecimal amount;
    }

    @Data
    @Builder
    public static class BySectorItem {
        private String sector;
        private String sectorName;
        private BigDecimal amount;
    }
}
