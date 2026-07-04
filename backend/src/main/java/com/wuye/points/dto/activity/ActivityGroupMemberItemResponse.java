package com.wuye.points.dto.activity;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@Schema(description = "活动小组成员项响应")
public class ActivityGroupMemberItemResponse {

    @Schema(description = "成员ID", example = "member_001")
    private String memberId;

    @Schema(description = "住户ID", example = "resident_001")
    private String residentId;

    @Schema(description = "成员姓名", example = "李四")
    private String name;

    @Schema(description = "头像URL", example = "https://example.com/avatar.jpg")
    private String avatarUrl;

    @Schema(description = "角色", example = "member")
    private String role;

    @Schema(description = "状态", example = "active")
    private String status;

    @Schema(description = "加入时间", example = "2024-01-01T00:00:00")
    private String joinedAt;
}
