package com.wuye.points.dto.activity;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class RemoveActivityGroupMemberResponse {

    private String groupId;
    private String memberId;
    private String residentId;
    private Integer memberCount;
    private String removedAt;
}
