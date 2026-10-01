package com.wuye.points.dto.resident;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

@Schema(description = "楼栋变更申请列表项（管理端）")
@Data
@Builder
public class BuildingChangeListItemResponse {

    @Schema(description = "申请ID", example = "bcr_xxx")
    private String id;

    @Schema(description = "申请住户ID", example = "res_xxx")
    private String residentId;

    @Schema(description = "申请住户姓名", example = "张三")
    private String residentName;

    @Schema(description = "申请住户手机号（脱敏）", example = "138****5678")
    private String residentPhone;

    @Schema(description = "当前楼栋", example = "1")
    private String currentBuilding;

    @Schema(description = "当前单元", example = "1")
    private String currentUnit;

    @Schema(description = "当前房号", example = "101")
    private String currentRoom;

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

    @Schema(description = "状态：pending/approved/rejected", example = "pending")
    private String status;

    @Schema(description = "驳回原因", example = "")
    private String rejectReason;

    @Schema(description = "申请时间", example = "2026-08-17T02:00:00.000Z")
    private String appliedAt;

    @Schema(description = "审批时间", example = "2026-08-17T02:30:00.000Z")
    private String auditedAt;
}
