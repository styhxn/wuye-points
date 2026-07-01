package com.wuye.points.dto.resident;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class ResidentDetailResponse {

    private String id;
    private String name;
    private String phone;
    private String avatarUrl;
    private Integer gender;
    private Integer age;
    private String role;
    private String userType;
    private String propertyCompanyId;
    private String propertyName;
    private String communityId;
    private String communityName;
    private String building;
    private String unit;
    private String room;
    private String familyId;
    private String familyName;
    private Integer pointBalance;
    private BigDecimal coinBalance;
    private Boolean coinFrozen;
    private BigDecimal totalConsumption;
    private Long totalOrders;
    private Boolean wechatBound;
    private String status;
    private String createdAt;
    private String updatedAt;
}
