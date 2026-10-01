package com.wuye.points.dto.admin.platformshare;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Schema(description = "更新平台分成比例请求")
public class UpdatePlatformShareRatesRequest {

    @NotBlank(message = "propertyCompanyId不能为空")
    @Schema(description = "物业公司ID", example = "pc_xxx")
    private String propertyCompanyId;

    @DecimalMin(value = "0.0000", message = "platformShareRate不能小于0")
    @DecimalMax(value = "1.0000", message = "platformShareRate不能大于1")
    @Schema(description = "平台从订单可分配金额中的分成比例（0~1）", example = "0.0500")
    private BigDecimal platformShareRate;

    @DecimalMin(value = "0.0000", message = "platformDeliveryShareRate不能小于0")
    @DecimalMax(value = "1.0000", message = "platformDeliveryShareRate不能大于1")
    @Schema(description = "平台从配送费中的分成比例（0~1）", example = "0.1000")
    private BigDecimal platformDeliveryShareRate;

    @DecimalMin(value = "0.0000", message = "platformWithdrawalFeeShareRate不能小于0")
    @DecimalMax(value = "1.0000", message = "platformWithdrawalFeeShareRate不能大于1")
    @Schema(description = "平台从提现手续费中的分成比例（0~1）", example = "1.0000")
    private BigDecimal platformWithdrawalFeeShareRate;

    @DecimalMin(value = "0.0000", message = "propertyShareRate不能小于0")
    @DecimalMax(value = "1.0000", message = "propertyShareRate不能大于1")
    @Schema(description = "物业公司分成比例（0~1）", example = "0.5000")
    private BigDecimal propertyShareRate;

    @DecimalMin(value = "0.0000", message = "coordinatorShareRate不能小于0")
    @DecimalMax(value = "1.0000", message = "coordinatorShareRate不能大于1")
    @Schema(description = "管理人员盘（统筹）分成比例（0~1）", example = "0.2500")
    private BigDecimal coordinatorShareRate;

    @DecimalMin(value = "0.0000", message = "sectorLeaderRate不能小于0")
    @DecimalMax(value = "1.0000", message = "sectorLeaderRate不能大于1")
    @Schema(description = "板块负责人比例（从统筹盘中抽取，0~1）", example = "0.1000")
    private BigDecimal sectorLeaderRate;

    @DecimalMin(value = "0.0000", message = "individualLeaderRate不能小于0")
    @DecimalMax(value = "1.0000", message = "individualLeaderRate不能大于1")
    @Schema(description = "个体负责人比例（从板块额中抽取，0~1）", example = "0.0000")
    private BigDecimal individualLeaderRate;

    @DecimalMin(value = "0.0000", message = "courierShareRate不能小于0")
    @DecimalMax(value = "1.0000", message = "courierShareRate不能大于1")
    @Schema(description = "快递员分成比例（0~1）", example = "0.1500")
    private BigDecimal courierShareRate;
}
