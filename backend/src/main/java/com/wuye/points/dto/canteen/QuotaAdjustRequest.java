package com.wuye.points.dto.canteen;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 管理端手动调整社区食堂主商家可用充值额度（调试/运营兜底用）
 */
@Data
public class QuotaAdjustRequest {

    /** 调整到账额度（免手续费，直接入账） */
    @NotNull(message = "调整额度不能为空")
    @DecimalMin(value = "0.01", message = "调整额度必须大于0")
    private BigDecimal amount;

    /** 备注（选填，仅记录用途，不落库） */
    private String remark;
}
