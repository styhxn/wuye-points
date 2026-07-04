package com.wuye.points.dto.delivery;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
@Schema(description = "配送规则响应")
public class DeliveryRuleResponse {

    @Schema(description = "规则ID", example = "rule_001")
    private String id;

    @Schema(description = "规则名称", example = "标准配送规则")
    private String name;

    @Schema(description = "基础配送费", example = "3.00")
    private BigDecimal baseFee;

    @Schema(description = "每公斤配送费", example = "0.50")
    private BigDecimal perKgFee;

    @Schema(description = "免配送费门槛金额", example = "50.00")
    private BigDecimal freeThreshold;

    @Schema(description = "最大重量限制(kg)", example = "10.0")
    private BigDecimal maxWeight;

    @Schema(description = "最大配送距离(km)", example = "5.0")
    private BigDecimal maxDistance;

    @Schema(description = "配送员分成比例", example = "0.8")
    private BigDecimal courierShareRate;

    @Schema(description = "配送员每单固定收入", example = "3.00")
    private BigDecimal courierPerOrder;

    @Schema(description = "超时时间(分钟)", example = "30")
    private Integer timeoutMinutes;

    @Schema(description = "物业公司ID", example = "property_001")
    private String propertyCompanyId;

    @Schema(description = "规则状态", example = "active", allowableValues = {"active", "inactive"})
    private String status;

    @Schema(description = "创建时间", example = "2024-01-01T00:00:00")
    private String createdAt;

    @Schema(description = "更新时间", example = "2024-01-01T00:00:00")
    private String updatedAt;
}
