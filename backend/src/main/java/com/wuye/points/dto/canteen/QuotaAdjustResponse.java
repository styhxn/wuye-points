package com.wuye.points.dto.canteen;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 管理端手动调整额度响应
 */
@Data
@Builder
public class QuotaAdjustResponse {

    /** 主商家ID */
    private String merchantId;

    /** 本次入账额度 */
    private BigDecimal amount;

    /** 调整后可用充值额度总额 */
    private BigDecimal remainingQuota;
}
