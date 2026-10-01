package com.wuye.points.dto.servicerequest;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
@Schema(description = "服务需求列表/详情项")
public class ServiceRequestItemResponse {

    @Schema(description = "需求ID")
    private String id;

    @Schema(description = "描述")
    private String description;

    @Schema(description = "状态（中文）")
    private String status;

    @Schema(description = "状态码")
    private String statusCode;

    @Schema(description = "匹配分类名称")
    private String matchedCategoryName;

    @Schema(description = "当前匹配商家名称")
    private String currentMerchantName;

    @Schema(description = "联系电话")
    private String contactPhone;

    @Schema(description = "服务地址")
    private String address;

    @Schema(description = "报价金额")
    private BigDecimal quoteAmount;

    @Schema(description = "创建时间")
    private String createdAt;
}
