package com.wuye.points.dto.canteen;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 社区食堂-定向充值响应
 */
@Data
@Builder
@Schema(description = "定向充值响应")
public class DirectedRechargeResponse {

    @Schema(description = "充值流水ID", example = "cdr_xxx")
    private String rechargeId;

    @Schema(description = "主商家体系ID", example = "mch_xxx")
    private String mainMerchantId;

    @Schema(description = "被充值用户ID", example = "res_xxx")
    private String residentId;

    @Schema(description = "被充值用户姓名", example = "张三")
    private String residentName;

    @Schema(description = "充值金额", example = "50.00")
    private BigDecimal amount;

    @Schema(description = "充值后该体系定向余额", example = "150.00")
    private BigDecimal balanceAfter;
}
