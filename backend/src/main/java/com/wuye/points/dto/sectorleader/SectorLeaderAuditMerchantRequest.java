package com.wuye.points.dto.sectorleader;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Schema(description = "板块负责人审批商家加入")
public class SectorLeaderAuditMerchantRequest {

    @Schema(description = "商家ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "商家ID不能为空")
    private String merchantId;

    @Schema(description = "是否同意加入", example = "true", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "approved不能为空")
    private Boolean approved;

    @Schema(description = "备注（拒绝时填写原因）")
    private String remark;
}
