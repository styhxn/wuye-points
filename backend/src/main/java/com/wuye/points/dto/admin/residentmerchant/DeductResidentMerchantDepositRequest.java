package com.wuye.points.dto.admin.residentmerchant;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Schema(description = "扣除业主商户保证金")
public class DeductResidentMerchantDepositRequest {

    @NotNull
    private BigDecimal amount;

    private String reason;
}
