package com.wuye.points.dto.admin.residentmerchant;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
@Schema(description = "管理端分销商品列表项")
public class DistributorProductListItemResponse {

    @Schema(description = "分销商品ID", example = "dpr_xxx")
    private String id;

    @Schema(description = "分销商家ID", example = "mch_xxx")
    private String distributorMerchantId;

    @Schema(description = "物业公司ID", example = "pc_demo001")
    private String propertyCompanyId;

    @Schema(description = "商品名称", example = "洗衣液")
    private String name;

    @Schema(description = "封面图", example = "/uploads/product.png")
    private String coverUrl;

    @Schema(description = "进货价", example = "20.00")
    private BigDecimal wholesalePrice;

    @Schema(description = "建议零售价", example = "35.00")
    private BigDecimal suggestedRetailPrice;

    @Schema(description = "库存", example = "100")
    private Integer stock;

    @Schema(description = "收费周期: one_time/monthly/quarterly/yearly", example = "monthly")
    private String billingCycle;

    @Schema(description = "状态", example = "active")
    private String status;

    @Schema(description = "货源类目", example = "食品饮料")
    private String category;

    @Schema(description = "创建时间", example = "2026-07-25T10:00:00Z")
    private String createdAt;
}
