package com.wuye.points.dto.resident;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

@Schema(description = "楼栋变更申请响应")
@Data
@Builder
public class BuildingChangeResponse {

    @Schema(description = "申请ID", example = "bcr_xxx")
    private String id;

    @Schema(description = "状态：pending/approved/rejected", example = "pending")
    private String status;

    @Schema(description = "目标小区ID", example = "com_def456")
    private String communityId;

    @Schema(description = "目标楼栋", example = "3")
    private String building;

    @Schema(description = "目标单元", example = "2")
    private String unit;

    @Schema(description = "目标楼层", example = "5")
    private String floor;

    @Schema(description = "目标房号", example = "501")
    private String room;

    @Schema(description = "驳回原因（status=rejected 时返回）", example = "")
    private String rejectReason;

    @Schema(description = "申请时间", example = "2026-08-17T02:00:00.000Z")
    private String appliedAt;

    @Schema(description = "审批时间", example = "2026-08-17T02:30:00.000Z")
    private String auditedAt;
}
