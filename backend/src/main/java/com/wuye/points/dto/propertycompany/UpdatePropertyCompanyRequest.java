package com.wuye.points.dto.propertycompany;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Schema(description = "更新物业公司请求")
public class UpdatePropertyCompanyRequest {

    @Schema(description = "物业公司名称", example = "万科物业")
    private String name;

    @Schema(description = "物业公司Logo地址", example = "https://example.com/logo.png")
    private String logoUrl;

    @Schema(description = "联系电话", example = "13800138000")
    private String contactPhone;

    @Schema(description = "公司地址", example = "北京市朝阳区建国路88号")
    private String address;

    @Schema(description = "物业公司状态", allowableValues = {"active", "inactive", "pending"}, example = "active")
    private String status;

    @Schema(description = "我们公司（平台）分成比例，一级三档之一；仅平台管理员可写", example = "0.10")
    private BigDecimal platformShareRate;

    @Schema(description = "物业公司分成比例，一级三档之一", example = "0.598")
    private BigDecimal propertyShareRate;

    @Schema(description = "管理盘（统筹）分成比例，一级三档之一", example = "0.302")
    private BigDecimal coordinatorShareRate;

    @Schema(description = "片区负责人分成比例", example = "0.15")
    private BigDecimal sectorLeaderRate;

    @Schema(description = "个人负责人分成比例", example = "0.10")
    private BigDecimal individualLeaderRate;

    @Schema(description = "积分展示是否启用", example = "true")
    private Boolean coinDisplayEnabled;

    @Schema(description = "积分发放模式", allowableValues = {"manual", "auto"}, example = "manual")
    private String coinIssueMode;

    @Schema(description = "积分有效期天数", example = "365")
    private Integer coinExpiryDays;

    @Schema(description = "积分默认冻结", example = "false")
    private Boolean coinFreezeDefault;

    @Schema(description = "配送基础费用", example = "5.00")
    private BigDecimal deliveryBaseFee;

    @Schema(description = "快递员配送分成比例", example = "0.70")
    private BigDecimal deliveryCourierShareRate;

    @Schema(description = "快递员每单配送费", example = "3.00")
    private BigDecimal deliveryCourierPerOrder;

    @Schema(description = "提现手续费比例", example = "0.01")
    private BigDecimal withdrawalFeeRate;

    @Schema(description = "积分兑换费率", example = "1.00")
    private BigDecimal pointToFeeRate;

    @Schema(description = "两年清零是否启用", example = "false")
    private Boolean twoYearClearEnabled;

    @Schema(description = "邻居每日联系上限", example = "10")
    private Integer neighborDailyContactLimit;
}
