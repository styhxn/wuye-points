package com.wuye.points.dto.announcement;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@Schema(description = "产品推荐发布请求")
public class ProductAnnouncementRequest {

    @NotBlank(message = "title不能为空")
    @Size(max = 200, message = "title长度不能超过200")
    @Schema(description = "公告标题", example = "本周精选商品推荐", requiredMode = Schema.RequiredMode.REQUIRED)
    private String title;

    @NotBlank(message = "content不能为空")
    @Schema(description = "公告内容", example = "新品上市，限时优惠", requiredMode = Schema.RequiredMode.REQUIRED)
    private String content;

    @Schema(description = "小区ID", example = "com_001")
    private String communityId;

    @Schema(description = "商家ID", example = "mch_001")
    private String merchantId;

    @Schema(description = "商品ID", example = "prd_001")
    private String productId;

    @Schema(description = "封面图片URL", example = "https://example.com/cover.jpg")
    private String coverUrl;

    @Schema(description = "发布时间（ISO8601），不传则立即发布", example = "2024-01-01T10:00:00Z")
    private String publishedAt;
}
