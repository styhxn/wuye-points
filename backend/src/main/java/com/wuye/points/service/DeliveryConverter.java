package com.wuye.points.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wuye.points.dto.delivery.MyDeliveryItemResponse;
import com.wuye.points.dto.delivery.PendingDeliveryItemResponse;
import com.wuye.points.entity.Delivery;
import com.wuye.points.entity.Merchant;
import com.wuye.points.entity.Order;
import com.wuye.points.util.DateTimeUtil;
import com.wuye.points.util.DisplayLabelUtil;
import com.wuye.points.util.PhoneUtil;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Map;

@Component
public class DeliveryConverter {

    private static final String MERCHANT_SPONSOR = "merchant";
    private static final String SETTLEMENT_PENDING = "pending";
    private static final ObjectMapper JSON_MAPPER = new ObjectMapper();

    public PendingDeliveryItemResponse toPendingItem(Delivery delivery,
                                                     Order order,
                                                     Merchant merchant,
                                                     int timeoutMinutes) {
        DeliverySettlement settlement = resolveSettlement(delivery, order);
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
                .residentId(order != null ? order.getResidentId() : null)
                .fee(defaultDecimal(delivery.getFee()))
                .originalDeliveryFee(settlement.originalDeliveryFee())
                .deliverySubsidySponsor(settlement.subsidySponsor())
                .deliverySubsidyAmount(settlement.subsidyAmount())
                .deliverySettlementBase(settlement.settlementBase())
                .platformDeliveryShare(defaultDecimal(delivery.getPlatformEarning()))
                .courierEarning(defaultDecimal(delivery.getCourierEarning()))
                .courierShare(defaultDecimal(delivery.getCourierEarning()))
                .settlementStatus(resolveSettlementStatus(delivery))
                .status(delivery.getStatus())
                .statusLabel(DisplayLabelUtil.deliveryStatusLabel(
                        DisplayLabelUtil.normalizeDeliveryStatus(delivery.getStatus())))
                .carrierType(delivery.getCarrierType())
                .merchantDeliveryFeeShare(delivery.getMerchantDeliveryFeeShare())
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
        DeliverySettlement settlement = resolveSettlement(delivery, order);
        String normalizedStatus = DisplayLabelUtil.normalizeDeliveryStatus(delivery.getStatus());
        String orderStatus = order != null ? order.getOrderStatus() : null;

        return MyDeliveryItemResponse.builder()
                .id(delivery.getId())
                .orderId(delivery.getOrderId())
                .orderNo(order != null ? order.getOrderNo() : null)
                .merchantId(order != null ? order.getMerchantId() : null)
                .merchantName(merchant != null ? merchant.getName() : null)
                .pickupAddress(delivery.getPickupAddress())
                .deliveryAddress(delivery.getDeliveryAddress())
                .contactPhone(contactPhone)
                .residentId(order != null ? order.getResidentId() : null)
                .fee(defaultDecimal(delivery.getFee()))
                .originalDeliveryFee(settlement.originalDeliveryFee())
                .deliverySubsidySponsor(settlement.subsidySponsor())
                .deliverySubsidyAmount(settlement.subsidyAmount())
                .deliverySettlementBase(settlement.settlementBase())
                .platformDeliveryShare(defaultDecimal(delivery.getPlatformEarning()))
                .courierEarning(defaultDecimal(delivery.getCourierEarning()))
                .courierShare(defaultDecimal(delivery.getCourierEarning()))
                .settlementStatus(resolveSettlementStatus(delivery))
                .status(delivery.getStatus())
                .statusLabel(DisplayLabelUtil.deliveryStatusLabel(normalizedStatus))
                .orderStatus(orderStatus)
                .orderStatusLabel(DisplayLabelUtil.orderStatusLabel(orderStatus))
                .carrierType(delivery.getCarrierType())
                .merchantDeliveryFeeShare(delivery.getMerchantDeliveryFeeShare())
                .acceptedAt(DateTimeUtil.toIsoUtc(delivery.getAcceptedAt()))
                .pickedAt(DateTimeUtil.toIsoUtc(delivery.getPickedAt()))
                .deliveredAt(DateTimeUtil.toIsoUtc(delivery.getDeliveredAt()))
                .createdAt(DateTimeUtil.toIsoUtc(delivery.getCreatedAt()))
                .proofImageUrls(parseStringArray(delivery.getProofImageUrls()))
                .completeRemark(delivery.getCompleteRemark())
                .build();
    }

    /** 配送结算链快照：原配送费 / 补贴 / 承担方 / 结算基数（订单快照优先，回退配送单） */
    public DeliverySettlement resolveSettlement(Delivery delivery, Order order) {
        BigDecimal fee = defaultDecimal(delivery.getFee());
        BigDecimal originalDeliveryFee = order != null && order.getOriginalDeliveryFee() != null
                ? order.getOriginalDeliveryFee() : fee;
        BigDecimal subsidyAmount = order != null && order.getDeliverySubsidyAmount() != null
                ? order.getDeliverySubsidyAmount()
                : originalDeliveryFee.subtract(fee).max(BigDecimal.ZERO);
        String sponsor = order != null && order.getDeliverySubsidySponsor() != null
                ? order.getDeliverySubsidySponsor()
                : (subsidyAmount.compareTo(BigDecimal.ZERO) > 0 ? MERCHANT_SPONSOR : "none");
        BigDecimal settlementBase = money(fee.add(subsidyAmount));
        return new DeliverySettlement(originalDeliveryFee, subsidyAmount, sponsor, settlementBase);
    }

    private String resolveSettlementStatus(Delivery delivery) {
        return StringUtils.hasText(delivery.getSettlementStatus())
                ? delivery.getSettlementStatus() : SETTLEMENT_PENDING;
    }

    private List<String> parseStringArray(String json) {
        if (!StringUtils.hasText(json)) {
            return List.of();
        }
        try {
            return JSON_MAPPER.readValue(json, new TypeReference<List<String>>() {
            });
        } catch (Exception e) {
            return List.of();
        }
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

    private BigDecimal money(BigDecimal value) {
        return defaultDecimal(value).setScale(2, RoundingMode.HALF_UP);
    }

    /** 配送结算链计算结果（用于响应组装） */
    public record DeliverySettlement(
            BigDecimal originalDeliveryFee,
            BigDecimal subsidyAmount,
            String subsidySponsor,
            BigDecimal settlementBase) {
    }
}