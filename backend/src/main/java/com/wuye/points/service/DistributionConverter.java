package com.wuye.points.service;

import com.wuye.points.dto.distribution.DistributionRecordItemResponse;
import com.wuye.points.entity.Coordinator;
import com.wuye.points.entity.Delivery;
import com.wuye.points.entity.DistributionRecord;
import com.wuye.points.entity.IndividualLeader;
import com.wuye.points.entity.Merchant;
import com.wuye.points.entity.Order;
import com.wuye.points.entity.PointPool;
import com.wuye.points.entity.PointPoolRecord;
import com.wuye.points.entity.Resident;
import com.wuye.points.entity.SectorLeader;
import com.wuye.points.util.DateTimeUtil;
import com.wuye.points.util.SectorCatalog;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Map;

@Component
public class DistributionConverter {

    private static final String CALCULATION_VERSION = "distribution_b_v1";
    private static final String DEFAULT_DEFICIT_SPONSOR = "property";
    private static final String MERCHANT_SPONSOR = "merchant";
    private static final int MONEY_SCALE = 2;
    private static final int RATE_SCALE = 4;

    public DistributionRecordItemResponse toListItem(
            DistributionRecord record,
            Order order,
            Delivery delivery,
            Merchant merchant,
            Resident resident,
            Map<String, Resident> residentNameMap) {

        BigDecimal pointCost = defaultDecimal(record.getPointCost());
        BigDecimal coinCost = defaultDecimal(record.getCoinCost());
        BigDecimal distributableAmount = defaultDecimal(record.getDistributableAmount());
        BigDecimal platformShare = defaultDecimal(record.getPlatformShare());
        BigDecimal propertyShare = defaultDecimal(record.getPropertyShare());

        // 商品实付 = 订单商品金额 − 优惠券（佣金基数输入口径，规范§4.2）
        BigDecimal productAmount = order != null
                ? money(defaultDecimal(order.getTotalAmount()).subtract(defaultDecimal(order.getCouponAmount()))
                        .max(BigDecimal.ZERO))
                : money(distributableAmount.add(pointCost));
        BigDecimal commissionBaseAmount = money(productAmount.multiply(defaultDecimal(record.getCommissionRate())));
        // 物业币是已收现金的支付工具，不是成本；商品实付只扣积分成本。
        BigDecimal productBase = money(productAmount.subtract(pointCost));
        // 缺口口径与试算 DistributionService#buildShareBreakdown 对齐：抽佣基础额先扣积分成本，
        // 再扣用户积分/奖励金两档留成，扣穿才算缺口。读侧此前漏算两档，同一笔单列表页缺口小于试算页。
        // V36 前的历史单两档为 null，defaultDecimal 后按 0 计，自动退化为旧口径。
        BigDecimal userPointBudget = defaultDecimal(record.getUserPointBudget());
        BigDecimal userRewardBudget = defaultDecimal(record.getUserRewardBudget());
        BigDecimal deficitAmount = money(pointCost.add(userPointBudget).add(userRewardBudget)
                .subtract(commissionBaseAmount).max(BigDecimal.ZERO));
        BigDecimal managementPoolAmount = money(distributableAmount.subtract(platformShare).subtract(propertyShare));

        // 配送链：金额以订单快照为准，平台/配送员收入以 deliveries 落库事实源为准
        BigDecimal deliveryFee = order != null ? defaultDecimal(order.getDeliveryFee()) : BigDecimal.ZERO;
        BigDecimal originalDeliveryFee = order != null && order.getOriginalDeliveryFee() != null
                ? order.getOriginalDeliveryFee() : deliveryFee;
        BigDecimal deliverySubsidyAmount = order != null && order.getDeliverySubsidyAmount() != null
                ? order.getDeliverySubsidyAmount()
                : originalDeliveryFee.subtract(deliveryFee).max(BigDecimal.ZERO);
        String deliverySubsidySponsor = order != null && order.getDeliverySubsidySponsor() != null
                ? order.getDeliverySubsidySponsor()
                : (deliverySubsidyAmount.compareTo(BigDecimal.ZERO) > 0 ? MERCHANT_SPONSOR : "none");
        BigDecimal deliverySettlementBase = money(deliveryFee.add(deliverySubsidyAmount));
        BigDecimal platformDeliveryShare = defaultDecimal(delivery != null ? delivery.getPlatformEarning() : null);
        BigDecimal courierEarning = defaultDecimal(delivery != null ? delivery.getCourierEarning() : null);

        BigDecimal merchantGoodsShare = money(productAmount.subtract(commissionBaseAmount));
        BigDecimal merchantDeliverySubsidy = MERCHANT_SPONSOR.equals(deliverySubsidySponsor)
                ? deliverySubsidyAmount : BigDecimal.ZERO;
        BigDecimal merchantAdjustmentAmount = BigDecimal.ZERO;
        BigDecimal merchantShare = money(merchantGoodsShare
                .subtract(merchantDeliverySubsidy).add(merchantAdjustmentAmount));

        return DistributionRecordItemResponse.builder()
                .id(record.getId())
                .orderId(record.getOrderId())
                .orderNo(order != null ? order.getOrderNo() : null)
                .merchantId(record.getMerchantId())
                .merchantName(merchant != null ? merchant.getName() : null)
                .residentId(record.getResidentId())
                .residentName(resident != null ? resident.getName() : null)
                .propertyCompanyId(record.getPropertyCompanyId())
                .totalAmount(money(productAmount.add(deliveryFee)))
                .commissionRate(record.getCommissionRate())
                .pointCost(pointCost)
                .coinCost(coinCost)
                .distributableAmount(distributableAmount)
                .propertyShare(propertyShare)
                .coordinatorShare(defaultDecimal(record.getCoordinatorShare()))
                .sectorLeaderShare(defaultDecimal(record.getSectorLeaderShare()))
                .individualLeaderShare(defaultDecimal(record.getIndividualLeaderShare()))
                .userPointShareRate(record.getUserPointShareRate())
                .userRewardShareRate(record.getUserRewardShareRate())
                .userPointBudget(record.getUserPointBudget())
                .userRewardBudget(record.getUserRewardBudget())
                .buildingLeaderShare(defaultDecimal(record.getBuildingLeaderShare()))
                .coordinatorId(record.getCoordinatorId())
                .coordinatorName(resolveResidentName(record.getCoordinatorId(), residentNameMap))
                .sectorLeaderId(record.getSectorLeaderId())
                .sectorLeaderName(resolveResidentName(record.getSectorLeaderId(), residentNameMap))
                .individualLeaderId(record.getIndividualLeaderId())
                .individualLeaderName(resolveLeaderName(record.getIndividualLeaderId(), residentNameMap))
                .buildingLeaderId(record.getBuildingLeaderId())
                .buildingLeaderName(resolveResidentName(record.getBuildingLeaderId(), residentNameMap))
                .createdAt(DateTimeUtil.toIsoUtc(record.getCreatedAt()))
                // ---------- 规范§21.1 商品分账链补充字段 ----------
                .productAmount(productAmount)
                .commissionBaseAmount(commissionBaseAmount)
                .productCommission(commissionBaseAmount)
                .productBase(productBase)
                .deficitAmount(deficitAmount)
                .deficitSponsor(DEFAULT_DEFICIT_SPONSOR)
                .platformShareRate(resolvePlatformShareRate(platformShare, distributableAmount))
                .platformShare(platformShare)
                .managementPoolAmount(managementPoolAmount)
                .sectorGrossAmount(money(defaultDecimal(record.getSectorLeaderShare())
                        .add(defaultDecimal(record.getIndividualLeaderShare()))))
                // ---------- 规范§21.1 配送分账链补充字段 ----------
                .originalDeliveryFee(originalDeliveryFee)
                .deliveryFee(deliveryFee)
                .effectiveDeliveryFee(deliveryFee)
                .deliveryWaiverAmount(deliverySubsidyAmount)
                .deliverySubsidySponsor(deliverySubsidySponsor)
                .deliverySubsidyAmount(deliverySubsidyAmount)
                .deliverySettlementBase(deliverySettlementBase)
                .merchantGoodsShare(merchantGoodsShare)
                .merchantDeliverySubsidy(merchantDeliverySubsidy)
                .merchantAdjustmentAmount(merchantAdjustmentAmount)
                .merchantShare(merchantShare)
                .platformDeliveryShare(platformDeliveryShare)
                .courierEarning(courierEarning)
                .courierShare(courierEarning)
                .calculationVersion(CALCULATION_VERSION)
                .status(resolveStatus(record))
                .build();
    }

