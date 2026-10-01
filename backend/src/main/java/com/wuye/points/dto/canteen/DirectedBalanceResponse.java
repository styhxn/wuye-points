package com.wuye.points.dto.canteen;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 社区食堂-用户定向余额项
 */
@Data
@Builder
@Schema(description = "用户定向余额项")
public class DirectedBalanceResponse {

    @Schema(description = "主商家体系ID", example = "mch_xxx")
    private String mainMerchantId;

    @Schema(description = "主商家名称", example = "幸福餐饮公司")
    private String mainMerchantName;

    @Schema(description = "可用定向余额", example = "98.00")
    private BigDecimal balance;
}
