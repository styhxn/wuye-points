package com.wuye.points.dto.admin.platformshare;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
@Schema(description = "平台收益明细项响应")
public class PlatformEarningRecordItemResponse {

    @Schema(description = "记录ID", example = "dr_xxx")
    private String id;

    @Schema(description = "收益类型: distribution/delivery/withdrawal", example = "distribution")
    private String type;

    @Schema(description = "物业公司ID", example = "pc_xxx")
    private String propertyCompanyId;

    @Schema(description = "物业公司名称", example = "XX物业")
    private String propertyName;

    @Schema(description = "收益金额", example = "100.00")
    private BigDecimal amount;

    @Schema(description = "订单ID（type=distribution/delivery 时返回）", example = "ord_xxx")
    private String orderId;

    @Schema(description = "提现记录ID（type=withdrawal 时返回）", example = "mw_xxx")
    private String withdrawalId;

    @Schema(description = "创建时间", example = "2026-01-01 10:00:00")
    private String createdAt;
}
