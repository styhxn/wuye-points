package com.wuye.points.payment;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 微信支付<b>直连商户</b>通道配置，绑定 application.yml 的 {@code wechat.direct} 前缀。
 *
 * <p>用于平台自营业务收款（物业费、物业币充值、食堂充值、商家广告、服务预约/订阅、活动组订阅、
 * 业主商户保证金等）——这些业务不涉及给商家分账，资金直接进平台自己的直连商户号。</p>
 *
 * <p>与 {@link WechatPayProperties}（服务商 sp_mchid）是两套完全独立的商户主体：
 * 证书、APIv3 密钥、API 证书序列号、回调地址均不可互换。缺省关闭（fail-closed），
 * 未配置时平台自营在线支付会被明确拒绝，而不是错误地落到服务商号上。</p>
 */
@Data
@Component
@ConfigurationProperties(prefix = "wechat.direct")
public class WechatDirectProperties implements WechatMerchantProperties {

    /** 直连商户通道开关（接入真实直连商户号后置 true）。 */
    private boolean enabled;

    /** 直连商户号。 */
    private String mchId;

    /** 微信 APIv3 密钥（32 字符，回调解密用）。 */
    private String apiV3Key;

    /** 商户 API 私钥文件路径（PEM）。 */
    private String privateKeyPath;

    /** 商户 API 私钥（Base64/PEM 文本，与 private-key-path 二选一）。 */
    private String privateKeyBase64;

    /** 商户 API 证书序列号（请求签名头 serial_no）。 */
    private String merchantSerialNumber;

    /** 绑定的小程序 AppID。 */
    private String appId;

    /** 支付结果异步通知地址（须指向 /payment/notify/wechatpay-direct）。 */
    private String notifyUrl;

    /** 退款结果异步通知地址。 */
    private String refundNotifyUrl;

    /** 微信支付网关（默认官方地址）。 */
    private String gateway = "https://api.mch.weixin.qq.com";

    /** 平台证书刷新间隔（毫秒）。 */
    private long certRefreshMillis = 3_600_000L;

    /** 微信支付公钥 ID（形如 PUB_KEY_ID_*）；配置后使用公钥验签，不再拉取平台证书。 */
    private String publicKeyId;

    /** 微信支付公钥文件路径（PEM）。 */
    private String publicKeyPath;

    /** 微信支付公钥（Base64/PEM 文本，与 public-key-path 二选一）。 */
    private String publicKeyBase64;

    @Override
    public boolean isConfigured() {
        return enabled
                && isNotBlank(mchId)
                && isNotBlank(apiV3Key) && apiV3Key.length() == 32
                && (isNotBlank(privateKeyBase64) || isNotBlank(privateKeyPath))
                && isNotBlank(merchantSerialNumber)
                && isNotBlank(appId)
                && isNotBlank(notifyUrl);
    }

    private static boolean isNotBlank(String s) {
        return s != null && !s.isBlank();
    }
}
