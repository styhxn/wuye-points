package com.wuye.points.dto.activity;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class JoinActivityGroupResponse {

    private String groupId;
    private String groupName;
    private String memberId;
    private String residentId;
    private String role;
    private Integer memberCount;
    private String joinedAt;
}
