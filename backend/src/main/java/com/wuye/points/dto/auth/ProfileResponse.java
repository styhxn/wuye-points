package com.wuye.points.dto.auth;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class ProfileResponse {

    private String id;
    private String openid;
    private String unionid;
    private String phone;
    private String name;
    private String avatarUrl;
    private Integer gender;
    private Integer age;
    private String maritalStatus;
    private Boolean hasChildren;
    private String userType;
    private String role;
    private String propertyCompanyId;
    private String propertyName;
    private String communityId;
    private String communityName;
    private String building;
    private String unit;
    private String room;
    private String familyId;
    private Integer pointBalance;
    private BigDecimal coinBalance;
    private Boolean coinFrozen;
    private Boolean coinHidden;
    private Boolean wechatBound;
    private String status;
    private String createdAt;
    private String updatedAt;
}
