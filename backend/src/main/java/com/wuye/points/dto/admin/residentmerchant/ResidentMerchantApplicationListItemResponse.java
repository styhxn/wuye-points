package com.wuye.points.dto.admin.residentmerchant;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
@Schema(description = "业主商户申请列表项")
public class ResidentMerchantApplicationListItemResponse {

    private String id;
    private String residentId;
    private String residentName;
    private String propertyCompanyId;
    private BigDecimal depositAmount;
    private String status;
    private String statusCode;
    private String visibility;
    private String createdAt;
}
