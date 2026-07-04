package com.wuye.points.dto.admin.pointpool;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
@Schema(description = "积分池余额响应")
public class PointPoolBalanceResponse {

    @Schema(description = "物业公司ID", example = "PC001")
    private String propertyCompanyId;

    @Schema(description = "物业公司名称", example = "XX物业")
    private String propertyCompanyName;

    @Schema(description = "当前余额", example = "50000.00")
    private BigDecimal balance;

    @Schema(description = "累计充值", example = "100000.00")
    private BigDecimal totalIn;

    @Schema(description = "累计消费", example = "50000.00")
    private BigDecimal totalOut;

    @Schema(description = "等价金额", example = "50000.00")
    private BigDecimal equivalentAmount;

    @Schema(description = "更新时间", example = "2024-01-01 10:00:00")
    private String updatedAt;
}
