package com.wuye.points.service;

import com.wuye.points.entity.CourierManager;
import com.wuye.points.entity.Merchant;
import com.wuye.points.entity.Resident;
import com.wuye.points.repository.DeliveryRepository;
import com.wuye.points.repository.DistributionRecordRepository;
import com.wuye.points.repository.MerchantWithdrawalRepository;
import com.wuye.points.repository.OrderRepository;
import com.wuye.points.repository.ResidentRepository;
import com.wuye.points.repository.ServiceOrderRepository;
import com.wuye.points.repository.WalletFlowRepository;
import com.wuye.points.repository.WithdrawalRecordRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * 可提现金额聚合计算服务
 *
 * <p>统一聚合 商家 / 配送员（含快递负责人）的可提现余额字段，
 * 避免在多个 Controller/Converter 中复制计算逻辑。
 *
 * <p>计算口径（无 T+N，查询时实时聚合）：
 * <ul>
 *   <li>商家：可提现 = 累计已完成订单营收(order_status=completed) - 平台抽佣 - 提现占用额(pending+approved+completed)</li>
 *   <li>配送员/快递负责人：可提现 = 已完成配送报酬(status=delivered/completed) - 提现占用额</li>
 *   <li>被 withdrawalBlocked=true 时直接返回 0</li>
 * </ul>
 *
 * <p>说明：为避免与 withdrawal_records / merchant_withdrawals 表产生数据不一致，
 * 此处不维护冗余的 total_withdrawn 列，而是在请求时实时聚合。
 */
@Service
@RequiredArgsConstructor
public class WithdrawableService {

    private final MerchantWithdrawalRepository merchantWithdrawalRepository;
    private final DistributionRecordRepository distributionRecordRepository;
    private final OrderRepository orderRepository;
    private final WithdrawalRecordRepository withdrawalRecordRepository;
    private final ResidentRepository residentRepository;
    private final ServiceOrderRepository serviceOrderRepository;
    private final DeliveryRepository deliveryRepository;
    private final WalletFlowRepository walletFlowRepository;

    /** 商家自配配送费分成的钱包流水类型（与 WalletFlowService.recordMerchantDelivery 保持一致） */
    private static final String ENTRY_MERCHANT_DELIVERY_FEE_SHARE = "merchant_delivery_fee_share";

