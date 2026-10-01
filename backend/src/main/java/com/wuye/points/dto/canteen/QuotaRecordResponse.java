package com.wuye.points.dto.canteen;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 社区食堂-商家额度流水项（购买/充值扣减统一视图）
 */
@Data
@Builder
@Schema(description = "商家额度流水项")
public class QuotaRecordResponse {

    @Schema(description = "记录ID", example = "cqp_xxx")
    private String id;

    @Schema(description = "类型 purchase购买额度 / recharge定向充值扣减", example = "purchase")
    private String type;

    @Schema(description = "金额（购买为正、充值扣减为负）", example = "100.00")
    private BigDecimal amount;

    @Schema(description = "手续费金额（购买时）", example = "2.00")
    private BigDecimal feeAmount;

    @Schema(description = "到账额度（购买时）", example = "98.00")
    private BigDecimal actualQuota;

    @Schema(description = "状态（购买时）pending/paid/refunded/closed", example = "paid")
    private String status;

    @Schema(description = "关联用户（充值时：被充值用户ID）", example = "res_xxx")
    private String targetResidentId;

    @Schema(description = "备注", example = "购买充值额度")
    private String remark;

    @Schema(description = "创建时间（ISO-8601）", example = "2026-08-27T10:00:00.000Z")
    private String createdAt;
}
