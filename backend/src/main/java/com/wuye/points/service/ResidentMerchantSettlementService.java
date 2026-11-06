package com.wuye.points.service;

import com.wuye.points.context.UserContext;
import com.wuye.points.dto.admin.residentmerchant.ResidentMerchantSettlementItemResponse;
import com.wuye.points.dto.common.PageResponse;
import com.wuye.points.dto.residentmerchant.SellerWalletResponse;
import com.wuye.points.entity.DistributorProduct;
import com.wuye.points.entity.Order;
import com.wuye.points.entity.OrderItem;
import com.wuye.points.entity.Resident;
import com.wuye.points.entity.ResidentMerchantListing;
import com.wuye.points.entity.ResidentMerchantSettlement;
import com.wuye.points.entity.WalletFlow;
import com.wuye.points.exception.BizException;
import com.wuye.points.exception.ErrorCode;
import com.wuye.points.repository.DistributorProductRepository;
import com.wuye.points.repository.OrderItemRepository;
import com.wuye.points.repository.ResidentMerchantListingRepository;
import com.wuye.points.repository.ResidentMerchantSettlementRepository;
import com.wuye.points.repository.ResidentRepository;
import com.wuye.points.repository.WalletFlowRepository;
import com.wuye.points.repository.WithdrawalRecordRepository;
import com.wuye.points.util.DateTimeUtil;
import com.wuye.points.util.RoleGuard;
import com.wuye.points.util.SnowflakeIdGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 业主商户卖货结算（件数制）：
 * 订单完成（确认收货）时，若订单来自业主商户上架（listing），按件数结算——
 * 商品额=零售价×件数（订单 totalAmount），货款=进货价×件数归一级代理（merchant 钱包），
 * 平台佣金=商品额×佣金率，差价=商品额−货款−佣金归店主（resident_merchant 钱包）；
 * sold_quantity 累加，卖满（sold=took）自动下架并标记 sold_out_unlisted。
 * 退款时冲正：负向钱包流水 + 结算单置 reversed + sold_quantity 回退。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ResidentMerchantSettlementService {

    private final ResidentMerchantSettlementRepository settlementRepository;
    private final ResidentMerchantListingRepository listingRepository;
    private final DistributorProductRepository distributorProductRepository;
    private final WithdrawalRecordRepository withdrawalRecordRepository;
    private final OrderItemRepository orderItemRepository;
    private final WalletFlowRepository walletFlowRepository;
    private final WalletFlowWriter walletFlowWriter;
    private final PlatformSettingService platformSettingService;
    private final ResidentRepository residentRepository;
    private final RoleWithdrawableService roleWithdrawableService;
    private final RoleGuard roleGuard;
    private final SnowflakeIdGenerator idGenerator;

    /** 业主卖货钱包提现类型（与 RoleWithdrawalService.WITHDRAWAL_TYPE_RESIDENT_SELLER 保持一致） */
    private static final String WITHDRAWAL_TYPE_RESIDENT_SELLER = "resident_seller";

    /** 店主差价入账流水类型 */
    private static final String ENTRY_RESELLER_PROFIT = "reseller_profit";
    /** 一级代理货款入账流水类型（商家钱包聚合口径，见 WithdrawableService） */
    private static final String ENTRY_DISTRIBUTOR_WHOLESALE_INCOME = "distributor_wholesale_income";

    /**
     * 订单完成时调用：幂等（existsByOrderId + 流水幂等键），非 listing 订单直接跳过。
     */
    @Transactional
    public void settleOnOrderCompleted(Order order) {
        if (order == null || order.getListingId() == null) {
            return;
        }
        if (settlementRepository.existsByOrderId(order.getId())) {
            return;
        }
        ResidentMerchantListing listing = listingRepository.findById(order.getListingId()).orElse(null);
        if (listing == null) {
            log.warn("[ResidentMerchantSettle] 上架记录不存在 orderId={} listingId={}",
                    order.getId(), order.getListingId());
            return;
        }
        DistributorProduct product =
                distributorProductRepository.findById(listing.getDistributorProductId()).orElse(null);
        if (product == null) {
            log.warn("[ResidentMerchantSettle] 一级货源商品不存在 listingId={}", listing.getId());
            return;
        }

        // 件数取订单明细（listing 订单单 item，dpr_ 货源商品）
        int quantity = resolveOrderQuantity(order.getId());
        if (quantity <= 0) {
            log.warn("[ResidentMerchantSettle] 订单明细件数异常 orderId={} quantity={}", order.getId(), quantity);
            return;
        }

        // 商品额=零售价×件数（不含配送费，即 order.totalAmount）；货款=进货价×件数
        BigDecimal productAmount = defaultAmount(order.getTotalAmount());
        BigDecimal wholesaleUnit = defaultAmount(product.getWholesalePrice());
        BigDecimal wholesaleAmount = wholesaleUnit.multiply(BigDecimal.valueOf(quantity))
                .setScale(2, RoundingMode.HALF_UP);
        BigDecimal commissionRate = platformSettingService.getResidentMerchantDistributorCommissionRate();
        BigDecimal platformCommission = productAmount.multiply(commissionRate).setScale(2, RoundingMode.HALF_UP);
        BigDecimal resellerProfit = productAmount.subtract(wholesaleAmount).subtract(platformCommission);
        if (resellerProfit.compareTo(BigDecimal.ZERO) < 0) {
            resellerProfit = BigDecimal.ZERO;
        }

        ResidentMerchantSettlement settlement = new ResidentMerchantSettlement();
        settlement.setId(idGenerator.generateId("rms"));
        settlement.setOrderId(order.getId());
        settlement.setListingId(listing.getId());
        settlement.setResidentId(listing.getResidentId());
        settlement.setDistributorMerchantId(product.getDistributorMerchantId());
        settlement.setOrderAmount(productAmount);
        settlement.setWholesaleAmount(wholesaleAmount);
        settlement.setResellerProfit(resellerProfit);
        settlement.setPlatformCommission(platformCommission);
        settlement.setQuantity(quantity);
        settlement.setStatus("settled");
        settlement.setSettleAt(LocalDateTime.now());
        settlementRepository.save(settlement);

        // 店主差价入账（业主卖货钱包，独立事务写流水，幂等键保证重复结算不重复入账）
        walletFlowWriter.write(null, order.getId(), null,
                "resident_merchant", listing.getResidentId(), ENTRY_RESELLER_PROFIT,
                resellerProfit, "withdrawable",
                "settlement:listing:" + order.getId() + ":reseller", null);
        // 一级代理货款入账（merchant 钱包，entryType=distributor_wholesale_income）
        walletFlowWriter.write(null, order.getId(), null,
                "merchant", product.getDistributorMerchantId(), ENTRY_DISTRIBUTOR_WHOLESALE_INCOME,
                wholesaleAmount, "withdrawable",
                "settlement:listing:" + order.getId() + ":distributor", null);

        // 已卖件数累加；卖满（sold=took）自动下架并标记卖完下架（替代原"卖1单即下架"）
        int sold = (listing.getSoldQuantity() == null ? 0 : listing.getSoldQuantity()) + quantity;
        listing.setSoldQuantity(sold);
        int took = listing.getTookQuantity() == null ? 0 : listing.getTookQuantity();
        if (took > 0 && sold >= took) {
            listing.setEnabled(false);
            listing.setSoldOutUnlisted(true);
        }
        listingRepository.save(listing);
    }

    /**
     * 退款冲正（auditRefund 通过后调用）：
     * 已结算（settled）的结算单写 2 条负向钱包流水（幂等键加 :refund，refRecordId 指向原流水）、
     * 结算单置 reversed、sold_quantity 回退件数；未结算的订单退款无需冲正（件数恢复由库存恢复链路处理）。
     */
    @Transactional
    public void refundOnOrderRefunded(Order order) {
        if (order == null || order.getListingId() == null) {
            return;
        }
        ResidentMerchantSettlement settlement = settlementRepository.findFirstByOrderId(order.getId())
                .orElse(null);
        if (settlement == null || !"settled".equals(settlement.getStatus())) {
            return;
        }
        int quantity = settlement.getQuantity() == null ? 0 : settlement.getQuantity();

        // 负向冲正流水：店主差价
        String resellerKey = "settlement:listing:" + order.getId() + ":reseller";
        WalletFlow resellerFlow = walletFlowRepository.findByIdempotencyKey(resellerKey).orElse(null);
        walletFlowWriter.write(null, order.getId(), null,
                "resident_merchant", settlement.getResidentId(), ENTRY_RESELLER_PROFIT,
                settlement.getResellerProfit().negate(), "withdrawable",
                resellerKey + ":refund", resellerFlow != null ? resellerFlow.getId() : null);
        // 负向冲正流水：一级代理货款
        String distributorKey = "settlement:listing:" + order.getId() + ":distributor";
        WalletFlow distributorFlow = walletFlowRepository.findByIdempotencyKey(distributorKey).orElse(null);
        walletFlowWriter.write(null, order.getId(), null,
                "merchant", settlement.getDistributorMerchantId(), ENTRY_DISTRIBUTOR_WHOLESALE_INCOME,
                settlement.getWholesaleAmount().negate(), "withdrawable",
                distributorKey + ":refund", distributorFlow != null ? distributorFlow.getId() : null);

        settlement.setStatus("reversed");
        settlementRepository.save(settlement);

        // 已卖件数回退（可售件数的恢复由库存恢复链路 restoreStock→ListingStockService 完成）
        listingRepository.findById(settlement.getListingId()).ifPresent(listing -> {
            int sold = listing.getSoldQuantity() == null ? 0 : listing.getSoldQuantity();
            listing.setSoldQuantity(Math.max(sold - quantity, 0));
            listingRepository.save(listing);
        });
        log.info("[ResidentMerchantSettle] 退款冲正完成 orderId={} quantity={}", order.getId(), quantity);
    }

    /** 解析订单的 listing 件数（dpr_ 明细总件数）。 */
    private int resolveOrderQuantity(String orderId) {
        List<OrderItem> items = orderItemRepository.findByOrderId(orderId);
        return items.stream()
                .filter(item -> item.getProductId() != null && item.getProductId().startsWith("dpr_"))
                .mapToInt(item -> item.getQuantity() == null ? 0 : item.getQuantity())
                .sum();
    }

    /**
     * 我的卖货明细（业主商户）。
     */
    @Transactional(readOnly = true)
    public PageResponse<ResidentMerchantSettlementItemResponse> listMySales(int page, int pageSize) {
        roleGuard.requireAnyRole("resident");
        int size = Math.min(Math.max(pageSize, 1), 100);
        Pageable pageable = PageRequest.of(Math.max(page, 1) - 1, size,
                Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<ResidentMerchantSettlement> result =
                settlementRepository.findByResidentIdOrderByCreatedAtDesc(UserContext.currentUserId(), pageable);
        List<ResidentMerchantSettlementItemResponse> list = result.getContent().stream()
                .map(this::toItem)
                .toList();
        return PageResponse.of(list, page, pageSize, result.getTotalElements());
    }

    /**
     * 业主卖货独立钱包概览：钱包金额来源于业主商城卖货收益，仅可用于提现。
     * 可提现余额取账号统一收益账户（与楼长/商家共用同一余额），口径与提现校验保持一致；
     * totalProfit / withdrawn / pending 仍是卖货这一路的分项统计。
     */
    @Transactional(readOnly = true)
    public SellerWalletResponse getSellerWallet() {
        roleGuard.requireAnyRole("resident");
        String residentId = UserContext.currentUserId();
        Resident resident = residentRepository.findById(residentId)
                .orElseThrow(() -> new BizException(ErrorCode.RESIDENT_NOT_FOUND));

        BigDecimal totalProfit = defaultAmount(settlementRepository.sumResellerProfitByResidentId(residentId));
        BigDecimal withdrawn = defaultAmount(withdrawalRecordRepository
                .sumAmountByApplicant(residentId, WITHDRAWAL_TYPE_RESIDENT_SELLER, "completed"));
        BigDecimal pending = defaultAmount(withdrawalRecordRepository
                .sumAmountByApplicant(residentId, WITHDRAWAL_TYPE_RESIDENT_SELLER, "pending"))
                .add(defaultAmount(withdrawalRecordRepository
                        .sumAmountByApplicant(residentId, WITHDRAWAL_TYPE_RESIDENT_SELLER, "approved")));

        RoleWithdrawableService.RoleBalance balance =
                roleWithdrawableService.forResidentSeller(residentId, resident.getPropertyCompanyId());

        return SellerWalletResponse.builder()
                .residentId(residentId)
                .totalProfit(totalProfit)
                .withdrawnAmount(withdrawn)
                .pendingAmount(pending)
                .withdrawableAmount(balance.withdrawableAmount())
                .feeRate(balance.feeRate())
                .withdrawFeeBearer(RoleWithdrawableService.FEE_BEARER_PAYEE)
                .build();
    }

    private ResidentMerchantSettlementItemResponse toItem(ResidentMerchantSettlement s) {
        return ResidentMerchantSettlementItemResponse.builder()
                .id(s.getId())
                .orderId(s.getOrderId())
                .listingId(s.getListingId())
                .residentId(s.getResidentId())
                .orderAmount(s.getOrderAmount())
                .resellerProfit(s.getResellerProfit())
                .commissionAmount(s.getResellerProfit())
                .distributorMerchantId(s.getDistributorMerchantId())
                .sourceType("distributor")
                .settlementOwner("distributor")
                .status(s.getStatus())
                .settleAt(DateTimeUtil.toIsoUtc(s.getSettleAt()))
                .createdAt(DateTimeUtil.toIsoUtc(s.getCreatedAt()))
                .build();
    }

    private BigDecimal defaultAmount(BigDecimal value) {
        return value != null ? value : BigDecimal.ZERO;
    }
}
