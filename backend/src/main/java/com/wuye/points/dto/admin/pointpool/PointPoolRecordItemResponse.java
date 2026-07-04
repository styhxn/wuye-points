package com.wuye.points.dto.admin.pointpool;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
@Schema(description = "积分池记录项响应")
public class PointPoolRecordItemResponse {

    @Schema(description = "记录ID", example = "R001")
    private String id;

    @Schema(description = "物业公司ID", example = "PC001")
    private String propertyCompanyId;

    @Schema(description = "积分池类型", example = "PROPERTY", allowableValues = {"PROPERTY", "PLATFORM", "MERCHANT"})
    private String poolType;

    @Schema(description = "来源", example = "RECHARGE", allowableValues = {"RECHARGE", "CONSUME", "ADJUST", "REFUND"})
    private String source;

    @Schema(description = "调整金额", example = "100.00")
    private BigDecimal amount;

    @Schema(description = "调整后余额", example = "5000.00")
    private BigDecimal balance;

    @Schema(description = "说明", example = "业主充值积分")
    private String description;

    @Schema(description = "来源ID", example = "ORD001")
    private String sourceId;

    @Schema(description = "操作人ID", example = "U001")
    private String operatorId;

    @Schema(description = "操作人名称", example = "张三")
    private String operatorName;

    @Schema(description = "创建时间", example = "2024-01-01 10:00:00")
    private String createdAt;
}
