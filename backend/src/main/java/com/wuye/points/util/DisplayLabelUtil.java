package com.wuye.points.util;

import org.springframework.util.StringUtils;

import java.util.Map;

/**
 * 管理端/订单展示用中文标签（保留原 code 字段供逻辑判断）
 */
public final class DisplayLabelUtil {

    private static final Map<String, String> PAYMENT_METHOD_LABELS = Map.of(
            "wechat", "微信支付",
            "point", "积分支付",
            "property_coin", "物业币支付",
            "mixed", "混合支付");

    private static final Map<String, String> DELIVERY_STATUS_LABELS = Map.of(
            "pending", "待抢单",
            "accepted", "已接单",
            "picked", "已取货",
            "delivering", "配送中",
            "completed", "已完成",
            "delivered", "已完成",
            "failed", "配送失败",
            "property_fallback", "物业兜底",
            "merchant_self", "商家自配中");

    private static final Map<String, String> FULFILLMENT_MODE_LABELS = Map.of(
            "pending_choice", "待选择",
            "merchant_self", "商家自配",
            "courier_hall", "平台配送",
            "none", "无需配送");

    private static final Map<String, String> COURIER_ONLINE_LABELS = Map.of(
            "online", "在线",
            "offline", "离线");

    private static final Map<String, String> RECENT_ACTIVITY_TYPE_LABELS = Map.of(
            "order", "新订单",
            "new_resident", "新业主入驻");

    private static final Map<String, String> ORDER_STATUS_LABELS = Map.of(
            "pending", "待支付",
            "paid", "已支付",
            "delivering", "配送中",
            "completed", "已完成",
            "cancelled", "已取消",
            "refunding", "退款中",
            "refunded", "已退款",
            "refund_rejected", "已驳回");

    private static final Map<String, String> LEADER_STATUS_LABELS = Map.of(
            "active", "启用",
            "inactive", "停用");

    private static final Map<String, String> PRODUCT_STATUS_LABELS = Map.of(
            "active", "在售",
            "inactive", "已下架",
            "sold_out", "已售罄");

    private DisplayLabelUtil() {
    }

    public static String orderStatusLabel(String orderStatus) {
        if (!StringUtils.hasText(orderStatus)) {
            return null;
        }
        return ORDER_STATUS_LABELS.getOrDefault(orderStatus, orderStatus);
    }

    public static String productStatusLabel(String status) {
        if (!StringUtils.hasText(status)) {
            return null;
        }
        return PRODUCT_STATUS_LABELS.getOrDefault(status, status);
    }

    public static String leaderStatusLabel(String status) {
        if (!StringUtils.hasText(status)) {
            return null;
        }
        return LEADER_STATUS_LABELS.getOrDefault(status, status);
    }

    /** 展示用：优先返回中文标签，无映射则原样返回 */
    public static String displayOrCode(String code, String label) {
        return StringUtils.hasText(label) ? label : code;
    }

    public static String paymentMethodLabel(String paymentMethod) {
        if (!StringUtils.hasText(paymentMethod)) {
            return null;
        }
        return PAYMENT_METHOD_LABELS.getOrDefault(paymentMethod, paymentMethod);
    }

    /**
     * 对外统一配送状态 code（历史数据 delivered 归一为 completed）
     */
    public static String normalizeDeliveryStatus(String status) {
        if (!StringUtils.hasText(status)) {
            return status;
        }
        if ("delivered".equals(status)) {
            return "completed";
        }
        return status;
    }

    public static String deliveryStatusLabel(String status) {
        if (!StringUtils.hasText(status)) {
            return null;
        }
        return DELIVERY_STATUS_LABELS.getOrDefault(status, status);
    }

    public static String courierOnlineLabel(String status) {
        if (!StringUtils.hasText(status)) {
            return null;
        }
        return COURIER_ONLINE_LABELS.getOrDefault(status, status);
    }

    public static String fulfillmentModeLabel(String mode) {
        if (!StringUtils.hasText(mode)) {
            return null;
        }
        return FULFILLMENT_MODE_LABELS.getOrDefault(mode, mode);
    }

    public static String recentActivityTypeLabel(String type) {
        if (!StringUtils.hasText(type)) {
            return null;
        }
        return RECENT_ACTIVITY_TYPE_LABELS.getOrDefault(type, type);
    }
}
