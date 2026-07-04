package com.wuye.points.dto.coin;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
@Schema(description = "物业币记录明细响应")
public class CoinRecordItemResponse {

    @Schema(description = "记录ID", example = "cr_abc123")
    private String id;

    @Schema(description = "用户ID", example = "res_b7m9x1")
    private String residentId;

    @Schema(description = "家庭ID", example = "fam_abc123")
    private String familyId;

    @Schema(description = "物业币类型", example = "earn")
    private String coinType;

    @Schema(description = "物业币数量", example = "10.00")
    private BigDecimal amount;

    @Schema(description = "操作后余额", example = "110.50")
    private BigDecimal balance;

    @Schema(description = "来源", example = "充值")
    private String source;

    @Schema(description = "来源ID", example = "recharge_abc123")
    private String sourceId;

    @Schema(description = "描述", example = "充值获得物业币")
    private String description;

    @Schema(description = "操作人ID", example = "res_b7m9x1")
    private String operatorId;

    @Schema(description = "创建时间", example = "2026-01-01T00:00:00.000Z")
    private String createdAt;
}
