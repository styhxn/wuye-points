package com.wuye.points.payment;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 微信电商收付通（平台收付通）分账配置，绑定 application.yml 的 {@code wechat-ecommerce} 前缀。
 *
 * <p>电商收付通模式：用户付款直进「商家二级商户号（sub_mchid）」，平台通过
 * {@code /v3/ecommerce/profitsharing/*} 主动发起分账（佣金→平台商户号、配送费→快递员 openid），
 * 售后窗口到期完结后剩余货款解冻给商家——完全规避平台归集资金的二清风险。</p>
 *
 * <p>与 {@link WechatSplitProperties}（普通分账业务参数）独立：本开关开启后，分账走电商收付通接口，
 * 普通分账（/{@code v3/profitsharing/*}）停用。默认关闭，切换前需在微信服务商平台完成资质与进件。</p>
 */
@Data
@Component
@ConfigurationProperties(prefix = "wechat-ecommerce")
public class EcommerceSplitProperties {

    /** 电商收付通分账总开关（资质与进件就绪后置 true）。 */
    private boolean enabled = false;

    /** 平台收款商户号（分账给平台/物业时使用）。留空默认取 wechat.pay.mchId（服务商号）。 */
    private String platformReceiverMchId;

    /** 平台二级商户号（若平台本身进件为二级商户则填，否则留空）。 */
    private String platformSubMchId;

    /** 添加分账接收方时的关系类型（PERSONAL_OPENID 收款方需在微信侧授权）。 */
    private String receiverRelationType = "SERVICE_PROVIDER";

    /** 分账结果异步通知地址（对应电商收付通分账回调，占位）。 */
    private String notifyUrl;

    /** 商家平台审核通过后是否自动提交微信进件（默认 false：运营后台手动提交）。 */
    private boolean applymentAutoSubmit = false;

    /** 进件结果异步通知地址（进件无真实回调，预留占位）。 */
    private String applymentNotifyUrl;

    /** 内部结算转账默认渠道：zero(微信零钱)/bank(银行卡)。 */
    private String transferChannel = "zero";

    /** 商家转账结果异步通知地址（对应 /api/v1/payment/ecommerce-transfer/notify）。 */
    private String transferNotifyUrl;

    /**
     * 解析平台收款商户号：优先独立配置，其次平台二级商户号，最后服务商商户号。
     */
    public String resolvePlatformReceiverMchId(WechatPayProperties pay) {
        if (platformReceiverMchId != null && !platformReceiverMchId.isBlank()) {
            return platformReceiverMchId;
        }
        if (platformSubMchId != null && !platformSubMchId.isBlank()) {
            return platformSubMchId;
        }
        return pay.getMchId();
    }

    /** 电商收付通是否具备发起真实调用的最低配置（平台收款商户号必须可解析）。 */
    public boolean isConfigured(WechatPayProperties pay) {
        if (!enabled) {
            return false;
        }
        String receiver = resolvePlatformReceiverMchId(pay);
        return receiver != null && !receiver.isBlank();
    }
}
