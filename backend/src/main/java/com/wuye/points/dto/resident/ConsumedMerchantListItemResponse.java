package com.wuye.points.dto.resident;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@Schema(description = "已消费商家列表项响应")
public class ConsumedMerchantListItemResponse {

    @Schema(description = "商家ID", example = "mch_xyz789")
    private String merchantId;

    @Schema(description = "商家名称", example = "楼下超市")
    private String merchantName;

    @Schema(description = "商家分类", example = "超市便利")
    private String category;

    @Schema(description = "商家封面图URL", example = "https://example.com/cover.jpg")
    private String coverUrls;

    @Schema(description = "商家等级", example = "A")
    private String merchantLevel;

    @Schema(description = "订单数量", example = "15")
    private Integer orderCount;

    @Schema(description = "最近下单时间", example = "2026-01-01T00:00:00.000Z")
    private String lastOrderAt;
}
