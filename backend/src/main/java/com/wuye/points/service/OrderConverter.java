package com.wuye.points.service;

import com.wuye.points.dto.order.OrderDetailResponse;
import com.wuye.points.dto.order.OrderItemSnapshotResponse;
import com.wuye.points.dto.order.OrderListItemResponse;
import com.wuye.points.entity.Delivery;
import com.wuye.points.entity.Merchant;
import com.wuye.points.entity.Order;
import com.wuye.points.entity.OrderItem;
import com.wuye.points.repository.DeliveryRepository;
import com.wuye.points.util.DateTimeUtil;
import com.wuye.points.util.DisplayLabelUtil;
import com.wuye.points.util.PhoneUtil;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

@Component
public class OrderConverter {

    private final MerchantConverter merchantConverter;
    private final DeliveryRepository deliveryRepository;

    public OrderConverter(MerchantConverter merchantConverter, DeliveryRepository deliveryRepository) {
        this.merchantConverter = merchantConverter;
        this.deliveryRepository = deliveryRepository;
    }

    public OrderListItemResponse toListItem(Order order,
                                            Merchant merchant,
                                            List<OrderItem> items,
                                            int previewLimit) {
        // 无批量传入配送单时的兼容重载：按订单单查一次（详情页等单笔场景）
        return toListItem(order, merchant, items, previewLimit,
                deliveryRepository.findByOrderId(order.getId()).orElse(null));
    }

    /**
     * 订单列表项转换（业务状态与物流状态分离）。
     *
     * <p>{@code delivery} 由调用方按订单批量加载后传入（避免 N+1）；
     * 为 {@code null} 表示本单无配送单（团购/食堂单、未选履约方式），配送状态字段留空。</p>
     */
    public OrderListItemResponse toListItem(Order order,
                                            Merchant merchant,
                                            List<OrderItem> items,
                                            int previewLimit,
                                            Delivery delivery) {
        List<OrderItem> displayItems = previewLimit > 0 && items.size() > previewLimit
                ? items.subList(0, previewLimit)
                : items;
        String deliveryStatusCode = delivery != null
                ? DisplayLabelUtil.normalizeDeliveryStatus(delivery.getStatus()) : null;

        return OrderListItemResponse.builder()
                .id(order.getId())
                .orderNo(order.getOrderNo())
                .merchantId(order.getMerchantId())
                .merchantName(merchant != null ? merchant.getName() : null)
                .merchantCoverUrl(resolveMerchantCover(merchant))
                .orderStatus(order.getOrderStatus())
                .refundStatus(order.getRefundStatus())
                .refundFailReason(order.getRefundFailReason())
                .cancelledBy(order.getCancelledBy())
                .deliveryStatusCode(deliveryStatusCode)
                .deliveryStatusLabel(DisplayLabelUtil.deliveryStatusLabel(deliveryStatusCode))
                .totalAmount(defaultDecimal(order.getTotalAmount()))
                .deliveryFee(defaultDecimal(order.getDeliveryFee()))
                .paymentMethod(order.getPaymentMethod())
                .pointUsed(defaultPoint(order.getPointUsed()))
                .coinUsed(defaultDecimal(order.getCoinUsed()))
                .cashAmount(defaultDecimal(order.getCashAmount()))
                .couponId(order.getCouponId())
                .couponAmount(defaultDecimal(order.getCouponAmount()))
                .itemCount(sumQuantity(items))
                .items(displayItems.stream().map(this::toItemSnapshot).toList())
                .familyId(order.getFamilyId())
                .courierId(order.getCourierId())
                .paidAt(DateTimeUtil.toIsoUtc(order.getPaidAt()))
                .deliveredAt(DateTimeUtil.toIsoUtc(order.getDeliveredAt()))
                .completedAt(DateTimeUtil.toIsoUtc(order.getCompletedAt()))
                .createdAt(DateTimeUtil.toIsoUtc(order.getCreatedAt()))
                .requiresDelivery(order.getRequiresDelivery() == null ? Boolean.TRUE : order.getRequiresDelivery())
                .fulfillmentMode(order.getFulfillmentMode())
                .fulfillmentModeLabel(DisplayLabelUtil.fulfillmentModeLabel(order.getFulfillmentMode()))
                .fulfillmentDeadline(DateTimeUtil.toIsoUtc(order.getFulfillmentDeadline()))
                .fulfillmentChosenAt(DateTimeUtil.toIsoUtc(order.getFulfillmentChosenAt()))
                .sourceType(order.getSourceType() != null ? order.getSourceType() : "mall")
                .distributorId(order.getDistributorId())
                .residentMerchantId(order.getResidentMerchantId())
                .settlementOwner(order.getSettlementOwner() != null ? order.getSettlementOwner() : "merchant")
                .build();
    }

