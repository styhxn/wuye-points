package com.wuye.points.dto.admin.transaction;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
@Schema(description = "平台交易明细行")
public class TransactionItemResponse {

    @Schema(description = "交易记录ID", example = "pr_xxx")
    private String id;

    @Schema(description = "交易分类", example = "payment")
    private String category;

    @Schema(description = "交易分类中文名", example = "支付")
    private String categoryLabel;

    @Schema(description = "所属分组", example = "PAYMENT")
    private String group;

    @Schema(description = "所属分组中文名", example = "支付与退款")
    private String groupLabel;

    @Schema(description = "发生时间", example = "2026-10-24 12:00:00")
    private String occurredAt;

    @Schema(description = "收支方向：income/expense/neutral", example = "income")
    private String direction;

    @Schema(description = "收支方向中文名", example = "收入")
    private String directionLabel;

    @Schema(description = "金额单位：CNY/POINT/COIN/CNY_DIRECTED", example = "CNY")
    private String unit;

    @Schema(description = "金额单位中文名", example = "元")
    private String unitLabel;

    @Schema(description = "交易金额（非负；方向见 direction）", example = "100.00")
    private BigDecimal amount;

    @Schema(description = "手续费", example = "0.60")
    private BigDecimal feeAmount;

    @Schema(description = "实收/实付/净额", example = "99.40")
    private BigDecimal netAmount;

    @Schema(description = "平台分成/佣金", example = "5.00")
    private BigDecimal platformAmount;

    @Schema(description = "业务状态", example = "paid")
    private String status;

    @Schema(description = "业务状态中文名", example = "已支付")
    private String statusLabel;

    @Schema(description = "业务类型", example = "order")
    private String bizType;

    @Schema(description = "业务类型中文名", example = "商品订单")
    private String typeLabel;

    @Schema(description = "说明/失败原因", example = "")
    private String description;

    @Schema(description = "关联订单ID", example = "ord_xxx")
    private String orderId;

    @Schema(description = "业务单ID", example = "ord_xxx")
    private String bizId;

    @Schema(description = "渠道流水号", example = "wx_xxx")
    private String tradeNo;

    @Schema(description = "资金主体类型", example = "merchant")
    private String partyType;

    @Schema(description = "资金主体ID", example = "mch_xxx")
    private String partyId;

    @Schema(description = "资金主体名称", example = "示例商家")
    private String partyName;

    @Schema(description = "物业公司ID", example = "pc_xxx")
    private String propertyCompanyId;

    @Schema(description = "物业公司名称", example = "示例物业")
    private String propertyCompanyName;

    @Schema(description = "现金部分（支付类）", example = "80.00")
    private BigDecimal cashAmount;

    @Schema(description = "积分抵扣（支付类）", example = "100")
    private BigDecimal pointUsed;

    @Schema(description = "物业币抵扣（支付类）", example = "10.00")
    private BigDecimal coinUsed;

    @Schema(description = "食堂定向余额抵扣（支付类）", example = "0.00")
    private BigDecimal directedUsed;
}
