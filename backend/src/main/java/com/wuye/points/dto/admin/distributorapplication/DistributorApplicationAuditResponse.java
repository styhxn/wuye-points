package com.wuye.points.dto.admin.distributorapplication;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

/**
 * 一级代理申请审核结果（管理端）
 */
@Data
@Builder
@Schema(description = "一级代理申请审核结果")
public class DistributorApplicationAuditResponse {

    @Schema(description = "申请ID", example = "dap_demo001")
    private String applicationId;

    @Schema(description = "审核结果 approved/rejected")
    private String auditStatus;

    @Schema(description = "申请人住户ID")
    private String residentId;

    @Schema(description = "联系人姓名")
    private String contactName;

    @Schema(description = "经营品类")
    private String businessScope;

    @Schema(description = "服务范围")
    private String serviceScope;

    @Schema(description = "拒绝原因（rejected 时非空）")
    private String rejectReason;

    @Schema(description = "审核时间", example = "2026-10-20T02:00:00.000Z")
    private String auditedAt;

    @Schema(description = "审核通过后创建/关联的商家载体ID（approved 时非空）")
    private String distributorMerchantId;
}
