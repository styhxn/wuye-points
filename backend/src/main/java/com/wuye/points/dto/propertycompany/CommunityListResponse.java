package com.wuye.points.dto.propertycompany;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class CommunityListResponse {

    private String propertyCompanyId;
    private List<CommunityItemResponse> list;
}
