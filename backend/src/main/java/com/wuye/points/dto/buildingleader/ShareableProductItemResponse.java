package com.wuye.points.dto.buildingleader;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
@Schema(description = "楼长可分享商品项（本小区服务范围）")
public class ShareableProductItemResponse {

    @Schema(description = "商品ID", example = "prd_xxxxxxxx")
    private String id;

    @Schema(description = "商品名称", example = "家政保洁2小时")
    private String name;

    @Schema(description = "商品封面", example = "https://example.com/cover.jpg")
    private String coverUrl;

    @Schema(description = "商品描述")
    private String description;

    @Schema(description = "现价（元）", example = "39.00")
    private BigDecimal price;

    @Schema(description = "会员价（元）", example = "35.00")
    private BigDecimal memberPrice;

    @Schema(description = "积分价（积分）", example = "3900")
    private Integer pointPrice;

    @Schema(description = "库存", example = "100")
    private Integer stock;

    @Schema(description = "商品分类", example = "cleaning")
    private String category;

    @Schema(description = "商家ID", example = "mch_xxxxxxxx")
    private String merchantId;

    @Schema(description = "商家名称", example = "演示生活超市")
    private String merchantName;
}
