package com.wuye.points.dto.residentmerchant;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
@Schema(description = "申请业主商户响应")
public class ApplyResidentMerchantResponse {

    private String applicationId;
    private BigDecimal depositAmount;
    private String status;
    private String statusCode;
    private String visibility;
}
