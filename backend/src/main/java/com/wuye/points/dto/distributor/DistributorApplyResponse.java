package com.wuye.points.dto.distributor;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

/**
 * 一级代理申请 / 我的申请状态响应（C端）
 */
@Data
@Builder
@Schema(description = "一级代理申请响应")
public class DistributorApplyResponse {

    @Schema(description = "申请ID", example = "dap_xxx")
    private String applicationId;

    @Schema(description = "申请状态 none/pending/approved/rejected", example = "pending")
    private String status;

    @Schema(description = "联系人姓名")
    private String contactName;

    @Schema(description = "联系电话")
    private String contactPhone;

    @Schema(description = "经营品类")
    private String businessScope;

    @Schema(description = "服务范围")
    private String serviceScope;

    @Schema(description = "备注")
    private String remark;

    @Schema(description = "拒绝原因（rejected 时非空）")
    private String rejectReason;

    @Schema(description = "审核通过后创建/关联的商家载体ID（approved 时非空）")
    private String distributorMerchantId;

    @Schema(description = "提交时间", example = "2026-10-20T02:00:00.000Z")
    private String submittedAt;

    @Schema(description = "提示信息")
    private String message;
}
