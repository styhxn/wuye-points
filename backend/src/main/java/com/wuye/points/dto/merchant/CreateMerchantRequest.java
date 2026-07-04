package com.wuye.points.dto.merchant;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Schema(description = "创建商家请求")
@Data
public class CreateMerchantRequest {

    @Schema(description = "商家名称", example = "好又多超市", required = true)
    @NotBlank(message = "商家名称不能为空")
    @Size(max = 100, message = "商家名称长度不能超过100")
    private String name;

    @Schema(description = "品类", example = "生鲜食品", required = true)
    @NotBlank(message = "品类不能为空")
    private String category;

    @Schema(description = "联系电话", example = "13800138000", required = true)
    @NotBlank(message = "联系电话不能为空")
    @Size(max = 20, message = "联系电话长度不能超过20")
    private String contactPhone;

    @Schema(description = "地址", example = "阳光花园1栋101")
    @Size(max = 200, message = "地址长度不能超过200")
    private String address;

    @Schema(description = "封面图URL列表")
    private List<String> coverUrls;

    @Schema(description = "视频URL")
    @Size(max = 500, message = "视频URL长度不能超过500")
    private String videoUrl;

    @Schema(description = "商家描述")
    private String description;

    @Schema(description = "营业时间", example = "09:00-22:00")
    @Size(max = 50, message = "营业时间长度不能超过50")
    private String businessHours;

    @Schema(description = "配送费", example = "5.00")
    private BigDecimal deliveryFee;

    @Schema(description = "免配送费门槛", example = "50.00")
    private BigDecimal freeDeliveryThreshold;

    @Schema(description = "抽佣比例", example = "0.10")
    private BigDecimal commissionRate;

    @Schema(description = "积分兑换比", example = "1.00")
    private BigDecimal pointExchangeRate;
}
