package com.wuye.points.service;

import com.wuye.points.context.UserContext;
import com.wuye.points.dto.admin.residentmerchant.ResidentMerchantSettlementItemResponse;
import com.wuye.points.dto.common.PageResponse;
import com.wuye.points.entity.DistributorProduct;
import com.wuye.points.entity.Order;
import com.wuye.points.entity.ResidentMerchantListing;
import com.wuye.points.entity.ResidentMerchantSettlement;
import com.wuye.points.exception.BizException;
import com.wuye.points.exception.ErrorCode;
import com.wuye.points.repository.DistributorProductRepository;
import com.wuye.points.repository.ResidentMerchantListingRepository;
import com.wuye.points.repository.ResidentMerchantSettlementRepository;
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
 * 业主商户卖货结算：
 * 订单完成（确认收货）时，若订单来自业主商户上架（listing），则生成卖货明细，
 * 按平台佣金比例扣除手续费，差价归业主商户，并自动下架该商品。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ResidentMerchantSettlementService {

    private final ResidentMerchantSettlementRepository settlementRepository;
    private final ResidentMerchantListingRepository listingRepository;
    private final DistributorProductRepository distributorProductRepository;
    private final PlatformSettingService platformSettingService;
    private final RoleGuard roleGuard;
    private final SnowflakeIdGenerator idGenerator;

    /**
     * 订单完成时调用：幂等，非 listing 订单直接跳过。
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

        BigDecimal orderAmount = defaultAmount(order.getTotalAmount());
        BigDecimal wholesale = defaultAmount(product.getWholesalePrice());
        BigDecimal commissionRate = platformSettingService.getResidentMerchantDistributorCommissionRate();
        BigDecimal platformCommission = orderAmount.multiply(commissionRate).setScale(2, RoundingMode.HALF_UP);
        BigDecimal resellerProfit = orderAmount.subtract(wholesale).subtract(platformCommission);
        if (resellerProfit.compareTo(BigDecimal.ZERO) < 0) {
            resellerProfit = BigDecimal.ZERO;
        }

        ResidentMerchantSettlement settlement = new ResidentMerchantSettlement();
        settlement.setId(idGenerator.generateId("rms"));
        settlement.setOrderId(order.getId());
        settlement.setListingId(listing.getId());
        settlement.setResidentId(listing.getResidentId());
        settlement.setDistributorMerchantId(product.getDistributorMerchantId());
        settlement.setOrderAmount(orderAmount);
        settlement.setWholesaleAmount(wholesale);
        settlement.setResellerProfit(resellerProfit);
        settlement.setPlatformCommission(platformCommission);
        settlement.setStatus("settled");
        settlement.setSettleAt(LocalDateTime.now());
        settlementRepository.save(settlement);

        // 产品卖出去后自动下架
        listing.setEnabled(false);
        listingRepository.save(listing);
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

    private ResidentMerchantSettlementItemResponse toItem(ResidentMerchantSettlement s) {
        return ResidentMerchantSettlementItemResponse.builder()
                .id(s.getId())
                .orderId(s.getOrderId())
                .listingId(s.getListingId())
                .residentId(s.getResidentId())
                .orderAmount(s.getOrderAmount())
                .resellerProfit(s.getResellerProfit())
                .status(s.getStatus())
                .settleAt(DateTimeUtil.toIsoUtc(s.getSettleAt()))
                .createdAt(DateTimeUtil.toIsoUtc(s.getCreatedAt()))
                .build();
    }

    private BigDecimal defaultAmount(BigDecimal value) {
        return value != null ? value : BigDecimal.ZERO;
    }
}
