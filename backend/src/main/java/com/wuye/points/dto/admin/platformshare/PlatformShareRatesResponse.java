package com.wuye.points.dto.admin.platformshare;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
@Schema(description = "平台分成比例配置响应")
public class PlatformShareRatesResponse {

    @Schema(description = "物业公司ID", example = "pc_xxx")
    private String propertyCompanyId;

    @Schema(description = "平台从订单可分配金额中的分成比例", example = "0.0500")
    private BigDecimal platformShareRate;

    @Schema(description = "平台从配送费中的分成比例", example = "0.1000")
    private BigDecimal platformDeliveryShareRate;

    @Schema(description = "平台从提现手续费中的分成比例", example = "1.0000")
    private BigDecimal platformWithdrawalFeeShareRate;

    @Schema(description = "物业公司分成比例", example = "0.5000")
    private BigDecimal propertyShareRate;

    @Schema(description = "快递员分成比例", example = "0.1500")
    private BigDecimal courierShareRate;

    @Schema(description = "统筹分成比例", example = "0.7000")
    private BigDecimal coordinatorShareRate;

    @Schema(description = "板块负责人比例", example = "0.1000")
    private BigDecimal sectorLeaderRate;

    @Schema(description = "个体负责人比例", example = "0.0000")
    private BigDecimal individualLeaderRate;
}
