package com.wuye.points.dto.propertyoperator;

import lombok.Data;

import java.util.List;

@Data
public class UpdatePropertyOperatorScopeRequest {

    private List<String> communityIds;

    private List<String> buildingNos;
}
