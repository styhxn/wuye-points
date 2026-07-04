package com.wuye.points.dto.merchant;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@Schema(description = "商家列表项响应")
public class MerchantListItemResponse {

    @Schema(description = "商家ID", example = "merchant_001")
    private String id;

    @Schema(description = "平台商家ID", example = "platform_001")
    private String platformMerchantId;

    @Schema(description = "物业公司ID", example = "property_001")
    private String propertyCompanyId;

    @Schema(description = "商家名称", example = "幸福超市")
    private String name;

    @Schema(description = "商家描述", example = "社区便利超市")
    private String description;

    @Schema(description = "封面图片URL列表")
    private List<String> coverUrls;

    @Schema(description = "视频URL", example = "https://example.com/video.mp4")
    private String videoUrl;

    @Schema(description = "二维码URL", example = "https://example.com/qrcode.png")
    private String qrCodeUrl;

    @Schema(description = "商家等级", example = "gold")
    private String merchantLevel;

    @Schema(description = "等级权重", example = "100")
    private Integer levelWeight;

    @Schema(description = "商家类别", example = "超市")
    private String category;

    @Schema(description = "营业时间", example = "08:00-22:00")
    private String businessHours;

    @Schema(description = "联系电话", example = "13800138000")
    private String contactPhone;

    @Schema(description = "商家地址", example = "幸福路100号")
    private String address;

    @Schema(description = "配送费", example = "3.00")
    private BigDecimal deliveryFee;

    @Schema(description = "免配送费门槛金额", example = "50.00")
    private BigDecimal freeDeliveryThreshold;

    @Schema(description = "会员优惠价", example = "9.50")
    private BigDecimal memberDiscountPrice;

    @Schema(description = "金币返利比例", example = "0.1")
    private BigDecimal coinRebateRate;

    @Schema(description = "是否开启金币返利", example = "true")
    private Boolean coinRebateEnabled;

    @Schema(description = "排序权重", example = "100")
    private Integer rankOrder;

    @Schema(description = "审核状态", example = "approved")
    private String auditStatus;

    @Schema(description = "商家状态", example = "active")
    private String status;

    @Schema(description = "创建时间", example = "2024-01-01T00:00:00")
    private String createdAt;
}
