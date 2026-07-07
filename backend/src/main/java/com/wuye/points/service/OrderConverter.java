package com.wuye.points.service;

import com.wuye.points.dto.order.OrderDetailResponse;
import com.wuye.points.dto.order.OrderItemSnapshotResponse;
import com.wuye.points.dto.order.OrderListItemResponse;
import com.wuye.points.entity.Merchant;
import com.wuye.points.entity.Order;
import com.wuye.points.entity.OrderItem;
import com.wuye.points.util.DateTimeUtil;
import com.wuye.points.util.PhoneUtil;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

@Component
public class OrderConverter {

    private final MerchantConverter merchantConverter;

    public OrderConverter(MerchantConverter merchantConverter) {
        this.merchantConverter = merchantConverter;
    }

    public OrderListItemResponse toListItem(Order order,
                                            Merchant merchant,
                                            List<OrderItem> items,
                                            int previewLimit) {
        List<OrderItem> displayItems = previewLimit > 0 && items.size() > previewLimit
                ? items.subList(0, previewLimit)
                : items;

        return OrderListItemResponse.builder()
                .id(order.getId())
                .orderNo(order.getOrderNo())
                .merchantId(order.getMerchantId())
                .merchantName(merchant != null ? merchant.getName() : null)
                .merchantCoverUrl(resolveMerchantCover(merchant))
                .orderStatus(order.getOrderStatus())
                .totalAmount(defaultDecimal(order.getTotalAmount()))
                .deliveryFee(defaultDecimal(order.getDeliveryFee()))
                .paymentMethod(order.getPaymentMethod())
                .pointUsed(defaultPoint(order.getPointUsed()))
                .coinUsed(defaultDecimal(order.getCoinUsed()))
                .cashAmount(defaultDecimal(order.getCashAmount()))
                .itemCount(sumQuantity(items))
                .items(displayItems.stream().map(this::toItemSnapshot).toList())
                .familyId(order.getFamilyId())
                .courierId(order.getCourierId())
                .paidAt(DateTimeUtil.toIsoUtc(order.getPaidAt()))
                .deliveredAt(DateTimeUtil.toIsoUtc(order.getDeliveredAt()))
                .completedAt(DateTimeUtil.toIsoUtc(order.getCompletedAt()))
                .createdAt(DateTimeUtil.toIsoUtc(order.getCreatedAt()))
                .build();
    }

    public OrderDetailResponse toDetail(Order order,
                                        Merchant merchant,
                                        List<OrderItem> items,
                                        boolean maskPhone) {
        OrderListItemResponse base = toListItem(order, merchant, items, 0);
        String contactPhone = order.getContactPhone();
        if (maskPhone && contactPhone != null) {
            contactPhone = PhoneUtil.mask(contactPhone);
        }

        return OrderDetailResponse.builder()
                .id(base.getId())
                .orderNo(base.getOrderNo())
                .merchantId(base.getMerchantId())
                .merchantName(base.getMerchantName())
                .merchantCoverUrl(base.getMerchantCoverUrl())
                .orderStatus(base.getOrderStatus())
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
                .residentId(order.getResidentId())
                .propertyCompanyId(order.getPropertyCompanyId())
                .deliveryAddress(order.getAddress())
                .contactPhone(contactPhone)
                .remark(order.getRemark())
                .cancelledAt(DateTimeUtil.toIsoUtc(order.getCancelledAt()))
                .version(order.getVersion())
                .updatedAt(DateTimeUtil.toIsoUtc(order.getUpdatedAt()))
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
