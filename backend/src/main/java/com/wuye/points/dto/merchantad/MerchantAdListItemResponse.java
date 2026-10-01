package com.wuye.points.dto.merchantad;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@Schema(description = "商家广告列表项")
public class MerchantAdListItemResponse {

    private String id;
    private String title;
    private String content;
    private List<String> imageUrls;
    private String productId;
    private Integer recipientCount;

    @Schema(description = "展示天数")
    private Integer durationDays;

    @Schema(description = "投放开始日期")
    private String startDate;

    @Schema(description = "投放结束日期")
    private String endDate;

    @Schema(description = "支付金额（0=免费额度投放）")
    private BigDecimal payAmount;

    @Schema(description = "支付方式: free/coin/point/wechat/mock")
    private String paymentMethod;

    private String createdAt;
}
