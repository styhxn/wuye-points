package com.wuye.points.dto.buildingleader;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
@Schema(description = "楼长分享商品给本单元响应")
public class ShareUnitProductResponse {

    @Schema(description = "本次生成的产品推荐公告ID（可在「我的运营记录」里查）", example = "ann_xxxxxxxx")
    private String announcementId;

    @Schema(description = "商品ID", example = "prd_xxxxxxxx")
    private String productId;

    @Schema(description = "商品名称（快照，供前端直接渲染回执卡片）", example = "家政保洁2小时")
    private String productName;

    @Schema(description = "商品封面（快照）", example = "https://example.com/cover.jpg")
    private String coverUrl;

    @Schema(description = "现价（元，快照）", example = "39.00")
    private BigDecimal price;

    @Schema(description = "送达单元", example = "5单元")
    private String unit;

    @Schema(description = "送达住户数（本单元其他 active 住户，不含楼长本人）", example = "18")
    private Integer recipientCount;

    @Schema(description = "发送时间（UTC ISO8601）", example = "2026-11-03T10:00:00Z")
    private String sentAt;
}
