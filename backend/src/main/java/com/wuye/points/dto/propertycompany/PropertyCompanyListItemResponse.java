package com.wuye.points.dto.propertycompany;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class PropertyCompanyListItemResponse {

    private String id;
    private String name;
    private String logoUrl;
    private String contactPhone;
    private String address;
    private String status;
    private Integer communityCount;
    private PropertyCompanyStats stats;
    private String createdAt;
    private String updatedAt;

    @Data
    @Builder
    public static class PropertyCompanyStats {
        private Long merchantCount;
        private Long residentCount;
    }
}
