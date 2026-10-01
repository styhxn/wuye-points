package com.wuye.points.constant;

/**
 * 商家履约方式选择与自配分账 —— 常量定义（需求《商家履约方式选择与自配分账-后端管理端需求》）。
 *
 * <p>履约方式（{@code Order.fulfillmentMode}）：
 * <ul>
 *   <li>{@code pending_choice}：待商家选择履约方式（支付后默认中间态）</li>
 *   <li>{@code merchant_self}：商家自配</li>
 *   <li>{@code courier_hall}：平台配送（发布到抢单大厅）</li>
 * </ul>
 *
 * <p>承运类型（{@code Delivery.carrierType}）：
 * <ul>
 *   <li>{@code courier}：平台配送员承运</li>
 *   <li>{@code merchant}：商家自配承运</li>
 * </ul>
 */
public final class FulfillmentConstants {

    private FulfillmentConstants() {
    }

    /** 履约方式：待选择 */
    public static final String FULFILLMENT_MODE_PENDING_CHOICE = "pending_choice";
    /** 履约方式：商家自配 */
    public static final String FULFILLMENT_MODE_MERCHANT_SELF = "merchant_self";
    /** 履约方式：平台配送（抢单大厅） */
    public static final String FULFILLMENT_MODE_COURIER_HALL = "courier_hall";
    /** 履约方式：无需配送（团购商品，支付后走商家核实） */
    public static final String FULFILLMENT_MODE_NONE = "none";

    /** 承运类型：平台配送员 */
    public static final String CARRIER_TYPE_COURIER = "courier";
    /** 承运类型：商家自配 */
    public static final String CARRIER_TYPE_MERCHANT = "merchant";

    /** 配送单状态：待商家选择履约方式 */
    public static final String DELIVERY_STATUS_PENDING_CHOICE = "pending_choice";
    /** 配送单状态：商家自配待配送 */
    public static final String DELIVERY_STATUS_MERCHANT_SELF = "merchant_self";
    /** 配送单状态：商家自配配送中 */
    public static final String DELIVERY_STATUS_MERCHANT_DELIVERING = "merchant_delivering";

    /** 商家默认履约方式：未指定（走平台配送兜底） */
    public static final String DEFAULT_FULFILLMENT_UNSPECIFIED = "unspecified";

    /** 履约选择超时时长（分钟），超时未选默认走平台配送 */
    public static final int FULFILLMENT_CHOICE_TIMEOUT_MINUTES = 30;

    /** 审计动作类型 */
    public static final class AuditAction {
        public static final String CHOOSE = "choose";
        public static final String AUTO_CHOOSE = "auto_choose";
        public static final String TIMEOUT = "timeout";
        public static final String OVERRIDE = "override";
        private AuditAction() {
        }
    }
}
