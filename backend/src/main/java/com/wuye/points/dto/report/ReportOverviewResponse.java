package com.wuye.points.dto.report;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
@Schema(description = "报表概览响应")
public class ReportOverviewResponse {

    @Schema(description = "查询范围")
    private Scope scope;

    @Schema(description = "汇总数据")
    private Summary summary;

    @Schema(description = "环比数据")
    private Comparison comparison;

    @Data
    @Builder
    @Schema(description = "查询范围")
    public static class Scope {
        @Schema(description = "物业公司ID", example = "pco_r4t5y")
        private String propertyCompanyId;

        @Schema(description = "物业公司名称", example = "万科物业")
        private String propertyCompanyName;
    }

    @Data
    @Builder
    @Schema(description = "汇总统计")
    public static class Summary {
        @Schema(description = "商户数量", example = "128")
        private long merchantCount;

        @Schema(description = "业主数量", example = "5000")
        private long residentCount;

        @Schema(description = "家庭数量", example = "3200")
        private long familyCount;

        @Schema(description = "消费总金额", example = "1250000.00")
        private BigDecimal totalConsumptionAmount;

        @Schema(description = "订单总数", example = "8500")
        private long totalOrderCount;

        @Schema(description = "积分池余额", example = "500000.00")
        private BigDecimal pointPoolBalance;

        @Schema(description = "物业币流通量", example = "300000.00")
        private BigDecimal propertyCoinCirculation;

        @Schema(description = "物业费收缴率", example = "0.92")
        private BigDecimal propertyFeeCollectionRate;

        @Schema(description = "物业费应收总额", example = "2000000.00")
        private BigDecimal propertyFeeTotalAmount;

        @Schema(description = "物业费已收总额", example = "1840000.00")
        private BigDecimal propertyFeePaidAmount;

        @Schema(description = "物业费逾期总额", example = "160000.00")
        private BigDecimal propertyFeeOverdueAmount;
    }

    @Data
    @Builder
    @Schema(description = "环比对比数据")
    public static class Comparison {
        @Schema(description = "消费增长率", example = "0.15")
        private BigDecimal consumptionGrowthRate;

        @Schema(description = "订单增长率", example = "0.08")
        private BigDecimal orderGrowthRate;

        @Schema(description = "业主增长率", example = "0.03")
        private BigDecimal residentGrowthRate;
    }
}
