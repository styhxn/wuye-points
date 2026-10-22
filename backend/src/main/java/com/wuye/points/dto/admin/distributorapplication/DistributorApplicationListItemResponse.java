package com.wuye.points.dto.admin.distributorapplication;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

/**
 * 一级代理申请列表项（管理端）
 */
@Data
@Builder
@Schema(description = "一级代理申请列表项")
public class DistributorApplicationListItemResponse {

    @Schema(description = "申请ID", example = "dap_demo001")
    private String id;

    @Schema(description = "申请人住户ID")
    private String residentId;

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

    @Schema(description = "审核状态 pending/approved/rejected")
    private String auditStatus;

    @Schema(description = "拒绝原因")
    private String rejectReason;

    @Schema(description = "审核通过后创建/关联的商家载体ID")
    private String distributorMerchantId;

    @Schema(description = "审核时间")
    private String auditedAt;

    @Schema(description = "提交时间")
    private String createdAt;

    @Schema(description = "物业公司ID")
    private String propertyCompanyId;
}