    /**
     * 计算商家可提现余额及营收统计
     *
     * @param merchant 商家实体
     * <p>可提现 = 已完成订单营业额 - 平台抽佣 - 提现占用额。
     * 平台抽佣即分账记录中的「订单金额 × 抽佣比例」，
     * 对应老板定稿口径：用户付 100、抽佣 20% 时商家实得 80，另外 20 进平台盘再做四档分配。
     *
     * @return 含 withdrawableAmount / totalWithdrawn / withdrawalBlocked / totalRevenue /
     *         totalOrders / platformCommission
     */
    @Transactional(readOnly = true)
    public MerchantWithdrawable calcMerchantWithdrawable(Merchant merchant) {
        if (merchant == null || merchant.getId() == null) {
            return new MerchantWithdrawable(
                    BigDecimal.ZERO, BigDecimal.ZERO, false, BigDecimal.ZERO, 0L, BigDecimal.ZERO, BigDecimal.ZERO);
        }
        boolean blocked = Boolean.TRUE.equals(merchant.getWithdrawalBlocked());

        RevenueStats stats = resolveRevenueStats(merchant);

        BigDecimal totalWithdrawn = merchantWithdrawalRepository
                .sumAmountByMerchantId(merchant.getId(), "completed");
        // 占用中的提现（pending/approved/completed；rejected 不占用）
        BigDecimal allWithdrawals = merchantWithdrawalRepository
                .sumAmountByMerchantId(merchant.getId(), null);

        // 商家收入：技师店走服务单营收（不分账记录），其余走分账 merchantShare（B 方案，历史回退 订单金额×(1−抽佣)）
        // 另叠加商家自配配送费分成（wallet_flows 兜底入账部分，entryType=merchant_delivery_fee_share）；
        // 微信 通道成功时该流水不产生，避免双算（资金在 微信 侧，不走内部可提现）
        boolean technicianSource = "technician".equals(merchant.getMerchantSource());
        BigDecimal merchantIncome = technicianSource
                ? defaultDecimal(stats.totalRevenue())
                : defaultDecimal(distributionRecordRepository
                        .sumMerchantIncomeForCompletedOrders(merchant.getId()))
                        .add(defaultDecimal(walletFlowRepository
                                .sumAmountByOwnerIdAndEntryType(merchant.getId(), ENTRY_MERCHANT_DELIVERY_FEE_SHARE)))
                        .setScale(2, RoundingMode.HALF_UP);
        // 应收：商家最终实得为负的订单金额合计（进入待偿账户，不进负可提现）
        BigDecimal receivableAmount = technicianSource
                ? BigDecimal.ZERO
                : defaultDecimal(distributionRecordRepository
                        .sumMerchantReceivableForCompletedOrders(merchant.getId()))
                        .setScale(2, RoundingMode.HALF_UP);

        // 平台抽佣（信息字段，兼容旧口径：订单金额 × 抽佣比例）
        BigDecimal platformCommission = defaultDecimal(
                distributionRecordRepository.sumCommissionByMerchantIdForCompletedOrders(merchant.getId()))
                .setScale(2, RoundingMode.HALF_UP);

        BigDecimal withdrawable = merchantIncome.subtract(defaultDecimal(allWithdrawals));
        if (withdrawable.compareTo(BigDecimal.ZERO) < 0) {
            withdrawable = BigDecimal.ZERO;
        }
        if (blocked) {
            withdrawable = BigDecimal.ZERO;
        }
        return new MerchantWithdrawable(
                withdrawable,
                defaultDecimal(totalWithdrawn),
                blocked,
                defaultDecimal(stats.totalRevenue()),
                stats.totalOrders(),
                platformCommission,
                receivableAmount);
    }

    private RevenueStats resolveRevenueStats(Merchant merchant) {
        if ("technician".equals(merchant.getMerchantSource())) {
            Resident provider = null;
            if (StringUtils.hasText(merchant.getResidentId())) {
                provider = residentRepository.findById(merchant.getResidentId()).orElse(null);
            }
            if (provider == null) {
                provider = residentRepository
                        .findByPhoneAndPropertyCompanyId(merchant.getContactPhone(), merchant.getPropertyCompanyId())
                        .orElse(null);
            }
            if (provider == null) {
                return new RevenueStats(BigDecimal.ZERO, 0L);
            }
            BigDecimal revenue = serviceOrderRepository.sumCompletedAmountByProviderId(provider.getId());
            long orders = serviceOrderRepository.countCompletedByProviderId(provider.getId());
            return new RevenueStats(defaultDecimal(revenue), orders);
        }
        BigDecimal revenue = orderRepository.sumCompletedRevenueByMerchantId(merchant.getId());
        long orders = orderRepository.countCompletedByMerchantId(merchant.getId());
        return new RevenueStats(defaultDecimal(revenue), orders);
    }

    private record RevenueStats(BigDecimal totalRevenue, long totalOrders) {
    }

