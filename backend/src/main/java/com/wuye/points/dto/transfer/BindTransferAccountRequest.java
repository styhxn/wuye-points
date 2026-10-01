package com.wuye.points.dto.transfer;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 四层树角色收款账号绑定请求（阶段2）。
 *
 * <p>前端「我的收款方式」页：物业/统筹/板块/个体负责人绑定本人收款渠道。
 * 零钱渠道填 {@code openid}；银行卡渠道填卡号/开户行/持卡人（明文提交，
 * 后端以微信平台证书 RSA-OAEP 加密落库，仅用于商家转账出款，不对外回显）。</p>
 */
@Schema(description = "四层树角色收款账号绑定请求（阶段2）")
@Data
public class BindTransferAccountRequest {

    @Schema(description = "收款渠道：zero(微信零钱)/bank(银行卡)", example = "zero", required = true)
    @NotBlank(message = "收款渠道不能为空")
    private String channel;

    @Schema(description = "微信 openid（zero 渠道必填）", example = "oUpF8uMuAJO_M2pxb1Q9zNjWeS6o")
    @Size(max = 64, message = "openid 长度不能超过64")
    private String openid;

    @Schema(description = "银行卡号（bank 渠道必填，明文提交，后端加密落库）", example = "6217000012345678901")
    @Size(max = 32, message = "银行卡号长度不能超过32")
    private String bankAccountNo;

    @Schema(description = "开户银行全称（bank 渠道必填）", example = "中国工商银行北京海淀支行")
    @Size(max = 128, message = "开户银行全称长度不能超过128")
    private String bankName;

    @Schema(description = "持卡人姓名（bank 渠道必填，明文提交，后端加密落库）", example = "张三")
    @Size(max = 64, message = "持卡人姓名长度不能超过64")
    private String accountName;
}
