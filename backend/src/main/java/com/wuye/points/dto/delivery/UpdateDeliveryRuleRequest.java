package com.wuye.points.dto.delivery;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Schema(description = "更新配送规则请求")
public class UpdateDeliveryRuleRequest {

    @Size(max = 100, message = "name长度不能超过100")
    @Schema(description = "规则名称", example = "标准配送规则")
    private String name;

    @DecimalMin(value = "0.00", message = "baseFee不能为负")
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

    @DecimalMin(value = "0.00", message = "courierPerOrder不能为负")
    @Schema(description = "配送员每单固定收入", example = "3.00")
    private BigDecimal courierPerOrder;

    @Min(value = 1, message = "timeoutMinutes至少为1")
    @Schema(description = "超时时间(分钟)", example = "30")
    private Integer timeoutMinutes;

    @Schema(description = "规则状态", example = "active")
    private String status;
}
