package com.wuye.points.dto.sectorleader;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Schema(description = "个体负责人设置与商家分成请求")
public class SetMerchantDistributionRequest {

    @NotBlank(message = "merchantId不能为空")
    @Schema(description = "商家ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "mch_xxx")
    private String merchantId;

    @NotNull(message = "commissionRate不能为空")
    @Schema(description = "分成比例", requiredMode = Schema.RequiredMode.REQUIRED, example = "0.08")
    private BigDecimal commissionRate;

    @Schema(description = "申请原因/备注", example = "与商家协商一致的分成比例")
    private String reason;
}
