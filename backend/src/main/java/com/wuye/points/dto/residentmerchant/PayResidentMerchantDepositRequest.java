package com.wuye.points.dto.residentmerchant;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@Schema(description = "缴纳业主商户保证金")
public class PayResidentMerchantDepositRequest {

    @NotBlank
    private String applicationId;

    @NotBlank
    @Schema(description = "wechat 或 mock")
    private String paymentMethod;

    @Schema(description = "微信小程序 openid（wechat 支付时必传，可省略由服务端取已绑定 openid）")
    private String openid;
}
