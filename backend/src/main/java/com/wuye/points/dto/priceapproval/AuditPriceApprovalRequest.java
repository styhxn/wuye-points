package com.wuye.points.dto.priceapproval;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Schema(description = "审批价格修改请求")
public class AuditPriceApprovalRequest {

    @NotNull(message = "approved不能为空")
    @Schema(description = "是否通过", example = "true", requiredMode = Schema.RequiredMode.REQUIRED)
    private Boolean approved;

    @Schema(description = "审批备注", example = "同意调整")
    private String remark;
}
