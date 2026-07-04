package com.wuye.points.dto.admin.merchantaudit;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Schema(description = "审核商家积分采购请求")
@Data
public class AuditMerchantPointPurchaseRequest {

    @Schema(description = "审核结果", example = "approved", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "审核结果不能为空")
    private String auditResult;

    @Schema(description = "实际积分数量", example = "1000")
    private Integer actualPointAmount;

    @Schema(description = "拒绝原因", example = "积分金额不符")
    private String rejectReason;

    @Schema(description = "备注", example = "已核实付款凭证")
    private String remark;
}
