package com.wuye.points.dto.coinwithdrawal;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
@Schema(description = "提现设置响应")
public class WithdrawalSettingsResponse {

    @Schema(description = "物业公司ID", example = "pc_xxx")
    private String propertyCompanyId;

    @Schema(description = "是否开启自动提现", example = "false")
    private Boolean autoEnabled;

    @Schema(description = "自动提现周期天数", example = "1")
    private Integer periodDays;

    @Schema(description = "提现手续费率", example = "0.0060")
    private BigDecimal feeRate;
}
