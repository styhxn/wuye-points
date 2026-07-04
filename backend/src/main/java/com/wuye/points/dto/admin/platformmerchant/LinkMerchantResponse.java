package com.wuye.points.dto.admin.platformmerchant;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@Schema(description = "关联商家响应")
public class LinkMerchantResponse {

    @Schema(description = "商家ID", example = "M001")
    private String merchantId;

    @Schema(description = "平台商家ID", example = "PM001")
    private String platformMerchantId;

    @Schema(description = "物业公司ID", example = "PC001")
    private String propertyCompanyId;

    @Schema(description = "审核状态", example = "pending_audit", allowableValues = {"pending_audit", "approved", "rejected"})
    private String auditStatus;
}
