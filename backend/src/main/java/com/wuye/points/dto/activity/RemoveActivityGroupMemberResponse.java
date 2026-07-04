package com.wuye.points.dto.activity;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

@Schema(description = "移除活动小组成员响应")
@Data
@Builder
public class RemoveActivityGroupMemberResponse {

    @Schema(description = "小组ID", example = "ag_xxx")
    private String groupId;

    @Schema(description = "成员ID", example = "agm_xxx")
    private String memberId;

    @Schema(description = "住户ID", example = "res_xxx")
    private String residentId;

    @Schema(description = "成员数量", example = "14")
    private Integer memberCount;

    @Schema(description = "移除时间", example = "2026-01-01T00:00:00.000Z")
    private String removedAt;
}
