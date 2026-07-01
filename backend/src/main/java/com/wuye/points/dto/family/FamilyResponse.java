package com.wuye.points.dto.family;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class FamilyResponse {

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
    private BigDecimal coinBalance;
    private Integer voteWeight;
    private String auditStatus;
    private String status;
    private String createdAt;
    private String updatedAt;
}
