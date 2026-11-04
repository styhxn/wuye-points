package com.wuye.points.service;

import com.wuye.points.dto.merchant.settlement.MerchantCoinReceivableItemResponse;
import com.wuye.points.dto.merchant.settlement.MerchantCoinReceivableResponse;
import com.wuye.points.entity.Delivery;
import com.wuye.points.entity.DistributionRecord;
import com.wuye.points.entity.Merchant;
import com.wuye.points.entity.MerchantWithdrawal;
import com.wuye.points.entity.Order;
import com.wuye.points.entity.PaymentRecord;
import com.wuye.points.entity.WechatSplitRecord;
import com.wuye.points.exception.BizException;
import com.wuye.points.exception.ErrorCode;
import com.wuye.points.payment.PaymentBizType;
import com.wuye.points.repository.DeliveryRepository;
import com.wuye.points.repository.DistributionRecordRepository;
import com.wuye.points.repository.MerchantWithdrawalRepository;
import com.wuye.points.repository.OrderRepository;
import com.wuye.points.repository.PaymentRecordRepository;
import com.wuye.points.repository.WechatSplitRecordRepository;
import com.wuye.points.util.DateTimeUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 商家物业币应收计算。
 *
 * <p>商家应得 = 商品金额（订单总额 − 优惠券）− 抽佣 − 商家承担的配送补贴。
 * 现金与物业币是两种资金来源：
 * <ul>
 *   <li>服务商/电商通道：现金在商家特约商户号，不属于平台内部可提现；</li>
 *   <li>直连通道：现金在平台直连商户号，属于平台内部应付；</li>
 *   <li>物业币：现金在充值时就已进入平台直连商户号，由平台内部兑付。</li>
 * </ul>
 * 本服务把每笔已完成订单拆成「微信侧现金 / 平台内部现金 / 物业币应收」三本账，
 * 并从商家提现占用中扣减对应来源，避免一笔钱从微信和内部各付一次。</p>
 */
@Service
@RequiredArgsConstructor
public class MerchantCoinReceivableService {

    private static final List<String> OCCUPIED_STATUSES = List.of("pending", "approved", "completed");
    private static final List<String> COMPLETED_STATUS = List.of("completed");
    private static final List<String> SPLIT_SETTLED_STATUSES = List.of("frozen", "finished");
    private static final String SOURCE_COIN = "coin_receivable";

    private final OrderRepository orderRepository;
    private final DistributionRecordRepository distributionRecordRepository;
    private final PaymentRecordRepository paymentRecordRepository;
    private final WechatSplitRecordRepository splitRecordRepository;
    private final DeliveryRepository deliveryRepository;
    private final MerchantWithdrawalRepository merchantWithdrawalRepository;

    /** 商家收益拆分结果。 */
    public record MerchantIncome(
            BigDecimal totalIncomeAmount,
            BigDecimal coinReceivableAmount,
            BigDecimal internalCashAmount,
            BigDecimal wechatCashAmount,
            BigDecimal coinSettledAmount,
            BigDecimal coinPendingAmount,
            BigDecimal coinAvailableAmount,
            List<OrderIncome> orders) {
    }

    /** 单笔订单收益拆分。 */
    public record OrderIncome(
            Order order,
            BigDecimal commissionRate,
            BigDecimal commissionAmount,
            BigDecimal merchantIncome,
            BigDecimal coinReceivableAmount,
            BigDecimal internalCashAmount,
            BigDecimal wechatCashAmount,
            String payMode) {
    }

    /** 内部收益店铺（技师店/组长店/业主小店）保留原内部提现口径，不拆现金/物业币。 */
    public boolean isInternalEarningsMerchant(Merchant merchant) {
        String source = merchant.getMerchantSource();
        return "technician".equals(source)
                || "group_leader".equals(source)
                || "resident_shop".equals(source);
    }

    @Transactional(readOnly = true)
    public MerchantIncome calc(Merchant merchant) {
        return calc(merchant, null);
    }

