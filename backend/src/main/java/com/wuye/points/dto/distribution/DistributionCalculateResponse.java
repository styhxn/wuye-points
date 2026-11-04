package com.wuye.points.dto.distribution;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 分账试算响应（规范§21.3，扁平字段，商品分账链 + 配送分账链）。
 */
@Data
@Builder
@Schema(description = "分配计算响应")
public class DistributionCalculateResponse {

    @Schema(description = "订单总额 = 商品金额 + 实付配送费（兼容旧字段）", example = "110.00")
    private BigDecimal totalAmount;

    @Schema(description = "商品金额（不含配送费，已扣除优惠券）", example = "100.00")
    private BigDecimal productAmount;

    @Schema(description = "佣金比例", example = "0.10")
    private BigDecimal commissionRate;

    @Schema(description = "积分成本", example = "0.00")
    private BigDecimal pointCost;

    @Schema(description = "物业币支付金额（快照；不作为成本扣减）", example = "0.00")
    private BigDecimal coinCost;

    @Schema(description = "抽佣基础额 = 商品金额 × 抽佣率", example = "10.00")
    private BigDecimal commissionBaseAmount;

    @Schema(description = "兼容旧字段，等价于 commissionBaseAmount", example = "10.00")
    private BigDecimal productCommission;

    @Schema(description = "商品实付 = 商品金额 − 积分成本（物业币为支付资金来源，不扣减）", example = "100.00")
    private BigDecimal productBase;

    @Schema(description = "用户积分预算比例快照（占抽佣基础额，V36 先扣档）", example = "0.1000")
    private BigDecimal userPointShareRate;

    @Schema(description = "用户奖励金预算比例快照（占抽佣基础额，V36 先扣档）", example = "0.1000")
    private BigDecimal userRewardShareRate;

    @Schema(description = "本次从平台盘切出的用户积分预算（元）", example = "0.50")
    private BigDecimal userPointBudget;

    @Schema(description = "本次从平台盘切出的用户奖励金预算（元）", example = "0.50")
    private BigDecimal userRewardBudget;

    @Schema(description = "平台盘 = max(抽佣基础额 − 积分成本 − 用户两档预算, 0)", example = "10.00")
    private BigDecimal distributableAmount;

    @Schema(description = "积分成本击穿差额（物业币不再击穿）", example = "0.00")
    private BigDecimal deficitAmount;

    @Schema(description = "击穿差额承担方，默认 property", example = "property")
    private String deficitSponsor;

    @Schema(description = "公司平台盘比例快照", example = "0.1000")
    private BigDecimal platformShareRate;

    @Schema(description = "公司商品平台盘收入", example = "1.00")
    private BigDecimal platformShare;

    @Schema(description = "物业商品平台盘收入（已扣楼长分成）", example = "5.41")
    private BigDecimal propertyShare;

    @Schema(description = "管理盘 = 平台盘 − 平台 − 物业（减法吸收分币误差）", example = "2.99")
    private BigDecimal managementPoolAmount;

    @Schema(description = "板块毛额 = 管理盘 × sectorLeaderRate", example = "1.79")
    private BigDecimal sectorGrossAmount;

    @Schema(description = "统筹实得 = 管理盘 − 板块毛额", example = "1.20")
    private BigDecimal coordinatorShare;

    @Schema(description = "板块负责人实得 = 板块毛额 − 个体负责人", example = "0.89")
    private BigDecimal sectorLeaderShare;

    @Schema(description = "个体负责人实得 = 板块毛额 × individualLeaderRate", example = "0.90")
    private BigDecimal individualLeaderShare;

    @Schema(description = "单元楼长份额（从物业公司分成中扣除）", example = "0.60")
    private BigDecimal buildingLeaderShare;

    @Schema(description = "满额减免前基础配送费", example = "10.00")
    private BigDecimal originalDeliveryFee;

    @Schema(description = "实付配送费", example = "10.00")
    private BigDecimal deliveryFee;

    @Schema(description = "兼容旧字段，等价于 deliveryFee", example = "10.00")
    private BigDecimal effectiveDeliveryFee;

    @Schema(description = "满额减免金额", example = "0.00")
    private BigDecimal deliveryWaiverAmount;

    @Schema(description = "减免承担方 none/merchant/property/platform", example = "none")
    private String deliverySubsidySponsor;

    @Schema(description = "配送补贴金额", example = "0.00")
    private BigDecimal deliverySubsidyAmount;

    @Schema(description = "配送结算基数 = 实付配送费 + 配送补贴", example = "10.00")
    private BigDecimal deliverySettlementBase;

    @Schema(description = "商家商品实得 = 商品金额 − 抽佣基础额", example = "90.00")
    private BigDecimal merchantGoodsShare;

    @Schema(description = "商家承担的配送补贴（承担方为 merchant 时为补贴额，否则 0）", example = "0.00")
    private BigDecimal merchantDeliverySubsidy;

    @Schema(description = "商家调整额（退款/补差/冲账）", example = "0.00")
    private BigDecimal merchantAdjustmentAmount;

    @Schema(description = "商家最终实得 = 商家商品实得 − 商家配送补贴 + 调整额", example = "90.00")
    private BigDecimal merchantShare;

    @Schema(description = "公司配送收入 = 配送结算基数 × platformDeliveryShareRate", example = "1.00")
    private BigDecimal platformDeliveryShare;

    @Schema(description = "配送员最终收入 = 配送结算基数 − 公司配送收入", example = "9.00")
    private BigDecimal courierEarning;

    @Schema(description = "兼容旧字段，等价于 courierEarning", example = "9.00")
    private BigDecimal courierShare;

    @Schema(description = "协调员ID", example = "coo_demo001")
    private String coordinatorId;

    @Schema(description = "片区负责人ID", example = "sec_demo001")
    private String sectorLeaderId;

    @Schema(description = "个人负责人ID", example = "ind_demo001")
    private String individualLeaderId;

    @Schema(description = "单元楼长ID（预览时按住户所属楼栋实时解析）", example = "bl_demo001")
    private String buildingLeaderId;

    @Schema(description = "计算版本 legacy/distribution_b_v1", example = "distribution_b_v1")
    private String calculationVersion;
}