package com.wuye.points.dto.propertycompany;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CommunityItemResponse {

    private String id;
    private String name;
    private String address;
    private Integer totalBuildings;
    private Integer totalUnits;
    private String status;
}
