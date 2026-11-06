package com.wuye.points.dto.merchant;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@Schema(description = "商家专属收款二维码")
public class MerchantQrCodeResponse {

    @Schema(description = "商家ID", example = "mch_35217712f21000")
    private String merchantId;

    @Schema(description = "商家名称")
    private String merchantName;

    @Schema(description = "二维码图片URL（已同步写入商家详情 qrCodeUrl，展示位可直接用）")
    private String qrCodeUrl;

    @Schema(description = "码类型：MINIPROGRAM_CODE=微信小程序码（永久有效）/ QR_LINK=普通链接二维码",
            allowableValues = {"MINIPROGRAM_CODE", "QR_LINK"})
    private String codeType;

    @Schema(description = "码内容：小程序码为页面路径，链接码为原始URL",
            example = "pages/service/merchantProducts?merchantId=mch_35217712f21000")
    private String codeContent;

    @Schema(description = "本次是否新生成（false 表示复用已有码）")
    private Boolean regenerated;
}
