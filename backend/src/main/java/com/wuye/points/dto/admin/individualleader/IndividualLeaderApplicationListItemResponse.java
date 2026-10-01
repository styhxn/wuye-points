package com.wuye.points.dto.admin.individualleader;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class IndividualLeaderApplicationListItemResponse {

    @Schema(description = "申请ID")
    private String id;

    @Schema(description = "申请人住户ID")
    private String residentId;

    @Schema(description = "申请人姓名")
    private String residentName;

    @Schema(description = "手机号")
    private String phone;

    @Schema(description = "物业公司ID")
    private String propertyCompanyId;

    @Schema(description = "申请板块")
    private String sector;

    @Schema(description = "自我介绍/备注")
    private String remark;

    @Schema(description = "审核状态")
    private String auditStatus;

    @Schema(description = "拒绝原因")
    private String rejectReason;

    @Schema(description = "申请时间")
    private String createdAt;

    @Schema(description = "审核时间")
    private String auditedAt;
}
