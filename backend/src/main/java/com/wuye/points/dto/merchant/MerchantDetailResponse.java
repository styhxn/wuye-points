package com.wuye.points.dto.merchant;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
@Schema(description = "商家详情响应")
public class MerchantDetailResponse {

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

    @Schema(description = "关联住户ID（用于联系商家/进聊天）", example = "res_xxx")
    private String residentId;

    @Schema(description = "商家地址", example = "幸福路100号")
    private String address;

    @Schema(description = "配送费", example = "3.00")
    private java.math.BigDecimal deliveryFee;

    @Schema(description = "免配送费门槛金额", example = "50.00")
    private java.math.BigDecimal freeDeliveryThreshold;

    @Schema(description = "是否启用满额减免", example = "true")
    private Boolean freeDeliveryEnabled;

    @Schema(description = "满额减免承担方：none/merchant/property/platform", example = "merchant")
    private String freeDeliverySponsor;

    @Schema(description = "配送范围：in_community/out_community/both", example = "both")
    private String deliveryScope;

    @Schema(description = "多档配送费配置")
    private List<DeliveryFeeTier> deliveryFeeTiers;

    @Schema(description = "会员优惠价", example = "9.50")
    private java.math.BigDecimal memberDiscountPrice;

    @Schema(description = "金币返利比例", example = "0.1")
    private java.math.BigDecimal coinRebateRate;

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

    @Schema(description = "更新时间", example = "2024-01-01T00:00:00")
    private String updatedAt;

    @Schema(description = "商家商品列表")
    private List<MerchantProductItemResponse> products;
}