    public OrderDetailResponse toDetail(Order order,
                                        Merchant merchant,
                                        List<OrderItem> items,
                                        boolean maskPhone) {
        return toDetail(order, merchant, items, maskPhone,
                deliveryRepository.findByOrderId(order.getId()).orElse(null));
    }

    /**
     * 订单详情转换。{@code delivery} 由调用方传入（单笔详情按订单单查一次）；
     * 为 {@code null} 表示本单无配送单，配送相关字段留空。
     */
    public OrderDetailResponse toDetail(Order order,
                                        Merchant merchant,
                                        List<OrderItem> items,
                                        boolean maskPhone,
                                        Delivery delivery) {
        OrderListItemResponse base = toListItem(order, merchant, items, 0, delivery);
        String contactPhone = order.getContactPhone();
        if (maskPhone && contactPhone != null) {
            contactPhone = PhoneUtil.mask(contactPhone);
        }
        String deliveryStatusCode = delivery != null
                ? DisplayLabelUtil.normalizeDeliveryStatus(delivery.getStatus()) : null;

        return OrderDetailResponse.builder()
                .id(base.getId())
                .orderNo(base.getOrderNo())
                .merchantId(base.getMerchantId())
                .merchantName(base.getMerchantName())
                .merchantCoverUrl(base.getMerchantCoverUrl())
                .orderStatus(base.getOrderStatus())
                .refundStatus(base.getRefundStatus())
                .refundFailReason(base.getRefundFailReason())
                .cancelledBy(base.getCancelledBy())
                .deliveryStatusCode(base.getDeliveryStatusCode())
                .deliveryStatusLabel(base.getDeliveryStatusLabel())
                .totalAmount(base.getTotalAmount())
                .deliveryFee(base.getDeliveryFee())
                .paymentMethod(base.getPaymentMethod())
                .pointUsed(base.getPointUsed())
                .coinUsed(base.getCoinUsed())
                .cashAmount(base.getCashAmount())
                .itemCount(base.getItemCount())
                .items(items.stream().map(this::toItemSnapshot).toList())
                .familyId(base.getFamilyId())
                .courierId(base.getCourierId())
                .paidAt(base.getPaidAt())
                .deliveredAt(base.getDeliveredAt())
                .completedAt(base.getCompletedAt())
                .createdAt(base.getCreatedAt())
                .requiresDelivery(base.getRequiresDelivery())
                .fulfillmentMode(base.getFulfillmentMode())
                .fulfillmentModeLabel(base.getFulfillmentModeLabel())
                .fulfillmentDeadline(base.getFulfillmentDeadline())
                .fulfillmentChosenAt(base.getFulfillmentChosenAt())
                .residentId(order.getResidentId())
                .propertyCompanyId(order.getPropertyCompanyId())
                .deliveryAddress(order.getAddress())
                .contactPhone(contactPhone)
                .remark(order.getRemark())
                .cancelledAt(DateTimeUtil.toIsoUtc(order.getCancelledAt()))
                .cancelReason(order.getCancelReason())
                .version(order.getVersion())
                .updatedAt(DateTimeUtil.toIsoUtc(order.getUpdatedAt()))
                .deliveryId(delivery != null ? delivery.getId() : null)
                .carrierType(delivery != null ? delivery.getCarrierType() : null)
                .deliveryDeliveredAt(DateTimeUtil.toIsoUtc(
                        delivery != null ? delivery.getDeliveredAt() : null))
                .build();
    }

    public OrderItemSnapshotResponse toItemSnapshot(OrderItem item) {
        return OrderItemSnapshotResponse.builder()
                .productId(item.getProductId())
                .productName(item.getProductName())
                .productCover(item.getProductCover())
                .quantity(item.getQuantity())
                .unitPrice(item.getUnitPrice())
                .totalPrice(item.getTotalPrice())
                .build();
    }

    public String resolveMerchantCover(Merchant merchant) {
        if (merchant == null || merchant.getCoverUrls() == null) {
            return null;
        }
        List<String> covers = merchantConverter.parseCoverUrls(merchant.getCoverUrls());
        return covers.isEmpty() ? null : covers.get(0);
    }

    private int sumQuantity(List<OrderItem> items) {
        if (items == null || items.isEmpty()) {
            return 0;
        }
        return items.stream().mapToInt(OrderItem::getQuantity).sum();
    }

    private BigDecimal defaultDecimal(BigDecimal value) {
        return value != null ? value : BigDecimal.ZERO;
    }

    private Integer defaultPoint(Integer value) {
        return value != null ? value : 0;
    }
}
