package com.wuye.points.dto.merchantad;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

import java.util.List;

@Data
@Schema(description = "发布商家广告（广告位按天计价）")
public class CreateMerchantAdRequest {

    @NotBlank
    private String title;

    @NotBlank
    private String content;

    private List<String> imageUrls;
    private String productId;

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