    @Transactional(readOnly = true)
    public MerchantIncome calc(Merchant merchant, String excludeWithdrawalId) {
        if (merchant == null || !StringUtils.hasText(merchant.getId())) {
            return emptyIncome();
        }
        boolean internal = isInternalEarningsMerchant(merchant);
        List<Order> orders = orderRepository
                .findByMerchantIdAndOrderStatusOrderByCompletedAtDesc(merchant.getId(), "completed");
        if (orders.isEmpty()) {
            return emptyIncome();
        }

        Set<String> orderIds = new HashSet<>();
        orders.forEach(order -> orderIds.add(order.getId()));
        Map<String, DistributionRecord> distMap = new HashMap<>();
        distributionRecordRepository.findByOrderIdIn(orderIds)
                .forEach(record -> distMap.putIfAbsent(record.getOrderId(), record));
        Map<String, PaymentRecord> paidMap = new HashMap<>();
        for (PaymentRecord record : paymentRecordRepository
                .findByBizTypeAndBizIdInOrderByCreatedAtDesc(PaymentBizType.ORDER, orderIds)) {
            if ("paid".equals(record.getStatus())) {
                paidMap.putIfAbsent(record.getBizId(), record);
            }
        }
        Map<String, BigDecimal> splitMap = new HashMap<>();
        for (WechatSplitRecord split : splitRecordRepository
                .findByOrderIdInAndStatusIn(orderIds, SPLIT_SETTLED_STATUSES)) {
            splitMap.merge(split.getOrderId(), nz(split.getFreezeAmount()), BigDecimal::add);
        }
        Map<String, Delivery> deliveryMap = new HashMap<>();
        deliveryRepository.findByOrderIdIn(orderIds)
                .forEach(delivery -> deliveryMap.put(delivery.getOrderId(), delivery));

        BigDecimal totalIncome = BigDecimal.ZERO;
        BigDecimal coinGross = BigDecimal.ZERO;
        BigDecimal internalCashGross = BigDecimal.ZERO;
        BigDecimal wechatCashGross = BigDecimal.ZERO;
        List<OrderIncome> items = new ArrayList<>();

        for (Order order : orders) {
            DistributionRecord dist = distMap.get(order.getId());
            BigDecimal goodsBase = money(nz(order.getTotalAmount()).subtract(nz(order.getCouponAmount())))
                    .max(BigDecimal.ZERO);
            BigDecimal commissionRate = dist != null && dist.getCommissionRate() != null
                    ? dist.getCommissionRate() : BigDecimal.ZERO;
            BigDecimal commission = money(goodsBase.multiply(commissionRate));
            BigDecimal subsidy = "merchant".equals(order.getDeliverySubsidySponsor())
                    ? nz(order.getDeliverySubsidyAmount()) : BigDecimal.ZERO;
            BigDecimal income = money(goodsBase.subtract(commission).subtract(subsidy))
                    .max(BigDecimal.ZERO);

            BigDecimal coinReceivable;
            BigDecimal internalCash;
            BigDecimal wechatCash;
            String payMode = null;
            if (internal) {
                // 内部收益店铺：全部收益保留原内部提现口径，不再拆物业币，避免双入口。
                coinReceivable = BigDecimal.ZERO;
                internalCash = income;
                wechatCash = BigDecimal.ZERO;
            } else {
                PaymentRecord paid = paidMap.get(order.getId());
                payMode = paid != null && StringUtils.hasText(paid.getPayMode())
                        ? paid.getPayMode()
                        : (splitMap.containsKey(order.getId())
                                ? "partner"
                                : (nz(order.getCashAmount()).compareTo(BigDecimal.ZERO) > 0 ? "direct" : null));
                boolean directCash = payMode == null || "direct".equals(payMode);
                BigDecimal goodsCash = money(nz(order.getCashAmount()).subtract(nz(order.getDeliveryFee())))
                        .max(BigDecimal.ZERO);
                Delivery delivery = deliveryMap.get(order.getId());
                BigDecimal deliveryPlannedSplit = delivery != null
                        ? nz(delivery.getPlatformEarning()).add(nz(delivery.getCourierEarning()))
                        : BigDecimal.ZERO;
                BigDecimal actualGoodsSplit = directCash
                        ? BigDecimal.ZERO
                        : nz(splitMap.get(order.getId())).subtract(deliveryPlannedSplit).max(BigDecimal.ZERO);
                BigDecimal cashCapacity = directCash
                        ? goodsCash
                        : goodsCash.subtract(actualGoodsSplit).max(BigDecimal.ZERO);
                coinReceivable = money(income.subtract(cashCapacity)).max(BigDecimal.ZERO);
                if (directCash) {
                    internalCash = money(income.subtract(coinReceivable)).max(BigDecimal.ZERO);
                    wechatCash = BigDecimal.ZERO;
                } else {
                    internalCash = BigDecimal.ZERO;
                    wechatCash = money(income.subtract(coinReceivable)).max(BigDecimal.ZERO);
                }
            }

            totalIncome = totalIncome.add(income);
            coinGross = coinGross.add(coinReceivable);
            internalCashGross = internalCashGross.add(internalCash);
            wechatCashGross = wechatCashGross.add(wechatCash);
            items.add(new OrderIncome(order, commissionRate, commission, income, coinReceivable,
                    internalCash, wechatCash, payMode));
        }

        BigDecimal occupiedCoin = excludeWithdrawalId == null
                ? defaultDecimal(merchantWithdrawalRepository.sumAmountByMerchantIdAndStatusesAndSource(
                        merchant.getId(), OCCUPIED_STATUSES, SOURCE_COIN))
                : defaultDecimal(merchantWithdrawalRepository
                        .sumAmountByMerchantIdAndStatusesAndSourceExcluding(
                                merchant.getId(), OCCUPIED_STATUSES, SOURCE_COIN, excludeWithdrawalId));
        BigDecimal completedCoin = defaultDecimal(merchantWithdrawalRepository
                .sumAmountByMerchantIdAndStatusesAndSource(
                        merchant.getId(), COMPLETED_STATUS, SOURCE_COIN));
        BigDecimal pendingCoin = money(occupiedCoin.subtract(completedCoin)).max(BigDecimal.ZERO);
        BigDecimal availableCoin = money(coinGross.subtract(occupiedCoin)).max(BigDecimal.ZERO);

        return new MerchantIncome(
                money(totalIncome),
                money(coinGross),
                money(internalCashGross),
                money(wechatCashGross),
                money(completedCoin),
                pendingCoin,
                availableCoin,
                items);
    }

