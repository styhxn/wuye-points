package com.wuye.points.dto.delivery;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;

@Schema(description = "创建配送规则请求")
@Data
public class CreateDeliveryRuleRequest {

    @Schema(description = "规则名称", example = "标准配送", required = true)
    @NotBlank(message = "name不能为空")
    @Size(max = 100, message = "name长度不能超过100")
    private String name;

    @Schema(description = "基础费用", example = "5.00", required = true)
    @NotNull(message = "baseFee不能为空")
    @DecimalMin(value = "0.00", message = "baseFee不能为负")
    private BigDecimal baseFee;

    @Schema(description = "每公斤费用", example = "1.00")
    private BigDecimal perKgFee;

    @Schema(description = "免运费门槛(金额)", example = "50.00")
    private BigDecimal freeThreshold;

    @Schema(description = "最大重量(kg)", example = "20.00")
    private BigDecimal maxWeight;

    @Schema(description = "最大配送距离(km)", example = "10.00")
    private BigDecimal maxDistance;

    @Schema(description = "快递员分成比例", example = "0.80")
    private BigDecimal courierShareRate;

    @Schema(description = "快递员每单固定收入", example = "3.00")
    @DecimalMin(value = "0.00", message = "courierPerOrder不能为负")
    private BigDecimal courierPerOrder;

    @Schema(description = "超时时间(分钟)", example = "30")
    @Min(value = 1, message = "timeoutMinutes至少为1")
    private Integer timeoutMinutes;

    @Schema(description = "物业公司ID", example = "pc_xxx")
    private String propertyCompanyId;
}
