package com.wuye.points.dto.family;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class FamilyMemberItemResponse {

    private String id;
    private String name;
    private String phone;
    private String avatarUrl;
    private String relation;
    private Boolean isOwner;
    private Integer pointBalance;
    private BigDecimal coinBalance;
    private String status;
    private String joinedAt;
}
