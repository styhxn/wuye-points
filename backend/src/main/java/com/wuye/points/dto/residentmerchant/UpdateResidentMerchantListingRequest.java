package com.wuye.points.dto.residentmerchant;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Schema(description = "更新业主商户上架")
public class UpdateResidentMerchantListingRequest {

    private BigDecimal retailPrice;
    private Boolean enabled;
}
