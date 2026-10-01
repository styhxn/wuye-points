package com.wuye.points.dto.distribution;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 分账计算结果。
 *
 * <p>商品分账链（§4）+ 配送分账链（§5）的完整输出，
 * 同时用于 {@code /distribution/calculate} 预览响应与正式结算落库。</p>
 *
 * <p>字段命名与规范§8.5.2 严格一致；兼容字段（productCommission / productBase / effectiveDeliveryFee）保留，
 * 语义分别等价于 commissionBaseAmount / 商品实付 / 实付配送费。</p>
 */
@Data
@Builder
public class CalculationResult {

    // ===== 入参回显 =====
    private String merchantId;
    private BigDecimal productAmount;
    private BigDecimal commissionRate;
    private BigDecimal pointUsed;
    private BigDecimal coinUsed;
    private BigDecimal pointExchangeRate;

    // ===== 商品分账链（§4） =====
    private BigDecimal pointCost;            // 积分成本 = pointUsed / pointExchangeRate
    private BigDecimal coinCost;             // 物业币成本（1 物业币 = 1 元）
    private BigDecimal commissionBaseAmount; // 抽佣基础额 = productAmount × commissionRate
    private BigDecimal productCommission;    // 兼容旧字段，等价于 commissionBaseAmount
    private BigDecimal productBase;          // 兼容旧字段：商品实付 = productAmount − pointCost − coinCost
    private BigDecimal distributableAmount;  // 平台盘 = max(commissionBaseAmount − pointCost − coinCost, 0)
    private BigDecimal merchantGoodsShare;   // 商家商品实得 = productAmount − commissionBaseAmount
    private BigDecimal deficitAmount;        // 成本击穿差额 = max(−raw, 0)
    private String deficitSponsor;           // 击穿承担方（默认 property）

    // ===== 商品平台盘一级分配（§4.2） =====
    private BigDecimal platformShareRate;
    private BigDecimal propertyRate;
    private BigDecimal coordinatorRate;
    private BigDecimal platformShare;        // 公司商品平台盘收入
    private BigDecimal propertyShare;        // 物业商品平台盘收入
    private BigDecimal managementPoolAmount; // 管理盘 = 平台盘 − 平台 − 物业（减法吸收分币误差）

    // ===== 管理盘级联分配（§4.3） =====
    private BigDecimal sectorLeaderRate;
    private BigDecimal individualLeaderRate;
    private BigDecimal sectorGrossAmount;    // 板块毛额 = 管理盘 × sectorLeaderRate
    private BigDecimal coordinatorShare;     // 统筹实得 = 管理盘 − 板块毛额
    private BigDecimal sectorLeaderShare;    // 板块负责人实得 = 板块毛额 − 个体负责人
    private BigDecimal individualLeaderShare;// 个体负责人实得 = 板块毛额 × individualLeaderRate

    // ===== 配送分账链（§5） =====
    private BigDecimal originalDeliveryFee;
    private BigDecimal deliveryWaiverAmount; // 满额减免金额
    private BigDecimal deliveryFee;          // 用户实际支付配送费
    private BigDecimal effectiveDeliveryFee; // 兼容旧字段，等价于 deliveryFee
    private String deliverySubsidySponsor;   // none/merchant/property/platform
    private BigDecimal deliverySubsidyAmount;// 注入配送链路的补贴金额（= 减免额）
    private BigDecimal deliverySettlementBase; // 配送结算基数 = deliveryFee + subsidyAmount
    private BigDecimal platformDeliveryShareRate;
    private BigDecimal platformDeliveryShare;  // 公司配送收入
    private BigDecimal courierEarning;         // 配送员最终收入 = 基数 − 公司配送收入

    // ===== 商家最终实得（§4.4） =====
    private BigDecimal merchantDeliverySubsidy; // 商家承担的配送补贴（承担方为 merchant 时 = 补贴额，否则 0）
    private BigDecimal merchantAdjustmentAmount;// 退款/补差/冲账调整额
    private BigDecimal merchantShare;           // 商家最终实得 = merchantGoodsShare − merchantDeliverySubsidy + adjustment

    // ===== 角色 ID 与对账 =====
    private String coordinatorId;
    private String sectorLeaderId;
    private String individualLeaderId;

    private String calculationVersion;
}
