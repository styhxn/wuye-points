package com.wuye.points.dto.admin.individualleader;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

/**
 * 个体负责人申请审核结果（2026-09-08 新增：POST /admin/individual-leaders/applications/{applicationId}/audit）
 *
 * <p>审核通过时 {@code individualLeader} 非空；审核拒绝时为空，仅返回申请本身的信息。
 */
@Data
@Builder
@Schema(description = "个体负责人申请审核结果")
public class IndividualLeaderApplicationAuditResponse {

    @Schema(description = "申请ID", example = "ila_demo001")
    private String applicationId;

    @Schema(description = "审核结果", example = "approved")
    private String auditStatus;

    @Schema(description = "申请人住户ID", example = "res_demo001")
    private String residentId;

    @Schema(description = "申请人姓名", example = "张三")
    private String residentName;

    @Schema(description = "申请板块", example = "repair")
    private String sector;

    @Schema(description = "拒绝原因（rejected 时非空）")
    private String rejectReason;

    @Schema(description = "审核时间")
    private String auditedAt;

    @Schema(description = "审核通过时创建的个体负责人ID", example = "il_demo001")
    private String individualLeaderId;

    @Schema(description = "审核通过时创建的个体负责人详情（rejected 时为 null）")
    private IndividualLeaderDetailResponse individualLeader;
}
