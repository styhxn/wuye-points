package com.wuye.points.dto.sectorleader;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Schema(description = "板块负责人审核商家加入请求")
public class AuditMerchantJoinRequest {

    @NotBlank(message = "merchantId不能为空")
    @Schema(description = "商家ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "mch_xxx")
    private String merchantId;

    @NotNull(message = "approved不能为空")
    @Schema(description = "是否同意", requiredMode = Schema.RequiredMode.REQUIRED, example = "true")
    private Boolean approved;

    @Schema(description = "审核原因/备注", example = "资质审核通过")
    private String reason;
}
