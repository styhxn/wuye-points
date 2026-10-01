package com.wuye.points.dto.merchantad;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

@Data
@Schema(description = "创建定向广告请求（广告位按天计价）")
public class TargetedMerchantAdRequest {

    @NotBlank
    @Schema(description = "商家ID", example = "mch_001")
    private String merchantId;

    @NotBlank
    @Size(max = 128)
    @Schema(description = "广告标题", example = "新品上市")
    private String title;

    @NotBlank
    @Schema(description = "广告内容", example = "限时优惠，新品上市")
    private String content;

    @Schema(description = "图片URL列表")
    private List<String> imageUrls;

    @Schema(description = "商品ID", example = "prd_001")
    private String productId;

    @Schema(description = "目标楼栋列表", example = "[\"1栋\", \"2栋\"]")
    private List<String> targetBuildings;

    @Schema(description = "目标性别: male/female/all", example = "all")
    private String targetGender;

    @Schema(description = "目标年龄段ID列表", example = "[\"ab_001\"]")
    private List<String> targetAgeBrackets;

    @NotNull
    @Min(1)
    @Max(30)
    @Schema(description = "广告展示天数（按天计价）", requiredMode = Schema.RequiredMode.REQUIRED, example = "7")
    private Integer durationDays;

    @NotBlank
    @Pattern(regexp = "free|coin|point|wechat|mock", message = "paymentMethod须为 free/coin/point/wechat/mock")
    @Schema(description = "支付方式: free(免费额度)/coin(物业币)/point(商家积分)/wechat(微信)/mock(模拟支付)",
            requiredMode = Schema.RequiredMode.REQUIRED, example = "coin",
            allowableValues = {"free", "coin", "point", "wechat", "mock"})
    private String paymentMethod;
}
