package com.wuye.points.dto.scanpay;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

/** 扫码付款状态（前端拉起支付后轮询，只有 status=paid 才算到账）。 */
@Schema(description = "扫码付款状态")
@Data
@Builder
public class ScanPayStatusResponse {

    private String scanPayId;
    private String merchantName;
    private BigDecimal amount;
    private String status;
    private Integer pointEarned;
    private String paidAt;
    private String paymentRecordId;
    private String outTradeNo;
    private String failReason;
}
