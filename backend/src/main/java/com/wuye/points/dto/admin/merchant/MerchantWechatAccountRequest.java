package com.wuye.points.dto.admin.merchant;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/** 管理端 — 商家特约商户号绑定请求（服务商模式手动绑定 / 进件结果补录）。 */
@Schema(description = "商家特约商户号绑定请求")
@Data
public class MerchantWechatAccountRequest {

    @Schema(description = "微信特约商户号（sub_mchid，8~20 位数字）", example = "1900000109", required = true)
    @NotBlank(message = "特约商户号不能为空")
    @Size(max = 32, message = "特约商户号长度不能超过32")
    private String subMchid;

    @Schema(description = "特约商户主体名称（营业执照名称）", example = "阳光水果店")
    @Size(max = 128, message = "主体名称长度不能超过128")
    private String merchantName;

    @Schema(description = "状态：active 可收款 / auditing 进件审核中 / frozen 冻结（默认 active）",
            example = "active")
    private String status;
}
