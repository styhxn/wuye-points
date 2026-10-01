package com.wuye.points.payment;

/**
 * 支付流水业务类型（payment_records.biz_type）。
 */
public final class PaymentBizType {

    /** 商品/外卖订单 */
    public static final String ORDER = "order";

    /** 物业费缴纳 */
    public static final String PROPERTY_FEE = "property_fee";

    /** 物业币充值 */
    public static final String COIN_RECHARGE = "coin_recharge";

    /** 社区服务-预约 */
    public static final String SERVICE_ORDER = "service_order";

    /** 社区服务-订阅 */
    public static final String SERVICE_SUBSCRIPTION = "service_subscription";

    /** 活动组订阅 */
    public static final String ACTIVITY_SUBSCRIPTION = "activity_subscription";

    /** 业主商户保证金 */
    public static final String RESIDENT_SHOP_DEPOSIT = "resident_shop_deposit";

    /** 社区食堂-主商家购买充值额度 */
    public static final String QUOTA_PURCHASE = "quota_purchase";

    private PaymentBizType() {
    }
}
