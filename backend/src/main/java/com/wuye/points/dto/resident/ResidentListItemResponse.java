package com.wuye.points.dto.resident;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class ResidentListItemResponse {

    private String id;
    private String name;
    private String phone;
    private String avatarUrl;
    private String role;
    private String userType;
    private String communityId;
    private String communityName;
    private String building;
    private String unit;
    private String room;
    private String familyId;
    private Integer pointBalance;
    private BigDecimal coinBalance;
    private String status;
    private String createdAt;
}
