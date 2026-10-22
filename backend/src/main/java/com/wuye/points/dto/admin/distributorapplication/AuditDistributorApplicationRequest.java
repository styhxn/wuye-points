package com.wuye.points.dto.admin.distributorapplication;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 一级代理申请审核请求（管理端）
 */
@Data
public class AuditDistributorApplicationRequest {

    @NotBlank(message = "审核结果不能为空")
    @Pattern(regexp = "approved|rejected", message = "auditStatus只能是approved或rejected")
    @Schema(description = "审核结果：approved=通过（自动创建商家载体并开通一级代理）/ rejected=驳回", example = "approved", requiredMode = Schema.RequiredMode.REQUIRED)
    private String auditStatus;

    @Size(max = 200, message = "拒绝原因不超过200字")
    @Schema(description = "拒绝原因（rejected时必填）", example = "暂不满足条件")
    private String rejectReason;
}
