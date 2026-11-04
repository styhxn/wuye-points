package com.wuye.points.payment;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * 微信支付分账（普通分账 / profit sharing）业务配置，绑定 application.yml 的 {@code wechat-split} 前缀。
 *
 * <p>仅包含分账业务参数（开关、售后窗口、灰度白名单、自动提现、归集户监控阈值等），
 * 不含任何第三方聚合支付（扫呗 / LCSW / CBK）配置。真正的微信接口参数在 {@link WechatPayProperties}。</p>
 */
@Data
@Component
@ConfigurationProperties(prefix = "wechat-split")
public class WechatSplitProperties {

    /** 是否启用微信分账。fail-closed：默认 false，须由配置显式开启（wechat-split.enabled）。 */
    private boolean enabled = false;

    /** 是否允许用户主动发起订单退款。 */
    private boolean userRefundEnabled = false;

    /**
     * Path B 自动提现开关（fail-closed 默认 false）。
     * <p>生效范围：仅「电商收付通」模式（{@code wechat-ecommerce.enabled=true}）下有真实资金动作；
     * 「普通分账」模式下收款方自行在微信商户平台提现，开启本开关仅推进本地状态位
     * {@code withdraw_status}，不代表平台自动打款。</p>
     */
    private boolean autoWithdrawEnabled = false;

    /** 归集户余额监控阈值（元），留空=不监控。 */
    private BigDecimal collectBalanceAlertThreshold;

    /** 灰度白名单（物业公司 ID 列表）；空=全部启用（受全局 enabled 约束）。 */
    private List<String> enabledPropertyCompanies = new ArrayList<>();

    /** 售后窗口天数（超过则自动完结划账）。微信服务商分账最长冻结 30 天，配置值须小于等于 30。 */
    private int afterSaleDays = 7;

    /**
     * 单笔订单分账总额上限比例（相对订单实付金额），默认 0.30。
     * <p>必须与微信商户平台「服务商分账 → 最大分账比例」保持一致：默认 30%，申请调高后同步调整本项。
     * 超出时整单拦截（不发起任何一笔），避免部分接收方分出后其余被微信拒绝，造成资金与本地记录不一致。</p>
     */
    private BigDecimal maxSplitRatio = new BigDecimal("0.30");

    /**
     * 该物业公司是否启用分账：全局关闭则 false；白名单非空时仅名单内启用。
     */
    public boolean isEnabledForProperty(String propertyCompanyId) {
        if (!enabled) {
            return false;
        }
        if (enabledPropertyCompanies == null || enabledPropertyCompanies.isEmpty()) {
            return true;
        }
        return propertyCompanyId != null && enabledPropertyCompanies.contains(propertyCompanyId);
    }
}