    /** 商家端物业币应收列表：按订单展示，并按提现单 FIFO 归集已结算/在途金额。 */
    @Transactional(readOnly = true)
    public MerchantCoinReceivableResponse list(Merchant merchant, int page, int pageSize) {
        if (pageSize > 100) {
            throw new BizException(ErrorCode.PARAM_OUT_OF_RANGE);
        }
        MerchantIncome income = calc(merchant);
        List<OrderIncome> coinOrders = income.orders().stream()
                .filter(item -> item.coinReceivableAmount().compareTo(BigDecimal.ZERO) > 0)
                .sorted(Comparator.comparing(item -> item.order().getCompletedAt(),
                        Comparator.nullsLast(Comparator.naturalOrder())))
                .toList();

        BigDecimal completedRemaining = income.coinSettledAmount();
        BigDecimal pendingRemaining = income.coinPendingAmount();
        List<MerchantCoinReceivableItemResponse> allItems = new ArrayList<>();
        long settledOrderCount = 0;
        for (OrderIncome item : coinOrders) {
            BigDecimal gross = item.coinReceivableAmount();
            BigDecimal settled = gross.min(completedRemaining).max(BigDecimal.ZERO);
            completedRemaining = completedRemaining.subtract(settled);
            BigDecimal remainingAfterSettled = gross.subtract(settled);
            BigDecimal pending = remainingAfterSettled.min(pendingRemaining).max(BigDecimal.ZERO);
            pendingRemaining = pendingRemaining.subtract(pending);
            BigDecimal available = remainingAfterSettled.subtract(pending).max(BigDecimal.ZERO);
            String status;
            if (available.compareTo(BigDecimal.ZERO) <= 0 && pending.compareTo(BigDecimal.ZERO) <= 0) {
                status = "settled";
                settledOrderCount++;
            } else if (settled.compareTo(BigDecimal.ZERO) > 0 || pending.compareTo(BigDecimal.ZERO) > 0) {
                status = "partial";
            } else if (pending.compareTo(BigDecimal.ZERO) > 0) {
                status = "pending";
            } else {
                status = "available";
            }
            allItems.add(MerchantCoinReceivableItemResponse.builder()
                    .orderId(item.order().getId())
                    .orderNo(item.order().getOrderNo())
                    .completedAt(DateTimeUtil.toIsoUtc(item.order().getCompletedAt()))
                    .totalAmount(money(item.order().getTotalAmount()))
                    .couponAmount(money(item.order().getCouponAmount()))
                    .coinUsed(money(item.order().getCoinUsed()))
                    .cashAmount(money(item.order().getCashAmount()))
                    .payMode(item.payMode())
                    .commissionRate(item.commissionRate())
                    .commissionAmount(item.commissionAmount())
                    .merchantIncome(item.merchantIncome())
                    .coinReceivableAmount(gross)
                    .settledAmount(money(settled))
                    .pendingAmount(money(pending))
                    .availableAmount(money(available))
                    .settlementStatus(status)
                    .build());
        }

        int safePage = Math.max(page, 1);
        int safePageSize = Math.max(pageSize, 1);
        int from = Math.min((safePage - 1) * safePageSize, allItems.size());
        int to = Math.min(from + safePageSize, allItems.size());

        return MerchantCoinReceivableResponse.builder()
                .grossAmount(income.coinReceivableAmount())
                .settledAmount(income.coinSettledAmount())
                .pendingAmount(income.coinPendingAmount())
                .availableAmount(income.coinAvailableAmount())
                .orderCount(coinOrders.size())
                .settledOrderCount(settledOrderCount)
                .items(allItems.subList(from, to))
                .page(safePage)
                .pageSize(safePageSize)
                .totalElements(allItems.size())
                .build();
    }

    private MerchantIncome emptyIncome() {
        return new MerchantIncome(BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO,
                BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, List.of());
    }

    private static BigDecimal money(BigDecimal value) {
        return nz(value).setScale(2, RoundingMode.HALF_UP);
    }

    private static BigDecimal nz(BigDecimal value) {
        return value != null ? value : BigDecimal.ZERO;
    }

    private static BigDecimal defaultDecimal(BigDecimal value) {
        return value != null ? value : BigDecimal.ZERO;
    }
}
