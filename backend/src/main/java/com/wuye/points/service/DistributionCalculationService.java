package com.wuye.points.service;

import com.wuye.points.dto.distribution.CalculationResult;
import com.wuye.points.dto.distribution.CalculationSnapshot;
import com.wuye.points.exception.BizException;
import com.wuye.points.exception.ErrorCode;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Set;

/**
 * 统一分账计算服务（规范§15.1 / §19 #9）。
 *
 * <p>移动端预览（{@code /distribution/calculate}）与订单正式结算（{@code DistributionService.calculateAndSave}）
 * 必须共用本服务的 {@link #calculate(CalculationSnapshot)}，禁止在两端各写一套公式。</p>
 *
 * <p>公式严格对齐《商品分账与满额配送费减免-移动端对齐规范》§4 / §5 / §11：</p>
 * <ul>
 *   <li>商品链：抽佣基础额 = 商品金额 × 抽佣比例；平台盘 = 抽佣基础额 − 积分成本 − 物业币成本（最低为0，击穿记缺口）；</li>
 *   <li>一级分配：平台/物业/管理盘 = 平台盘 × 各自比例，管理盘用减法吸收分币误差；</li>
 *   <li>管理盘级联：板块毛额 = 管理盘 × sectorLeaderRate；个体负责人 = 板块毛额 × individualLeaderRate；
 *       板块负责人 = 板块毛额 − 个体负责人；统筹 = 管理盘 − 板块毛额；</li>
 *   <li>配送链：补贴注入配送结算基数；平台配送分成 = 基数 × 比例；配送员 = 基数 − 平台配送分成（减法吸收误差）；</li>
 *   <li>商家最终实得 = 商家商品实得 − 商家承担配送补贴 + 调整额。</li>
 * </ul>
 *
 * <p>所有金额使用 BigDecimal 定点计算，保留两位小数（ROUND_HALF_UP），禁止二进制浮点。</p>
 */
@Service
public class DistributionCalculationService {

    /** 当前计算版本（规范§17.1 新旧订单边界，B 方案） */
    public static final String CALCULATION_VERSION = "distribution_b_v1";

    /** 规范§2.4 / §6.3：满额减免默认由商家承担 */
    public static final String DEFAULT_SUBSIDY_SPONSOR = "merchant";

    /** 成本击穿默认承担方：物业运营成本（规范§4.1） */
    public static final String DEFAULT_DEFICIT_SPONSOR = "property";

    /** 合法配送补贴承担方（规范§3.2 / §5.2） */
    public static final Set<String> VALID_SUBSIDY_SPONSORS =
            Set.of("none", "merchant", "property", "platform");

    private static final int SCALE = 2;
    private static final RoundingMode RM = RoundingMode.HALF_UP;
    private static final BigDecimal DEFAULT_POINT_EXCHANGE_RATE = new BigDecimal("100");

