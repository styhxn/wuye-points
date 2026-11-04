package com.wuye.points.dto.coinfund;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Schema(description = "平台向物业公司划转物业币代管资金")
public class TransferPropertyCoinFundRequest {

    @Schema(description = "物业公司ID", required = true)
    @NotBlank(message = "propertyCompanyId不能为空")
    private String propertyCompanyId;

    @Schema(description = "划转金额", required = true)
    @NotNull(message = "划转金额不能为空")
    @DecimalMin(value = "0.01", message = "划转金额必须大于0")
    private BigDecimal amount;

    @Schema(description = "转账凭证/银行流水号")
    private String transferProof;

    @Schema(description = "备注")
    private String remark;
}
