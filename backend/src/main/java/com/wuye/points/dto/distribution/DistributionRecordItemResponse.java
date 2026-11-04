package com.wuye.points.dto.distribution;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
@Schema(description = "分配记录项响应")
public class DistributionRecordItemResponse {

    @Schema(description = "记录ID", example = "dist_r7m9x1")
    private String id;

    @Schema(description = "订单ID", example = "ord_k3p5n2")
    private String orderId;

    @Schema(description = "订单编号", example = "ORD20260101001")
    private String orderNo;

    @Schema(description = "商户ID", example = "mer_q2w4e6")
    private String merchantId;

    @Schema(description = "商户名称", example = "好邻居超市")
    private String merchantName;

    @Schema(description = "住户ID", example = "res_b7m9x1")
    private String residentId;

    @Schema(description = "住户姓名", example = "张三")
    private String residentName;

    @Schema(description = "物业公司ID", example = "prop_a1c3e5")
    private String propertyCompanyId;

    @Schema(description = "总金额", example = "1000.00")
    private BigDecimal totalAmount;

    @Schema(description = "佣金比例", example = "0.10")
    private BigDecimal commissionRate;

    @Schema(description = "积分成本", example = "50.00")
    private BigDecimal pointCost;

    @Schema(description = "物业币支付金额（快照；不作为成本扣减）", example = "30.00")
    private BigDecimal coinCost;

    @Schema(description = "可分配金额", example = "920.00")
    private BigDecimal distributableAmount;

    @Schema(description = "物业公司份额", example = "460.00")
    private BigDecimal propertyShare;

    @Schema(description = "协调员份额", example = "230.00")
    private BigDecimal coordinatorShare;

    @Schema(description = "片区负责人份额", example = "138.00")
    private BigDecimal sectorLeaderShare;

    @Schema(description = "个人负责人份额", example = "92.00")
    private BigDecimal individualLeaderShare;

    @Schema(description = "用户积分预算比例快照（占抽佣基础额，V36 先扣档；历史数据为 null）", example = "0.1000")
    private BigDecimal userPointShareRate;

    @Schema(description = "用户奖励金预算比例快照（占抽佣基础额，V36 先扣档；历史数据为 null）", example = "0.1000")
    private BigDecimal userRewardShareRate;

    @Schema(description = "本次从平台盘切出的用户积分预算（元；历史数据为 null）", example = "0.50")
    private BigDecimal userPointBudget;

    @Schema(description = "本次从平台盘切出的用户奖励金预算（元；历史数据为 null）", example = "0.50")
    private BigDecimal userRewardBudget;

    @Schema(description = "单元楼长份额（从物业公司分成中扣除）", example = "46.00")
    private BigDecimal buildingLeaderShare;

    @Schema(description = "协调员ID", example = "coord_abc123")
    private String coordinatorId;

    @Schema(description = "协调员姓名", example = "李四")
    private String coordinatorName;

    @Schema(description = "片区负责人ID", example = "sector_xyz789")
    private String sectorLeaderId;

    @Schema(description = "片区负责人姓名", example = "王五")
    private String sectorLeaderName;

    @Schema(description = "个人负责人ID", example = "ind_def456")
    private String individualLeaderId;

    @Schema(description = "个人负责人姓名", example = "赵六")
    private String individualLeaderName;

    @Schema(description = "单元楼长ID", example = "bl_abc123")
    private String buildingLeaderId;

    @Schema(description = "单元楼长姓名", example = "钱七")
    private String buildingLeaderName;

    @Schema(description = "创建时间", example = "2026-01-01T00:00:00.000Z")
    private String createdAt;

    // ---------- 规范§21.1 商品分账链补充字段 ----------

    @Schema(description = "商品金额（不含配送费，已扣优惠券）", example = "100.00")
    private BigDecimal productAmount;

    @Schema(description = "抽佣基础额 = 商品金额 × 抽佣率", example = "10.00")
    private BigDecimal commissionBaseAmount;

    @Schema(description = "兼容旧字段，等价于 commissionBaseAmount", example = "10.00")
    private BigDecimal productCommission;

    @Schema(description = "商品实付 = 商品金额 − 积分成本（物业币为支付资金来源，不扣减）", example = "100.00")
    private BigDecimal productBase;

    @Schema(description = "积分成本击穿差额（物业币不再击穿）", example = "0.00")
    private BigDecimal deficitAmount;

    @Schema(description = "击穿差额承担方，默认 property", example = "property")
    private String deficitSponsor;

    @Schema(description = "公司平台盘比例快照", example = "0.1000")
    private BigDecimal platformShareRate;

    @Schema(description = "公司商品平台盘收入", example = "1.00")
    private BigDecimal platformShare;

    @Schema(description = "管理盘 = 平台盘 − 平台 − 物业", example = "2.99")
    private BigDecimal managementPoolAmount;

    @Schema(description = "板块毛额 = 管理盘 × sectorLeaderRate", example = "1.79")
    private BigDecimal sectorGrossAmount;

    // ---------- 规范§21.1 配送分账链补充字段 ----------

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

    @Schema(description = "商家承担的配送补贴", example = "0.00")
    private BigDecimal merchantDeliverySubsidy;

    @Schema(description = "商家调整额（退款/补差/冲账）", example = "0.00")
    private BigDecimal merchantAdjustmentAmount;

    @Schema(description = "商家最终实得 = 商家商品实得 − 商家配送补贴 + 调整额", example = "90.00")
    private BigDecimal merchantShare;

    @Schema(description = "公司配送收入", example = "1.00")
    private BigDecimal platformDeliveryShare;

    @Schema(description = "配送员最终收入", example = "9.00")
    private BigDecimal courierEarning;

    @Schema(description = "兼容旧字段，等价于 courierEarning", example = "9.00")
    private BigDecimal courierShare;

    @Schema(description = "计算版本", example = "distribution_b_v1")
    private String calculationVersion;

    @Schema(description = "记录状态 pending/distributed/reversed/partially_reversed", example = "distributed")
    private String status;
}