    public CalculationResult calculate(CalculationSnapshot s) {
        BigDecimal ZERO = BigDecimal.ZERO;

        BigDecimal productAmount = nz(s.getProductAmount());
        BigDecimal commissionRate = nz(s.getCommissionRate());
        BigDecimal pointUsed = nz(s.getPointUsed());
        BigDecimal coinUsed = nz(s.getCoinUsed());
        BigDecimal pointExchangeRate = (s.getPointExchangeRate() != null && s.getPointExchangeRate().compareTo(ZERO) > 0)
                ? s.getPointExchangeRate() : DEFAULT_POINT_EXCHANGE_RATE;
        BigDecimal originalDeliveryFee = nz(s.getOriginalDeliveryFee());
        BigDecimal deliveryFee = nz(s.getDeliveryFee());
        String sponsor = resolveSponsor(s.getDeliverySubsidySponsor());

        BigDecimal platformShareRate = rate(s.getPlatformShareRate(), "0.1000");
        BigDecimal propertyRate = rate(s.getPropertyRate(), "0.6000");
        BigDecimal coordinatorRate = rate(s.getCoordinatorRate(), "0.3000");
        BigDecimal sectorLeaderRate = rate(s.getSectorLeaderRate(), "0.6000");
        BigDecimal individualLeaderRate = rate(s.getIndividualLeaderRate(), "0.5000");
        BigDecimal platformDeliveryShareRate = rate(s.getPlatformDeliveryShareRate(), "0.1000");
        BigDecimal merchantAdjustmentAmount = nz(s.getMerchantAdjustmentAmount());

        validateRates(platformShareRate, propertyRate, coordinatorRate,
                sectorLeaderRate, individualLeaderRate, platformDeliveryShareRate);

        // ===== 商品分账链（§4） =====
        BigDecimal pointCost = pointUsed.divide(pointExchangeRate, SCALE, RM);
        BigDecimal coinCost = coinUsed;
        BigDecimal commissionBaseAmount = money(productAmount.multiply(commissionRate));
        BigDecimal merchantGoodsShare = money(productAmount.subtract(commissionBaseAmount));
        BigDecimal rawDistributableAmount = money(commissionBaseAmount.subtract(pointCost).subtract(coinCost));
        BigDecimal distributableAmount = rawDistributableAmount.max(ZERO);
        BigDecimal deficitAmount = rawDistributableAmount.negate().max(ZERO);

        BigDecimal platformShare = ZERO;
        BigDecimal propertyShare = ZERO;
        BigDecimal managementPoolAmount = ZERO;
        BigDecimal sectorGrossAmount = ZERO;
        BigDecimal coordinatorShare = ZERO;
        BigDecimal sectorLeaderShare = ZERO;
        BigDecimal individualLeaderShare = ZERO;
        if (distributableAmount.compareTo(ZERO) > 0) {
            // 一级分配（§4.2）：平台、物业独立取整，管理盘用减法吸收分币误差
            platformShare = money(distributableAmount.multiply(platformShareRate));
            propertyShare = money(distributableAmount.multiply(propertyRate));
            managementPoolAmount = money(distributableAmount.subtract(platformShare).subtract(propertyShare));
            // 管理盘级联（§4.3）
            sectorGrossAmount = money(managementPoolAmount.multiply(sectorLeaderRate));
            individualLeaderShare = money(sectorGrossAmount.multiply(individualLeaderRate));
            sectorLeaderShare = money(sectorGrossAmount.subtract(individualLeaderShare));
            coordinatorShare = money(managementPoolAmount.subtract(sectorGrossAmount));
        }

        // ===== 配送分账链（§5） =====
        BigDecimal deliveryWaiverAmount = money(originalDeliveryFee.subtract(deliveryFee)).max(ZERO);
        // 减免额注入配送链路（§5.2）：无论承担方是谁，配送结算基数 = 用户配送费 + 补贴
        BigDecimal deliverySubsidyAmount = deliveryWaiverAmount;
        BigDecimal deliverySettlementBase = money(deliveryFee.add(deliverySubsidyAmount));
        BigDecimal platformDeliveryShare = money(deliverySettlementBase.multiply(platformDeliveryShareRate));
        BigDecimal courierEarning = money(deliverySettlementBase.subtract(platformDeliveryShare));

        // 商家最终实得（§4.4 / §5.2）：仅当承担方为 merchant 时才扣减商家
        BigDecimal merchantDeliverySubsidy = "merchant".equals(sponsor) ? deliverySubsidyAmount : ZERO;
        BigDecimal merchantShare = money(merchantGoodsShare.subtract(merchantDeliverySubsidy).add(merchantAdjustmentAmount));

        // ===== 守恒校验（§11.3） =====
        BigDecimal totalAmount = s.getTotalAmount();
        assertConservation(
                distributableAmount,
                platformShare, propertyShare, managementPoolAmount,
                coordinatorShare, sectorLeaderShare, individualLeaderShare,
                deliverySettlementBase, platformDeliveryShare, courierEarning,
                merchantGoodsShare, merchantDeliverySubsidy, merchantAdjustmentAmount, merchantShare,
                totalAmount, productAmount, deliveryFee);

        String version = (s.getCalculationVersion() != null) ? s.getCalculationVersion() : CALCULATION_VERSION;
        String deficitSponsor = deficitAmount.compareTo(ZERO) > 0
                ? (s.getDeficitSponsor() != null ? s.getDeficitSponsor() : DEFAULT_DEFICIT_SPONSOR)
                : null;

        return CalculationResult.builder()
                .merchantId(s.getMerchantId())
                .productAmount(productAmount)
                .commissionRate(commissionRate)
                .pointUsed(pointUsed)
                .coinUsed(coinUsed)
                .pointExchangeRate(pointExchangeRate)
                .pointCost(pointCost)
                .coinCost(coinCost)
                .commissionBaseAmount(commissionBaseAmount)
                // 兼容旧字段：productCommission 与 commissionBaseAmount 同义
                .productCommission(commissionBaseAmount)
                .productBase(money(productAmount.subtract(pointCost).subtract(coinCost)))
                .distributableAmount(distributableAmount)
                .merchantGoodsShare(merchantGoodsShare)
                .platformShareRate(platformShareRate)
                .propertyRate(propertyRate)
                .coordinatorRate(coordinatorRate)
                .sectorLeaderRate(sectorLeaderRate)
                .individualLeaderRate(individualLeaderRate)
                .platformShare(platformShare)
                .propertyShare(propertyShare)
                .managementPoolAmount(managementPoolAmount)
                .sectorGrossAmount(sectorGrossAmount)
                .coordinatorShare(coordinatorShare)
                .sectorLeaderShare(sectorLeaderShare)
                .individualLeaderShare(individualLeaderShare)
                .originalDeliveryFee(originalDeliveryFee)
                .deliveryWaiverAmount(deliveryWaiverAmount)
                .deliveryFee(deliveryFee)
                .effectiveDeliveryFee(deliveryFee)
                .deliverySubsidySponsor(sponsor)
                .deliverySubsidyAmount(deliverySubsidyAmount)
                .deliverySettlementBase(deliverySettlementBase)
                .platformDeliveryShareRate(platformDeliveryShareRate)
                .platformDeliveryShare(platformDeliveryShare)
                .courierEarning(courierEarning)
                .merchantDeliverySubsidy(merchantDeliverySubsidy)
                .merchantAdjustmentAmount(merchantAdjustmentAmount)
                .merchantShare(merchantShare)
                .deficitAmount(deficitAmount)
                .deficitSponsor(deficitSponsor)
                .coordinatorId(s.getCoordinatorId())
                .sectorLeaderId(s.getSectorLeaderId())
                .individualLeaderId(s.getIndividualLeaderId())
                .calculationVersion(version)
                .build();
    }

