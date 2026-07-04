package com.wuye.points.dto.propertycompany;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
@Schema(description = "当前物业公司信息响应")
public class PropertyCompanyCurrentResponse {

    @Schema(description = "物业公司ID", example = "res_b7m9x1")
    private String id;

    @Schema(description = "物业公司名称", example = "万科物业")
    private String name;

    @Schema(description = "物业公司Logo地址", example = "https://example.com/logo.png")
    private String logoUrl;

    @Schema(description = "联系电话", example = "13800138000")
    private String contactPhone;

    @Schema(description = "公司地址", example = "北京市朝阳区建国路88号")
    private String address;

    @Schema(description = "分成配置")
    private DistributionRates distributionRates;

    @Schema(description = "积分展示是否启用", example = "true")
    private Boolean coinDisplayEnabled;

    @Schema(description = "积分发放模式", allowableValues = {"manual", "auto"}, example = "manual")
    private String coinIssueMode;

    @Schema(description = "积分有效期天数", example = "365")
    private Integer coinExpiryDays;

    @Schema(description = "配送配置")
    private DeliveryConfig deliveryConfig;

    @Schema(description = "提现手续费比例", example = "0.01")
    private BigDecimal withdrawalFeeRate;

    @Schema(description = "积分兑换费率", example = "1.00")
    private BigDecimal pointToFeeRate;

    @Schema(description = "两年清零是否启用", example = "false")
    private Boolean twoYearClearEnabled;

    @Schema(description = "邻居每日联系上限", example = "10")
    private Integer neighborDailyContactLimit;

    @Data
    @Builder
    @Schema(description = "分成比例配置")
    public static class DistributionRates {

        @Schema(description = "物业公司分成比例", example = "0.30")
        private BigDecimal propertyRate;

        @Schema(description = "协调员分成比例", example = "0.20")
        private BigDecimal coordinatorRate;

        @Schema(description = "片区负责人分成比例", example = "0.15")
        private BigDecimal sectorLeaderRate;

        @Schema(description = "个人负责人分成比例", example = "0.10")
        private BigDecimal individualLeaderRate;
    }

    @Data
    @Builder
    @Schema(description = "配送配置")
    public static class DeliveryConfig {

        @Schema(description = "配送基础费用", example = "5.00")
        private BigDecimal baseFee;

        @Schema(description = "快递员配送分成比例", example = "0.70")
        private BigDecimal courierShareRate;

        @Schema(description = "快递员每单配送费", example = "3.00")
        private BigDecimal courierPerOrder;
    }
}
