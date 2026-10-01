package com.wuye.points.dto.admin.platformshare;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@Schema(description = "平台收益统计响应")
public class PlatformEarningsStatsResponse {

    @Schema(description = "订单分成收益", example = "5000.00")
    private BigDecimal totalPlatformShare;

    @Schema(description = "配送费分成收益", example = "200.00")
    private BigDecimal totalDeliveryEarning;

    @Schema(description = "提现手续费收益", example = "150.00")
    private BigDecimal totalWithdrawalFeeShare;

    @Schema(description = "总收益", example = "5350.00")
    private BigDecimal totalEarning;

    @Schema(description = "按物业公司分组统计")
    private List<ByPropertyItem> byProperty;

    @Data
    @Builder
    @Schema(description = "按物业公司统计项")
    public static class ByPropertyItem {

        @Schema(description = "物业公司ID", example = "pc_xxx")
        private String propertyCompanyId;

        @Schema(description = "物业公司名称", example = "XX物业")
        private String propertyName;

        @Schema(description = "订单分成收益", example = "3000.00")
        private BigDecimal platformShare;

        @Schema(description = "配送费分成收益", example = "100.00")
        private BigDecimal deliveryEarning;

        @Schema(description = "提现手续费收益", example = "80.00")
        private BigDecimal withdrawalFeeShare;
    }
}
