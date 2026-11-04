package com.wuye.points.payment;

/**
 * 支付通道标识（落库字段 {@code payment_records.pay_mode}）。
 *
 * <p>每一笔支付在预下单时即固化通道，后续查单 / 退款 / 关单 / 分账严格按通道路由，
 * 即使运营后续调整配置也不会把老流水发到错误的微信商户主体。</p>
 */
public final class WechatPayModes {

    /** 直连商户（平台自营收款，无分账）。 */
    public static final String DIRECT = "direct";

    /** 服务商普通分账（资金进商家特约商户号，按订单分账）。 */
    public static final String PARTNER = "partner";

    /** 电商收付通（资金进商家二级商户号，走 /v3/ecommerce/* 分账）。 */
    public static final String ECOMMERCE = "ecommerce";

    private WechatPayModes() {
    }

    /**
     * 解析流水实际通道：优先取落库值；历史流水按是否带二级商户号推断
     * （有 sub_mchid 视为服务商/收付通，按全局收付通开关区分；无则视为直连）。
     */
    public static String resolve(String storedMode, String subMchid, boolean ecommerceEnabled) {
        if (storedMode != null && !storedMode.isBlank()) {
            return storedMode.trim().toLowerCase();
        }
        if (subMchid != null && !subMchid.isBlank()) {
            return ecommerceEnabled ? ECOMMERCE : PARTNER;
        }
        return DIRECT;
    }
}
