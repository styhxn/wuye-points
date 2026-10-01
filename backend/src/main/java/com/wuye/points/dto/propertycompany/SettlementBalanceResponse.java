package com.wuye.points.dto.propertycompany;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
@Schema(description = "分成账户余额响应")
public class SettlementBalanceResponse {

    @Schema(description = "物业公司ID", example = "pc_xxx")
    private String propertyCompanyId;

    @Schema(description = "分成账户余额（业主消费分成自动进入，物业可自行提现）", example = "1234.56")
    private BigDecimal settlementBalance;
}
