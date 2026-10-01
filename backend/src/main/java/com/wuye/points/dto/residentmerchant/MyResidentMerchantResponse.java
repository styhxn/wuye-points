package com.wuye.points.dto.residentmerchant;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
@Schema(description = "我的业主商户状态")
public class MyResidentMerchantResponse {

    private String applicationId;
    private String status;
    private String statusCode;
    private String visibility;
    private String depositStatus;
    private BigDecimal depositAmount;
    private BigDecimal balance;
    private Long listingCount;
}
