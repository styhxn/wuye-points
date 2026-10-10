package com.wuye.points.dto.distribution;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@Schema(description = "分配统计响应")
public class DistributionStatsResponse {

    @Schema(description = "汇总统计")
    private Summary summary;

    @Schema(description = "按物业公司统计")
    private List<ByPropertyItem> byProperty;

    @Schema(description = "按协调员统计")
    private List<ByCoordinatorItem> byCoordinator;

    @Schema(description = "按片区统计")
    private List<BySectorItem> bySector;

    @Data
    @Builder
    @Schema(description = "汇总统计信息")
    public static class Summary {

        @Schema(description = "商品金额合计（不含配送费）", example = "125000.00")
        private BigDecimal productAmount;

        @Schema(description = "总订单额合计（含配送费）", example = "125800.00")
        private BigDecimal totalOrderAmount;

        @Schema(description = "商品平台盘合计（抽佣基础额 − 积分/物业币成本）", example = "15000.00")
        private BigDecimal totalDistributableAmount;

        @Schema(description = "商家商品实得合计", example = "100000.00")
        private BigDecimal merchantGoodsAmount;

        @Schema(description = "商家配送补贴合计（商家承担部分）", example = "2000.00")
        private BigDecimal merchantDeliverySubsidyAmount;

        @Schema(description = "商家最终实得合计 = 商家商品实得 − 商家配送补贴", example = "98000.00")
        private BigDecimal merchantAmount;

        @Schema(description = "公司商品平台盘收入合计", example = "1500.00")
        private BigDecimal platformAmount;

        @Schema(description = "公司配送收入合计（与 platformAmount 分开统计，不得合并）", example = "1000.00")
        private BigDecimal platformDeliveryAmount;

        @Schema(description = "配送员收入合计", example = "9000.00")
        private BigDecimal courierAmount;

        @Schema(description = "物业公司商品平台盘收入合计", example = "4500.00")
        private BigDecimal propertyAmount;

        @Schema(description = "管理盘合计（= 统筹 + 板块负责人 + 个体负责人）", example = "10500.00")
        private BigDecimal managementPoolAmount;

        @Schema(description = "统筹总金额", example = "4200.00")
        private BigDecimal coordinatorAmount;

        @Schema(description = "板块负责人总金额", example = "3150.00")
        private BigDecimal sectorLeaderAmount;

        @Schema(description = "个体负责人总金额", example = "3150.00")
        private BigDecimal individualLeaderAmount;

        @Schema(description = "单元楼长总金额", example = "3000.00")
        private BigDecimal buildingLeaderAmount;

        @Schema(description = "成本击穿差额合计", example = "0.00")
        private BigDecimal deficitAmount;

        @Schema(description = "冲正金额合计（负向记录绝对值之和）", example = "0.00")
        private BigDecimal reversalAmount;
    }

    @Data
    @Builder
    @Schema(description = "按物业公司统计项")
    public static class ByPropertyItem {

        @Schema(description = "物业公司ID", example = "prop_a1c3e5")
        private String propertyCompanyId;

        @Schema(description = "物业公司名称", example = "阳光物业")
        private String propertyName;

        @Schema(description = "金额", example = "25000.00")
        private BigDecimal amount;
    }

    @Data
    @Builder
    @Schema(description = "按协调员统计项")
    public static class ByCoordinatorItem {

        @Schema(description = "协调员ID", example = "coord_abc123")
        private String coordinatorId;

        @Schema(description = "协调员姓名", example = "李四")
        private String name;

        @Schema(description = "金额", example = "12500.00")
        private BigDecimal amount;
    }

    @Data
    @Builder
    @Schema(description = "按片区统计项")
    public static class BySectorItem {

        @Schema(description = "片区编码", example = "A")
        private String sector;

        @Schema(description = "片区名称", example = "A片区")
        private String sectorName;

        @Schema(description = "金额", example = "7500.00")
        private BigDecimal amount;
    }
}
