package com.wuye.points.dto.auth;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ResidentAuthSummaryResponse {

    private String id;
    private String name;
    private String avatarUrl;
    private String phone;
    private String role;
    private String propertyCompanyId;
    private String communityId;
    private String familyId;
    private Boolean wechatBound;
    private Boolean newUser;
    private String status;
}
