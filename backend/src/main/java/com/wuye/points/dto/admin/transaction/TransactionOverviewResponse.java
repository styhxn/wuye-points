package com.wuye.points.dto.admin.transaction;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@Schema(description = "平台交易总览")
public class TransactionOverviewResponse {

    @Schema(description = "查询区间")
    private Range range;

    @Schema(description = "口径说明：分组合计默认剔除与钱包账本重叠的过程类分类")
    private String note;

    @Schema(description = "按 分组+单位 汇总")
    private List<GroupStat> groups;

    @Schema(description = "按交易分类汇总")
    private List<CategoryStat> categories;

    @Schema(description = "按分类+状态汇总")
    private List<StatusStat> statusBreakdown;

    @Schema(description = "按日趋势（同一单位内收入/支出）")
    private List<TrendPoint> trend;

    @Data
    @Builder
    @Schema(description = "查询区间")
    public static class Range {
        @Schema(description = "开始时间", example = "2026-09-25 00:00:00")
        private String start;

        @Schema(description = "结束时间", example = "2026-10-24 23:59:59")
        private String end;

        @Schema(description = "物业公司ID，null=全部")
        private String propertyCompanyId;
    }

    @Data
    @Builder
    @Schema(description = "分组汇总（按单位拆分，不同单位不可直接相加）")
    public static class GroupStat {
        @Schema(description = "分组", example = "PAYMENT")
        private String group;

        @Schema(description = "分组中文名", example = "支付与退款")
        private String groupLabel;

        @Schema(description = "单位", example = "CNY")
        private String unit;

        @Schema(description = "单位中文名", example = "元")
        private String unitLabel;

        @Schema(description = "笔数", example = "10")
        private long count;

        @Schema(description = "交易总额（不区分方向）", example = "1000.00")
        private BigDecimal volumeAmount;

        @Schema(description = "收入金额", example = "900.00")
        private BigDecimal incomeAmount;

        @Schema(description = "支出金额", example = "100.00")
        private BigDecimal expenseAmount;

        @Schema(description = "手续费合计", example = "3.00")
        private BigDecimal feeAmount;

        @Schema(description = "实收/实付合计", example = "897.00")
        private BigDecimal actualAmount;

        @Schema(description = "平台分成/佣金合计", example = "50.00")
        private BigDecimal platformAmount;
    }

    @Data
    @Builder
    @Schema(description = "交易分类汇总")
    public static class CategoryStat {
        @Schema(description = "交易分类", example = "payment")
        private String category;

        @Schema(description = "交易分类中文名", example = "支付")
        private String categoryLabel;

        @Schema(description = "所属分组", example = "PAYMENT")
        private String group;

        @Schema(description = "所属分组中文名", example = "支付与退款")
        private String groupLabel;

        @Schema(description = "单位", example = "CNY")
        private String unit;

        @Schema(description = "单位中文名", example = "元")
        private String unitLabel;

        @Schema(description = "笔数", example = "10")
        private long count;

        @Schema(description = "交易总额（不区分方向）", example = "1000.00")
        private BigDecimal volumeAmount;

        @Schema(description = "收入金额", example = "900.00")
        private BigDecimal incomeAmount;

        @Schema(description = "支出金额", example = "100.00")
        private BigDecimal expenseAmount;

        @Schema(description = "手续费合计", example = "3.00")
        private BigDecimal feeAmount;

        @Schema(description = "实收/实付合计", example = "897.00")
        private BigDecimal actualAmount;

        @Schema(description = "平台分成/佣金合计", example = "50.00")
        private BigDecimal platformAmount;
    }

    @Data
    @Builder
    @Schema(description = "状态汇总")
    public static class StatusStat {
        @Schema(description = "交易分类", example = "payment")
        private String category;

        @Schema(description = "交易分类中文名", example = "支付")
        private String categoryLabel;

        @Schema(description = "状态", example = "paid")
        private String status;

        @Schema(description = "状态中文名", example = "已支付")
        private String statusLabel;

        @Schema(description = "笔数", example = "10")
        private long count;

        @Schema(description = "金额", example = "1000.00")
        private BigDecimal amount;

        @Schema(description = "单位", example = "CNY")
        private String unit;

        @Schema(description = "单位中文名", example = "元")
        private String unitLabel;
    }

    @Data
    @Builder
    @Schema(description = "日趋势")
    public static class TrendPoint {
        @Schema(description = "日期", example = "2026-10-24")
        private String date;

        @Schema(description = "单位", example = "CNY")
        private String unit;

        @Schema(description = "单位中文名", example = "元")
        private String unitLabel;

        @Schema(description = "收入金额", example = "900.00")
        private BigDecimal incomeAmount;

        @Schema(description = "支出金额", example = "100.00")
        private BigDecimal expenseAmount;

        @Schema(description = "笔数", example = "10")
        private long count;
    }
}