    /**
     * 守恒校验（规范§11.3）：误差必须为 0.00，不得依靠前端容忍。
     */
    private void assertConservation(BigDecimal distributableAmount,
                                    BigDecimal platformShare,
                                    BigDecimal propertyShare,
                                    BigDecimal managementPoolAmount,
                                    BigDecimal coordinatorShare,
                                    BigDecimal sectorLeaderShare,
                                    BigDecimal individualLeaderShare,
                                    BigDecimal deliverySettlementBase,
                                    BigDecimal platformDeliveryShare,
                                    BigDecimal courierEarning,
                                    BigDecimal merchantGoodsShare,
                                    BigDecimal merchantDeliverySubsidy,
                                    BigDecimal merchantAdjustmentAmount,
                                    BigDecimal merchantShare,
                                    BigDecimal totalAmount,
                                    BigDecimal productAmount,
                                    BigDecimal deliveryFee) {
        boolean productChainOk = distributableAmount.compareTo(
                money(platformShare.add(propertyShare).add(managementPoolAmount))) == 0;
        boolean managementPoolOk = managementPoolAmount.compareTo(
                money(coordinatorShare.add(sectorLeaderShare).add(individualLeaderShare))) == 0;
        boolean deliveryChainOk = deliverySettlementBase.compareTo(
                money(platformDeliveryShare.add(courierEarning))) == 0;
        boolean merchantOk = merchantShare.compareTo(
                money(merchantGoodsShare.subtract(merchantDeliverySubsidy).add(merchantAdjustmentAmount))) == 0;
        boolean userPayOk = totalAmount == null
                || totalAmount.compareTo(money(productAmount.add(deliveryFee))) == 0;
        if (!productChainOk || !managementPoolOk || !deliveryChainOk || !merchantOk || !userPayOk) {
            throw new BizException(ErrorCode.DISTRIBUTION_NOT_CONSERVED,
                    "分账金额不守恒：platform=" + platformShare + " property=" + propertyShare
                            + " managementPool=" + managementPoolAmount + " deliveryBase=" + deliverySettlementBase
                            + " merchantShare=" + merchantShare);
        }
    }

    /**
     * 承担方解析：缺省取商家承担（规范§6.3 系统默认 merchant），非法值直接拒绝（规范§16 96007）。
     */
    private String resolveSponsor(String sponsor) {
        if (sponsor == null || sponsor.isBlank()) {
            return DEFAULT_SUBSIDY_SPONSOR;
        }
        String normalized = sponsor.trim();
        // 历史数据无法确定承担方（规范§17.2）：按无减免处理，不得擅自回填为商家承担
        if ("unknown".equals(normalized)) {
            return "none";
        }
        if (!VALID_SUBSIDY_SPONSORS.contains(normalized)) {
            throw new BizException(ErrorCode.DELIVERY_SUBSIDY_SPONSOR_INVALID);
        }
        return normalized;
    }

    /**
     * 比例合法性校验（规范§6.2）：一级三项之和必须为 100%（§4.2）。
     */
    private void validateRates(BigDecimal platformShareRate,
                               BigDecimal propertyRate,
                               BigDecimal coordinatorRate,
                               BigDecimal sectorLeaderRate,
                               BigDecimal individualLeaderRate,
                               BigDecimal platformDeliveryShareRate) {
        if (isInvalidRate(platformShareRate) || isInvalidRate(propertyRate)
                || isInvalidRate(coordinatorRate) || isInvalidRate(sectorLeaderRate)
                || isInvalidRate(individualLeaderRate)) {
            throw new BizException(ErrorCode.DISTRIBUTION_RATE_INVALID, "分账比例需在0~1之间");
        }
        if (isInvalidRate(platformDeliveryShareRate)) {
            throw new BizException(ErrorCode.DELIVERY_SHARE_RATE_INVALID);
        }
        BigDecimal one = new BigDecimal("1.0000");
        if (platformShareRate.add(propertyRate).add(coordinatorRate).compareTo(one) != 0) {
            throw new BizException(ErrorCode.DISTRIBUTION_RATE_TOTAL_INVALID);
        }
    }

    private boolean isInvalidRate(BigDecimal rate) {
        return rate.compareTo(BigDecimal.ZERO) < 0 || rate.compareTo(new BigDecimal("1.0000")) > 0;
    }

    private BigDecimal rate(BigDecimal value, String fallback) {
        return value != null ? value : new BigDecimal(fallback);
    }

    private BigDecimal money(BigDecimal value) {
        return value.setScale(SCALE, RM);
    }

    private BigDecimal nz(BigDecimal v) {
        return v == null ? BigDecimal.ZERO : v;
    }
}