    /**
     * 计算快递负责人可提现余额（名下配送员 + 负责人本人已完成配送报酬）
     */
    @Transactional(readOnly = true)
    public CourierWithdrawable calcCourierManagerWithdrawable(CourierManager manager) {
        if (manager == null || manager.getId() == null) {
            return new CourierWithdrawable(BigDecimal.ZERO, BigDecimal.ZERO, false);
        }
        boolean blocked = false;
        if (StringUtils.hasText(manager.getResidentId())) {
            Resident resident = residentRepository.findById(manager.getResidentId()).orElse(null);
            if (resident != null) {
                blocked = Boolean.TRUE.equals(resident.getWithdrawalBlocked());
            }
        }

        Set<String> courierIdSet = new LinkedHashSet<>();
        // 负责人本人接单也要计入
        if (StringUtils.hasText(manager.getResidentId())) {
            courierIdSet.add(manager.getResidentId());
        }
        residentRepository.findByCourierManagerId(manager.getId()).stream()
                .map(Resident::getId)
                .filter(StringUtils::hasText)
                .forEach(courierIdSet::add);

        List<String> courierIds = new ArrayList<>(courierIdSet);
        BigDecimal totalEarned = courierIds.isEmpty()
                ? BigDecimal.ZERO
                : defaultDecimal(deliveryRepository.sumDeliveredEarningByCourierIds(courierIds));

        BigDecimal totalWithdrawn = defaultDecimal(withdrawalRecordRepository
                .sumAmountByApplicant(manager.getId(), "courier", "completed"));
        BigDecimal allWithdrawals = defaultDecimal(withdrawalRecordRepository
                .sumAmountByApplicant(manager.getId(), "courier", null));

        BigDecimal withdrawable = totalEarned.subtract(allWithdrawals);
        if (withdrawable.compareTo(BigDecimal.ZERO) < 0) {
            withdrawable = BigDecimal.ZERO;
        }
        if (blocked) {
            withdrawable = BigDecimal.ZERO;
        }
        return new CourierWithdrawable(withdrawable, totalWithdrawn, blocked);
    }

    /**
     * 计算单个配送员（业主）个人可提现余额：本人已完成配送报酬 - 本人提现占用额
     */
    @Transactional(readOnly = true)
    public CourierWithdrawable calcCourierResidentWithdrawable(String courierResidentId) {
        if (!StringUtils.hasText(courierResidentId)) {
            return new CourierWithdrawable(BigDecimal.ZERO, BigDecimal.ZERO, false);
        }
        Resident resident = residentRepository.findById(courierResidentId).orElse(null);
        boolean blocked = resident != null && Boolean.TRUE.equals(resident.getWithdrawalBlocked());

        BigDecimal totalEarned = defaultDecimal(
                deliveryRepository.sumDeliveredEarningByCourierIds(List.of(courierResidentId)));
        BigDecimal totalWithdrawn = defaultDecimal(withdrawalRecordRepository
                .sumAmountByApplicant(courierResidentId, "courier", "completed"));
        BigDecimal allWithdrawals = defaultDecimal(withdrawalRecordRepository
                .sumAmountByApplicant(courierResidentId, "courier", null));

        BigDecimal withdrawable = totalEarned.subtract(allWithdrawals);
        if (withdrawable.compareTo(BigDecimal.ZERO) < 0) {
            withdrawable = BigDecimal.ZERO;
        }
        if (blocked) {
            withdrawable = BigDecimal.ZERO;
        }
        return new CourierWithdrawable(withdrawable, totalWithdrawn, blocked);
    }

    private static BigDecimal defaultDecimal(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }

    /**
     * 商家可提现余额结果
     */
    public record MerchantWithdrawable(
            BigDecimal withdrawableAmount,
            BigDecimal totalWithdrawn,
            boolean withdrawalBlocked,
            BigDecimal totalRevenue,
            long totalOrders,
            /** 已完成订单被平台抽走的佣金合计（totalRevenue 中不属于商家的部分） */
            BigDecimal platformCommission,
            /** 应收金额：商家最终实得为负的订单待偿合计（规范§4.4 / §10.5） */
            BigDecimal receivableAmount
    ) {
    }

    /**
     * 配送员/快递负责人可提现余额结果
     */
    public record CourierWithdrawable(
            BigDecimal withdrawableAmount,
            BigDecimal totalWithdrawn,
            boolean withdrawalBlocked
    ) {
    }
}
