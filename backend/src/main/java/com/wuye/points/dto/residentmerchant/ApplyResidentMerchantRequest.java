package com.wuye.points.dto.residentmerchant;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Schema(description = "申请业主商户请求")
public class ApplyResidentMerchantRequest {

    @NotNull
    @AssertTrue(message = "须同意协议")
    @Schema(description = "是否同意协议", requiredMode = Schema.RequiredMode.REQUIRED)
    private Boolean agreedTerms;
}
