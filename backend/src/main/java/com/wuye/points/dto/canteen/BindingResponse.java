package com.wuye.points.dto.canteen;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 社区食堂-绑定关系项
 */
@Data
@Builder
@Schema(description = "绑定关系项")
public class BindingResponse {

    @Schema(description = "绑定ID", example = "cmb_xxx")
    private String id;

    @Schema(description = "主商家ID", example = "mch_xxx")
    private String mainMerchantId;

    @Schema(description = "主商家名称", example = "幸福餐饮公司")
    private String mainMerchantName;

    @Schema(description = "子商家ID", example = "mch_yyy")
    private String subMerchantId;

    @Schema(description = "子商家名称", example = "麻辣烫窗口")
    private String subMerchantName;

    @Schema(description = "供货优惠比例", example = "0.20")
    private BigDecimal supplyDiscountRate;

    @Schema(description = "状态 active/inactive", example = "active")
    private String status;

    @Schema(description = "创建时间（ISO-8601）", example = "2026-08-27T10:00:00.000Z")
    private String createdAt;
}
