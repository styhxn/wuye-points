package com.wuye.points.dto.coin;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
@Schema(description = "消费物业币响应")
public class SpendCoinResponse {

    @Schema(description = "记录ID", example = "cr_abc123")
    private String recordId;

    @Schema(description = "用户ID", example = "res_b7m9x1")
    private String residentId;

    @Schema(description = "消费物业币数量", example = "10.00")
    private BigDecimal coinAmount;

    @Schema(description = "操作后余额", example = "110.50")
    private BigDecimal balanceAfter;

    @Schema(description = "操作后家庭余额", example = "490.00")
    private BigDecimal familyBalanceAfter;

    @Schema(description = "来源", example = "商品购买")
    private String source;

    @Schema(description = "操作人ID", example = "sys_admin")
    private String operatorId;

    @Schema(description = "创建时间", example = "2026-01-01T00:00:00.000Z")
    private String createdAt;
}