    /** 平台盘比例快照 = 平台收入 / 平台盘（平台盘为 0 时无法反推，返回 null） */
    private BigDecimal resolvePlatformShareRate(BigDecimal platformShare, BigDecimal distributableAmount) {
        if (distributableAmount.compareTo(BigDecimal.ZERO) <= 0) {
            return null;
        }
        return platformShare.divide(distributableAmount, RATE_SCALE, RoundingMode.HALF_UP);
    }

    /** 冲正记录（金额为负）标记为 reversed，其余为 distributed */
    private String resolveStatus(DistributionRecord record) {
        return defaultDecimal(record.getTotalAmount()).compareTo(BigDecimal.ZERO) < 0
                ? "reversed" : "distributed";
    }

    public String sectorLabel(String sector) {
        if (sector == null) {
            return null;
        }
        // 2026-09-08：板块标签统一由 SectorCatalog 提供（含历史旧编码兼容展示）
        return SectorCatalog.label(sector);
    }

    private String resolveResidentName(String leaderId, Map<String, Resident> leaderResidentMap) {
        if (leaderId == null) {
            return null;
        }
        Resident resident = leaderResidentMap.get(leaderId);
        return resident != null ? resident.getName() : null;
    }

    private String resolveLeaderName(String individualLeaderId, Map<String, Resident> residentNameMap) {
        return resolveResidentName(individualLeaderId, residentNameMap);
    }

    private BigDecimal defaultDecimal(BigDecimal value) {
        return value != null ? value : BigDecimal.ZERO;
    }

    private BigDecimal money(BigDecimal value) {
        return value.setScale(MONEY_SCALE, RoundingMode.HALF_UP);
    }
}