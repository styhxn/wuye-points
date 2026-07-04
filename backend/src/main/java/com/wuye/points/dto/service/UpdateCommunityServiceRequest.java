package com.wuye.points.dto.service;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
@Schema(description = "更新社区服务请求")
public class UpdateCommunityServiceRequest {

    @Schema(description = "服务名称", example = "家政保洁")
    private String name;

    @Schema(description = "服务描述", example = "专业家政保洁服务")
    private String description;

    @Schema(description = "封面图片URL列表", example = "[\"https://example.com/cover.jpg\"]")
    private List<String> coverUrls;

    @Schema(description = "服务分类", example = "家政")
    private String category;

    @Schema(description = "价格", example = "100.00")
    private BigDecimal price;

    @Schema(description = "会员价格", example = "80.00")
    private BigDecimal memberPrice;

    @Schema(description = "价格单位", example = "次")
    private String priceUnit;

    @Schema(description = "是否订阅服务", example = "false")
    private Boolean isSubscription;

    @Schema(description = "排序", example = "1")
    private Integer rankOrder;

    @Schema(description = "状态", example = "启用")
    private String status;
}
