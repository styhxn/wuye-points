package com.wuye.points.dto.activity;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@Schema(description = "加入活动小组响应")
public class JoinActivityGroupResponse {

    @Schema(description = "小组ID", example = "grp_001")
    private String groupId;

    @Schema(description = "小组名称", example = "周末跑步团")
    private String groupName;

    @Schema(description = "成员ID", example = "member_001")
    private String memberId;

    @Schema(description = "住户ID", example = "resident_001")
    private String residentId;

    @Schema(description = "角色", example = "member")
    private String role;

    @Schema(description = "成员数量", example = "26")
    private Integer memberCount;

    @Schema(description = "加入时间", example = "2024-01-01T00:00:00")
    private String joinedAt;
}
