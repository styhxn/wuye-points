package com.wuye.points.payment;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/**
 * 微信支付（平台微信支付分账 partner 模式）配置，绑定 application.yml 的 {@code wechat.pay} 前缀。
 *
 * <p>仅承载「微信官方支付 / 普通分账」所需配置，不含任何第三方聚合支付（扫呗 / LCSW / CBK）字段。
 * 资金分账走微信官方 {@code /v3/profitsharing/*} 普通分账，接收方类型为 MERCHANT_ID / PERSONAL_OPENID，
 * 不使用电商收付通的 sub_mchid 进件模式。</p>
 */
@Data
@Component
@ConfigurationProperties(prefix = "wechat.pay")
public class WechatPayProperties {

    /** 微信支付总开关（接入真实商户信息后置 true）。 */
    private boolean enabled;

    /** 服务商商户号（sp_mchid）。 */
    private String mchId;

    /** 微信 APIv3 密钥（回调报文 AES 解密用）。 */
    private String apiV3Key;

    /** 商户 API 私钥文件路径（PEM）。 */
    private String privateKeyPath;

    /** 商户 API 私钥（Base64，与 private-key-path 二选一）。 */
    private String privateKeyBase64;

    /** 商户证书序列号（请求签名头 serial_no）。 */
    private String merchantSerialNumber;

    /** 绑定的 AppID（小程序）。 */
    private String appId;

    /** 支付结果异步通知地址。 */
    private String notifyUrl;

    /** 退款结果异步通知地址。 */
    private String refundNotifyUrl;

    /** 平台二级商户号（普通分账 partner 模式下作为出账方）。 */
    private String platformSubMchId;

    /** 二级商户号列表（逗号分隔，灰度/多商户用）。 */
    private String subMchIds;

    /** 是否开启分账（下单时标记 profit_sharing）。 */
    private boolean profitSharing;

    /** 提现出款账户类型（BASIC/FEES/OPERATION）。 */
    private String accountType;

    /** 提现结果异步通知地址。 */
    private String withdrawNotifyUrl;

    /** 添加分账接收方的关系类型（必须与实际业务关系一致）。 */
    private String receiverRelationType;

    /** 自定义关系说明（relation_type=CUSTOM 时必填）。 */
    private String receiverCustomRelation;

    /** 微信支付网关（默认官方地址）。 */
    private String gateway = "https://api.mch.weixin.qq.com";

    /** 平台证书刷新间隔（毫秒）。 */
    private long certRefreshMillis = 3_600_000L;

    /** 是否已具备发起真实微信调用的最低配置。 */
    public boolean isConfigured() {
        return enabled
                && isNotBlank(mchId)
                && isNotBlank(apiV3Key)
                && (isNotBlank(privateKeyBase64) || isNotBlank(privateKeyPath))
                && isNotBlank(merchantSerialNumber)
                && isNotBlank(appId);
    }

    private static boolean isNotBlank(String s) {
        return s != null && !s.isBlank();
    }
}
