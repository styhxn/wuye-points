package com.wuye.points.dto.admin.residentmerchant;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 管理端确认业主商户保证金（线下收款）请求。
 *
 * <p>用于线上微信支付不可用/失败时，由物业线下收取保证金后在后台确认，
 * 将申请从 pending_deposit 推进到 pending_audit（或直接 active），解除线上支付阻塞。
 */
@Data
@Schema(description = "管理端确认业主商户保证金已缴纳")
public class ConfirmResidentMerchantDepositRequest {

    @Schema(description = "收款方式：offline(线下收款，默认)/transfer(银行转账)/cash(现金)/wechat(微信，人工核实)",
            example = "offline")
    @Size(max = 20)
    private String paymentMethod;

    @Schema(description = "是否直接开通（跳过待审核），默认 false；true 时申请直接置 active")
    private Boolean directActivate;

    @Schema(description = "备注（收款凭证号等，200字以内）")
    @Size(max = 200)
    private String remark;

    public String resolvePaymentMethod() {
        return paymentMethod == null || paymentMethod.trim().isEmpty() ? "offline" : paymentMethod.trim();
    }
}
