package com.wuye.points.dto.propertyoperator;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class PropertyOperatorListItemResponse {

    private String id;
    private String residentId;
    private String name;
    private String phone;
    private String propertySubRole;
    private List<String> communityIds;
    private List<String> buildingNos;
    private String status;
    private String createdAt;
}
