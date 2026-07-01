package com.wuye.points.dto.activity;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ActivityGroupMemberItemResponse {

    private String memberId;
    private String residentId;
    private String name;
    private String avatarUrl;
    private String role;
    private String status;
    private String joinedAt;
}
