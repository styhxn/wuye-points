package com.wuye.points.service;

import com.wuye.points.dto.delivery.MyDeliveryItemResponse;
import com.wuye.points.dto.delivery.PendingDeliveryItemResponse;
import com.wuye.points.entity.Delivery;
import com.wuye.points.entity.Merchant;
import com.wuye.points.entity.Order;
import com.wuye.points.util.DateTimeUtil;
import com.wuye.points.util.PhoneUtil;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Map;

@Component
public class DeliveryConverter {

    public PendingDeliveryItemResponse toPendingItem(Delivery delivery,
                                                     Order order,
                                                     Merchant merchant,
                                                     int timeoutMinutes) {
        return PendingDeliveryItemResponse.builder()
                .id(delivery.getId())
                .orderId(delivery.getOrderId())
                .orderNo(order != null ? order.getOrderNo() : null)
                .merchantId(order != null ? order.getMerchantId() : null)
                .merchantName(merchant != null ? merchant.getName() : null)
                .merchantAddress(merchant != null ? merchant.getAddress() : null)
                .pickupAddress(delivery.getPickupAddress())
                .deliveryAddress(delivery.getDeliveryAddress())
                .contactPhone(maskPhone(delivery.getContactPhone()))
                .fee(defaultDecimal(delivery.getFee()))
                .courierEarning(defaultDecimal(delivery.getCourierEarning()))
                .status(delivery.getStatus())
                .timeoutMinutes(timeoutMinutes)
                .createdAt(DateTimeUtil.toIsoUtc(delivery.getCreatedAt()))
                .build();
    }

    public MyDeliveryItemResponse toMyItem(Delivery delivery,
                                           Order order,
                                           Merchant merchant,
                                           boolean maskPhone) {
        String contactPhone = delivery.getContactPhone();
        if (maskPhone && contactPhone != null) {
            contactPhone = PhoneUtil.mask(contactPhone);
        }

        return MyDeliveryItemResponse.builder()
                .id(delivery.getId())
                .orderId(delivery.getOrderId())
                .orderNo(order != null ? order.getOrderNo() : null)
                .merchantId(order != null ? order.getMerchantId() : null)
                .merchantName(merchant != null ? merchant.getName() : null)
                .pickupAddress(delivery.getPickupAddress())
                .deliveryAddress(delivery.getDeliveryAddress())
                .contactPhone(contactPhone)
                .fee(defaultDecimal(delivery.getFee()))
                .courierEarning(defaultDecimal(delivery.getCourierEarning()))
                .status(delivery.getStatus())
                .acceptedAt(DateTimeUtil.toIsoUtc(delivery.getAcceptedAt()))
                .pickedAt(DateTimeUtil.toIsoUtc(delivery.getPickedAt()))
                .deliveredAt(DateTimeUtil.toIsoUtc(delivery.getDeliveredAt()))
                .createdAt(DateTimeUtil.toIsoUtc(delivery.getCreatedAt()))
                .build();
    }

    public Map<String, Order> indexOrders(java.util.List<Order> orders) {
        return orders.stream().collect(java.util.stream.Collectors.toMap(Order::getId, o -> o, (a, b) -> a));
    }

    public Map<String, Merchant> indexMerchants(java.util.List<Merchant> merchants) {
        return merchants.stream().collect(java.util.stream.Collectors.toMap(Merchant::getId, m -> m, (a, b) -> a));
    }

    private String maskPhone(String phone) {
        return phone != null ? PhoneUtil.mask(phone) : null;
    }

    private BigDecimal defaultDecimal(BigDecimal value) {
        return value != null ? value : BigDecimal.ZERO;
    }
}
