package com.wuye.points.dto.activity;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class LeaveActivityGroupResponse {

    private String groupId;
    private String residentId;
    private Integer memberCount;
    private String leftAt;
}
