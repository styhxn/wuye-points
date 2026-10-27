package com.wuye.points.service;

import com.wuye.points.entity.BuildingLeader;
import com.wuye.points.entity.Merchant;
import com.wuye.points.payment.WechatSplitOwnerTypes;
import com.wuye.points.repository.BuildingLeaderRepository;
import com.wuye.points.repository.DistributionRecordRepository;
import com.wuye.points.repository.MerchantRepository;
import com.wuye.points.repository.ResidentMerchantSettlementRepository;
import com.wuye.points.repository.WithdrawalRecordRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

/**
 * 账号统一收益账户（v8.21）。
 *
 * <p>把同一住户账号下的楼长分成、业主卖货收益、名下商家收益合并为一个口径，
 * 楼长工作台展示与提现都走这里，避免各角色各记一套余额后重复提现。</p>
 *
 * <p>口径：统一可提现 = 楼长分成 + 业主卖货收益 + 名下商家剩余可提现
 * − 账号下全部角色已提现/在途 − 已由微信分账实时到账的楼长分成。</p>
 */
@Service
@RequiredArgsConstructor
public class UnifiedEarningsService {

    private static final List<String> COMPLETED = List.of("completed");
    private static final List<String> OCCUPIED = List.of("pending", "approved");
    private static final List<String> ALL_OCCUPIED = List.of("pending", "approved", "completed");

    private final BuildingLeaderRepository buildingLeaderRepository;
    private final DistributionRecordRepository distributionRecordRepository;
    private final ResidentMerchantSettlementRepository settlementRepository;
    private final MerchantRepository merchantRepository;
    private final WithdrawalRecordRepository withdrawalRecordRepository;
    private final WithdrawableService withdrawableService;

    public record UnifiedEarnings(BigDecimal totalEarning, BigDecimal withdrawnAmount,
                                  BigDecimal pendingAmount, BigDecimal withdrawableAmount) {
    }

    @Transactional(readOnly = true)
    public UnifiedEarnings calc(String residentId) {
        if (residentId == null) {
            return new UnifiedEarnings(BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO);
        }
        List<BuildingLeader> leaders = buildingLeaderRepository.findByResidentId(residentId);
        BigDecimal leaderShare = leaders.stream()
                .map(l -> defaultDecimal(distributionRecordRepository
                        .sumBuildingLeaderShareByBuildingLeaderId(l.getId())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal splitSettled = leaders.stream()
                .map(l -> defaultDecimal(withdrawableService.calcSplitSettledAmount(
                        WechatSplitOwnerTypes.BUILDING_LEADER.name(), l.getId())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal sellerProfit = defaultDecimal(
                settlementRepository.sumResellerProfitByResidentId(residentId));

        BigDecimal merchantRemaining = BigDecimal.ZERO;
        for (Merchant merchant : merchantRepository.findByResidentId(residentId)) {
            merchantRemaining = merchantRemaining.add(defaultDecimal(
                    withdrawableService.calcMerchantWithdrawable(merchant).withdrawableAmount()));
        }

        BigDecimal totalEarning = leaderShare.add(sellerProfit).add(merchantRemaining)
                .setScale(2, java.math.RoundingMode.HALF_UP);
        BigDecimal withdrawn = defaultDecimal(withdrawalRecordRepository
                .sumAmountByResidentIdAndStatusIn(residentId, COMPLETED));
        BigDecimal pending = defaultDecimal(withdrawalRecordRepository
                .sumAmountByResidentIdAndStatusIn(residentId, OCCUPIED));
        BigDecimal withdrawable = totalEarning
                .subtract(defaultDecimal(withdrawalRecordRepository
                        .sumAmountByResidentIdAndStatusIn(residentId, ALL_OCCUPIED)))
                .subtract(splitSettled)
                .max(BigDecimal.ZERO)
                .setScale(2, java.math.RoundingMode.HALF_UP);

        return new UnifiedEarnings(totalEarning, withdrawn, pending, withdrawable);
    }

    private BigDecimal defaultDecimal(BigDecimal value) {
        return value != null ? value : BigDecimal.ZERO;
    }
}
