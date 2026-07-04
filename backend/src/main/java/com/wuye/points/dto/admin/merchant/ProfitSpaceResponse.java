package com.wuye.points.dto.admin.merchant;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Schema(description = "商家利润空间响应")
@Data
@Builder
public class ProfitSpaceResponse {

    @Schema(description = "商家ID", example = "merchant_001")
    private String merchantId;

    @Schema(description = "商家名称", example = "张记小厨")
    private String merchantName;

    @Schema(description = "物业公司ID", example = "company_001")
    private String propertyCompanyId;

    @Schema(description = "消费金额（元）", example = "1000.00")
    private BigDecimal consumptionAmount;

    @Schema(description = "佣金比例", example = "0.10")
    private BigDecimal commissionRate;

    @Schema(description = "佣金金额（元）", example = "100.00")
    private BigDecimal commissionAmount;

    @Schema(description = "可分配金额（元）", example = "80.00")
    private BigDecimal distributableAmount;

    @Schema(description = "物业分成（元）", example = "30.00")
    private BigDecimal propertyShare;

    @Schema(description = "协调员分成（元）", example = "20.00")
    private BigDecimal coordinatorShare;

    @Schema(description = "部门负责人分成（元）", example = "20.00")
    private BigDecimal sectorLeaderShare;

    @Schema(description = "个人负责人分成（元）", example = "10.00")
    private BigDecimal individualLeaderShare;
}
