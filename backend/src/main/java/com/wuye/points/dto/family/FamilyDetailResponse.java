package com.wuye.points.dto.family;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class FamilyDetailResponse {

    private String id;
    private String name;
    private String ownerId;
    private String ownerName;
    private String communityId;
    private String communityName;
    private String building;
    private String unit;
    private String room;
    private Integer memberCount;
    private Integer pointBalance;
    private java.math.BigDecimal coinBalance;
    private Integer voteWeight;
    private String auditStatus;
    private String status;
    private List<FamilyMemberItemResponse> members;
    private String createdAt;
    private String updatedAt;
}
