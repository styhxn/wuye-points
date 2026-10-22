package com.wuye.points.service;

import com.wuye.points.entity.Order;
import com.wuye.points.entity.OrderItem;
import com.wuye.points.entity.ResidentMerchantListing;
import com.wuye.points.exception.BizException;
import com.wuye.points.exception.ErrorCode;
import com.wuye.points.repository.OrderItemRepository;
import com.wuye.points.repository.OrderRepository;
import com.wuye.points.repository.ResidentMerchantListingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 业主小店上架商品（listing）件数锁定/恢复。
 *
 * <p>件数口径：took_quantity = sold_quantity + 支付锁定中件数 + quantity_remaining。
 * 买家支付成功（含微信预下单锁定）时扣 quantity_remaining；取消/超时/退款时加回。
 * 幂等由订单状态机保证：一单最多支付一次、取消/退款一次（与商城库存 ProductStockService 模式一致）。
 */
@Service
@RequiredArgsConstructor
public class ListingStockService {

    private final ResidentMerchantListingRepository listingRepository;
    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;

    /**
     * 按订单恢复 listing 件数（支付超时自动取消兜底）：非 listing 订单空操作。
     */
    @Transactional
    public void restoreByOrderId(String orderId) {
        Order order = orderRepository.findById(orderId).orElse(null);
        if (order == null || order.getListingId() == null) {
            return;
        }
        int quantity = orderItemRepository.findByOrderId(orderId).stream()
                .filter(item -> item.getProductId() != null && item.getProductId().startsWith("dpr_"))
                .mapToInt(item -> item.getQuantity() == null ? 0 : item.getQuantity())
                .sum();
        restoreForOrder(order.getListingId(), quantity);
    }

    /**
     * 支付成功（含微信预下单预占）：锁定件数，可售余量扣减。
     */
    @Transactional
    public void deductForOrder(String listingId, int quantity) {
        if (quantity <= 0) {
            return;
        }
        ResidentMerchantListing listing = getListing(listingId);
        int remaining = listing.getQuantityRemaining() == null ? 0 : listing.getQuantityRemaining();
        if (remaining < quantity) {
            throw new BizException(ErrorCode.LISTING_STOCK_INSUFFICIENT);
        }
        listing.setQuantityRemaining(remaining - quantity);
        listingRepository.save(listing);
    }

    /**
     * 取消/超时/退款：恢复件数到可售余量。
     * 因卖完自动下架（sold_out_unlisted=true）且恢复后有余量时自动重新上架。
     */
    @Transactional
    public void restoreForOrder(String listingId, int quantity) {
        if (quantity <= 0) {
            return;
        }
        ResidentMerchantListing listing = getListing(listingId);
        int remaining = listing.getQuantityRemaining() == null ? 0 : listing.getQuantityRemaining();
        listing.setQuantityRemaining(remaining + quantity);
        if (Boolean.TRUE.equals(listing.getSoldOutUnlisted()) && listing.getQuantityRemaining() > 0) {
            listing.setSoldOutUnlisted(false);
            listing.setEnabled(true);
        }
        listingRepository.save(listing);
    }

    private ResidentMerchantListing getListing(String listingId) {
        return listingRepository.findById(listingId)
                .orElseThrow(() -> new BizException(ErrorCode.PRODUCT_NOT_FOUND, "上架商品不存在"));
    }
}
