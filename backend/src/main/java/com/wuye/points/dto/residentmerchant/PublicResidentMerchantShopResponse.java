package com.wuye.points.dto.residentmerchant;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@Schema(description = "业主商户公开橱窗（对外展示）")
public class PublicResidentMerchantShopResponse {

    @Schema(description = "业主商户(住户)ID")
    private String residentId;

    @Schema(description = "店主姓名")
    private String residentName;

    @Schema(description = "物业公司ID")
    private String propertyCompanyId;

    @Schema(description = "店铺展示状态：public/private")
    private String visibility;

    @Schema(description = "店铺状态")
    private String status;

    @Schema(description = "保证金金额")
    private BigDecimal depositAmount;

    @Schema(description = "对外展示的商品列表（仅上架且启用的）")
    private List<PublicResidentMerchantListingItemResponse> listings;

    @Schema(description = "对外商品数量")
    private Integer listingCount;
}
