package com.wuye.points.dto.activity;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class ActivityGroupMemberListResponse {

    private String groupId;
    private String groupName;
    private Integer memberCount;
    private List<ActivityGroupMemberItemResponse> list;
}
