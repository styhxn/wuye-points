package com.wuye.points.dto.merchant;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
@Schema(description = "更新商家请求")
public class UpdateMerchantRequest {

    @Size(max = 100, message = "商家名称长度不能超过100")
    @Schema(description = "商家名称", example = "幸福超市")
    private String name;

    @Schema(description = "商家描述", example = "社区便利超市")
    private String description;

    @Schema(description = "封面图片URL列表")
    private List<String> coverUrls;

    @Size(max = 500, message = "视频URL长度不能超过500")
    @Schema(description = "视频URL", example = "https://example.com/video.mp4")
    private String videoUrl;

    @Size(max = 50, message = "营业时间长度不能超过50")
    @Schema(description = "营业时间", example = "08:00-22:00")
    private String businessHours;

    @Size(max = 20, message = "联系电话长度不能超过20")
    @Schema(description = "联系电话", example = "13800138000")
    private String contactPhone;

    @Size(max = 200, message = "地址长度不能超过200")
    @Schema(description = "商家地址", example = "幸福路100号")
    private String address;

    @Schema(description = "配送费", example = "3.00")
    private BigDecimal deliveryFee;

    @Schema(description = "免配送费门槛金额", example = "50.00")
    private BigDecimal freeDeliveryThreshold;

    @Schema(description = "是否启用满额减免", example = "true")
    private Boolean freeDeliveryEnabled;

    @Schema(description = "满额减免承担方：none/merchant/property/platform", example = "merchant")
    private String freeDeliverySponsor;

    @Schema(description = "配送范围：in_community/out_community/both", example = "both")
    private String deliveryScope;

    @Schema(description = "多档配送费配置（与 PUT /merchants/my/delivery-fees 同一结构）")
    private List<DeliveryFeeTier> deliveryFeeTiers;

    @Schema(description = "商家等级（仅platform_admin可设置）", example = "gold")
    private String merchantLevel;

    @Schema(description = "排序权重", example = "100")
    private Integer rankOrder;
}
