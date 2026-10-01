package com.wuye.points.dto.distribution;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 分账计算入参快照。
 *
 * <p>所有费率/角色 ID 已由调用方（DistributionService）基于商家、物业、协调员等配置解析完成，
 * 本快照为「已解析后的纯计算入参」，保证 {@code /distribution/calculate} 与正式结算共用同一套公式。</p>
 *
 * <p>对应规范《商品分账与满额配送费减免-移动端对齐规范》§4 / §5 / §15.1。</p>
 */
@Data
public class CalculationSnapshot {

    /** 商家 ID（仅用于追溯，不参与计算） */
    private String merchantId;

    /** 物业公司 ID（仅用于追溯，不参与计算） */
    private String propertyCompanyId;

    /** 商品金额（subtotal，不含配送费） */
    private BigDecimal productAmount;

    /** 基础配送费（满额减免前） */
    private BigDecimal originalDeliveryFee;

    /** 实付配送费（= 满额减免后用户应付） */
    private BigDecimal deliveryFee;

    /**
     * 减免承担方：none / merchant / property / platform，缺省 merchant（规范§6.3）。
     * 下单时已由订单级活动、物业级活动、商家配置、系统默认四级优先级解析并保存快照。
     */
    private String deliverySubsidySponsor;

    /** 使用积分数（积分成本 = pointUsed / pointExchangeRate） */
    private BigDecimal pointUsed;

    /** 使用物业币数（1 物业币 = 1 元，直接作为成本） */
    private BigDecimal coinUsed;

    /** 商家物业佣金比例（用于商品平台服务费基数） */
    private BigDecimal commissionRate;

    /** 平台商品盘比例（公司从商品平台盘中分成，默认 0.10） */
    private BigDecimal platformShareRate;

    /** 平台配送分成比例（物业配置，默认 0.10） */
    private BigDecimal platformDeliveryShareRate;

    /** 积分汇率：多少积分 = 1 元（默认 100） */
    private BigDecimal pointExchangeRate;

    /** 物业分成比例（商品平台盘内，默认 0.60） */
    private BigDecimal propertyRate;

    /** 统筹/管理盘比例（商品平台盘内，默认 0.30） */
    private BigDecimal coordinatorRate;

    /** 板块负责人费率（管理盘内板块毛额抽取比例，默认 0.60） */
    private BigDecimal sectorLeaderRate;

    /** 个体负责人费率（板块毛额内抽取比例，默认 0.50） */
    private BigDecimal individualLeaderRate;

    /** 商家调整额（退款/补差/冲账等明确调整，默认 0） */
    private BigDecimal merchantAdjustmentAmount;

    /** 订单应付总额（仅用于守恒校验：productAmount + deliveryFee == totalAmount） */
    private BigDecimal totalAmount;

    /** 协调员 ID */
    private String coordinatorId;

    /** 片区长 ID */
    private String sectorLeaderId;

    /** 楼栋长 ID */
    private String individualLeaderId;

    /** 成本击穿承担方（默认 property，规范§4.1） */
    private String deficitSponsor;

    /** 计算版本（缺省使用 DistributionCalculationService.CALCULATION_VERSION） */
    private String calculationVersion;
}
