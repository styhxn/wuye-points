package com.wuye.points.dto.communityentity;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CommunityEntityListItemResponse {

    private String id;
    private String name;
    private String description;
    private String adminResidentId;
    private String adminResidentName;
    private String status;
    private Integer boundPropertyCount;
    private String createdAt;
}
