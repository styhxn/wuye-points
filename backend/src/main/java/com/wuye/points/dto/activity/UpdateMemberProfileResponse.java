package com.wuye.points.dto.activity;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@Schema(description = "更新组员个人简介响应")
public class UpdateMemberProfileResponse {

    @Schema(description = "活动组ID", example = "act_001")
    private String groupId;

    @Schema(description = "成员ID", example = "agm_001")
    private String memberId;

    @Schema(description = "住户ID", example = "res_001")
    private String residentId;

    @Schema(description = "个人简介", example = "热爱跑步的程序员")
    private String introduction;

    @Schema(description = "更新时间", example = "2024-01-01T10:00:00")
    private String updatedAt;
}
