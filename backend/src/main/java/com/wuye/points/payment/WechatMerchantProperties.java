package com.wuye.points.payment;

/**
 * 微信支付「商户通道」公共配置契约。
 *
 * <p>平台同时接入两个真实微信商户主体，二者是<b>完全独立</b>的账号（不同商户号、API 证书、APIv3 密钥、
 * 绑定 AppID 与结算账户），不能混用凭据：</p>
 * <ul>
 *   <li>{@link WechatPayProperties}（{@code wechat.pay}）—— <b>服务商</b>通道（sp_mchid），
 *       用于商城订单（资金进商家特约商户号）与官方分账 {@code /v3/profitsharing/*}；</li>
 *   <li>{@code WechatDirectProperties}（{@code wechat.direct}）—— <b>直连商户</b>通道，
 *       用于平台自营收款（物业费/充值/食堂/广告/服务预约等，资金进平台商户号）。</li>
 * </ul>
 *
 * <p>两个通道各自持有独立的 {@code WechatHttpClient} / {@code WechatCertManager} / {@code WechatNotifyService}，
 * 请求签名、回调验签、APIv3 解密均按流水落库的 {@code pay_mode} 路由，禁止跨通道复用凭据。</p>
 *
 * <p><b>回调验签两种模式</b>：老商户使用「平台证书」（{@code /v3/certificates} 动态下载，序列号形如 40 位十六进制）；
 * 2024 年后新开商户默认使用「微信支付公钥」（序列号形如 {@code PUB_KEY_ID_*}）。
 * 配置了 {@code public-key-id} + 公钥文件即进入公钥模式，不再请求平台证书接口。</p>
 */
public interface WechatMerchantProperties {

    /** 该通道总开关。 */
    boolean isEnabled();

    /** 商户号：服务商通道为 sp_mchid，直连通道为普通商户号。 */
    String getMchId();

    /** APIv3 密钥（32 字符，回调解密用）。 */
    String getApiV3Key();

    /** 商户 API 私钥文件路径（PEM）。 */
    String getPrivateKeyPath();

    /** 商户 API 私钥（Base64/PEM 文本，与 private-key-path 二选一）。 */
    String getPrivateKeyBase64();

    /** 商户 API 证书序列号（请求签名头 serial_no）。 */
    String getMerchantSerialNumber();

    /** 绑定的小程序 AppID。 */
    String getAppId();

    /** 支付结果异步通知地址。 */
    String getNotifyUrl();

    /** 退款结果异步通知地址。 */
    String getRefundNotifyUrl();

    /** 微信支付网关。 */
    String getGateway();

    /** 平台证书刷新间隔（毫秒；公钥模式无意义）。 */
    long getCertRefreshMillis();

    /** 微信支付公钥 ID（形如 PUB_KEY_ID_*），配置后进入公钥验签模式。 */
    String getPublicKeyId();

    /** 微信支付公钥文件路径（PEM）。 */
    String getPublicKeyPath();

    /** 微信支付公钥（Base64/PEM 文本，与 public-key-path 二选一）。 */
    String getPublicKeyBase64();

    /** 是否已具备发起真实微信调用的最低配置。 */
    boolean isConfigured();

    /** 是否使用「微信支付公钥」模式验签（新商户默认；否则回退平台证书）。 */
    default boolean isPublicKeyMode() {
        return notBlank(getPublicKeyId())
                && (notBlank(getPublicKeyPath()) || notBlank(getPublicKeyBase64()));
    }

    private static boolean notBlank(String s) {
        return s != null && !s.isBlank();
    }
}
