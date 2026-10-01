package com.wuye.points.dto.order;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Schema(description = "支付订单请求")
public class PayOrderRequest {

    @NotBlank(message = "支付方式不能为空")
    @Schema(description = "支付方式", required = true, example = "wechat", allowableValues = {"point", "property_coin", "wechat", "mixed"})
    private String paymentMethod;

    @Min(value = 0, message = "使用积分不能为负数")
    @Schema(description = "使用积分", example = "50")
    private Integer pointUsed;

    @DecimalMin(value = "0", message = "使用物业币不能为负数")
    @Schema(description = "使用物业币", example = "10.00")
    private BigDecimal coinUsed;

    @Schema(description = "微信支付码", example = "134567890123456789")
    private String wechatPayCode;

    @Schema(description = "家庭ID", example = "fam_abc123")
    private String familyId;

    @Schema(description = "用户openid（wechat/mixed 含现金部分时使用；不传则取用户绑定openid）", example = "oGX3Z5Xx...")
    private String openid;
}
