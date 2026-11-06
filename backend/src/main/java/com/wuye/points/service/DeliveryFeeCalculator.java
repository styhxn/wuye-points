package com.wuye.points.service;

import com.wuye.points.dto.merchant.DeliveryFeeTier;
import com.wuye.points.entity.Merchant;
import com.wuye.points.entity.MerchantCommunityDistance;
import com.wuye.points.repository.MerchantCommunityDistanceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.List;

/**
 * 配送费报价：商家自由配置的档位优先，未命中回落到默认配送费。
 *
 * <p>业务口径（2026-11-06 老板定）：不做系统自动测距，改成让商家自己把区间和费用设好。
 * 因此这里只消费两份已有数据：</p>
 * <ul>
 *   <li>{@code merchants.delivery_fee_tiers}：档位数组，每档可选维度
 *       {@code amount}（按订单商品金额）或 {@code km}（按商家到买家小区的距离）；</li>
 *   <li>{@code merchants.delivery_fee}：<b>默认值</b>，一档都没命中（或没配档位）时按它收。</li>
 * </ul>
 *
 * <p>距离档不要求买家在配送范围内，也不要求平台维护经纬度：距离只取
 * {@code merchant_community_distances}（管理端「商家×小区距离」表）里配好的值；
 * 没配到距离时该档<b>不命中</b>，自然回落到默认配送费，绝不报错、也不会算出负数。</p>
 *
 * <p>满额减免不在本类里做：减免是「命中价格后再归零」的独立开关，
 * 由调用方（{@code OrderService.calculateDeliveryFee}）在本方法结果之上继续判断，
 * 保证「关掉减免开关就一定收费」这条口径不被档位绕过。</p>
 */
@Component
@RequiredArgsConstructor
public class DeliveryFeeCalculator {

    private final MerchantCommunityDistanceRepository distanceRepository;
    private final MerchantConverter merchantConverter;

    /**
     * @param productAmount 订单商品金额（不含配送费、不含券），与满额减免的门槛口径一致
     * @param communityId   买家所在小区，用于 {@code km} 维度取距离；可为空
     * @return 档位费或默认配送费；商家未配置任何档位时即 {@code delivery_fee}
     */
    public BigDecimal quote(Merchant merchant, BigDecimal productAmount, String communityId) {
        BigDecimal defaultFee = merchant == null || merchant.getDeliveryFee() == null
                ? BigDecimal.ZERO : merchant.getDeliveryFee();
        if (merchant == null || !StringUtils.hasText(merchant.getDeliveryFeeTiers())) {
            return defaultFee;
        }

        List<DeliveryFeeTier> tiers = merchantConverter.parseDeliveryFeeTiers(merchant.getDeliveryFeeTiers());
        if (tiers.isEmpty()) {
            return defaultFee;
        }

        BigDecimal distanceKm = null;
        boolean needDistance = tiers.stream().anyMatch(t -> t.isEnabled() && "km".equals(t.resolvedMetric()));
        if (needDistance && StringUtils.hasText(communityId)) {
            distanceKm = distanceRepository.findByMerchantIdAndCommunityId(merchant.getId(), communityId)
                    .map(MerchantCommunityDistance::getDistanceKm)
                    .orElse(null);
        }

        for (DeliveryFeeTier tier : tiers) {
            if (!tier.isEnabled() || tier.getFee() == null) {
                continue;
            }
            if (matches(tier, productAmount, distanceKm)) {
                return tier.getFee();
            }
        }
        return defaultFee;
    }

    /** 区间口径统一为「含下界、不含上界」，上界为空表示不限；与 {@code validateDeliveryFeeTiers} 一致。 */
    private boolean matches(DeliveryFeeTier tier, BigDecimal productAmount, BigDecimal distanceKm) {
        BigDecimal value = "km".equals(tier.resolvedMetric()) ? distanceKm : productAmount;
        if (value == null) {
            return false;
        }
        BigDecimal min = tier.resolvedMin();
        BigDecimal max = tier.resolvedMax();
        if (value.compareTo(min) < 0) {
            return false;
        }
        return max == null || value.compareTo(max) < 0;
    }
}
